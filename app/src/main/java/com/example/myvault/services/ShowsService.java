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
import java.util.Locale;

public class ShowsService {

    private FirebaseDatabase db;
    private DatabaseReference reference;
    private ContentService contentService;

    public ShowsService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("content");
    }


    // Llamadas a API Shows

    public void fetchRecentShowsAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_tmdb_tv_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Primer día del mes
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = String.format(Locale.US, "%d-%02d-%02d", year, month, day);

        // Fecha actual
        calendar.set(Calendar.DAY_OF_MONTH, day);
        String endDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day);

        RequestQueue queue = Volley.newRequestQueue(context);
        String urlRecent = "https://api.themoviedb.org/3/discover/tv?api_key=87bb7efee694a2a79d9514b2c909e544&language=es-ES&sort_by=popularity.desc&first_air_date.gte=" + startDate + "&first_air_date.lte=" + endDate + "&page=1";

        contentService = new ContentService(context);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlRecent, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                        for (int i = 0; i < results.length(); i++) {

                            JSONObject showJson = results.getJSONObject(i);

                            String originalLanguage = showJson.optString("original_language", "");
                            JSONArray originCountries = showJson.optJSONArray("origin_country");
                            boolean isJapanese = originalLanguage.equals("ja");

                            if (originCountries != null) {
                                for (int j = 0; j < originCountries.length(); j++) {
                                    String country = originCountries.getString(j);
                                    if (country.equals("JP")) {
                                        isJapanese = true;
                                        break;
                                    }
                                }
                            }

                            if (isJapanese) {
                                Log.d("ContentService", "Omitida serie japonesa por tratarse de anime: " + showJson.optString("name"));
                                continue;
                            }


                            String tmdbTVID = showJson.getString("id");
                            String title = showJson.getString("name");
                            String description = showJson.getString("overview");
                            String releaseDate = showJson.getString("first_air_date");
                            String posterPath = showJson.getString("poster_path");
                            double rating = showJson.optDouble("vote_average", 0.0);
                            String coverImage = "https://image.tmdb.org/t/p/w500" + posterPath;

                            List<String> genres = new ArrayList<>();
                            JSONArray genreArray = showJson.optJSONArray("genre_ids");
                            if (genreArray != null) {
                                for (int j = 0; j < genreArray.length(); j++) {
                                    genres.add(String.valueOf(genreArray.getInt(j)));
                                }
                            }

                            Content content = new Content(
                                    "cat_2",
                                    title,
                                    description,
                                    releaseDate,
                                    genres,
                                    String.valueOf(rating),
                                    coverImage,
                                    "TMDB",
                                    null,
                                    tmdbTVID
                            );
                            content.setOrigin("recent_tv_" + year);

                            reference.orderByChild("tmdbTVID").equalTo(tmdbTVID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean exists = false;
                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        Content existingContent = childSnapshot.getValue(Content.class);
                                        if (existingContent != null && existingContent.getTmdbTVID().equals(tmdbTVID)) {
                                            exists = true;
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        Log.d("ContentService", "Insertando en Firebase: " + title + " con origin: " + content.getOrigin());
                                        contentService.insertShow(content);
                                        Log.d("ContentService", "Serie añadida: " + title);
                                    } else {
                                        Log.d("ContentService", "Duplicado omitido con el mismo tmdbTVID: " + title);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                }
                            });
                        }

                        // Actualizar la fecha de última actualización
                        prefs.edit().putLong("last_tmdb_tv_update", now).apply();

                    } catch (JSONException e) {
                        Log.e("ContentService", "Error procesando JSON", e);
                    }
                },
                error -> Log.e("ContentService", "Error en la solicitud TMDb", error)
        );

        queue.add(request);
        if (onComplete != null) onComplete.run();

    }

    public void fetchPopularShowsAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_tmdb_tv_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Fecha actual
        calendar.set(Calendar.DAY_OF_MONTH, day);
        String date = String.format(Locale.US, "%d-%02d-%02d", year-1, month + 1, day);

        RequestQueue queue = Volley.newRequestQueue(context);
        String urlRecent = "https://api.themoviedb.org/3/discover/tv?api_key=87bb7efee694a2a79d9514b2c909e544&language=es-ES&sort_by=popularity.desc&first_air_date.gte="+date+"&page=1";

        contentService = new ContentService(context);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlRecent, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject showJson = results.getJSONObject(i);

                            String originalLanguage = showJson.optString("original_language", "");
                            JSONArray originCountries = showJson.optJSONArray("origin_country");
                            boolean isJapanese = originalLanguage.equals("ja");

                            if (originCountries != null) {
                                for (int j = 0; j < originCountries.length(); j++) {
                                    String country = originCountries.getString(j);
                                    if (country.equals("JP")) {
                                        isJapanese = true;
                                        break;
                                    }
                                }
                            }

                            if (isJapanese) {
                                Log.d("ContentService", "Omitida serie japonesa por tratarse de anime: " + showJson.optString("name"));
                                continue;
                            }

                            String tmdbTVID = showJson.getString("id");
                            String title = showJson.getString("name");
                            String description = showJson.getString("overview");
                            String releaseDate = showJson.getString("first_air_date");
                            String posterPath = showJson.getString("poster_path");
                            double rating = showJson.optDouble("vote_average", 0.0);
                            String coverImage = "https://image.tmdb.org/t/p/w500" + posterPath;

                            List<String> genres = new ArrayList<>();
                            JSONArray genreArray = showJson.optJSONArray("genre_ids");
                            if (genreArray != null) {
                                for (int j = 0; j < genreArray.length(); j++) {
                                    genres.add(String.valueOf(genreArray.getInt(j)));
                                }
                            }

                            Content content = new Content(
                                    "cat_2",
                                    title,
                                    description,
                                    releaseDate,
                                    genres,
                                    String.valueOf(rating),
                                    coverImage,
                                    "TMDB",
                                    null,
                                    tmdbTVID
                            );
                            content.setOrigin("popular_tv_" + year);

                            reference.orderByChild("tmdbTVID").equalTo(tmdbTVID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean exists = false;
                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        Content existingContent = childSnapshot.getValue(Content.class);
                                        if (existingContent != null && existingContent.getTmdbTVID().equals(tmdbTVID)) {
                                            exists = true;
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        Log.d("ContentService", "Insertando en Firebase: " + title + " con origin: " + content.getOrigin());
                                        contentService.insertShow(content);
                                        Log.d("ContentService", "Serie añadida: " + title);
                                    } else {
                                        Log.d("ContentService", "Duplicado omitido con el mismo tmdbTVID: " + title);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                }
                            });
                        }

                        // Actualizar la fecha de última actualización
                        prefs.edit().putLong("last_tmdb_tv_update", now).apply();

                    } catch (JSONException e) {
                        Log.e("ContentService", "Error procesando JSON", e);
                    }
                },
                error -> Log.e("ContentService", "Error en la solicitud TMDb", error)
        );

        queue.add(request);
        if (onComplete != null) onComplete.run();

    }

    public void fetchUpcomingShowsAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_tmdb_tv_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Fecha actual
        calendar.set(Calendar.DAY_OF_MONTH, day);
        String date = String.format(Locale.US, "%d-%02d-%02d", year-1, month + 1, day);

        RequestQueue queue = Volley.newRequestQueue(context);
        String urlRecent = "https://api.themoviedb.org/3/discover/tv?api_key=87bb7efee694a2a79d9514b2c909e544&language=es-ES&sort_by=popularity.desc&first_air_date.gte="+date+"&page=1";

        contentService = new ContentService(context);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlRecent, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject showJson = results.getJSONObject(i);

                            String originalLanguage = showJson.optString("original_language", "");
                            JSONArray originCountries = showJson.optJSONArray("origin_country");
                            boolean isJapanese = originalLanguage.equals("ja");

                            if (originCountries != null) {
                                for (int j = 0; j < originCountries.length(); j++) {
                                    String country = originCountries.getString(j);
                                    if (country.equals("JP")) {
                                        isJapanese = true;
                                        break;
                                    }
                                }
                            }

                            if (isJapanese) {
                                Log.d("ContentService", "Omitida serie japonesa por tratarse de anime: " + showJson.optString("name"));
                                continue;
                            }

                            String tmdbTVID = showJson.getString("id");
                            String title = showJson.getString("name");
                            String description = showJson.getString("overview");
                            String releaseDate = showJson.getString("first_air_date");
                            String posterPath = showJson.getString("poster_path");
                            double rating = showJson.optDouble("vote_average", 0.0);
                            String coverImage = "https://image.tmdb.org/t/p/w500" + posterPath;

                            List<String> genres = new ArrayList<>();
                            JSONArray genreArray = showJson.optJSONArray("genre_ids");
                            if (genreArray != null) {
                                for (int j = 0; j < genreArray.length(); j++) {
                                    genres.add(String.valueOf(genreArray.getInt(j)));
                                }
                            }

                            Content content = new Content(
                                    "cat_2",
                                    title,
                                    description,
                                    releaseDate,
                                    genres,
                                    String.valueOf(rating),
                                    coverImage,
                                    "TMDB",
                                    null,
                                    tmdbTVID
                            );
                            content.setOrigin("upcoming_tv_" + year);

                            reference.orderByChild("tmdbTVID").equalTo(tmdbTVID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean exists = false;
                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        Content existingContent = childSnapshot.getValue(Content.class);
                                        if (existingContent != null && existingContent.getTmdbTVID().equals(tmdbTVID)) {
                                            exists = true;
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        Log.d("ContentService", "Insertando en Firebase: " + title + " con origin: " + content.getOrigin());
                                        contentService.insertShow(content);
                                        Log.d("ContentService", "Serie añadida: " + title);
                                    } else {
                                        Log.d("ContentService", "Duplicado omitido con el mismo tmdbTVID: " + title);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                }
                            });
                        }

                        // Actualizar la fecha de última actualización
                        prefs.edit().putLong("last_tmdb_tv_update", now).apply();

                    } catch (JSONException e) {
                        Log.e("ContentService", "Error procesando JSON", e);
                    }
                },
                error -> Log.e("ContentService", "Error en la solicitud TMDb", error)
        );

        queue.add(request);
        if (onComplete != null) onComplete.run();
    }


}
