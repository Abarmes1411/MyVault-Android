package com.example.myvault.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvault.R;
import com.example.myvault.models.User;
import com.example.myvault.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private EditText etProfileUsername, etProfileName, etProfileSurname, etProfileEmail;
    private Button btnEditProfile, btnSaveProfile;
    private UserService userService;
    private String uidCurrentUser;
    private DatabaseReference reference;
    private ImageView ivProfilePic;
    private User users;
    private View mainContent;
    private View progressBar;
    private String selectedProfileImage;
    private TextView tvSuggestProfile;


    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private boolean newImageSelected = false;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context context;
    private View view;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        mainContent = view.findViewById(R.id.main);
        progressBar = view.findViewById(R.id.progressBarProfile);

        mainContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        etProfileUsername = view.findViewById(R.id.etProfileUsername);
        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileSurname = view.findViewById(R.id.etProfileSurname);
        etProfileEmail = view.findViewById(R.id.etProfileEmail);
        tvSuggestProfile = view.findViewById(R.id.tvSuggestProfile);
        tvSuggestProfile.setVisibility(View.GONE);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        btnSaveProfile.setVisibility(View.GONE);

        uidCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("users").child(uidCurrentUser);

        loadUserData();

        btnEditProfile.setOnClickListener(v -> enableEditing());

        btnSaveProfile.setOnClickListener(v -> saveChanges());

        // Desactivar el click hasta que se habilite la edición
        ivProfilePic.setEnabled(false);
        ivProfilePic.setOnClickListener(v -> {
            if (ivProfilePic.isEnabled()) {
                showImagePickerDialog();
            }
        });


        return view;
    }





    private void loadUserData() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        users = user;
                        Log.d("FIREBASE_USER", "User loaded: " + user.getName());

                        etProfileUsername.setText(user.getUsername());
                        etProfileName.setText(user.getName());
                        etProfileSurname.setText(user.getSurname());
                        etProfileEmail.setText(user.getEmail());

                        if (user.getProfilePic() != null) {
                            String resourceName = user.getProfilePic().replace(".png", "");
                            int imageResId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());
                            if (imageResId != 0) {
                                ivProfilePic.setImageResource(imageResId);
                            } else {
                                ivProfilePic.setImageResource(R.drawable.movie_profile_pic);
                            }
                        }



                        progressBar.setVisibility(View.GONE);
                        mainContent.setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_ERROR", "Error: " + error.getMessage());
                Toast.makeText(requireContext(), "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    private void enableEditing() {
        etProfileUsername.setEnabled(true);
        etProfileName.setEnabled(true);
        etProfileSurname.setEnabled(true);
        etProfileEmail.setEnabled(false);
        ivProfilePic.setEnabled(true);

        btnEditProfile.setVisibility(View.GONE);
        btnSaveProfile.setVisibility(View.VISIBLE);
        tvSuggestProfile.setVisibility(View.VISIBLE);
    }


    private void saveChanges() {
        String newUsername = etProfileUsername.getText().toString().trim();
        String newName = etProfileName.getText().toString().trim();
        String newSurname = etProfileSurname.getText().toString().trim();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newSurname) || TextUtils.isEmpty(newUsername)) {
            Toast.makeText(context, "Todos los campos deben estar completos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar datos en Realtime Database
        reference.child("name").setValue(newName);
        reference.child("surname").setValue(newSurname);
        reference.child("username").setValue(newUsername);

        // Si hay imagen nueva, subirla
        if (newImageSelected && selectedProfileImage != null) {
            reference.child("profilePic").setValue(selectedProfileImage);
        }
        else {
            disableEditing();
        }
    }


    private void disableEditing() {
        etProfileUsername.setEnabled(false);
        etProfileName.setEnabled(false);
        etProfileSurname.setEnabled(false);
        etProfileEmail.setEnabled(false);
        ivProfilePic.setEnabled(false);

        btnEditProfile.setVisibility(View.VISIBLE);
        btnSaveProfile.setVisibility(View.GONE);
        tvSuggestProfile.setVisibility(View.GONE);
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Elige una imagen de perfil");

        View view = getLayoutInflater().inflate(R.layout.dialog_image_picker, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        ImageView img1 = view.findViewById(R.id.movie_profile_pic_id);
        ImageView img2 = view.findViewById(R.id.show_profile_pic_id);
        ImageView img3 = view.findViewById(R.id.game_profile_pic_id);
        ImageView img4 = view.findViewById(R.id.manganovel_profile_pic_id);

        View.OnClickListener listener = v -> {
            String selected = (String) v.getTag();
            selectedProfileImage = selected;

            String resourceName = selected.replace(".png", "");

            int resId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());
            ivProfilePic.setImageResource(resId);

            newImageSelected = true;
            dialog.dismiss();
        };


        img1.setOnClickListener(listener);
        img2.setOnClickListener(listener);
        img3.setOnClickListener(listener);
        img4.setOnClickListener(listener);

        dialog.show();
    }

    /**

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfilePic.setImageURI(imageUri);
            newImageSelected = true;
        }
    }


    private void uploadImageToFirebase(Runnable onSuccessCallback) {
        String fileName = "profile_" + uidCurrentUser + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pics/" + fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    reference.child("profilePic").setValue(imageUrl);
                    Toast.makeText(getContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                    onSuccessCallback.run();
                }))
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_STORAGE", "Error al subir imagen: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    */
}