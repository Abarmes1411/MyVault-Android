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

    public String insertBook(Content content) {
        String baseTitle = content.getTitle();
        if (baseTitle == null || baseTitle.isEmpty()) {
            Log.e("InsertBook", "El título es nulo o vacío. No se puede insertar.");
            return null;
        }

        String normalizedTitle = baseTitle.toLowerCase().replaceAll("[^a-z0-9]", "");
        String releaseDate = content.getReleaseDate() != null ? content.getReleaseDate() : "unknown";
        String uniqueKey = normalizedTitle + "_" + releaseDate;

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
                        int existingPriority = getOriginBookPriority(existing.getOrigin());
                        int newPriority = getOriginBookPriority(content.getOrigin());

                        if (newPriority < existingPriority) {
                            newReference.setValue(content);
                            Log.d("InsertBook", "Sobrescrito por: " + content.getOrigin());
                        } else {
                            Log.d("InsertBook", "No sobrescrito, origin existente con mayor o igual prioridad: " + existing.getOrigin());
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
                                        insertMovie(newContent);
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
                                        insertMovie(content);
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
                                        insertMovie(content);
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
                                        insertShow(content);
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
                                        insertShow(content);
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
                                        insertShow(content);
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
                                            insertAnime(content);
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
                                        insertAnime(content);
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
                                        insertAnime(content);
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

    // Llamadas a API Mangas

    public void fetchTopOngoingMangasAndSave(Context context, Runnable onComplete){
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_mangas_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;


        try {
            // Añadir la descripción en la query de AniList
            String graphqlQuery = "{ \"query\": \"query { Page(perPage: 20) { media(type: MANGA, status: RELEASING, sort: POPULARITY_DESC) { id title { romaji english native } startDate { year month day } coverImage { large } siteUrl popularity description genres averageScore } } }\" }";

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

                                String mangaID = animeJson.getString("id");
                                String title = animeJson.getJSONObject("title").optString("english", "Título desconocido");
                                String originalTitle = animeJson.getJSONObject("title").optString("romaji", "Título desconocido");


                                JSONObject startDate = animeJson.getJSONObject("startDate");
                                String releaseDate = String.format("%04d-%02d-%02d",
                                        startDate.optInt("year", 0),
                                        startDate.optInt("month", 0),
                                        startDate.optInt("day", 0));

                                int rating = animeJson.optInt("averageScore", 0);
                                JSONArray genreArray = animeJson.optJSONArray("genres");
                                List<String> genres = new ArrayList<>();
                                if (genreArray != null) {
                                    for (int j = 0; j < genreArray.length(); j++) {
                                        genres.add(genreArray.getString(j));
                                    }
                                }
                                String coverImage = animeJson.getJSONObject("coverImage").getString("large");
                                String description = animeJson.optString("description", "Descripción no disponible");
                                String popularity = String.valueOf(animeJson.optInt("popularity", 0));


                                int finalI = i;
                                reference.orderByChild("mangaID").equalTo(mangaID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean exists = false;

                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            Content existingContent = childSnapshot.getValue(Content.class);
                                            if (existingContent != null && mangaID.equals(existingContent.getMangaID())) {
                                                exists = true;
                                                break;
                                            }
                                        }

                                        if (!exists) {
                                            Content content = new Content(
                                                    "cat_5",
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
                                            content.setOrigin("ongoing_" + year);
                                            insertManga(content);
                                            Log.d("ContentService", "Anime añadido desde ongoing: " + title);
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

    public void fetchNewsMangasAndSave(Context context, Runnable onComplete){
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_new_mangas_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        // Rango de fechas del año actual
        int startDate = year * 10000 + 101; // 1 de enero
        int endDate = year * 10000 + 1231; // 31 de diciembre

        try{
        String graphqlQuery = "{ \"query\": \"query { Page(perPage: 20) { media(type: MANGA, sort: POPULARITY_DESC, startDate_greater: " + startDate + ", startDate_lesser: " + endDate + ") { id title { romaji english native } startDate { year month day } coverImage { large } siteUrl popularity description genres averageScore } } }\" }";

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

                            JSONObject startDateJson = mangaJson.getJSONObject("startDate");
                            String releaseDate = String.format("%04d-%02d-%02d",
                                    startDateJson.optInt("year", 0),
                                    startDateJson.optInt("month", 0),
                                    startDateJson.optInt("day", 0));

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
                                                "cat_5",
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
                                        content.setOrigin("new_manga_" + year);
                                        insertManga(content);
                                        Log.d("ContentService", "Manga añadido desde new_recognized: " + title);
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

    public void fetchBestMangaYearlyAndSave(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_mangas_last_year_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int lastYear = currentYear - 1;

        try{
        // Query GraphQL con filtro por fechas del año pasado
        String graphqlQuery = "{ \"query\": \"query { Page(perPage: 20) { media(type: MANGA, sort: POPULARITY_DESC, startDate_greater: " + lastYear + "0101, startDate_lesser: " + lastYear + "1231) { id title { romaji english native } startDate { year month day } coverImage { large } siteUrl popularity description genres averageScore } } }\" }";

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

                            int finalI = i;
                            reference.orderByChild("animeID").equalTo(mangaID).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                "cat_5",
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
                                        content.setOrigin("bestof_" + lastYear);
                                        insertManga(content);
                                        Log.d("ContentService", "Manga añadido desde bestof_" + lastYear + ": " + title);
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

    // LLamadas a API Novels

    public void fetchTopOngoingNovelsAndSave(Context context, Runnable onComplete){
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_novels_update", 0);
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

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
                                        insertManga(content);
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
                                        insertManga(content);
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
                                            insertManga(content);
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

                                            content.setOrigin("recent_game_" + currentYear);
                                            insertGame(content);
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

                                            insertGame(content);

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

                                            insertGame(content);

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



    // Llamadas a API Books

    public void fetchPopularBooksAndSave(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("myvault_prefs", Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_fiction_books_update", 0);
        long now = System.currentTimeMillis();

        String url = "https://www.googleapis.com/books/v1/volumes?q=subject:fiction&orderBy=newest&maxResults=40&langRestrict=es";

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray items = response.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                            String title = volumeInfo.optString("title", "Sin título");

                            // FILTRO para evitar colecciones, packs o publicaciones mensuales
                            String titleLower = title.toLowerCase();
                            if (titleLower.contains("pack") || titleLower.contains("colection") || titleLower.contains("volume") ||
                                    titleLower.contains("box set") || titleLower.contains("serie") ||
                                    titleLower.contains("january") || titleLower.contains("february") || titleLower.contains("march") ||
                                    titleLower.contains("april") || titleLower.contains("may") || titleLower.contains("june") ||
                                    titleLower.contains("julie") || titleLower.contains("august") || titleLower.contains("september") ||
                                    titleLower.contains("october") || titleLower.contains("november") || titleLower.contains("december")) {
                                continue;
                            }

                            String description = volumeInfo.optString("description", "Sin descripción");
                            String releaseDate = volumeInfo.optString("publishedDate", "Desconocida");

                            // Extraer el año del publishedDate (puede venir como "2024-03-12", "2022", etc.)
                            int publishedYear = 0;
                            if (!releaseDate.equals("Desconocida")) {
                                try {
                                    publishedYear = Integer.parseInt(releaseDate.substring(0, 4));
                                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                                    publishedYear = 0; // Si no se puede extraer bien el año
                                }
                            }

                            // Filtrar libros publicados desde 2020 en adelante
                            if (publishedYear < 2020) {
                                continue;
                            }

                            String publisher = volumeInfo.optString("publisher", "Desconocido");
                            String pages = String.valueOf(volumeInfo.optInt("pageCount", 0));
                            String language = volumeInfo.optString("language", "es");

                            List<String> authors = new ArrayList<>();
                            JSONArray authorsArray = volumeInfo.optJSONArray("authors");
                            if (authorsArray != null) {
                                for (int j = 0; j < authorsArray.length(); j++) {
                                    authors.add(authorsArray.getString(j));
                                }
                            }

                            String coverImage = "";
                            if (volumeInfo.has("imageLinks")) {
                                JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                                coverImage = imageLinks.optString("thumbnail", "");
                            }
                            if (coverImage == null || coverImage.trim().isEmpty()) {
                                coverImage = "drawable/no_image_available.jpg";
                            }

                            JSONObject saleInfo = item.optJSONObject("saleInfo");
                            boolean isEbook = false;
                            String saleability = "NOT_FOR_SALE";
                            String retailPrice = "";
                            String currency = "";

                            if (saleInfo != null) {
                                isEbook = saleInfo.optBoolean("isEbook", false);
                                saleability = saleInfo.optString("saleability", "NOT_FOR_SALE");

                                JSONObject retailPriceObj = saleInfo.optJSONObject("retailPrice");
                                if (retailPriceObj != null) {
                                    retailPrice = String.valueOf(retailPriceObj.optDouble("amount", 0));
                                    currency = retailPriceObj.optString("currencyCode", "");
                                }
                            }

                            String bookID = item.optString("id", "");

                            Content content = new Content(
                                    "cat_2",
                                    title,
                                    description,
                                    releaseDate,
                                    coverImage,
                                    "",
                                    "GoogleBooks",
                                    "popular_fiction_books_" + Calendar.getInstance().get(Calendar.YEAR),
                                    publisher,
                                    authors,
                                    isEbook,
                                    saleability,
                                    pages,
                                    language,
                                    retailPrice,
                                    currency,
                                    bookID
                            );

                            insertBook(content);
                        }

                        prefs.edit().putLong("last_fiction_books_update", now).apply();

                    } catch (JSONException e) {
                        Log.e("BookService", "Error al procesar JSON de libros", e);
                    }
                },
                error -> Log.e("BookService", "Error al obtener libros", error)
        );

        queue.add(request);
    }







}
