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
import java.util.Objects;

public class MoviesService {

    private FirebaseDatabase db;
    private DatabaseReference reference;
    private ContentService contentService;

    public MoviesService(Context context){
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("content");
    }


    // Llamadas a API Movies

    public void fetchRecentMoviesAndSave(Context context, Runnable onComplete) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        String endDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day);

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://api.themoviedb.org/3/discover/movie?api_key=87bb7efee694a2a79d9514b2c909e544&language=es-ES&region=ES&sort_by=popularity.desc&primary_release_date.gte=" + startDate + "&primary_release_date.lte=" + endDate + "&vote_count.gte=20&page=1";


        contentService = new ContentService(context);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject movieJson = results.getJSONObject(i);

                            String tmdbID = movieJson.getString("id");
                            String title = movieJson.getString("title");
                            String description = movieJson.getString("overview");
                            String releaseDate = movieJson.getString("release_date");
                            String posterPath = movieJson.getString("poster_path");
                            double rating = movieJson.optDouble("vote_average", 0.0);
                            String coverImage = "https://image.tmdb.org/t/p/w500" + posterPath;
                            String origin = "recent_" + year;

                            List<String> genres = new ArrayList<>();
                            JSONArray genreArray = movieJson.optJSONArray("genres");
                            if (genreArray != null) {
                                for (int j = 0; j < genreArray.length(); j++) {
                                    genres.add(genreArray.getString(j));
                                }
                            }

                            Content newContent = new Content("cat_1", title, description, releaseDate, genres, String.valueOf(rating), coverImage, "TMDB", tmdbID, null);
                            newContent.setOrigin(origin);

                            reference.orderByChild("tmdbID").equalTo(tmdbID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean exists = false;

                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        Content existing = childSnapshot.getValue(Content.class);
                                        if (existing != null && origin.equals(existing.getOrigin())) {
                                            exists = true;

                                            boolean needsUpdate =
                                                    !Objects.equals(existing.getTitle(), title) ||
                                                            !Objects.equals(existing.getDescription(), description) ||
                                                            !Objects.equals(existing.getReleaseDate(), releaseDate) ||
                                                            !Objects.equals(existing.getRating(), String.valueOf(rating)) ||
                                                            !Objects.equals(existing.getCoverImage(), coverImage) ||
                                                            !Objects.equals(existing.getGenresTMDB(), genres);

                                            if (needsUpdate) {
                                                Log.d("ContentService", "Actualizando película: " + title);
                                                childSnapshot.getRef().setValue(newContent);
                                            } else {
                                                Log.d("ContentService", "Película sin cambios: " + title);
                                            }
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        contentService.insertMovie(newContent);
                                        Log.d("ContentService", "Película añadida: " + title);
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
                error -> Log.e("ContentService", "Error en la solicitud TMDb", error)
        );

        queue.add(request);
        if (onComplete != null) onComplete.run();
    }



    public void fetchPopularMoviesAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_tmdb_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);


        RequestQueue queue = Volley.newRequestQueue(context);
        String urlRecent = "https://api.themoviedb.org/3/movie/popular?api_key=87bb7efee694a2a79d9514b2c909e544&language=es-ES&page=1";


        contentService = new ContentService(context);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlRecent, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject movieJson = results.getJSONObject(i);

                            String tmdbID = movieJson.getString("id");
                            String title = movieJson.getString("title");
                            String description = movieJson.getString("overview");
                            String releaseDate = movieJson.getString("release_date");
                            String posterPath = movieJson.getString("poster_path");
                            double rating = movieJson.optDouble("vote_average", 0.0);
                            String coverImage = "https://image.tmdb.org/t/p/w500" + posterPath;

                            List<String> genres = new ArrayList<>();
                            JSONArray genreArray = movieJson.optJSONArray("genres");
                            if (genreArray != null) {
                                for (int j = 0; j < genreArray.length(); j++) {
                                    genres.add(genreArray.getString(j));
                                }
                            }

                            // Se comprueba si la pelicula ya existe en la base de datos
                            int finalI = i;
                            // Se comprueba si la pelicula ya existe en la base de datos por tmdbID
                            reference.orderByChild("tmdbID").equalTo(tmdbID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean exists = false;
                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        Content existingContent = childSnapshot.getValue(Content.class);
                                        if (existingContent != null && existingContent.getTmdbID().equals(tmdbID)) {
                                            exists = true;
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        Content content = new Content(
                                                "cat_1",
                                                title,
                                                description,
                                                releaseDate,
                                                genres,
                                                String.valueOf(rating),
                                                coverImage,
                                                "TMDB",
                                                tmdbID,
                                                null
                                        );
                                        content.setOrigin("popular_" + year);
                                        contentService.insertMovie(content);
                                        Log.d("ContentService", "Película añadida: " + title);
                                    } else {
                                        Log.d("ContentService", "Duplicado omitido con el mismo tmdbID: " + title);
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
                error -> Log.e("ContentService", "Error en la solicitud TMDb", error)
        );

        queue.add(request);

        if (onComplete != null) onComplete.run();

    }

    public void fetchUpcomingMoviesAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_tmdb_update", 0);
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

        // Verificar que el día exista en el nuevo mes
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (futureDay > maxDay) {
            futureDay = maxDay;
        }

        String endDate = String.format(Locale.US, "%d-%02d-%02d", futureYear, futureMonth + 1, futureDay);


        RequestQueue queue = Volley.newRequestQueue(context);
        String urlRecent = "https://api.themoviedb.org/3/movie/upcoming?api_key=87bb7efee694a2a79d9514b2c909e544&language=es-ES&region=ES&sort_by=popularity.desc&primary_release_date.gte="+startDate+"&primary_release_date.lte="+endDate+"&vote_count.gte=20&page=1";


        contentService = new ContentService(context);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlRecent, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject movieJson = results.getJSONObject(i);

                            String tmdbID = movieJson.getString("id");
                            String title = movieJson.getString("title");
                            String description = movieJson.getString("overview");
                            String releaseDate = movieJson.getString("release_date");
                            String posterPath = movieJson.getString("poster_path");
                            double rating = movieJson.optDouble("vote_average", 0.0);
                            String coverImage = "https://image.tmdb.org/t/p/w500" + posterPath;


                            List<String> genres = new ArrayList<>();
                            JSONArray genreArray = movieJson.optJSONArray("genres");
                            if (genreArray != null) {
                                for (int j = 0; j < genreArray.length(); j++) {
                                    genres.add(genreArray.getString(j));
                                }
                            }

                            // Se comprueba si la pelicula ya existe en la base de datos
                            int finalI = i;
                            reference.orderByChild("tmdbID").equalTo(tmdbID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean exists = false;
                                    Log.d("ContentUpcoming", "Respuesta de Upcoming: " + response.toString());

                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        Content existingContent = childSnapshot.getValue(Content.class);
                                        if (existingContent != null && existingContent.getTmdbID().equals(tmdbID)) {
                                            exists = true;
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        Content content = new Content(
                                                "cat_1",
                                                title,
                                                description,
                                                releaseDate,
                                                genres,
                                                String.valueOf(rating),
                                                coverImage,
                                                "TMDB",
                                                tmdbID,
                                                null
                                        );
                                        content.setOrigin("upcoming_" + year);
                                        contentService.insertMovie(content);
                                        Log.d("ContentService", "Película añadida: " + title);
                                    } else {
                                        Log.d("ContentService", "Duplicado omitido con el mismo tmdbID: " + title);
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
                error -> Log.e("ContentService", "Error en la solicitud TMDb", error)
        );

        queue.add(request);

        if (onComplete != null) onComplete.run();
    }


}
