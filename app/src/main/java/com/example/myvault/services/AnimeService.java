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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AnimeService {


    private FirebaseDatabase db;
    private DatabaseReference reference;
    private ContentService contentService;

    public AnimeService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("content");
    }


    // Llamadas a API Anime

    public void fetchSeasonedAnimeAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_anime_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        String season;
        if (month >= 1 && month <= 3) {
            season = "WINTER";
        } else if (month >= 4 && month <= 6) {
            season = "SPRING";
        } else if (month >= 7 && month <= 9) {
            season = "SUMMER";
        } else {
            season = "FALL";
        }


        contentService = new ContentService(context);


        try {
            String graphqlQuery = "{ \"query\": \"query { Page(page: 1, perPage: 20) { media(season: " + season + ", seasonYear: " + year + ", type: ANIME, sort: POPULARITY_DESC) { id title { english } startDate { year month day } status episodes genres averageScore studios { nodes { name } } siteUrl description coverImage { large } } } }\" }";

            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "https://graphql.anilist.co";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    new JSONObject(graphqlQuery),
                    response -> {
                        try {
                            JSONArray results = response.getJSONObject("data").getJSONObject("Page").getJSONArray("media");
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject animeJson = results.getJSONObject(i);
                                String animeID = animeJson.getString("id");
                                String title = animeJson.getJSONObject("title").optString("english", "Título desconocido");

                                JSONObject startDate = animeJson.getJSONObject("startDate");
                                String releaseDate = String.format("%04d-%02d-%02d",
                                        startDate.optInt("year", 0),
                                        startDate.optInt("month", 0),
                                        startDate.optInt("day", 0));

                                int rating = animeJson.optInt("averageScore", 0);
                                int episodes = animeJson.optInt("episodes", 0);
                                JSONArray genreArray = animeJson.optJSONArray("genres");
                                List<String> genres = new ArrayList<>();
                                if (genreArray != null) {
                                    for (int j = 0; j < genreArray.length(); j++) {
                                        genres.add(genreArray.getString(j));
                                    }
                                }
                                JSONArray studiosArray = animeJson.getJSONObject("studios").getJSONArray("nodes");
                                List<String> studios = new ArrayList<>();
                                for (int j = 0; j < studiosArray.length(); j++) {
                                    studios.add(studiosArray.getJSONObject(j).getString("name"));
                                }
                                String coverImage = animeJson.getJSONObject("coverImage").getString("large");
                                String description = animeJson.optString("description", "Descripción no disponible");

                                reference.orderByChild("animeID").equalTo(animeID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean exists = false;
                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            Content existingContent = childSnapshot.getValue(Content.class);
                                            if (existingContent != null && animeID.equals(existingContent.getAnimeID())) {
                                                exists = true;
                                                break;
                                            }
                                        }

                                        if (!exists) {
                                            Content content = new Content(
                                                    "cat_4",
                                                    title,
                                                    description,
                                                    releaseDate,
                                                    String.valueOf(rating),
                                                    coverImage,
                                                    "AniList",
                                                    String.valueOf(episodes),
                                                    genres,
                                                    studios,
                                                    animeID
                                            );
                                            content.setAnimeID(animeID);
                                            content.setOrigin("seasonal_" + year);
                                            contentService.insertAnime(content);
                                            Log.d("ContentService", "Anime añadido desde seasonal: " + title);
                                        } else {
                                            Log.d("ContentService", "Duplicado omitido con el mismo animeID: " + title);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            Log.e("ContentService", "Error procesando JSON", e);
                        }
                    },
                    error -> Log.e("ContentService", "Error en la solicitud AniList", error)
            );

            queue.add(request);

        } catch (JSONException e) {
            Log.e("ContentService", "Error creando JSONObject", e);
        }

        if (onComplete != null) onComplete.run();
    }


    public void fetchPopularAnimeAndSave(Context context, Runnable onComplete){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);


        contentService = new ContentService(context);


        try {
            String graphqlQuery = "{ \"query\": \"query { Page(page: 1, perPage: 50) { media(seasonYear: " + year + ", type: ANIME, sort: POPULARITY_DESC) { id title { english } startDate { year month day } status episodes genres averageScore studios { nodes { name } } siteUrl description coverImage { large } } } }\" }";

            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "https://graphql.anilist.co";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    new JSONObject(graphqlQuery),
                    response -> {
                        try {
                            JSONArray results = response.getJSONObject("data").getJSONObject("Page").getJSONArray("media");
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                            Calendar today = Calendar.getInstance();

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject animeJson = results.getJSONObject(i);

                                JSONObject startDate = animeJson.getJSONObject("startDate");
                                int yearStart = startDate.optInt("year", 0);
                                int monthStart = startDate.optInt("month", 0);
                                int dayStart = startDate.optInt("day", 0);

                                if (yearStart == 0 || monthStart == 0 || dayStart == 0) continue; // ignorar si no tiene fecha válida

                                Calendar animeDate = Calendar.getInstance();
                                animeDate.set(yearStart, monthStart - 1, dayStart); // mes empieza en 0

                                if (animeDate.after(today)) continue; // ignorar si aún no se ha estrenado

                                String animeID = animeJson.getString("id");
                                String title = animeJson.getJSONObject("title").optString("english", "Título desconocido");
                                String releaseDate = String.format("%04d-%02d-%02d", yearStart, monthStart, dayStart);
                                int rating = animeJson.optInt("averageScore", 0);
                                int episodes = animeJson.optInt("episodes", 0);

                                JSONArray genreArray = animeJson.optJSONArray("genres");
                                List<String> genres = new ArrayList<>();
                                if (genreArray != null) {
                                    for (int j = 0; j < genreArray.length(); j++) {
                                        genres.add(genreArray.getString(j));
                                    }
                                }

                                JSONArray studiosArray = animeJson.getJSONObject("studios").getJSONArray("nodes");
                                List<String> studios = new ArrayList<>();
                                for (int j = 0; j < studiosArray.length(); j++) {
                                    studios.add(studiosArray.getJSONObject(j).getString("name"));
                                }

                                String coverImage = animeJson.getJSONObject("coverImage").getString("large");
                                String description = animeJson.optString("description", "Descripción no disponible");

                                reference.orderByChild("animeID").equalTo(animeID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean existsWithSeasonal = false;
                                        boolean alreadyExists = false;

                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            Content existingContent = childSnapshot.getValue(Content.class);
                                            if (existingContent != null && animeID.equals(existingContent.getAnimeID())) {
                                                alreadyExists = true;
                                                if (existingContent.getOrigin() != null && existingContent.getOrigin().startsWith("seasonal_")) {
                                                    existsWithSeasonal = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!alreadyExists || !existsWithSeasonal) {
                                            Content content = new Content(
                                                    "cat_4",
                                                    title,
                                                    description,
                                                    releaseDate,
                                                    String.valueOf(rating),
                                                    coverImage,
                                                    "AniList",
                                                    String.valueOf(episodes),
                                                    genres,
                                                    studios,
                                                    animeID
                                            );
                                            content.setAnimeID(animeID);
                                            content.setOrigin("popular_" + year);
                                            contentService.insertAnime(content);
                                            Log.d("ContentService", "Anime añadido desde popular: " + title);
                                        } else {
                                            Log.d("ContentService", "Anime ya registrado con prioridad por temporada: " + title);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                    }
                                });

                            }

                        } catch (JSONException e) {
                            Log.e("ContentService", "Error procesando JSON", e);
                        }
                    },
                    error -> Log.e("ContentService", "Error en la solicitud AniList", error)
            );

            queue.add(request);

        } catch (JSONException e) {
            Log.e("ContentService", "Error creando JSONObject", e);
        }
        if (onComplete != null) onComplete.run();

    }

    public void fetchBestAnimeYearlyAndSave(Context context, Runnable onComplete){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 1;

        contentService = new ContentService(context);


        try{
            // Query GraphQL para los mejores animes del año pasado (2024)
            String graphqlQuery = "{ \"query\": \"query { Page(page: 1, perPage: 50) { media(seasonYear: " + year + ", type: ANIME, sort: SCORE_DESC, status: FINISHED) { id title { english romaji native } startDate { year month day } status episodes genres averageScore studios { nodes { name } } siteUrl description coverImage { large } } } }\" }";

            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "https://graphql.anilist.co";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    new JSONObject(graphqlQuery),
                    response -> {
                        try {
                            JSONArray results = response.getJSONObject("data").getJSONObject("Page").getJSONArray("media");
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                            Calendar today = Calendar.getInstance();

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject animeJson = results.getJSONObject(i);

                                JSONObject startDate = animeJson.getJSONObject("startDate");
                                int yearStart = startDate.optInt("year", 0);
                                int monthStart = startDate.optInt("month", 0);
                                int dayStart = startDate.optInt("day", 0);

                                if (yearStart == 0 || monthStart == 0 || dayStart == 0) continue; // ignorar si no tiene fecha válida

                                Calendar animeDate = Calendar.getInstance();
                                animeDate.set(yearStart, monthStart - 1, dayStart); // mes empieza en 0

                                if (animeDate.after(today)) continue; // ignorar si aún no se ha estrenado

                                String animeID = animeJson.getString("id");
                                String title = animeJson.getJSONObject("title").optString("english", "Título desconocido");
                                String releaseDate = String.format("%04d-%02d-%02d", yearStart, monthStart, dayStart);
                                int rating = animeJson.optInt("averageScore", 0);
                                int episodes = animeJson.optInt("episodes", 0);

                                JSONArray genreArray = animeJson.optJSONArray("genres");
                                List<String> genres = new ArrayList<>();
                                if (genreArray != null) {
                                    for (int j = 0; j < genreArray.length(); j++) {
                                        genres.add(genreArray.getString(j));
                                    }
                                }

                                JSONArray studiosArray = animeJson.getJSONObject("studios").getJSONArray("nodes");
                                List<String> studios = new ArrayList<>();
                                for (int j = 0; j < studiosArray.length(); j++) {
                                    studios.add(studiosArray.getJSONObject(j).getString("name"));
                                }

                                String coverImage = animeJson.getJSONObject("coverImage").getString("large");
                                String description = animeJson.optString("description", "Descripción no disponible");

                                // Verificar si el anime ya existe en Firebase
                                reference.orderByChild("animeID").equalTo(animeID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean existsWithSeasonal = false;
                                        boolean alreadyExists = false;

                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            Content existingContent = childSnapshot.getValue(Content.class);
                                            if (existingContent != null && animeID.equals(existingContent.getAnimeID())) {
                                                alreadyExists = true;
                                                if (existingContent.getOrigin() != null && existingContent.getOrigin().startsWith("best_")) {
                                                    existsWithSeasonal = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!alreadyExists || !existsWithSeasonal) {
                                            Content content = new Content(
                                                    "cat_4",
                                                    title,
                                                    description,
                                                    releaseDate,
                                                    String.valueOf(rating),
                                                    coverImage,
                                                    "AniList",
                                                    String.valueOf(episodes),
                                                    genres,
                                                    studios,
                                                    animeID
                                            );
                                            content.setAnimeID(animeID);
                                            content.setOrigin("best_" + year); // Establecer el origen como "best_2024"
                                            contentService.insertAnime(content);
                                            Log.d("ContentService", "Anime añadido desde best: " + title);
                                        } else {
                                            Log.d("ContentService", "Anime ya registrado con prioridad 'best': " + title);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                    }
                                });

                            }

                        } catch (JSONException e) {
                            Log.e("ContentService", "Error procesando JSON", e);
                        }
                    },
                    error -> Log.e("ContentService", "Error en la solicitud AniList", error)
            );

            queue.add(request);
        } catch (JSONException e) {
            Log.e("ContentService", "Error creando JSONObject", e);
        }
        if (onComplete != null) onComplete.run();

    }

}
