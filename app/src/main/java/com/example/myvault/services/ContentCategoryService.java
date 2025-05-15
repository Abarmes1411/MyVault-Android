package com.example.myvault.services;

import android.content.Context;

import com.example.myvault.models.Chat;
import com.example.myvault.models.ContentCategory;
import com.example.myvault.models.Message;
import com.example.myvault.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ContentCategoryService {

    public interface OnGetPersonaListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    private FirebaseDatabase db;
    private DatabaseReference reference;

    public ContentCategoryService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("contentCategories");
    }

    public String insert(ContentCategory contentCategory) {
        DatabaseReference newReference = reference.push();
        newReference.setValue(contentCategory);
        return newReference.getKey();
    }


    public void update(ContentCategory contentCategory){
        reference.child(contentCategory.getId()).setValue(contentCategory);
    }

    public void delete(ContentCategory contentCategory){
        reference.child(contentCategory.getId()).removeValue();
    }


    public void deleteByID(String contentCategory){
        reference.child(contentCategory).removeValue();
    }

}

