package com.example.myvault.services;

import android.content.Context;

import com.example.myvault.models.CustomList;
import com.example.myvault.models.Message;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomListService {


    private FirebaseDatabase db;
    private DatabaseReference reference;

    public CustomListService(Context context){
        db = FirebaseDatabase.getInstance();

    }

    public void insertCustomList(String userID, String name, CustomList customList) {
        DatabaseReference customListRef = db.getReference("users")
                .child(userID)
                .child("customLists")
                .child(name);

        customList.setId(name);
        customListRef.setValue(customList);
    }


    public void update(String userID, CustomList customList) {
        db.getReference("users")
                .child(userID)
                .child("customLists")
                .child(customList.getId())
                .setValue(customList);
    }

    public void delete(String userID, String customList) {
        db.getReference("users")
                .child(userID)
                .child("customLists")
                .child(customList)
                .removeValue();
    }
}
