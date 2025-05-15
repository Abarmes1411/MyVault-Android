package com.example.myvault.services;

import android.content.Context;

import com.example.myvault.models.Message;
import com.example.myvault.models.ReviewAI;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MessageService{


    private FirebaseDatabase db;
    private DatabaseReference reference;

    public MessageService(Context context){
        db = FirebaseDatabase.getInstance();

    }

    public String insert(String chatID, Message message) {
        DatabaseReference messageRef = db.getReference("chats").child(chatID).child("messages");

        DatabaseReference newReference = messageRef.push();
        String id = newReference.getKey();
        message.setId(id);

        newReference.setValue(message);

        return id;
    }

    public void update(String chatID, Message message) {
        db.getReference("chats")
                .child(chatID)
                .child("messages")
                .child(message.getId())
                .setValue(message);
    }

    public void delete(String chatID, String message) {
        db.getReference("chats")
                .child(chatID)
                .child("messages")
                .child(message)
                .removeValue();
    }
}

