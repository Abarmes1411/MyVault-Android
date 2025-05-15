package com.example.myvault.services;

import android.content.Context;

import com.example.myvault.models.ReviewAI;
import com.example.myvault.models.UserReview;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReviewAiService{


    private FirebaseDatabase db;
    private DatabaseReference reference;

    public ReviewAiService(Context context){
        db = FirebaseDatabase.getInstance();

    }

    public String insert(String contentID, ReviewAI aiReview) {
        DatabaseReference aiReviewRef = db.getReference("content").child(contentID).child("aiReview");

        DatabaseReference newReference = aiReviewRef.push();
        String id = newReference.getKey();
        aiReview.setId(id);

        newReference.setValue(aiReview);

        return id;
    }

    public void update(String contentID, ReviewAI aiReview) {
        db.getReference("content")
                .child(contentID)
                .child("aiReview")
                .child(aiReview.getId())
                .setValue(aiReview);
    }

    public void delete(String contentID, String aiReview) {
        db.getReference("content")
                .child(contentID)
                .child("userReviews")
                .child(aiReview)
                .removeValue();
    }
}
