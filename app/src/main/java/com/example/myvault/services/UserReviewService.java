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

import java.util.HashMap;
import java.util.Map;

public class UserReviewService {


    private FirebaseDatabase db;
    private DatabaseReference reference;

    public UserReviewService(Context context){
        db = FirebaseDatabase.getInstance();

    }

    public void insertOrUpdate(String contentID, UserReview userReview, String contentKey) {
        DatabaseReference contentReviewRef = db.getReference("content")
                .child(contentKey)
                .child("userReviews");

        contentReviewRef.orderByChild("userID").equalTo(userReview.getUserID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Ya existe una review del usuario: actualizamos
                            for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                                String existingReviewId = reviewSnapshot.getKey();
                                userReview.setId(existingReviewId);

                                update(contentID, userReview);
                                Log.d("InsertReview", "Review existente, actualizando con ID: " + existingReviewId);
                                break;
                            }
                        } else {
                            // Nueva review
                            DatabaseReference newReference = contentReviewRef.push();
                            String id = newReference.getKey();
                            userReview.setId(id);

                            // Insertar en nodo de contenido (con userID incluido)
                            Map<String, Object> fullReviewData = new HashMap<>();
                            fullReviewData.put("id", userReview.getId());
                            fullReviewData.put("comment", userReview.getComment());
                            fullReviewData.put("rating", userReview.getRating());
                            fullReviewData.put("contentID", userReview.getContentID());
                            fullReviewData.put("reviewDate", userReview.getReviewDate());
                            fullReviewData.put("userID", userReview.getUserID());

                            newReference.setValue(fullReviewData)
                                    .addOnSuccessListener(aVoid -> Log.d("InsertReview", "Review nueva insertada en content con ID: " + id))
                                    .addOnFailureListener(e -> Log.e("InsertReview", "Error al insertar la review en content", e));

                            // Insertar en nodo de usuario (sin userID)
                            Map<String, Object> userReviewData = new HashMap<>(fullReviewData);
                            userReviewData.remove("userID");

                            db.getReference("users")
                                    .child(userReview.getUserID())
                                    .child("userReviews")
                                    .child(id)
                                    .setValue(userReviewData)
                                    .addOnSuccessListener(aVoid -> Log.d("InsertReview", "Review también insertada en nodo usuario"))
                                    .addOnFailureListener(e -> Log.e("InsertReview", "Error al insertar la review en nodo usuario", e));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("InsertReview", "Error al comprobar si la review existe", error.toException());
                    }
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
