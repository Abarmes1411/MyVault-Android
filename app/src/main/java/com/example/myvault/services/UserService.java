package com.example.myvault.services;

import android.content.Context;

import com.example.myvault.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserService {

    public interface OnGetPersonaListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    private FirebaseDatabase db;
    private DatabaseReference reference;

    public UserService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("users");
    }

    public void insert(User user) {
        reference.child(user.getId()).setValue(user);
    }


    public void getUserByUid(String uid, final OnGetPersonaListener listener) {
        reference.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    listener.onSuccess(user);
                } else {
                    listener.onFailure("No se encontró el user con UID: " + uid);
                }
            } else {
                listener.onFailure("Error al obtener datos: " + task.getException().getMessage());
            }
        });
    }

    public void update(User user){
        reference.child(user.getId()).setValue(user);
    }

    public void delete(User user){
        reference.child(user.getId()).removeValue();
    }


    public void deleteByID(String id){
        reference.child(id).removeValue();
    }

}
