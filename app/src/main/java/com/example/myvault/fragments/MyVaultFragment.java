package com.example.myvault.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myvault.R;
import com.example.myvault.activities.DetailContentActivity;
import com.example.myvault.adapters.DetailListAdapter;
import com.example.myvault.models.Content;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyVaultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyVaultFragment extends Fragment {

    private View view;
    private RecyclerView rvMovies, rvSeries, rvGames, rvAnimes, rvMangas, rvNovels;
    private DetailListAdapter adapterMovies, adapterSeries, adapterGames, adapterAnimes, adapterMangas, adapterNovels;
    private View mainContent;
    private View progressBar;

    // Listas que almacenarán los contenidos según su categoría
    private List<Content> contentMovies = new ArrayList<>();
    private List<Content> contentSeries = new ArrayList<>();
    private List<Content> contentGames = new ArrayList<>();
    private List<Content> contentAnimes = new ArrayList<>();
    private List<Content> contentMangas = new ArrayList<>();
    private List<Content> contentNovels = new ArrayList<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public MyVaultFragment() {
        // Constructor requerido
    }

    public static MyVaultFragment newInstance(String param1, String param2) {
        MyVaultFragment fragment = new MyVaultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_vault, container, false);

        mainContent = view.findViewById(R.id.main);
        progressBar = view.findViewById(R.id.progressBarMovies);

        // Mostrar solo el ProgressBar al principio
        mainContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Obtener referencias a los RecyclerViews del layout
        rvMovies  = view.findViewById(R.id.rvMyMovies);
        rvSeries  = view.findViewById(R.id.rvMyShows);
        rvGames   = view.findViewById(R.id.rvMyGames);
        rvAnimes  = view.findViewById(R.id.rvMyAnimes);
        rvMangas  = view.findViewById(R.id.rvMyMangas);
        rvNovels  = view.findViewById(R.id.rvMyNovels);

        adapterMovies = new DetailListAdapter(contentMovies, requireContext(), item -> openDetailActivity(item));
        adapterSeries = new DetailListAdapter(contentSeries, requireContext(), item -> openDetailActivity(item));
        adapterGames = new DetailListAdapter(contentGames, requireContext(), item -> openDetailActivity(item));
        adapterAnimes = new DetailListAdapter(contentAnimes, requireContext(), item -> openDetailActivity(item));
        adapterMangas = new DetailListAdapter(contentMangas, requireContext(), item -> openDetailActivity(item));
        adapterNovels = new DetailListAdapter(contentNovels, requireContext(), item -> openDetailActivity(item));

        // Asignar adapters vacíos con sus respectivas listas
        rvMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMovies.setAdapter(adapterMovies);

        rvSeries.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSeries.setAdapter(adapterSeries);

        rvGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvGames.setAdapter(adapterGames);

        rvAnimes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvAnimes.setAdapter(adapterAnimes);

        rvMangas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvMangas.setAdapter(adapterMangas);

        rvNovels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvNovels.setAdapter(adapterNovels);




        // Cargar el contenido almacenado en el vault del usuario
        loadVaultContent();
        Log.d("MyVaultDespuesMetodo", "Películas cargadas: " + contentMovies.size());
        Log.d("MyVaultDespuesMetodo", "Series cargadas: " + contentSeries.size());
        Log.d("MyVaultDespuesMetodo", "Juegos cargados: " + contentGames.size());
        Log.d("MyVaultDespuesMetodo", "Animes cargados: " + contentAnimes.size());
        Log.d("MyVaultDespuesMetodo", "Mangas cargados: " + contentMangas.size());
        Log.d("MyVaultDespuesMetodo", "Novelas cargadas: " + contentNovels.size());

        return view;
    }

    private void loadVaultContent() {

        contentMovies.clear();
        contentSeries.clear();
        contentGames.clear();
        contentAnimes.clear();
        contentMangas.clear();
        contentNovels.clear();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference vaultRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("myVault");

        vaultRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                long totalItems = task.getResult().getChildrenCount();
                AtomicInteger itemsProcessed = new AtomicInteger(0);

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String contentId = snapshot.getValue(String.class);
                    if (contentId != null) {
                        DatabaseReference contentRef = FirebaseDatabase.getInstance()
                                .getReference("content")
                                .child(contentId);
                        Log.d("loadVaultContent", "contentId: " + contentId);

                        contentRef.get().addOnCompleteListener(contentTask -> {
                            if (contentTask.isSuccessful() && contentTask.getResult().exists()) {
                                Content content = contentTask.getResult().getValue(Content.class);
                                if (content != null) {
                                    Log.d("loadVaultContent", "Contenido cargado: " + content.getTitle());
                                    String category = content.getCategoryID();
                                    switch (category) {
                                        case "cat_1":
                                            contentMovies.add(content);
                                            break;
                                        case "cat_2":
                                            contentSeries.add(content);
                                            break;
                                        case "cat_3":
                                            contentGames.add(content);
                                            break;
                                        case "cat_4":
                                            contentAnimes.add(content);
                                            break;
                                        case "cat_5":
                                            contentMangas.add(content);
                                            break;
                                        case "cat_6":
                                            contentNovels.add(content);
                                            break;
                                        default:
                                            Log.d("loadVaultContent", "Categoría no definida: " + category);
                                    }
                                }
                            } else {
                                Log.e("loadVaultContent", "Error o contenido no existe para id: "
                                        + contentId + " " + contentTask.getException());
                            }

                            if (itemsProcessed.incrementAndGet() == totalItems) {
                                Log.d("loadVaultContent", "Actualizando adapters");
                                adapterMovies.notifyDataSetChanged();
                                adapterSeries.notifyDataSetChanged();
                                adapterGames.notifyDataSetChanged();
                                adapterAnimes.notifyDataSetChanged();
                                adapterMangas.notifyDataSetChanged();
                                adapterNovels.notifyDataSetChanged();

                                progressBar.setVisibility(View.GONE);
                                mainContent.setVisibility(View.VISIBLE);

                                checkIfEmpty();

                                Log.d("MyVaultDespuesMetodo", "Películas cargadas: " + contentMovies.size());
                                Log.d("MyVaultDespuesMetodo", "Series cargadas: " + contentSeries.size());
                                Log.d("MyVaultDespuesMetodo", "Juegos cargados: " + contentGames.size());
                                Log.d("MyVaultDespuesMetodo", "Animes cargados: " + contentAnimes.size());
                                Log.d("MyVaultDespuesMetodo", "Mangas cargadas: " + contentMangas.size());
                                Log.d("MyVaultDespuesMetodo", "Novelas cargadas: " + contentNovels.size());
                            }
                        });
                    } else {
                        if (itemsProcessed.incrementAndGet() == totalItems) {
                            Log.d("loadVaultContent", "Actualizando adapters (algunos items nulos)");
                            adapterMovies.notifyDataSetChanged();
                            adapterSeries.notifyDataSetChanged();
                            adapterGames.notifyDataSetChanged();
                            adapterAnimes.notifyDataSetChanged();
                            adapterMangas.notifyDataSetChanged();
                            adapterNovels.notifyDataSetChanged();

                            progressBar.setVisibility(View.GONE);
                            mainContent.setVisibility(View.VISIBLE);

                            checkIfEmpty();
                        }
                    }
                }
            } else if(!task.getResult().exists() || task.getResult().getChildrenCount() == 0) {

                Log.d("loadVaultContent", "Vault vacío");
                progressBar.setVisibility(View.GONE);
                mainContent.setVisibility(View.VISIBLE);
                checkIfEmpty();
                return;
            }


        });
    }

    private void checkIfEmpty() {
        boolean isMoviesEmpty = contentMovies.isEmpty();
        boolean isSeriesEmpty = contentSeries.isEmpty();
        boolean isGamesEmpty = contentGames.isEmpty();
        boolean isAnimesEmpty = contentAnimes.isEmpty();
        boolean isMangasEmpty = contentMangas.isEmpty();
        boolean isNovelsEmpty = contentNovels.isEmpty();

        boolean isAllEmpty = isMoviesEmpty && isSeriesEmpty && isGamesEmpty && isAnimesEmpty && isMangasEmpty && isNovelsEmpty;


        view.findViewById(R.id.tvEmptyVaultMessage).setVisibility(isAllEmpty ? View.VISIBLE : View.GONE);
        if(isAllEmpty){
            view.findViewById(R.id.tvMyMovies).setVisibility(View.GONE);
            view.findViewById(R.id.tvMyShows).setVisibility(View.GONE);
            view.findViewById(R.id.tvMyGames).setVisibility(View.GONE);
            view.findViewById(R.id.tvMyAnimes).setVisibility(View.GONE);
            view.findViewById(R.id.tvMyMangas).setVisibility(View.GONE);
            view.findViewById(R.id.tvMyNovels).setVisibility(View.GONE);
        }

        // Mostrar mensajes individuales solo si hay contenido en otras categorías
        view.findViewById(R.id.tvEmptyMoviesMessage).setVisibility(!isAllEmpty && isMoviesEmpty ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tvEmptyShowsMessage).setVisibility(!isAllEmpty && isSeriesEmpty ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tvEmptyGamesMessage).setVisibility(!isAllEmpty && isGamesEmpty ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tvEmptyAnimesMessage).setVisibility(!isAllEmpty && isAnimesEmpty ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tvEmptyMangasMessage).setVisibility(!isAllEmpty && isMangasEmpty ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tvEmptyNovelsMessage).setVisibility(!isAllEmpty && isNovelsEmpty ? View.VISIBLE : View.GONE);
    }


    private void openDetailActivity(Content item) {
        Intent intent = new Intent(requireContext(), DetailContentActivity.class);

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

