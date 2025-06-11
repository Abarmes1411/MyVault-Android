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

public class NovelService {

    private FirebaseDatabase db;
    private DatabaseReference reference;
    private ContentService contentService;

    public NovelService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("content");
    }

    // LLamadas a API Novels

    public void fetchTopOngoingNovelsAndSave(Context context, Runnable onComplete){
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_novels_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        contentService = new ContentService(context);


        try{
            // Query modificada para novelas (format: NOVEL)
            String graphqlQuery = "{ \"query\": \"query { Page(perPage: 20) { media(type: MANGA, format: NOVEL, status: RELEASING, sort: POPULARITY_DESC) { id title { romaji english native } startDate { year month day } coverImage { large } siteUrl popularity description genres averageScore } } }\" }";

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
                                JSONObject novelJson = results.getJSONObject(i);

                                String novelID = novelJson.getString("id");
                                String title = novelJson.getJSONObject("title").optString("english", "Título desconocido");
                                String originalTitle = novelJson.getJSONObject("title").optString("romaji", "Título desconocido");

                                JSONObject startDate = novelJson.getJSONObject("startDate");
                                String releaseDate = String.format("%04d-%02d-%02d",
                                        startDate.optInt("year", 0),
                                        startDate.optInt("month", 0),
                                        startDate.optInt("day", 0));

                                int rating = novelJson.optInt("averageScore", 0);
                                JSONArray genreArray = novelJson.optJSONArray("genres");
                                List<String> genres = new ArrayList<>();
                                if (genreArray != null) {
                                    for (int j = 0; j < genreArray.length(); j++) {
                                        genres.add(genreArray.getString(j));
                                    }
                                }
                                String coverImage = novelJson.getJSONObject("coverImage").getString("large");
                                String description = novelJson.optString("description", "Descripción no disponible");
                                String popularity = String.valueOf(novelJson.optInt("popularity", 0));

                                int finalI = i;
                                reference.orderByChild("mangaID").equalTo(novelID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean exists = false;

                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            Content existingContent = childSnapshot.getValue(Content.class);
                                            if (existingContent != null && novelID.equals(existingContent.getMangaID())) {
                                                exists = true;
                                                break;
                                            }
                                        }

                                        if (!exists) {
                                            Content content = new Content(
                                                    "cat_6",
                                                    title,
                                                    originalTitle,
                                                    description,
                                                    releaseDate,
                                                    String.valueOf(rating),
                                                    coverImage,
                                                    "AniList",
                                                    genres,
                                                    popularity,
                                                    novelID
                                            );
                                            content.setMangaID(novelID);
                                            content.setOrigin("ongoing_novel_" + year);
                                            contentService.insertManga(content);
                                            Log.d("ContentService", "Novela añadida desde ongoing: " + title);
                                        } else {
                                            Log.d("ContentService", "Duplicado omitido con el mismo novelID: " + title);
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


    public void fetchBestLightNovelsYearlyAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_light_novels_last_year_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int lastYear = currentYear - 1;

        contentService = new ContentService(context);


        try{
            // Query GraphQL para novelas ligeras publicadas el año pasado
            String graphqlQuery = "{ \"query\": \"query { Page(perPage: 20) { media(type: MANGA, format: NOVEL, sort: POPULARITY_DESC, startDate_greater: " + lastYear + "0101, startDate_lesser: " + lastYear + "1231) { id title { romaji english native } startDate { year month day } coverImage { large } siteUrl popularity description genres averageScore } } }\" }";

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
                                JSONObject mangaJson = results.getJSONObject(i);

                                String mangaID = mangaJson.getString("id");
                                String title = mangaJson.getJSONObject("title").optString("english", "Título desconocido");
                                String originalTitle = mangaJson.getJSONObject("title").optString("romaji", "Título desconocido");

                                JSONObject startDate = mangaJson.getJSONObject("startDate");
                                String releaseDate = String.format("%04d-%02d-%02d",
                                        startDate.optInt("year", 0),
                                        startDate.optInt("month", 0),
                                        startDate.optInt("day", 0));

                                int rating = mangaJson.optInt("averageScore", 0);
                                JSONArray genreArray = mangaJson.optJSONArray("genres");
                                List<String> genres = new ArrayList<>();
                                if (genreArray != null) {
                                    for (int j = 0; j < genreArray.length(); j++) {
                                        genres.add(genreArray.getString(j));
                                    }
                                }

                                String coverImage = mangaJson.getJSONObject("coverImage").getString("large");
                                String description = mangaJson.optString("description", "Descripción no disponible");
                                String popularity = String.valueOf(mangaJson.optInt("popularity", 0));

                                reference.orderByChild("mangaID").equalTo(mangaID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean exists = false;
                                        boolean existsWithNews = false;
                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            Content existingContent = childSnapshot.getValue(Content.class);
                                            if (existingContent != null && mangaID.equals(existingContent.getMangaID())) {
                                                exists = true;
                                                if (existingContent.getOrigin() != null && existingContent.getOrigin().startsWith("new_manga_")) {
                                                    existsWithNews = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!exists || !existsWithNews) {
                                            Content content = new Content(
                                                    "cat_6",
                                                    title,
                                                    originalTitle,
                                                    description,
                                                    releaseDate,
                                                    String.valueOf(rating),
                                                    coverImage,
                                                    "AniList",
                                                    genres,
                                                    popularity,
                                                    mangaID
                                            );
                                            content.setMangaID(mangaID);
                                            content.setOrigin("bestof_lightnovel_" + lastYear);
                                            contentService.insertManga(content);
                                            Log.d("ContentService", "Novela ligera añadida desde bestof_lightnovel_" + lastYear + ": " + title);
                                        } else {
                                            Log.d("ContentService", "Duplicado omitido con el mismo mangaID: " + title);
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


    public void fetchNewsNovelsAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_new_novels_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        int startDate = year * 10000 + 101; // 1 de enero
        int endDate = year * 10000 + 1231; // 31 de diciembre


        contentService = new ContentService(context);

        try{
            String graphqlQuery = "{ \"query\": \"query { Page(perPage: 20) { media(type: MANGA, format: NOVEL, sort: POPULARITY_DESC, startDate_greater: " + startDate + ", startDate_lesser: " + endDate + ") { id title { romaji english native } startDate { year month day } coverImage { large } siteUrl popularity description genres averageScore } } }\" }";

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
                                JSONObject novelJson = results.getJSONObject(i);

                                String mangaID = novelJson.getString("id");
                                String title = novelJson.getJSONObject("title").optString("english", "Título desconocido");
                                String originalTitle = novelJson.getJSONObject("title").optString("romaji", "Título desconocido");

                                JSONObject startDateJson = novelJson.getJSONObject("startDate");
                                String releaseDate = String.format("%04d-%02d-%02d",
                                        startDateJson.optInt("year", 0),
                                        startDateJson.optInt("month", 0),
                                        startDateJson.optInt("day", 0));

                                int rating = novelJson.optInt("averageScore", 0);
                                JSONArray genreArray = novelJson.optJSONArray("genres");
                                List<String> genres = new ArrayList<>();
                                if (genreArray != null) {
                                    for (int j = 0; j < genreArray.length(); j++) {
                                        genres.add(genreArray.getString(j));
                                    }
                                }

                                String coverImage = novelJson.getJSONObject("coverImage").getString("large");
                                String description = novelJson.optString("description", "Descripción no disponible");
                                String popularity = String.valueOf(novelJson.optInt("popularity", 0));

                                reference.orderByChild("mangaID").equalTo(mangaID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean exists = false;
                                        boolean existsWithOngoing = false;
                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            Content existingContent = childSnapshot.getValue(Content.class);
                                            if (existingContent != null && mangaID.equals(existingContent.getMangaID())) {
                                                exists = true;
                                                if (existingContent.getOrigin() != null && existingContent.getOrigin().startsWith("ongoing")) {
                                                    existsWithOngoing = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!exists || !existsWithOngoing) {
                                            Content content = new Content(
                                                    "cat_6",
                                                    title,
                                                    originalTitle,
                                                    description,
                                                    releaseDate,
                                                    String.valueOf(rating),
                                                    coverImage,
                                                    "AniList",
                                                    genres,
                                                    popularity,
                                                    mangaID
                                            );
                                            content.setMangaID(mangaID);
                                            content.setOrigin("new_novel_" + year);
                                            contentService.insertManga(content);
                                            Log.d("ContentService", "Novela añadida desde new_recognized: " + title);
                                        } else {
                                            Log.d("ContentService", "Duplicado omitido con el mismo mangaID: " + title);
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
