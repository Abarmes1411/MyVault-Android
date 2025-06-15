package com.example.myvault.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myvault.R;
import com.example.myvault.adapters.UserReviewsInDetailContentAdapter;
import com.example.myvault.models.Content;
import com.example.myvault.models.ReviewWithUserAUX;
import com.example.myvault.models.User;
import com.example.myvault.models.UserReview;
import com.example.myvault.services.AiServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import okhttp3.*;
import org.json.JSONObject;

public class DetailContentActivity extends AppCompatActivity {

    private ImageView ivCover, star1, star2, star3, star4, star5;
    private TextView tvTitle, tvOriginalTitle, tvDescription, tvAIOpinion, tvReleaseDate, tvGenres, tvOriginalRating, tvContentOrigin, tvAIReview, tvPlatforms, tvWebsite, tvDevelopers, tvEpisodes, tvStudios;
    private Button btnAddUserReview, btnAddList, btnAddVault;
    private Content content;
    private ListView lvUserReview;
    private String contentKey;
    private DatabaseReference reference;
    private View progressBar;
    private View detailContentRoot;
    private String userID;
    private ActivityResultLauncher<Intent> addReviewLauncher;
    String apiKey;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_content);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference contentRef = database.getReference("content");

        String tmdbID = getIntent().getStringExtra("contentTMDBID");
        String tmdbTVID = getIntent().getStringExtra("contentTMDBTVID");
        String gameID = getIntent().getStringExtra("contentGamesID");
        String mangaID = getIntent().getStringExtra("contentMangaID");
        String animeID = getIntent().getStringExtra("contentAnimeID");
        String novelID = getIntent().getStringExtra("contentNovelID");
        apiKey = getResources().getString(R.string.deepl_api_key);



        progressBar = findViewById(R.id.progressBarDetailContent);
        detailContentRoot = findViewById(R.id.detailContentRoot);
        progressBar.setVisibility(View.VISIBLE);
        detailContentRoot.setVisibility(View.GONE);

        if (tmdbID != null) {
            queryContent(contentRef, "tmdbID", tmdbID);
        } else if (tmdbTVID != null) {
            queryContent(contentRef, "tmdbTVID", tmdbTVID);
        } else if (gameID != null) {
            queryContent(contentRef, "gameID", gameID);
        } else if (mangaID != null) {
            queryContent(contentRef, "mangaID", mangaID);
        } else if (animeID != null) {
            queryContent(contentRef, "animeID", animeID);
        } else if (novelID != null) {
            queryContent(contentRef, "mangaID", novelID);
        }



        addReviewLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        loadDB(contentKey);
                        loadUserReviewsAndDisplayStars(contentKey);
                    }
                }
        );

    }

    private void loadComponents(){
        ivCover = findViewById(R.id.ivCover);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
        tvTitle = findViewById(R.id.tvTitle);
        tvOriginalTitle = findViewById(R.id.tvOriginalTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvReleaseDate = findViewById(R.id.tvReleaseDate);
        tvGenres = findViewById(R.id.tvGenres);
        tvOriginalRating = findViewById(R.id.tvOriginalRating);
        tvContentOrigin = findViewById(R.id.tvContentOrigin);
        tvAIOpinion = findViewById(R.id.tvAIOpinion);
        btnAddUserReview = findViewById(R.id.btnAddUserReview);
        tvPlatforms = findViewById(R.id.tvPlatforms);
        tvWebsite = findViewById(R.id.tvWebsite);
        tvDevelopers = findViewById(R.id.tvDevelopers);
        tvEpisodes = findViewById(R.id.tvEpisodes);
        tvStudios = findViewById(R.id.tvStudios);
        lvUserReview = findViewById(R.id.lvUserReview);
        btnAddList = findViewById(R.id.btnAddList);
        btnAddVault = findViewById(R.id.btnAddVault);





        if(content.getCoverImage()==null || content.getCoverImage().equals("") || content.getCoverImage().contains("null")){
            ivCover.setImageResource(R.drawable.no_image_available);
        }else{
            Picasso.get().load(content.getCoverImage()).into(ivCover);
        }

        if(content.getTitle()==null){
            tvOriginalTitle.setText(content.getOriginalTitle());
        }else{
            tvOriginalTitle.setVisibility(View.GONE);
            tvTitle.setText(content.getTitle());
        }

        if(tvDescription.getText().equals("")){
            tvDescription.setText("Sinopsis no disponible");
        }else{
            tvDescription.setText("'" + cleanDescription(content.getDescription()) + "'");

        }

        if(tvReleaseDate.getText().equals("")){
            tvReleaseDate.setText("Fecha de Salida No disponible");
        }else{
            tvReleaseDate.setText(boldLabel("Fecha de Salida: ", content.getReleaseDate()));

        }

        tvAIOpinion.setText(boldLabel("Opinión General:", ""));
        String ratingStr = content.getRating();
        double rating = 0.0;

        try {
            rating = Double.parseDouble(ratingStr);
            if (rating > 10) {
                rating = rating / 10.0;
            }
        } catch (NumberFormatException e) {
            rating = 0.0;
        }

        if(tvOriginalRating.getText().equals("") || rating==0.0){
            tvOriginalRating.setText("Calificación original No disponible");
        }else{
            tvOriginalRating.setText(boldLabel("Calificación original: ", String.format("%.1f", rating)));

        }

        if(tvContentOrigin.getText().equals("")){
            tvContentOrigin.setText("Origen no disponible");
        }else{
            tvContentOrigin.setText(boldLabel("Origen: ", content.getSource()));

        }



        btnAddUserReview.setOnClickListener(v -> {
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userID)
                    .child("userReviews");

            // Obtener el contentID según categoría para buscar review existente
            String contentID = null;
            switch (content.getCategoryID()) {
                case "cat_1":
                    contentID = content.getTmdbID();
                    break;
                case "cat_2":
                    contentID = content.getTmdbTVID();
                    break;
                case "cat_3":
                    contentID = content.getGameID();
                    break;
                case "cat_4":
                    contentID = content.getAnimeID();
                    break;
                case "cat_5":
                    contentID = content.getMangaID();
                    break;
                case "cat_6":
                    contentID = content.getMangaID();
                    break;
            }

            if (contentID == null) {
                Toast.makeText(this, "ID del contenido no disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            // Comprobar si el usuario ya tiene una review para este contentID
            reviewsRef.orderByChild("contentID").equalTo(contentID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Ya hay una review para este contenido
                                Toast.makeText(DetailContentActivity.this, "Ya has creado una review para este contenido.", Toast.LENGTH_LONG).show();
                            } else {
                                // No hay review, abrir la actividad para añadirla
                                Intent intent = new Intent(DetailContentActivity.this, AddOrEditReviewActivity.class);
                                switch (content.getCategoryID()) {
                                    case "cat_1":
                                        intent.putExtra("contentTMDBID", content.getTmdbID());
                                        break;
                                    case "cat_2":
                                        intent.putExtra("contentTMDBTVID", content.getTmdbTVID());
                                        break;
                                    case "cat_3":
                                        intent.putExtra("contentGamesID", content.getGameID());
                                        break;
                                    case "cat_4":
                                        intent.putExtra("contentAnimeID", content.getAnimeID());
                                        break;
                                    case "cat_5":
                                        intent.putExtra("contentMangaID", content.getMangaID());
                                        break;
                                    case "cat_6":
                                        intent.putExtra("contentNovelID", content.getMangaID());
                                        break;
                                }
                                intent.putExtra("contentKey", contentKey);
                                addReviewLauncher.launch(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(DetailContentActivity.this, "Error al comprobar reviews", Toast.LENGTH_SHORT).show();
                        }
                    });
        });



        btnAddVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddVault.setEnabled(false);
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String contentID = content.getId();

                DatabaseReference vaultRef = FirebaseDatabase.getInstance()
                        .getReference("users").child(userID).child("myVault");
                Log.d("FIREBASE", "User ID: " + userID);
                Log.d("FIREBASE", "Content ID: " + contentKey);

                vaultRef.orderByValue().equalTo(contentKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(DetailContentActivity.this, "Ya está en MyVault", Toast.LENGTH_SHORT).show();
                                    btnAddVault.setEnabled(true);
                                } else {
                                    vaultRef.push().setValue(contentKey)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("FIREBASE", vaultRef.toString());
                                                Toast.makeText(DetailContentActivity.this, "Añadido a MyVault", Toast.LENGTH_SHORT).show();

                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(DetailContentActivity.this, "Error al añadir a MyVault", Toast.LENGTH_SHORT).show();
                                                btnAddVault.setEnabled(true);
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(DetailContentActivity.this, "Error de base de datos", Toast.LENGTH_SHORT).show();
                                btnAddVault.setEnabled(true);
                            }
                        });
            }
        });



        btnAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference listsRef = FirebaseDatabase.getInstance()
                        .getReference("users").child(userID).child("customLists");

                listsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(DetailContentActivity.this, "No tienes listas creadas", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<String> listNames = new ArrayList<>();
                        for (DataSnapshot listSnapshot : snapshot.getChildren()) {
                            listNames.add(listSnapshot.getKey());
                        }

                        String[] listArray = listNames.toArray(new String[0]);
                        final int[] selectedIndex = {-1};

                        new AlertDialog.Builder(DetailContentActivity.this)
                                .setTitle("Selecciona una lista")
                                .setSingleChoiceItems(listArray, -1, (dialog, which) -> {
                                    selectedIndex[0] = which;
                                })
                                .setPositiveButton("Confirmar", (dialog, which) -> {
                                    if (selectedIndex[0] != -1) {
                                        String selectedList = listArray[selectedIndex[0]];
                                        String contentID = contentKey;

                                        DatabaseReference itemsRef = listsRef.child(selectedList).child("items");

                                        // Verificar duplicado
                                        itemsRef.orderByValue().equalTo(contentID)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            Toast.makeText(DetailContentActivity.this, "Este contenido ya está en la lista", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            itemsRef.push().setValue(contentID)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        Toast.makeText(DetailContentActivity.this, "Añadido a " + selectedList, Toast.LENGTH_SHORT).show();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(DetailContentActivity.this, "Error al añadir", Toast.LENGTH_SHORT).show();
                                                                    });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(DetailContentActivity.this, "Error al comprobar la lista", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(DetailContentActivity.this, "No seleccionaste ninguna lista", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DetailContentActivity.this, "Error al cargar listas", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });




        if (content.getCategoryID().equals("cat_1")) {
            tvPlatforms.setVisibility(View.GONE);
            tvWebsite.setVisibility(View.GONE);
            tvDevelopers.setVisibility(View.GONE);
            tvEpisodes.setVisibility(View.GONE);
            tvStudios.setVisibility(View.GONE);

            String genreText = getGenreNamesFromStringIds(content.getGenresTMDB(), movieGenresMap);
            if(tvGenres.getText().equals("") || genreText==null || genreText.equals("")){
                tvGenres.setText("Géneros: No disponible");
            }else{
                tvGenres.setText(boldLabel("Géneros: ", genreText));
            }

        } else if (content.getCategoryID().equals("cat_2")) {
            tvPlatforms.setVisibility(View.GONE);
            tvWebsite.setVisibility(View.GONE);
            tvDevelopers.setVisibility(View.GONE);
            tvEpisodes.setVisibility(View.GONE);
            tvStudios.setVisibility(View.GONE);

            String genreText = getGenreNamesFromStringIds(content.getGenresTMDB(), tvGenresMap);
            if(tvGenres.getText().equals("") || genreText==null || genreText.equals("")){
                tvGenres.setText("Géneros: No disponible");
            }else{
                tvGenres.setText(boldLabel("Géneros: ", genreText));
            }


        } else if(content.getCategoryID().equals("cat_3")){


            tvEpisodes.setVisibility(View.GONE);
            tvStudios.setVisibility(View.GONE);

            if(content.getGenresGame()!=null){
                tvGenres.setText(boldLabel("Géneros: ", content.getGenresGame().toString()));
            }else{
                tvGenres.setText("Géneros: No disponible");
            }

            if(content.getPlatforms()!=null){
                tvPlatforms.setText(boldLabel("Plataformas: ", content.getPlatforms().toString()));
            }else{
                tvPlatforms.setText("Plataformas: No disponible");
            }

            if(content.getWebsite()!=null){
                tvWebsite.setText(boldLabel("Sitio Web: ", content.getWebsite()));
            }else{
                tvWebsite.setText("Sitio Web: No disponible");
            }

            if(content.getDevelopers()!=null){
                tvDevelopers.setText(boldLabel("Desarrolladores: ", content.getDevelopers().toString()));
            }else{
                tvDevelopers.setText("Desarrolladores: No disponible");
            }


        }else if(content.getCategoryID().equals("cat_4")){

            tvPlatforms.setVisibility(View.GONE);
            tvWebsite.setVisibility(View.GONE);
            tvDevelopers.setVisibility(View.GONE);

            if(content.getGenresAnime() != null) {
                tvGenres.setText(boldLabel("Géneros: ", translateGenre(content.getGenresAnime())));
            } else {
                tvGenres.setText("Géneros: No disponible");
            }

            if(content.getEpisodes()!=null){
                tvEpisodes.setText(boldLabel("Nº Episodios: ", String.valueOf(content.getEpisodes())));
            }else{
                tvEpisodes.setText("Nº Episodios: No disponible");
            }

            if(content.getStudios()!=null){
                tvStudios.setText(boldLabel("Estudios: ", content.getStudios().toString()));
            }else{
                tvStudios.setText("Estudios: No disponible");
            }

        }else if (content.getCategoryID().equals("cat_5") || content.getCategoryID().equals("cat_6")) {

            if(content.getGenresManga() != null) {
                tvGenres.setText(boldLabel("Géneros: ", translateGenre(content.getGenresManga())));
            } else {
                tvGenres.setText("Géneros: No disponible");
            }
            tvPlatforms.setVisibility(View.GONE);
            tvWebsite.setVisibility(View.GONE);
            tvDevelopers.setVisibility(View.GONE);
            tvEpisodes.setVisibility(View.GONE);
            tvStudios.setVisibility(View.GONE);


        }

        progressBar.setVisibility(View.GONE);
        detailContentRoot.setVisibility(View.VISIBLE);
    }

    private void loadDB(String contentKey) {
        // Referencias a elementos de la interfaz
        lvUserReview = findViewById(R.id.lvUserReview);
        tvAIReview = findViewById(R.id.tvAIReview);
        TextView tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Referencia a la base de datos de Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reviewsRef = database.getReference("content/" + contentKey + "/userReviews");

        // Lista donde se guardarán las reviews junto a sus datos de usuario
        List<ReviewWithUserAUX> reviewList = new ArrayList<>();
        // Adaptador para mostrar las reviews en el ListView
        UserReviewsInDetailContentAdapter adapter = new UserReviewsInDetailContentAdapter(this, reviewList);
        lvUserReview.setAdapter(adapter);

        // Consultar las últimas 10 reviews ordenadas por fecha
        reviewsRef
                .orderByChild("reviewDate")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();

                        // Si no hay reviews, se oculta el ListView y se muestra un mensaje vacío
                        if (!snapshot.exists()) {
                            lvUserReview.setVisibility(View.GONE);
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                            return;
                        }

                        // Total de reviews obtenidas
                        int totalReviews = (int) snapshot.getChildrenCount();
                        // Contador para saber cuándo se han procesado todas
                        final int[] loadedCount = {0};

                        // Recorrer cada review
                        for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                            UserReview review = reviewSnap.getValue(UserReview.class);
                            if (review == null) {
                                loadedCount[0]++;
                                continue;
                            }

                            // Obtener ID de usuario asociado a la review
                            String userID = review.getUserID();

                            // Buscar los datos del usuario a partir del ID
                            database.getReference("users/" + userID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override public void onDataChange(@NonNull DataSnapshot userSnap) {
                                            User user = userSnap.getValue(User.class);
                                            if (user != null) {
                                                // Añadir review con datos del usuario a la lista
                                                reviewList.add(new ReviewWithUserAUX(
                                                        user,
                                                        review.getComment(),
                                                        review.getRating(),
                                                        review.getReviewDate()
                                                ));
                                            }
                                            loadedCount[0]++;
                                            if (loadedCount[0] == totalReviews) {
                                                // Cuando todas las reviews están cargadas, se actualiza el adaptador y se ajusta la altura
                                                adapter.notifyDataSetChanged();
                                                setListViewHeightBasedOnChildren(lvUserReview);
                                                if (reviewList.isEmpty()) {
                                                    lvUserReview.setVisibility(View.GONE);
                                                    tvEmptyMessage.setVisibility(View.VISIBLE);
                                                } else {
                                                    lvUserReview.setVisibility(View.VISIBLE);
                                                    tvEmptyMessage.setVisibility(View.GONE);
                                                }
                                            }
                                        }

                                        @Override public void onCancelled(@NonNull DatabaseError e) {
                                            loadedCount[0]++;
                                            if (loadedCount[0] == totalReviews) {
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        Log.e("FIREBASE", "Error al cargar las reseñas: " + e.getMessage());
                        lvUserReview.setVisibility(View.GONE);
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                    }
                });
    }




    private void queryContent(DatabaseReference ref, String child, String id) {
        // Consulta a la base de datos buscando por un campo específico (por ejemplo, ID)
        ref.orderByChild(child).equalTo(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Recorrer resultados encontrados
                        for (DataSnapshot contentSnap : snapshot.getChildren()) {
                            content = contentSnap.getValue(Content.class);
                            if (content != null) {
                                contentKey = contentSnap.getKey();
                                // Limpiar HTML de la descripción
                                String descripcionHTMLLimpia = cleanDescription(content.getDescription());

                                // Si la descripción es válida y está en inglés, se traduce con IA
                                if (!descripcionHTMLLimpia.isEmpty() && descripcionHTMLLimpia.length() > 20) {
                                    if (englishCheck(descripcionHTMLLimpia)) {
                                        tvAIReview = findViewById(R.id.tvAIReview);

                                        AiServices ai = new AiServices(DetailContentActivity.this, contentKey, tvAIReview);

                                        // Traducir descripción con DeepL
                                        ai.traducirConDeepL(descripcionHTMLLimpia, traduccion -> {
                                            content.setDescription(traduccion);

                                            // Guardar traducción en la base de datos
                                            FirebaseDatabase.getInstance().getReference("content")
                                                    .child(contentKey)
                                                    .child("description")
                                                    .setValue(traduccion);

                                            // Mostrar descripción traducida
                                            if (tvDescription != null) {
                                                tvDescription.setText("'" + traduccion + "'");
                                            }

                                            // Cargar componentes visuales, estrellas, reviews y resumen AI
                                            loadComponents();
                                            loadUserReviewsAndDisplayStars(contentKey);
                                            loadDB(contentKey);

                                            ai.loadAndGenerateSummary();

                                            // Ocultar barra de carga y mostrar contenido
                                            progressBar.setVisibility(View.GONE);
                                            detailContentRoot.setVisibility(View.VISIBLE);

                                        }, DetailContentActivity.this);
                                        return;
                                    } else {
                                        // Si no está en inglés, se usa la descripción limpia sin traducir
                                        content.setDescription(descripcionHTMLLimpia);
                                    }
                                } else {
                                    // Si no hay descripción válida, se pone por defecto
                                    content.setDescription("Sinopsis no disponible");
                                }

                                // Cargar componentes, estrellas, reviews
                                loadComponents();
                                loadUserReviewsAndDisplayStars(contentKey);
                                loadDB(contentKey);

                                // Mostrar contenido y ocultar progreso
                                progressBar.setVisibility(View.GONE);
                                detailContentRoot.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Manejo de errores al consultar contenido
                        Log.e("FIREBASE", "Error al cargar el contenido: " + error.getMessage());
                        progressBar.setVisibility(View.GONE);
                        detailContentRoot.setVisibility(View.VISIBLE);
                    }
                });
    }


    // Metodo que limpia la descripción de HTML
    private String cleanDescription(String rawDescription) {
        if (rawDescription == null) return "";
        return Html.fromHtml(rawDescription, Html.FROM_HTML_MODE_LEGACY).toString().trim();
    }


    // Metodo que comprueba si la descripción es en inglés o no
    private boolean englishCheck(String texto) {
        String lower = texto.toLowerCase();
        return lower.contains("the ") || lower.contains("is ") || lower.contains("with ") || lower.contains("a ") || lower.contains("in ");
    }


    // Géneros de películas
    private static final Map<Integer, String> movieGenresMap = new HashMap<Integer, String>() {{
        put(28, "Acción");
        put(12, "Aventura");
        put(16, "Animación");
        put(35, "Comedia");
        put(80, "Crimen");
        put(99, "Documental");
        put(18, "Drama");
        put(10751, "Familia");
        put(14, "Fantasía");
        put(36, "Historia");
        put(27, "Terror");
        put(10402, "Música");
        put(9648, "Misterio");
        put(10749, "Romance");
        put(878, "Ciencia ficción");
        put(10770, "Película de TV");
        put(53, "Suspense");
        put(10752, "Bélica");
        put(37, "Western");
    }};

    // Géneros de series
    private static final Map<Integer, String> tvGenresMap = new HashMap<Integer, String>() {{
        put(10759, "Action & Adventure");
        put(16, "Animación");
        put(35, "Comedia");
        put(80, "Crimen");
        put(99, "Documental");
        put(18, "Drama");
        put(10751, "Familia");
        put(10762, "Kids");
        put(9648, "Misterio");
        put(10763, "News");
        put(10764, "Reality");
        put(10765, "Sci-Fi & Fantasy");
        put(10766, "Soap");
        put(10767, "Talk");
        put(10768, "War & Politics");
        put(37, "Western");
    }};

    // Géneros de animes, mangas y novelas
    private static final Map<String, String> aniListGenresMap = new HashMap<String, String>() {{
        put("Action", "Acción");
        put("Adventure", "Aventura");
        put("Comedy", "Comedia");
        put("Drama", "Drama");
        put("Ecchi", "Ecchi");
        put("Fantasy", "Fantasía");
        put("Horror", "Horror");
        put("Mahou Shoujo", "Chica mágica");
        put("Mecha", "Mecha");
        put("Music", "Música");
        put("Mystery", "Misterio");
        put("Psychological", "Psicológico");
        put("Romance", "Romántico");
        put("Sci-Fi", "Ciencia Ficción");
        put("Slice of Life", "Vida cotidiana");
        put("Sports", "Deportes");
        put("Supernatural", "Sobrenatural");
        put("Thriller", "Suspense");
    }};


    // Método para traducir un género
    private String translateGenre(List<String> genres) {
        List<String> translateds = new ArrayList<>();
        for (String genre : genres) {
            String translated = aniListGenresMap.getOrDefault(genre, genre);
            translateds.add(translated);
        }
        return String.join(", ", translateds);
    }



    // Metodo para obtener los nombres de los géneros a partir de sus IDs
    private String getGenreNamesFromStringIds(List<String> genreIds, Map<Integer, String> genreMap) {
        if (genreIds == null || genreIds.isEmpty()) {
            return "No disponible";
        }

        List<String> genreNames = new ArrayList<>();
        for (String idStr : genreIds) {
            try {
                int id = Integer.parseInt(idStr);
                if (genreMap.containsKey(id)) {
                    genreNames.add(genreMap.get(id));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return TextUtils.join(", ", genreNames);
    }

    // Metodo para traducir géneros de juegos
    private static final Map<String, String> gameGenresMap = new HashMap<String, String>() {{
        put("Action", "Acción");
        put("Adventure", "Aventura");
        put("RPG", "Rol");
        put("Shooter", "Disparos");
        put("Strategy", "Estrategia");
        put("Puzzle", "Puzles");
        put("Racing", "Carreras");
        put("Board Games", "Juegos de mesa");
        put("Indie", "Indie");
        put("Arcade", "Arcade");
        put("Sports", "Deportes");
        put("Card", "Cartas");
        put("Casual", "Casual");
        put("Platformer", "Plataformas");
        put("Fighting", "Lucha");
        put("Educational", "Educativo");
        put("Simulation", "Simulación");
        put("Massively Multiplayer", "Multijugador masivo");
        put("Family", "Familiar");
    }};


    private String translateGenresGames(List<String> generos) {
        List<String> translateds = new ArrayList<>();
        for (String genre : generos) {
            String translated = gameGenresMap.getOrDefault(genre, genre);
            translateds.add(translated);
        }
        return String.join(", ", translateds);
    }


    // Carga las reviews de un contenido específico desde Firebase y muestra las estrellas promedio
    private void loadUserReviewsAndDisplayStars(String contentKey) {
        // Referencia al nodo de reseñas de usuarios dentro del contenido
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
                .getReference("content")
                .child(contentKey)
                .child("userReviews");

        // Escucha una sola vez los datos del nodo
        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalRating = 0;
                int count = 0;

                // Recorre cada reseña de usuario
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    // Obtiene la puntuación (rating) de la reseña
                    Long ratingLong = reviewSnapshot.child("rating").getValue(Long.class);
                    if (ratingLong != null) {
                        totalRating += ratingLong; // Suma la puntuación
                        count++; // Aumenta el contador de reseñas válidas
                    }
                }

                // Si hay al menos una puntuación válida, calcula el promedio
                if (count > 0) {
                    double averageRating = totalRating / count;
                    updateStars(averageRating); // Muestra las estrellas según el promedio
                } else {
                    updateStars(0); // Si no hay puntuaciones, muestra 0 estrellas
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // En caso de error al leer de Firebase, se muestra un log de error
                Log.e("Firebase", "Error al obtener reviews: " + error.getMessage());
            }
        });
    }

    // Actualiza las estrellas en pantalla según la puntuación promedio
    private void updateStars(double average) {
        // Array con las referencias a los ImageView de estrellas
        ImageView[] stars = {star1, star2, star3, star4, star5};

        // Recorre cada estrella y decide qué imagen mostrar según el promedio
        for (int i = 0; i < stars.length; i++) {
            if (average >= i + 1) {
                stars[i].setImageResource(R.drawable.full_star); // Estrella llena
            } else if (average >= i + 0.5) {
                stars[i].setImageResource(R.drawable.half_star); // Media estrella
            } else {
                stars[i].setImageResource(R.drawable.empty_star); // Estrella vacía
            }
        }
    }

    // Devuelve un texto con la etiqueta en negrita seguida de su valor
    private SpannableString boldLabel(String label, String value) {
        SpannableString spannable = new SpannableString(label + value);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    // Ajusta la altura de un ListView según sus elementos para que se vea completo
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // Cuando no hay adapter, no hacer nada
            return;
        }

        int totalHeight = 0;
        // Mide la altura de cada ítem del ListView y la suma
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += listItem.getMeasuredHeight();
        }

        // Ajusta la altura total del ListView incluyendo los espacios entre ítems
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout(); // Solicita que se redibuje con la nueva altura
    }

}