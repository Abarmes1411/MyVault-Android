package com.example.myvault.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myvault.R;
import com.example.myvault.activities.DetailItemCustomListsActivity;
import com.example.myvault.adapters.CustomListsAdapter;
import com.example.myvault.models.CustomListItem;
import com.example.myvault.services.CustomListService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CustomListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomListsFragment extends Fragment {

    private View view;
    private ListView lvCustomLists;
    private DatabaseReference reference;
    private String uidCurrentUser;
    private ImageButton buttonInsertCustomList;
    private CustomListService customListService;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CustomListsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CustomListsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomListsFragment newInstance(String param1, String param2) {
        CustomListsFragment fragment = new CustomListsFragment();
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

        view = inflater.inflate(R.layout.fragment_custom_lists, container, false);
        lvCustomLists = view.findViewById(R.id.lvCustomLists);
        buttonInsertCustomList = view.findViewById(R.id.buttonInsertCustomList);
        customListService = new CustomListService(getActivity());

        uidCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uidCurrentUser)
                .child("customLists");

        buttonInsertCustomList.setOnClickListener(v -> {
            // Crear un AlertDialog con un EditText para insertar el nombre de la lista
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
            builder.setTitle("Nombre de la nueva lista");

            final android.widget.EditText input = new android.widget.EditText(getActivity());
            input.setHint("Introduce un nombre");
            builder.setView(input);

            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    // Crear e insertar la nueva lista
                    com.example.myvault.models.CustomList newList = new com.example.myvault.models.CustomList();
                    newList.setListName(name);
                    newList.setId(name); // usas el nombre como ID directamente

                    // Insertar usando el servicio
                    customListService.insertCustomList(uidCurrentUser, name, newList);
                }
            });

            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

            builder.show();
        });


        List<CustomListItem> customListItems = new ArrayList<>();
        CustomListsAdapter adapter = new CustomListsAdapter(getActivity(), customListItems);
        lvCustomLists.setAdapter(adapter);

        lvCustomLists.setOnItemLongClickListener((parent, view1, position, id) -> {
            CustomListItem selectedItem = customListItems.get(position);

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
            builder.setTitle("Opciones para \"" + selectedItem.getTitle() + "\"");

            String[] opciones = {"Editar nombre de la lista", "Eliminar lista"};
            builder.setItems(opciones, (dialog, which) -> {
                switch (which) {
                    case 0: // Editar nombre
                        editCustomListName(selectedItem.getTitle());
                        break;
                    case 1: // Eliminar
                        deleteCustomList(selectedItem.getTitle());
                        break;
                }
            });

            builder.show();
            return true;
        });

        lvCustomLists.setOnItemClickListener((parent, view1, position, id) -> {

            CustomListItem selectedItem = customListItems.get(position);
            String listName = selectedItem.getTitle();

            Intent intent = new Intent(getActivity(), DetailItemCustomListsActivity.class);
            intent.putExtra("listName", listName);
            startActivity(intent);
        });


        reference.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customListItems.clear();

                for (DataSnapshot listSnapshot : snapshot.getChildren()) {
                    String name = listSnapshot.child("listName").getValue(String.class);
                    long itemCount = listSnapshot.child("items").getChildrenCount();

                    if (name != null) {
                        customListItems.add(new CustomListItem(name, (int) itemCount));
                    }
                }

                adapter.notifyDataSetChanged();
                toggleEmptyMessage(customListItems.isEmpty());
            }





            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e("CustomListsFragment", "Error al obtener las listas personalizadas", error.toException());
            }
        });

        return view;
    }


    private void toggleEmptyMessage(boolean show) {
        TextView emptyMessage = view.findViewById(R.id.tvEmptyMessage);
        emptyMessage.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void deleteCustomList(String listName) {
        reference.child(listName).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("CustomListsFragment", "Lista eliminada correctamente");
            } else {
                Log.e("CustomListsFragment", "Error al eliminar lista");
            }
        });
    }

    private void editCustomListName(String oldName) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Editar nombre de la lista");

        final android.widget.EditText input = new android.widget.EditText(getActivity());
        input.setText(oldName);
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(oldName)) {
                reference.child(oldName).get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        // Convertir el snapshot a un Map para poder editar los campos
                        java.util.HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue();

                        // Actualizar los campos internos
                        data.put("id", newName);
                        data.put("listName", newName);

                        // Guardar bajo el nuevo nombre
                        reference.child(newName).setValue(data).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Eliminar el antiguo nodo
                                reference.child(oldName).removeValue();
                            }
                        });
                    }
                });
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }



}