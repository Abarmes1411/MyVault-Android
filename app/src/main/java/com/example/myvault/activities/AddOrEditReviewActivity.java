package com.example.myvault.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myvault.R;
import com.example.myvault.models.UserReview;
import com.example.myvault.services.UserReviewService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddOrEditReviewActivity extends AppCompatActivity {

    private EditText editReview;
    private ImageView star1, star2, star3, star4, star5;
    private Button buttonSave, buttonBack;
    private double currentRating = 0f;
    private UserReviewService userReviewService;
    private String contentKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();

        buttonSave.setOnClickListener(v -> {
            String reviewText = editReview.getText().toString().trim();
            if (reviewText.isEmpty()) {
                editReview.setError("Escribe tu reseña");
                return;
            }

            // Obtener todos los posibles IDs del intent
            String tmdbID = getIntent().getStringExtra("contentTMDBID");
            String tmdbTVID = getIntent().getStringExtra("contentTMDBTVID");
            String gameID = getIntent().getStringExtra("contentGamesID");
            String mangaID = getIntent().getStringExtra("contentMangaID");
            String animeID = getIntent().getStringExtra("contentAnimeID");
            String novelID = getIntent().getStringExtra("contentNovelID");

            // Determinar el ID válido
            String contentID = null;
            if (tmdbID != null) contentID = tmdbID;
            else if (tmdbTVID != null) contentID = tmdbTVID;
            else if (gameID != null) contentID = gameID;
            else if (mangaID != null) contentID = mangaID;
            else if (animeID != null) contentID = animeID;
            else if (novelID != null) contentID = novelID;

            if (contentID == null) {
                Log.e("ReviewActivity", "No se recibió ningún ID de contenido.");
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = user.getUid();


            // Crear y completar la review
            UserReview userReview = new UserReview();
            userReview.setComment(reviewText);
            userReview.setRating(currentRating);
            userReview.setUserID(userId);
            userReview.setContentID(contentID);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            userReview.setReviewDate(sdf.format(calendar.getTime()));

            Log.d("ReviewActivity", "Review creada: " + userReview.toString());
            Log.d("ReviewActivity", "ID del contenido: " + contentID);
            Log.d("ReviewActivity", "ID del usuario: " + userId);


            userReviewService.insert(contentKey, userReview);

            Toast.makeText(this, "Reseña guardada", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            if(tmdbID != null){
                resultIntent.putExtra("contentTMDBID", tmdbID);
            }else if(tmdbTVID != null){
                resultIntent.putExtra("contentTMDBTVID", tmdbTVID);
            }else if(gameID != null){
                resultIntent.putExtra("contentGamesID", gameID);
            }else if(mangaID != null){
                resultIntent.putExtra("contentMangaID", mangaID);
            }else if(animeID != null){
                resultIntent.putExtra("contentAnimeID", animeID);
            }else if(novelID != null){
                resultIntent.putExtra("contentNovelID", novelID);
            }
            setResult(RESULT_OK, resultIntent);
            finish();


        });



        buttonBack.setOnClickListener(v -> {
            finish();
        });

    }

    private void loadComponents(){

        editReview = findViewById(R.id.editReview);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
        buttonSave = findViewById(R.id.buttonSave);
        buttonBack = findViewById(R.id.buttonBack);
        userReviewService = new UserReviewService(this);

        String tmdbID = getIntent().getStringExtra("contentTMDBID");
        String tmdbTVID = getIntent().getStringExtra("contentTMDBTVID");
        String gameID = getIntent().getStringExtra("contentGameID");
        String mangaID = getIntent().getStringExtra("contentMangaID");
        String animeID = getIntent().getStringExtra("contentAnimeID");
        String novelID = getIntent().getStringExtra("contentNovelID");

        Log.d("ReviewActivity", "TMDB: " + tmdbID + ", TV: " + tmdbTVID + ", Game: " + gameID + ", Manga: " + mangaID + ", Anime: " + animeID + ", Novel: " + novelID);

        contentKey = getIntent().getStringExtra("contentKey");
        if (contentKey == null) {
            Log.e("AddReview", "No se recibió la clave del contenido");
            return;
        }


        setupStarRating();

    }

    private void setupStarRating() {
        ImageView[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnTouchListener((v, event) -> {
                float touchX = event.getX();
                float width = v.getWidth();
                boolean isHalf = touchX < width / 2;

                currentRating = index + (isHalf ? 0.5f : 1f);
                updateStars();
                return true;
            });
        }
    }


    private void updateStars() {
        ImageView[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            if (i + 1 <= currentRating) {
                stars[i].setImageResource(R.drawable.full_star);
            } else if (i + 0.5f == currentRating) {
                stars[i].setImageResource(R.drawable.half_star);
            } else {
                stars[i].setImageResource(R.drawable.empty_star);
            }
        }
    }

}