package com.example.myvault.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import com.example.myvault.R;
import com.example.myvault.activities.ChatActivity;
import com.example.myvault.activities.UserListActivity;
import com.example.myvault.adapters.FriendsAdapter;
import com.example.myvault.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private View view;
    private ListView lvFriendsList;
    private Button buttonAddFriend;
    private FriendsAdapter adapter;
    private List<User> userList;

    public FriendsFragment() {
        // Constructor vacío requerido
    }

    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString("ARG_PARAM1", param1);
        args.putString("ARG_PARAM2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString("ARG_PARAM1");
            String mParam2 = getArguments().getString("ARG_PARAM2");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_friends, container, false);

        lvFriendsList = view.findViewById(R.id.lvFriendsList);
        buttonAddFriend = view.findViewById(R.id.buttonAddFriend);

        userList = new ArrayList<>();
        adapter = new FriendsAdapter(requireContext(), userList);
        lvFriendsList.setAdapter(adapter);

        loadDB();

        lvFriendsList.setOnItemClickListener((parent, view1, position, id) -> {
            User selectedUser = userList.get(position);

            String[] opciones = {"Comenzar Chat", "Reviews del Usuario", "Eliminar Amigo"};
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Opciones")
                    .setItems(opciones, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                Log.d("Amigos", "Iniciar chat con: " + selectedUser.getUsername());
                                Intent chatIntent = new Intent(requireContext(), ChatActivity.class);
                                chatIntent.putExtra("chatUserId", selectedUser.getId());
                                startActivity(chatIntent);
                                break;
                            case 1:
                                Log.d("Amigos", "Ver reviews de: " + selectedUser.getUsername());
                                UserReviewsFragment fragment = new UserReviewsFragment();
                                Bundle args = new Bundle();
                                args.putString("selectedUserId", selectedUser.getId());
                                fragment.setArguments(args);
                                requireActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.containerFrame, fragment)
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case 2:
                                Log.d("Amigos", "Eliminar amigo: " + selectedUser.getId());
                                eliminarAmigo(selectedUser.getId());
                                break;
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        buttonAddFriend.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), UserListActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadDB() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("friends");
        Log.d("loadDB", "Cargando amigos de " + uid);

        friendsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Log.d("loadDB", "Amigos encontrados: " + task.getResult().getChildrenCount());
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String friendId = snapshot.getKey();
                    Log.d("loadDB", "Snapshot key (friendUID): " + friendId + " value: " + snapshot.getValue());
                    if (friendId != null) {
                        DatabaseReference friendRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(friendId);

                        friendRef.get().addOnCompleteListener(friendTask -> {
                            if (friendTask.isSuccessful()) {
                                if (friendTask.getResult().exists()) {
                                    User friend = friendTask.getResult().getValue(User.class);
                                    if (friend != null) {
                                        Log.d("loadDB", "Datos del amigo recibidos: " + friend.toString());
                                        userList.add(friend);
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Log.w("loadDB", "No se pudo convertir a User: " + friendTask.getResult().getValue());
                                    }
                                } else {
                                    Log.w("loadDB", "El nodo del amigo no existe: " + friendId);
                                }
                            } else {
                                Log.e("loadDB", "Error al obtener datos del amigo: ", friendTask.getException());
                            }
                        });
                    } else {
                        Log.d("loadDB", "No se encontró friendId en el snapshot");
                    }
                }
            } else {
                Log.d("loadDB", "No se encontraron amigos");
            }
        });
    }

    private void eliminarAmigo(String friendUid) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference friendRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUid)
                .child("friends")
                .child(friendUid);
        Log.d("Amigos", "Eliminando amigo: " + friendUid);
        Log.d("Amigos", "Current UID: " + currentUid);
        friendRef.removeValue().addOnSuccessListener(aVoid -> {
            Log.d("Amigos", "Amigo eliminado: " + friendUid);
            // Eliminar de la lista local
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getId().equals(friendUid)) {
                    userList.remove(i);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Amigos", "Error al eliminar amigo", e);
        });
    }
}
