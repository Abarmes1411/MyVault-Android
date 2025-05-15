package com.example.myvault.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myvault.models.User;
import com.example.myvault.models.UserReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserReviewService {


    private FirebaseDatabase db;
    private DatabaseReference reference;

    public UserReviewService(Context context){
        db = FirebaseDatabase.getInstance();

    }

    public void insert(String contentID, UserReview userReview) {
        DatabaseReference contentReviewRef = db.getReference("content").child(contentID).child("userReviews");
        DatabaseReference newReference = contentReviewRef.push();
        String id = newReference.getKey();
        userReview.setId(id);

        // Nodo de contenido
        newReference.setValue(userReview)
                .addOnSuccessListener(aVoid -> {
                    Log.d("InsertReview", "Reseña guardada en contenido con ID: " + id);
                })
                .addOnFailureListener(e -> {
                    Log.e("InsertReview", "Error al guardar en contenido", e);
                });

        // Nodo del usuario
        DatabaseReference userReviewRef = db.getReference("users")
                .child(userReview.getUserID())
                .child("userReviews")
                .child(id);

        userReviewRef.setValue(userReview)
                .addOnSuccessListener(aVoid -> {
                    Log.d("InsertReview", "Reseña guardada también en el nodo del usuario");
                })
                .addOnFailureListener(e -> {
                    Log.e("InsertReview", "Error al guardar en el nodo del usuario", e);
                });
    }

    public void update(String contentID, UserReview userReview) {
        // Actualizar en contenido
        db.getReference("content")
                .child(contentID)
                .child("userReviews")
                .child(userReview.getId())
                .setValue(userReview);

        // Actualizar en usuario
        db.getReference("users")
                .child(userReview.getUserID())
                .child("userReviews")
                .child(userReview.getId())
                .setValue(userReview);
    }

    public void delete(String userID, String reviewID, String contentID) {
        DatabaseReference contentRef = FirebaseDatabase.getInstance().getReference("content");
        contentRef.orderByChild("id").equalTo(contentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot contentSnap : snapshot.getChildren()) {
                    String contentKey = contentSnap.getKey();

                    if (contentKey != null) {
                        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("content")
                                .child(contentKey)
                                .child("userReviews")
                                .child(reviewID);
                        reviewRef.removeValue();


                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                                .child(userID)
                                .child("userReviews")
                                .child(reviewID);
                        userRef.removeValue();

                        Log.d("UserReviewService", "Reseña eliminada con contentKey: " + contentKey);
                    } else {
                        Log.e("UserReviewService", "contentKey es null");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserReviewService", "Error al buscar el contenido: " + error.getMessage());
            }
        });
    }

}
