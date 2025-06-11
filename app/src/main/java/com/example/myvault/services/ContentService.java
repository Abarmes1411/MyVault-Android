package com.example.myvault.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myvault.models.Content;
import com.example.myvault.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ContentService {

    public interface OnGetPersonaListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    private FirebaseDatabase db;
    private DatabaseReference reference;

    public ContentService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("content");
    }

    public String insertMovie(Content content) {
        // Normalizar el título: minúsculas, sin espacios ni caracteres especiales
        String normalizedTitle = content.getTitle().toLowerCase().replaceAll("[^a-z0-9]", "");

        // Obtener la fecha de estreno
        String releaseDate = content.getReleaseDate();

        // Crear la clave única basada en título y fecha
        String uniqueKey = normalizedTitle + "_" + releaseDate.charAt(2) + releaseDate.charAt(3);

        content.setId(uniqueKey);

        // Insertar en Firebase con esa clave
        DatabaseReference newReference = reference.child(uniqueKey);

        newReference.setValue(content, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e("Firebase", "Error al insertar el contenido", databaseError.toException());
                } else {

                    content.setId(databaseReference.getKey());
                    Log.d("Firebase", "Contenido insertado correctamente con ID: " + content.getId());
                }
            }
        });

        return content.getId();
    }


    public String insertShow(Content content) {
        // Normalizar el título: minúsculas, sin espacios ni caracteres especiales
        String normalizedTitle = content.getTitle().toLowerCase().replaceAll("[^a-z0-9]", "");

        // Obtener la fecha de estreno
        String releaseDate = content.getReleaseDate(); // por ejemplo: "2025-01-11"

        // Crear la clave única basada en título y fecha
        String uniqueKey = normalizedTitle + "_" + releaseDate;

        content.setId(uniqueKey);

        // Referencia a la base de datos
        DatabaseReference newReference = reference.child(uniqueKey);

        // Verificar si el contenido ya existe en la base de datos
        newReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    newReference.setValue(content);
                    Log.d("InsertShow", "Insertado: " + uniqueKey + " con origin: " + content.getOrigin());
                } else {
                    Log.d("InsertShow", "Contenido ya existe: " + uniqueKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InsertShow", "Error al comprobar existencia: " + error.getMessage());
            }
        });

        return uniqueKey;
    }



    public String insertAnime(Content content) {
        String baseTitle = content.getTitle();

        // Normalizar el título: minúsculas, sin espacios ni caracteres especiales
        String normalizedTitle = baseTitle.toLowerCase().replaceAll("[^a-z0-9]", "");
        String releaseDate = content.getReleaseDate();
        String uniqueKey = normalizedTitle + "_" + releaseDate;

        content.setId(uniqueKey);

        // Referencia a la base de datos
        DatabaseReference newReference = reference.child(uniqueKey);

        // Verificar si el contenido ya existe en la base de datos
        newReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    newReference.setValue(content);
                    Log.d("InsertAnime", "Insertado: " + uniqueKey + " con origin: " + content.getOrigin());
                } else {
                    // Si ya existe, comprobar la prioridad
                    Content existing = snapshot.getValue(Content.class);
                    if (existing != null && existing.getOrigin() != null) {
                        int existingPriority = getOriginAnimePriority(existing.getOrigin());
                        int newPriority = getOriginAnimePriority(content.getOrigin());

                        // Lógica para comparar prioridades
                        if (newPriority < existingPriority) {
                            newReference.setValue(content);
                            Log.d("InsertAnime", "Sobrescrito por: " + content.getOrigin());
                        } else {
                            Log.d("InsertAnime", "No sobrescrito, origin existente con mayor o igual prioridad: " + existing.getOrigin());
                        }
                    } else {
                        newReference.setValue(content);
                        Log.d("InsertAnime", "Sobrescrito (origin desconocido): " + content.getOrigin());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InsertAnime", "Error al comprobar existencia: " + error.getMessage());
            }
        });

        return uniqueKey;
    }



    public String insertManga(Content content) {
        String baseTitle = content.getTitle();
        if (baseTitle == null || baseTitle.equalsIgnoreCase("null")) {
            baseTitle = content.getOriginalTitle();
            content.setTitle(baseTitle);
        }

        String normalizedTitle = baseTitle.toLowerCase().replaceAll("[^a-z0-9]", "");
        String releaseDate = content.getReleaseDate();
        String uniqueKey = normalizedTitle + "_" + releaseDate;

        content.setId(uniqueKey);

        DatabaseReference newReference = reference.child(uniqueKey);

        newReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    newReference.setValue(content);
                    Log.d("InsertManga", "Insertado: " + uniqueKey + " con origin: " + content.getOrigin());
                } else {
                    Content existing = snapshot.getValue(Content.class);
                    if (existing != null && existing.getOrigin() != null) {
                        int existingPriority = getOriginMangaPriority(existing.getOrigin());
                        int newPriority = getOriginMangaPriority(content.getOrigin());

                        if (newPriority < existingPriority) {
                            newReference.setValue(content);
                            Log.d("InsertManga", "Sobrescrito por: " + content.getOrigin());
                        } else {
                            Log.d("InsertManga", "No sobrescrito, origin existente con mayor o igual prioridad: " + existing.getOrigin());
                        }
                    } else {
                        newReference.setValue(content);
                        Log.d("InsertManga", "Sobrescrito (origin desconocido): " + content.getOrigin());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InsertManga", "Error al comprobar existencia: " + error.getMessage());
            }
        });

        return uniqueKey;
    }

    public String insertGame(Content content) {
        String baseTitle = content.getTitle();
        if (baseTitle == null || baseTitle.isEmpty()) {
            Log.e("InsertGame", "El título es nulo o vacío. No se puede insertar.");
            return null;
        }

        String normalizedTitle = baseTitle.toLowerCase().replaceAll("[^a-z0-9]", "");
        String releaseDate = content.getReleaseDate() != null ? content.getReleaseDate() : "unknown";
        String uniqueKey = normalizedTitle + "_" + releaseDate;

        content.setId(uniqueKey);

        DatabaseReference newReference = reference.child(uniqueKey);

        newReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    newReference.setValue(content);
                    Log.d("InsertGame", "Insertado: " + uniqueKey + " con origin: " + content.getOrigin());
                } else {
                    Content existing = snapshot.getValue(Content.class);
                    if (existing != null && existing.getOrigin() != null) {
                        int existingPriority = getOriginGamePriority(existing.getOrigin());
                        int newPriority = getOriginGamePriority(content.getOrigin());

                        if (newPriority < existingPriority) {
                            newReference.setValue(content);
                            Log.d("InsertGame", "Sobrescrito por: " + content.getOrigin());
                        } else {
                            Log.d("InsertGame", "No sobrescrito, origin existente con mayor o igual prioridad: " + existing.getOrigin());
                        }
                    } else {
                        newReference.setValue(content);
                        Log.d("InsertGame", "Sobrescrito (origin desconocido): " + content.getOrigin());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InsertGame", "Error al comprobar existencia: " + error.getMessage());
            }
        });

        return uniqueKey;
    }


    private int getOriginMangaPriority(String origin) {
        switch (origin) {
            case "ongoing":
                return 0;
            case "news":
                return 1;
            case "bestof":
                return 2;
            default:
                return 99;
        }
    }

    private int getOriginAnimePriority(String origin) {
        switch (origin) {
            case "seasonal":
                return 0;
            case "popular":
                return 1;
            case "best":
                return 2;
            default:
                return 99;
        }
    }

    private int getOriginGamePriority(String origin) {
        switch (origin) {
            case "recent_game":
                return 0;
            case "popular_game":
                return 1;
            case "upcoming_game":
                return 2;
            default:
                return 99;
        }
    }


    private int getOriginBookPriority(String origin) {
        switch (origin) {
            case "popular_fiction_books_":
                return 0;
            case "popular_game":
                return 1;
            case "upcoming_game":
                return 2;
            default:
                return 99;
        }
    }



    public void update(Content content){
        reference.child(content.getId()).setValue(content);
    }

    public void delete(Content content){
        reference.child(content.getId()).removeValue();
    }


    public void deleteByID(String content){
        reference.child(content).removeValue();
    }

}
