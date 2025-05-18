package com.example.myvault.services;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myvault.enums.Categories;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchService {


    public interface Callback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    public static void searchMinimal(String query, Categories category, Callback<List<Content>> callback, Context context) {
        switch (category) {
            case PELICULAS:
                buscarEnTMDb(query, "movie", callback, context);
                break;
            case SERIES:
                buscarEnTMDb(query, "tv", callback, context);
                break;
            case VIDEOJUEGOS:
                buscarEnRAWG(query, callback, context);
                break;
            case ANIME:
                buscarEnAniList(query, "ANIME", null, callback, context);
                break;
            case MANGAS:
                buscarEnAniList(query, "MANGA", null, callback, context);
                break;
            case NOVELAS_LIGERAS:
                buscarEnAniList(query, "MANGA", "NOVEL", callback, context);
                break;
        }
    }



    public static void fetchDetails(Content minimal, Callback<Content> callback, Context context) {
        Log.d("ContentService", "Buscando detalles de: " + minimal.getId());
        Log.d("ContentService", "Categoría: " + minimal.getCategoryID());
        Log.d("ContentService", "Título: " + minimal.getTitle());
        String categoryID = minimal.getCategoryID();
        if (categoryID == null) {
            callback.onError(new IllegalArgumentException("categoryID es null en Content"));
            return;
        }
        Categories catenum = getCategoryEnumFromID(categoryID);

        switch (catenum) {
            case PELICULAS:
                obtenerDetallesTMDb(minimal.getId(), "movie", callback, context);
                break;
            case SERIES:
                obtenerDetallesTMDb(minimal.getId(), "tv", callback, context);
                break;
            case VIDEOJUEGOS:
                obtenerDetallesRAWG(minimal.getId(), callback, context);
                break;
            case ANIME:
            case MANGAS:
            case NOVELAS_LIGERAS:
                obtenerDetallesAniList(minimal.getId(), catenum, callback, context);
                break;
        }
    }


    private static Categories getCategoryEnumFromID(String categoryID) {
        switch (categoryID) {
            case "cat_1":
                return Categories.PELICULAS;
            case "cat_2":
                return Categories.SERIES;
            case "cat_3":
                return Categories.VIDEOJUEGOS;
            case "cat_4":
                return Categories.ANIME;
            case "cat_5":
                return Categories.MANGAS;
            case "cat_6":
                return Categories.NOVELAS_LIGERAS;
            default:
                throw new IllegalArgumentException("Categoría desconocida: " + categoryID);
        }
    }


    private static void buscarEnTMDb(String query, String type, Callback<List<Content>> callback, Context context) {
        String url = "https://api.themoviedb.org/3/search/" + type +
                "?api_key=87bb7efee694a2a79d9514b2c909e544&language=es-ES&query=" + Uri.encode(query);

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Content> result = new ArrayList<>();
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject obj = results.getJSONObject(i);
                            String id = obj.getString("id");
                            String title = obj.optString("title", obj.optString("name", ""));
                            String date = obj.optString("release_date", obj.optString("first_air_date", ""));
                            String image = "https://image.tmdb.org/t/p/w500" + obj.optString("poster_path", "");

                            String originalLanguage = obj.optString("original_language", "");
                            JSONArray originCountries = obj.optJSONArray("origin_country");
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
                                Log.d("ContentService", "Omitida serie japonesa por tratarse de anime: " + obj.optString("name"));
                                continue;
                            }

                            Categories cat;
                            if (type.equals("movie")) {
                                cat = Categories.PELICULAS;
                            } else if (type.equals("tv")) {
                                cat = Categories.SERIES;
                            } else {
                                continue;
                            }

                            Content c = new Content(id, title, date, image, cat);
                            c.setCategoryID(cat.getId());
                            Log.d("CategoryID", "Category ID: " + cat.getId());
                            Log.d("ContentService", "Película encontrada: " + title);
                            Log.d("ContentService", "ID: " + id);
                            Log.d("ContentService", "Fecha de lanzamiento: " + date);
                            Log.d("ContentService", "Imagen: " + image);
                            c.setId(id);
                            result.add(c);
                        }
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                }, callback::onError);

        queue.add(request);
    }


    private static void buscarEnRAWG(String query, Callback<List<Content>> callback, Context context) {
        String url = "https://api.rawg.io/api/games?key=bd8a21ccc892473cbb6c36919b2a9e56&search=" + Uri.encode(query);
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Content> result = new ArrayList<>();
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject obj = results.getJSONObject(i);
                            String id = obj.getString("id");
                            String title = obj.getString("name");
                            String date = obj.optString("released", "");
                            String image = obj.optString("background_image", "");

                            Content c = new Content(id, title, date, image, Categories.VIDEOJUEGOS);
                            c.setCategoryID(Categories.VIDEOJUEGOS.getId());
                            Log.d("CategoryID", "Category ID: " + Categories.VIDEOJUEGOS.getId());
                            c.setId(id);
                            result.add(c);
                        }
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                }, callback::onError);

        queue.add(request);
    }

    private static void buscarEnAniList(String query, String mediaType, String format, Callback<List<Content>> callback, Context context) {
        String graphql = "query ($search: String) { " +
                "Page(perPage: 20) { " +
                "media(search: $search, type: " + mediaType +
                (format != null ? ", format: " + format : "") +
                ") { " +
                "id " +
                "title { romaji } " +
                "startDate { year month day } " +
                "coverImage { large } " +
                "siteUrl " +
                "isAdult " +
                "} } }";

        JSONObject variables = new JSONObject();
        try {
            variables.put("search", query);
        } catch (JSONException e) {
            callback.onError(e);
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("query", graphql);
            json.put("variables", variables);
        } catch (JSONException e) {
            callback.onError(e);
            return;
        }

        String url = "https://graphql.anilist.co";
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    List<Content> result = new ArrayList<>();
                    try {
                        JSONArray mediaArray = response.getJSONObject("data")
                                .getJSONObject("Page")
                                .getJSONArray("media");

                        for (int i = 0; i < mediaArray.length(); i++) {
                            JSONObject obj = mediaArray.getJSONObject(i);

                            if (obj.optBoolean("isAdult", false)) {
                                continue;
                            }

                            String id = obj.getString("id");
                            String title = obj.getJSONObject("title").optString("romaji", "Sin título");
                            JSONObject startDate = obj.optJSONObject("startDate");
                            String date = "";
                            if (startDate != null)
                                date = startDate.optInt("year", 0) + "-" +
                                        startDate.optInt("month", 0) + "-" +
                                        startDate.optInt("day", 0);

                            String image = obj.getJSONObject("coverImage").optString("large", "");
                            Categories cat = mediaType.equals("ANIME") ? Categories.ANIME :
                                    format != null ? Categories.NOVELAS_LIGERAS : Categories.MANGAS;

                            Content c = new Content(id, title, date, image, cat);
                            c.setCategoryID(cat.getId());
                            c.setId(id);
                            result.add(c);
                        }
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                }, callback::onError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private static void obtenerDetallesTMDb(String id, String type, Callback<Content> callback, Context context) {

        String url = "https://api.themoviedb.org/3/" + type + "/" + id + "?api_key=87bb7efee694a2a79d9514b2c909e544&language=es-ES";

        RequestQueue queue = Volley.newRequestQueue(context);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");
        ContentService contentService = new ContentService(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String title = response.getString("title");
                        String description = response.getString("overview");
                        String releaseDate = response.getString("release_date");
                        String posterPath = response.optString("poster_path", null);
                        double rating = response.optDouble("vote_average", 0.0);
                        String coverImage = posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : "";

                        List<String> genres = new ArrayList<>();
                        JSONArray genreArray = response.optJSONArray("genres");
                        if (genreArray != null) {
                            for (int j = 0; j < genreArray.length(); j++) {
                                genres.add(genreArray.getString(j));
                            }
                        }

                        if (type.equals("tv")) {
                            Content content = new Content("cat_2", title, description, releaseDate, genres, String.valueOf(rating), coverImage, "TMDB", null, id);

                            reference.orderByChild("tmdbTVID").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean exists = false;

                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        Content existing = childSnapshot.getValue(Content.class);
                                        if (existing != null) {
                                            exists = true;

                                            boolean needsUpdate =
                                                    !Objects.equals(existing.getTitle(), title) ||
                                                            !Objects.equals(existing.getDescription(), description) ||
                                                            !Objects.equals(existing.getReleaseDate(), releaseDate) ||
                                                            !Objects.equals(existing.getRating(), String.valueOf(rating)) ||
                                                            !Objects.equals(existing.getCoverImage(), coverImage) ||
                                                            !Objects.equals(existing.getGenresTMDB(), genres);

                                            if (needsUpdate) {
                                                Log.d("ContentService", "Actualizando serie: " + title);
                                                childSnapshot.getRef().setValue(content);
                                                callback.onSuccess(content);
                                            } else {
                                                Log.d("ContentService", "Serie sin cambios: " + title);
                                                callback.onSuccess(existing);
                                            }
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        contentService.insertMovie(content);
                                        Log.d("ContentService", "Serie añadida: " + title);
                                        callback.onSuccess(content);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                    callback.onError(error.toException());
                                }
                            });
                        } else if (type.equals("movie")) {
                            Content content = new Content("cat_1", title, description, releaseDate, genres, String.valueOf(rating), coverImage, "TMDB", id, null);

                            reference.orderByChild("tmdbID").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean exists = false;

                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        Content existing = childSnapshot.getValue(Content.class);
                                        if (existing != null) {
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
                                                childSnapshot.getRef().setValue(content);
                                                callback.onSuccess(content);
                                            } else {
                                                Log.d("ContentService", "Película sin cambios: " + title);
                                                callback.onSuccess(existing);
                                            }
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        contentService.insertMovie(content);
                                        Log.d("ContentService", "Película añadida: " + title);
                                        callback.onSuccess(content);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                                    callback.onError(error.toException());
                                }
                            });
                        }

                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                }, callback::onError);

        queue.add(request);
    }


    private static void obtenerDetallesRAWG(String id, Callback<Content> callback, Context context) {
        String url = "https://api.rawg.io/api/games/" + id + "?key=bd8a21ccc892473cbb6c36919b2a9e56";

        RequestQueue queue = Volley.newRequestQueue(context);

        ContentService contentService;
        contentService = new ContentService(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String rawgID = response.getString("id");
                        String title = response.getString("name");
                        String description = response.optString("description_raw", "Sin descripción");
                        String releaseDate = response.optString("released", "Desconocida");
                        String rating = String.valueOf(response.optDouble("rating", 0.0));
                        String added = response.optString("added", "Desconocida");
                        String coverImage = response.optString("background_image", "");
                        String website = response.optString("website", "Sin sitio web");

                        List<String> platforms = new ArrayList<>();
                        JSONArray platformArray = response.optJSONArray("platforms");
                        if (platformArray != null) {
                            for (int j = 0; j < platformArray.length(); j++) {
                                JSONObject platformObj = platformArray.getJSONObject(j).getJSONObject("platform");
                                platforms.add(platformObj.getString("name"));
                            }
                        }

                        List<String> genres = new ArrayList<>();
                        JSONArray genreArray = response.optJSONArray("genres");
                        if (genreArray != null) {
                            for (int j = 0; j < genreArray.length(); j++) {
                                genres.add(genreArray.getJSONObject(j).getString("name"));
                            }
                        }

                        List<String> developers = new ArrayList<>();
                        JSONArray developersArray = response.optJSONArray("developers");
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

                        Log.d("ContentService", "Juego reciente añadido: " + title);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                        reference.orderByChild("gameID").equalTo(rawgID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    contentService.insertGame(content);

                                } else {
                                    Log.d("ContentService", "Juego duplicado omitido: " + title);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Firebase", "Error buscando duplicados: " + error.getMessage());
                            }
                        });

                        callback.onSuccess(content);
                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                }, callback::onError);

        queue.add(request);
    }

    private static void obtenerDetallesAniList(String id, Categories category, Callback<Content> callback, Context context) {
        String mediaType = category == Categories.ANIME ? "ANIME" : "MANGA";

        String graphqlQuery = "query ($id: Int) {" +
                " Media(id: $id, type: " + mediaType + ") {" +
                "   id title { romaji english native } startDate { year month day } description coverImage { large }" +
                "   siteUrl genres averageScore episodes chapters volumes format popularity } }";

        ContentService contentService = new ContentService(context);

        JSONObject variables = new JSONObject();
        try {
            variables.put("id", Integer.parseInt(id));
        } catch (JSONException e) {
            callback.onError(e);
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("query", graphqlQuery);
            json.put("variables", variables);
        } catch (JSONException e) {
            callback.onError(e);
            return;
        }

        String url = "https://graphql.anilist.co";
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    try {
                        JSONObject media = response.getJSONObject("data").getJSONObject("Media");

                        String title = media.getJSONObject("title").optString("english", media.getJSONObject("title").optString("romaji", "Sin título"));
                        String originalTitle = media.getJSONObject("title").optString("romaji", "Sin título original");

                        JSONObject startDate = media.getJSONObject("startDate");
                        String date = String.format("%04d-%02d-%02d",
                                startDate.optInt("year", 0),
                                startDate.optInt("month", 0),
                                startDate.optInt("day", 0));

                        String image = media.getJSONObject("coverImage").optString("large", "");
                        String description = media.optString("description", "").replaceAll("<[^>]*>", "");
                        String rating = String.valueOf(media.optInt("averageScore", 0));
                        String popularity = String.valueOf(media.optInt("popularity", 0));

                        JSONArray genresArr = media.optJSONArray("genres");
                        List<String> genres = new ArrayList<>();
                        if (genresArr != null) {
                            for (int i = 0; i < genresArr.length(); i++) {
                                genres.add(genresArr.getString(i));
                            }
                        }

                        List<String> studios = new ArrayList<>();
                        if (category == Categories.ANIME) {
                            JSONArray studiosArray = media.getJSONObject("studios").optJSONArray("nodes");
                            if (studiosArray != null) {
                                for (int j = 0; j < studiosArray.length(); j++) {
                                    studios.add(studiosArray.getJSONObject(j).getString("name"));
                                }
                            }
                        }

                        String episodes = media.has("episodes") && !media.isNull("episodes") ? String.valueOf(media.getInt("episodes")) : "";
                        String format = media.optString("format", "");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

                        if (category == Categories.ANIME) {
                            reference.orderByChild("animeID").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        Content content = new Content("cat_4", title, description, date, rating, image, "AniList", episodes, genres, new ArrayList<>(), id);
                                        content.setAnimeID(id);
                                        contentService.insertAnime(content);
                                        Log.d("ContentService", "Anime añadido: " + title);
                                        callback.onSuccess(content);
                                    } else {
                                        for (DataSnapshot snap : snapshot.getChildren()) {
                                            Content existing = snap.getValue(Content.class);
                                            callback.onSuccess(existing);
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error buscando duplicados (anime): " + error.getMessage());
                                    callback.onError(error.toException());
                                }
                            });
                        } else {
                            String categoria = format.equalsIgnoreCase("NOVEL") ? "cat_6" : "cat_5";
                            reference.orderByChild("mangaID").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        Content content = new Content(categoria, title, originalTitle, description, date, rating, image, "AniList", genres, popularity, id);
                                        content.setMangaID(id);
                                        contentService.insertManga(content);
                                        Log.d("ContentService", "Manga/Novela añadida: " + title);
                                        callback.onSuccess(content);
                                    } else {
                                        for (DataSnapshot snap : snapshot.getChildren()) {
                                            Content existing = snap.getValue(Content.class);
                                            callback.onSuccess(existing);
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error buscando duplicados (manga/novela): " + error.getMessage());
                                    callback.onError(error.toException());
                                }
                            });
                        }

                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                }, callback::onError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }





}
