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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GamesService {


    private FirebaseDatabase db;
    private DatabaseReference reference;
    private ContentService contentService;

    public GamesService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("content");
    }

    // Llamadas a API Games

    public void fetchRecentGamesAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_rawg_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Restar un mes de la fecha actual
        calendar.add(Calendar.MONTH, -1);

        // Formatear la fecha resultante como "yyyy-MM-dd"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(calendar.getTime());

        // Obtener la fecha de hoy en el mismo formato
        String today = sdf.format(Calendar.getInstance().getTime());

        String urlRecent = "https://api.rawg.io/api/games?key=bd8a21ccc892473cbb6c36919b2a9e56&dates=" + startDate + "," + today + "&ordering=-added&page_size=10";

        contentService = new ContentService(context);


        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlRecent, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject gameJson = results.getJSONObject(i);
                            String rawgID = gameJson.getString("id");

                            String detailUrl = "https://api.rawg.io/api/games/" + rawgID + "?key=bd8a21ccc892473cbb6c36919b2a9e56";

                            JsonObjectRequest detailRequest = new JsonObjectRequest(Request.Method.GET, detailUrl, null,
                                    detailResponse -> {
                                        try {
                                            String title = detailResponse.getString("name");
                                            String description = detailResponse.optString("description_raw", "Sin descripción");
                                            String releaseDate = detailResponse.optString("released", "Desconocida");
                                            String rating = String.valueOf(detailResponse.optDouble("rating", 0.0));
                                            String added = detailResponse.optString("added", "Desconocida");
                                            String coverImage = detailResponse.optString("background_image", "");
                                            String website = detailResponse.optString("website", "Sin sitio web");

                                            List<String> platforms = new ArrayList<>();
                                            JSONArray platformArray = detailResponse.optJSONArray("platforms");
                                            if (platformArray != null) {
                                                for (int j = 0; j < platformArray.length(); j++) {
                                                    JSONObject platformObj = platformArray.getJSONObject(j).getJSONObject("platform");
                                                    platforms.add(platformObj.getString("name"));
                                                }
                                            }

                                            List<String> genres = new ArrayList<>();
                                            JSONArray genreArray = detailResponse.optJSONArray("genres");
                                            if (genreArray != null) {
                                                for (int j = 0; j < genreArray.length(); j++) {
                                                    genres.add(genreArray.getJSONObject(j).getString("name"));
                                                }
                                            }

                                            List<String> developers = new ArrayList<>();
                                            JSONArray developersArray = detailResponse.optJSONArray("developers");
                                            if (developersArray != null) {
                                                for (int j = 0; j < developersArray.length(); j++) {
                                                    developers.add(developersArray.getJSONObject(j).getString("name"));
                                                }
                                            }
                                            Content content = new Content(
                                                    "cat_3", title, description, releaseDate, rating, coverImage, "RAWG", platforms, website, genres, developers, added, rawgID
                                            );
                                            content.setOrigin("recent_game_" + currentYear);
                                            contentService.insertGame(content);
                                            Log.d("ContentService", "Juego reciente añadido: " + title);
                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");
                                            reference.orderByChild("gameID").equalTo(rawgID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (!snapshot.exists()) {
                                                    } else {
                                                        Log.d("ContentService", "Juego reciente duplicado omitido: " + title);
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                                }
                                            });
                                        } catch (JSONException e) {
                                            Log.e("ContentService", "Error procesando detalle de juego", e);
                                        }
                                    },
                                    error -> Log.e("ContentService", "Error solicitando detalles del juego", error)
                            );
                            queue.add(detailRequest);
                        }
                        prefs.edit().putLong("last_rawg_update", now).apply();
                    } catch (JSONException e) {
                        Log.e("ContentService", "Error procesando JSON principal", e);
                    }
                },
                error -> Log.e("ContentService", "Error en la solicitud RAWG", error)
        );
        queue.add(request);
        if (onComplete != null) onComplete.run();

    }

    public void fetchPopularGamesAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_rawg_popular_update", 0);
        long now = System.currentTimeMillis();

        // Fecha de inicio (1 de enero del año actual)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String startDate = currentYear + "-01-01";

        // Fecha actual
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(calendar.getTime());

        // URL para juegos populares
        String urlPopular = "https://api.rawg.io/api/games?key=bd8a21ccc892473cbb6c36919b2a9e56&dates=" + startDate + "," + today + "&ordering=-rating&page_size=10";

        RequestQueue queue = Volley.newRequestQueue(context);

        contentService = new ContentService(context);



        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlPopular, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject gameJson = results.getJSONObject(i);
                            String rawgID = gameJson.getString("id");

                            // URL para detalles del juego individual
                            String detailUrl = "https://api.rawg.io/api/games/" + rawgID + "?key=bd8a21ccc892473cbb6c36919b2a9e56";

                            JsonObjectRequest detailRequest = new JsonObjectRequest(Request.Method.GET, detailUrl, null,
                                    detailResponse -> {
                                        try {
                                            String title = detailResponse.getString("name");
                                            String description = detailResponse.optString("description_raw", "Sin descripción");
                                            String releaseDate = detailResponse.optString("released", "Desconocida");
                                            String rating = String.valueOf(detailResponse.optDouble("rating", 0.0));
                                            String added = detailResponse.optString("added", "Desconocida");
                                            String coverImage = detailResponse.optString("background_image", "");

                                            // Obtener plataformas
                                            List<String> platforms = new ArrayList<>();
                                            JSONArray platformArray = detailResponse.optJSONArray("platforms");
                                            if (platformArray != null) {
                                                for (int j = 0; j < platformArray.length(); j++) {
                                                    JSONObject platformObj = platformArray.getJSONObject(j).getJSONObject("platform");
                                                    platforms.add(platformObj.getString("name"));
                                                }
                                            }

                                            // Obtener géneros
                                            List<String> genres = new ArrayList<>();
                                            JSONArray genreArray = detailResponse.optJSONArray("genres");
                                            if (genreArray != null) {
                                                for (int j = 0; j < genreArray.length(); j++) {
                                                    genres.add(genreArray.getJSONObject(j).getString("name"));
                                                }
                                            }

                                            List<String> developers = new ArrayList<>();
                                            JSONArray developersArray = detailResponse.getJSONArray("developers");
                                            for (int j = 0; j < developersArray.length(); j++) {
                                                genres.add(developersArray.getJSONObject(j).getString("name"));
                                            }

                                            // Sitio web
                                            String website = detailResponse.optString("website", "Sin sitio web");

                                            Content content = new Content(
                                                    "cat_3",
                                                    title,
                                                    description,
                                                    releaseDate,
                                                    rating,
                                                    coverImage,
                                                    "RAWG",
                                                    platforms,
                                                    website,
                                                    genres,
                                                    developers,
                                                    added,
                                                    rawgID
                                            );

                                            content.setOrigin("popular_game_"+currentYear);

                                            contentService.insertGame(content);

                                        } catch (JSONException e) {
                                            Log.e("ContentService", "Error procesando detalle de juego", e);
                                        }
                                    },
                                    error -> Log.e("ContentService", "Error solicitando detalles del juego", error)
                            );

                            queue.add(detailRequest);
                        }


                        prefs.edit().putLong("last_rawg_popular_update", now).apply();

                    } catch (JSONException e) {
                        Log.e("ContentService", "Error procesando JSON principal", e);
                    }
                },
                error -> Log.e("ContentService", "Error en la solicitud RAWG", error)
        );

        queue.add(request);
        if (onComplete != null) onComplete.run();

    }

    public void fetchUpcomingGamesAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_rawg_upcoming_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Fecha actual como inicio
        String startDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day);

        // Sumar un mes
        calendar.add(Calendar.MONTH, 1);
        int futureYear = calendar.get(Calendar.YEAR);
        int futureMonth = calendar.get(Calendar.MONTH);
        int futureDay = day;

        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (futureDay > maxDay) {
            futureDay = maxDay;
        }

        String endDate = String.format(Locale.US, "%d-%02d-%02d", futureYear, futureMonth + 1, futureDay);

        // URL para juegos futuros
        String urlPopular = "https://api.rawg.io/api/games?key=bd8a21ccc892473cbb6c36919b2a9e56&dates="+startDate+","+endDate+"&ordering=-added&page_size=10";

        RequestQueue queue = Volley.newRequestQueue(context);


        contentService = new ContentService(context);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlPopular, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject gameJson = results.getJSONObject(i);
                            String rawgID = gameJson.getString("id");

                            // URL para detalles del juego individual
                            String detailUrl = "https://api.rawg.io/api/games/" + rawgID + "?key=bd8a21ccc892473cbb6c36919b2a9e56";

                            JsonObjectRequest detailRequest = new JsonObjectRequest(Request.Method.GET, detailUrl, null,
                                    detailResponse -> {
                                        try {
                                            String title = detailResponse.getString("name");
                                            String description = detailResponse.optString("description_raw", "Sin descripción");
                                            String releaseDate = detailResponse.optString("released", "Desconocida");
                                            String rating = String.valueOf(detailResponse.optDouble("rating", 0.0));
                                            String added = detailResponse.optString("added", "Desconocida");
                                            String coverImage = detailResponse.optString("background_image", "");

                                            // Obtener plataformas
                                            List<String> platforms = new ArrayList<>();
                                            JSONArray platformArray = detailResponse.optJSONArray("platforms");
                                            if (platformArray != null) {
                                                for (int j = 0; j < platformArray.length(); j++) {
                                                    JSONObject platformObj = platformArray.getJSONObject(j).getJSONObject("platform");
                                                    platforms.add(platformObj.getString("name"));
                                                }
                                            }

                                            // Obtener géneros
                                            List<String> genres = new ArrayList<>();
                                            JSONArray genreArray = detailResponse.optJSONArray("genres");
                                            if (genreArray != null) {
                                                for (int j = 0; j < genreArray.length(); j++) {
                                                    genres.add(genreArray.getJSONObject(j).getString("name"));
                                                }
                                            }

                                            List<String> developers = new ArrayList<>();
                                            JSONArray developersArray = detailResponse.getJSONArray("developers");
                                            for (int j = 0; j < developersArray.length(); j++) {
                                                genres.add(developersArray.getJSONObject(j).getString("name"));
                                            }

                                            // Sitio web
                                            String website = detailResponse.optString("website", "");

                                            Content content = new Content(
                                                    "cat_3",
                                                    title,
                                                    description,
                                                    releaseDate,
                                                    rating,
                                                    coverImage,
                                                    "RAWG",
                                                    platforms,
                                                    website,
                                                    genres,
                                                    developers,
                                                    added,
                                                    rawgID
                                            );

                                            content.setOrigin("upcoming_game_"+year);

                                            contentService.insertGame(content);

                                        } catch (JSONException e) {
                                            Log.e("ContentService", "Error procesando detalle de juego", e);
                                        }
                                    },
                                    error -> Log.e("ContentService", "Error solicitando detalles del juego", error)
                            );

                            queue.add(detailRequest);
                        }


                        prefs.edit().putLong("last_rawg_upcoming_update", now).apply();

                    } catch (JSONException e) {
                        Log.e("ContentService", "Error procesando JSON principal", e);
                    }
                },
                error -> Log.e("ContentService", "Error en la solicitud RAWG", error)
        );

        queue.add(request);
        if (onComplete != null) onComplete.run();

    }
}
