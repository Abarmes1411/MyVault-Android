package com.example.myvault.services;

import android.content.Context;

import com.example.myvault.models.Chat;
import com.example.myvault.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatService {

    public interface OnGetPersonaListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    private FirebaseDatabase db;
    private DatabaseReference reference;

    public ChatService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("chats");
    }

    public String insert(Chat chat) {
        DatabaseReference newReference = reference.push();
        newReference.setValue(chat);
        return newReference.getKey();
    }


    public void update(Chat chat){
        reference.child(chat.getId()).setValue(chat);
    }

    public void delete(Chat chat){
        reference.child(chat.getId()).removeValue();
    }


    public void deleteByID(String chat){
        reference.child(chat).removeValue();
    }

}
