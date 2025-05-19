package com.example.myvault.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myvault.R;
import com.example.myvault.adapters.DetailListAdapter;
import com.example.myvault.models.Content;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailItemCustomListsActivity extends AppCompatActivity {

    private DetailListAdapter adapter;
    private FlexboxLayout flexboxLayout;
    private TextView tvListName;
    private DatabaseReference reference;
    private String uidCurrentUser;
    private String listName;
    private List<Content> contentList;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_item_custom_lists);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();
        loadContentItems();


        btnBack.setOnClickListener(v -> {
            finish();
        });


    }

    private void loadComponents() {
        flexboxLayout = findViewById(R.id.flexContainer);
        tvListName = findViewById(R.id.tvListName);
        btnBack = findViewById(R.id.btnBack);

        listName = getIntent().getStringExtra("listName");
        tvListName.setText(listName);

        uidCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("DetailListActivity", "UID del usuario actual: " + uidCurrentUser);
        Log.d("DetailListActivity", "Nombre de la lista: " + listName);
        reference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uidCurrentUser)
                .child("customLists")
                .child(listName)
                .child("items");

        contentList = new ArrayList<>();
    }

    private void loadContentItems() {
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                flexboxLayout.removeAllViews();
                contentList.clear();

                Iterable<DataSnapshot> items = task.getResult().getChildren();
                boolean hasItems = false;

                for (DataSnapshot snapshot : items) {
                    String contentId = snapshot.getValue(String.class);
                    if (contentId != null) {
                        hasItems = true;

                        DatabaseReference contentRef = FirebaseDatabase.getInstance()
                                .getReference("content")
                                .child(contentId);

                        contentRef.get().addOnSuccessListener(contentSnapshot -> {
                            Content content = contentSnapshot.getValue(Content.class);
                            if (content != null) {
                                addItemToFlexbox(content);
                                contentList.add(content);
                            }
                            // Verificar después de cada carga si fue el último
                            if (contentList.size() == task.getResult().getChildrenCount()) {
                                toggleEmptyMessage(contentList.isEmpty());
                            }
                        }).addOnFailureListener(e -> {
                            Log.e("loadContentItems", "Error al obtener contenido", e);
                        });
                    }
                }

                // Si no hay ningún item válido
                if (!hasItems) {
                    toggleEmptyMessage(true);
                }

            } else {
                Log.e("loadContentItems", "Error al cargar lista", task.getException());
            }
        });
    }



    private void addItemToFlexbox(Content content) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_detail_custom_list, flexboxLayout, false);

        TextView tvTitle = itemView.findViewById(R.id.tvTitle);
        TextView tvDate = itemView.findViewById(R.id.tvDate);
        TextView tvContentCategory = itemView.findViewById(R.id.tvContentCategory);
        ImageView ivCover = itemView.findViewById(R.id.ivCover);

        tvTitle.setText(content.getTitle());
        tvDate.setText(content.getReleaseDate());

        if(content.getCategoryID().equals("cat_1")) {
            tvContentCategory.setText("Pelicula");
        }else if(content.getCategoryID().equals("cat_2")) {
            tvContentCategory.setText("Serie");
        }else if(content.getCategoryID().equals("cat_3")) {
            tvContentCategory.setText("Videojuego");
        }else if(content.getCategoryID().equals("cat_4")) {
            tvContentCategory.setText("Anime");
        }else if(content.getCategoryID().equals("cat_5")) {
            tvContentCategory.setText("Manga");
        }else if(content.getCategoryID().equals("cat_6")) {
            tvContentCategory.setText("Novela L.");

        }

        Picasso.get().load(content.getCoverImage()).into(ivCover);

        itemView.setOnClickListener(v -> openDetailActivity(content));


        flexboxLayout.addView(itemView);
    }

    private void toggleEmptyMessage(boolean show) {
        TextView emptyMessage = findViewById(R.id.tvEmptyMessage);
        emptyMessage.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void openDetailActivity(Content item) {
        Intent intent = new Intent(this, DetailContentActivity.class);

        switch (item.getCategoryID()) {
            case "cat_1":
                intent.putExtra("contentTMDBID", item.getTmdbID());
                break;
            case "cat_2":
                intent.putExtra("contentTMDBTVID", item.getTmdbTVID());
                break;
            case "cat_3":
                intent.putExtra("contentGamesID", item.getGameID());
                break;
            case "cat_4":
                intent.putExtra("contentAnimeID", item.getAnimeID());
                break;
            case "cat_5":
                intent.putExtra("contentMangaID", item.getMangaID());
                break;
            case "cat_6":
                intent.putExtra("contentNovelID", item.getMangaID());
                break;
            default:
                Log.w("openDetailActivity", "Categoría no reconocida: " + item.getCategoryID());
                return;
        }

        startActivity(intent);
    }

}