package com.example.myvault.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.activities.DetailContentActivity;
import com.example.myvault.adapters.MangaAdapter;
import com.example.myvault.models.Content;
import com.example.myvault.services.ContentService;
import com.example.myvault.services.MangasService;
import com.example.myvault.services.NovelService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MangasFragment extends Fragment {

    private TextView titleBestYearly, titleBestYearlyNovel, titleNewsNovel, titleTopNovels, titleNews, titleTopOngoing;
    private RecyclerView rvNewsMangas, rvTopOngoingMangas, rvBestMangasYearly, rvNewsNovels, rvTopOngoingNovels, rvBestMangasYearlyNovel;
    private MangaAdapter adapterOngoing, adapterNews, adapterBestYearly, adapterNewsNovel, adapterTopOngoingNovels, adapterBestYearlyNovel;
    private List<Content> ongoingMangaList = new ArrayList<>();
    private List<Content> newsMangaList = new ArrayList<>();
    private List<Content> bestMangaYearlyList = new ArrayList<>();
    private List<Content> newsNovelList = new ArrayList<>();
    private List<Content> topOngoingNovelList = new ArrayList<>();
    private List<Content> bestMangaYearlyNovelList = new ArrayList<>();

    private DatabaseReference dbRef;
    private ContentService contentService;
    private MangasService mangasService;
    private NovelService novelService;
    private View mainContentManga;
    private View progressBarManga;
    private View progressBarNovel;
    private View mainContentNovel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mangas, container, false); // idealmente cambia esto a fragment_manga
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            loadComponents(view);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadComponents(View view) throws JSONException {

        mainContentManga = view.findViewById(R.id.mainContentMangas);
        progressBarManga = view.findViewById(R.id.progressBarManga);

        mainContentNovel = view.findViewById(R.id.mainContentNovels);
        progressBarNovel = view.findViewById(R.id.progressBarNovels);

        // Mostrar solo el ProgressBar al principio
        mainContentManga.setVisibility(View.GONE);
        progressBarManga.setVisibility(View.VISIBLE);

        mainContentNovel.setVisibility(View.GONE);
        progressBarNovel.setVisibility(View.GONE);

        titleBestYearly = view.findViewById(R.id.titleBestYearly);
        titleBestYearlyNovel = view.findViewById(R.id.titleBestYearlyNovel);
        titleNewsNovel = view.findViewById(R.id.titleNewsNovel);
        titleTopNovels = view.findViewById(R.id.titleTopNovels);
        titleNews = view.findViewById(R.id.titleNews);
        titleTopOngoing = view.findViewById(R.id.title);

        rvNewsMangas = view.findViewById(R.id.rvNewsMangas);
        rvTopOngoingMangas = view.findViewById(R.id.rvTopOngoingMangas);
        rvBestMangasYearly = view.findViewById(R.id.rvBestMangasYearly);
        rvNewsNovels = view.findViewById(R.id.rvNewsNovels);
        rvTopOngoingNovels = view.findViewById(R.id.rvTopOngoingNovels);
        rvBestMangasYearlyNovel = view.findViewById(R.id.rvBestMangasYearlyNovel);

        rvNewsMangas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopOngoingMangas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBestMangasYearly.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNewsNovels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopOngoingNovels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBestMangasYearlyNovel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Adapters con listeners
        adapterOngoing = new MangaAdapter(ongoingMangaList, getContext(), content -> openDetailActivity(content.getMangaID(), true));
        adapterNews = new MangaAdapter(newsMangaList, getContext(), content -> openDetailActivity(content.getMangaID(), true));
        adapterBestYearly = new MangaAdapter(bestMangaYearlyList, getContext(), content -> openDetailActivity(content.getMangaID(), true));
        adapterNewsNovel = new MangaAdapter(newsNovelList, getContext(), content -> openDetailActivity(content.getMangaID(), false));
        adapterTopOngoingNovels = new MangaAdapter(topOngoingNovelList, getContext(), content -> openDetailActivity(content.getMangaID(), false));
        adapterBestYearlyNovel = new MangaAdapter(bestMangaYearlyNovelList, getContext(), content -> openDetailActivity(content.getMangaID(), false));

        rvTopOngoingMangas.setAdapter(adapterOngoing);
        rvNewsMangas.setAdapter(adapterNews);
        rvBestMangasYearly.setAdapter(adapterBestYearly);
        rvNewsNovels.setAdapter(adapterNewsNovel);
        rvTopOngoingNovels.setAdapter(adapterTopOngoingNovels);
        rvBestMangasYearlyNovel.setAdapter(adapterBestYearlyNovel);

        contentService = new ContentService(getContext());
        mangasService = new MangasService(getContext());
        novelService = new NovelService(getContext());


        if (shouldUpdateTodayManga()) {
            Log.d("MangaFragment", "Actualizando datos de mangas...");
            mangasService.fetchNewsMangasAndSave(requireContext(), () -> {
                mangasService.fetchTopOngoingMangasAndSave(requireContext(), () -> {
                    mangasService.fetchBestMangaYearlyAndSave(requireContext(), () -> {
                        saveUpdateDateManga();
                        loadMangaData();
                    });
                });
            });
        } else {
            loadMangaData();
        }

        // Tab switch
        Button btnMangas = view.findViewById(R.id.btnMangas);
        Button btnNovelas = view.findViewById(R.id.btnNovelas);
        View tabIndicator = view.findViewById(R.id.tabIndicator);

        rvNewsNovels.setVisibility(View.GONE);
        rvTopOngoingNovels.setVisibility(View.GONE);
        rvBestMangasYearlyNovel.setVisibility(View.GONE);
        titleTopNovels.setVisibility(View.GONE);
        titleNewsNovel.setVisibility(View.GONE);
        titleBestYearlyNovel.setVisibility(View.GONE);

        btnMangas.post(() -> {
            highlightTab(btnMangas, btnNovelas);
            animateIndicatorTo(btnMangas, tabIndicator);
        });

        btnMangas.setOnClickListener(v -> {
            highlightTab(btnMangas, btnNovelas);
            animateIndicatorTo(btnMangas, tabIndicator);
            toggleViewsMangas(true);
        });

        btnNovelas.setOnClickListener(v -> {
            highlightTab(btnNovelas, btnMangas);
            animateIndicatorTo(btnNovelas, tabIndicator);
            toggleViewsMangas(false);

            progressBarNovel.setVisibility(View.VISIBLE);
            mainContentNovel.setVisibility(View.GONE);



            if (shouldUpdateTodayNovels()) {
                Log.d("MangaFragment", "Actualizando datos de novelas...");
                novelService.fetchNewsNovelsAndSave(requireContext(), () -> {
                    novelService.fetchTopOngoingNovelsAndSave(requireContext(), () -> {
                        novelService.fetchBestLightNovelsYearlyAndSave(requireContext(), () -> {
                            saveUpdateDateNovels();
                            loadNovelData();
                        });
                    });
                });
            } else {
                loadNovelData();
            }

        });
    }

    private void openDetailActivity(String id, boolean isManga) {
        Intent intent = new Intent(getContext(), DetailContentActivity.class);
        intent.putExtra(isManga ? "contentMangaID" : "contentNovelID", id);
        startActivity(intent);
    }

    private void highlightTab(Button selected, Button unselected) {
        selected.setAlpha(1f);
        unselected.setAlpha(0.5f);
    }

    private void animateIndicatorTo(Button target, View indicator) {
        indicator.animate().x(target.getX()).setDuration(200).start();
    }

    private void toggleViewsMangas(boolean showMangas) {
        int show = View.VISIBLE;
        int hide = View.GONE;
        rvNewsMangas.setVisibility(showMangas ? show : hide);
        rvTopOngoingMangas.setVisibility(showMangas ? show : hide);
        rvBestMangasYearly.setVisibility(showMangas ? show : hide);
        titleTopOngoing.setVisibility(showMangas ? show : hide);
        titleNews.setVisibility(showMangas ? show : hide);
        titleBestYearly.setVisibility(showMangas ? show : hide);

        rvNewsNovels.setVisibility(showMangas ? hide : show);
        rvTopOngoingNovels.setVisibility(showMangas ? hide : show);
        rvBestMangasYearlyNovel.setVisibility(showMangas ? hide : show);
        titleTopNovels.setVisibility(showMangas ? hide : show);
        titleNewsNovel.setVisibility(showMangas ? hide : show);
        titleBestYearlyNovel.setVisibility(showMangas ? hide : show);

        mainContentManga.setVisibility(showMangas ? View.VISIBLE : View.GONE);
        mainContentNovel.setVisibility(showMangas ? View.GONE : View.VISIBLE);

    }


    private void loadMangaData() {
        dbRef = FirebaseDatabase.getInstance().getReference("content");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ongoingMangaList.clear();
                newsMangaList.clear();
                bestMangaYearlyList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Content content = child.getValue(Content.class);
                    if (content == null) continue;

                    if ("cat_5".equals(content.getCategoryID())) {
                        String origins = content.getOrigin();

                        if (origins != null) {
                            if (origins.contains("ongoing")) {
                                ongoingMangaList.add(content);
                            }
                            if (origins.contains("new_manga")) {
                                newsMangaList.add(content);
                            }
                            if (origins.contains("bestof")) {
                                bestMangaYearlyList.add(content);
                            }
                        }
                    }
                }

                Collections.sort(ongoingMangaList, (a1, a2) -> Integer.compare(Integer.parseInt(a2.getRating()), Integer.parseInt(a1.getRating())));
                Collections.sort(newsMangaList, (a1, a2) -> Integer.compare(Integer.parseInt(a2.getRating()), Integer.parseInt(a1.getRating())));
                Collections.sort(bestMangaYearlyList, (a1, a2) -> Integer.compare(Integer.parseInt(a2.getRating()), Integer.parseInt(a1.getRating())));

                adapterOngoing.notifyDataSetChanged();
                adapterNews.notifyDataSetChanged();
                adapterBestYearly.notifyDataSetChanged();

                progressBarManga.setVisibility(View.GONE);
                mainContentManga.setVisibility(View.VISIBLE);
                mainContentNovel.setVisibility(View.GONE);
                progressBarNovel.setVisibility(View.GONE);
                mainContentManga.requestFocus();
                mainContentManga.requestLayout();
                mainContentManga.invalidate();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar mangas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNovelData() {
        dbRef = FirebaseDatabase.getInstance().getReference("content");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsNovelList.clear();
                topOngoingNovelList.clear();
                bestMangaYearlyNovelList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Content content = child.getValue(Content.class);
                    if (content == null) continue;

                    boolean isNovel = "cat_6".equals(content.getCategoryID());
                    List<String> genres = content.getGenresManga();
                    boolean hasEcchi = genres != null && genres.contains("Ecchi");

                    if (isNovel && !hasEcchi) {
                        String origins = content.getOrigin();

                        if (origins != null) {
                            if (origins.contains("new_novel")) {
                                newsNovelList.add(content);
                            }
                            if (origins.contains("ongoing_novel")) {
                                topOngoingNovelList.add(content);
                            }
                            if (origins.contains("bestof_lightnovel")) {
                                bestMangaYearlyNovelList.add(content);
                            }
                        }
                    }
                }

                Collections.sort(newsNovelList, (n1, n2) -> Integer.compare(Integer.parseInt(n2.getRating()), Integer.parseInt(n1.getRating())));
                Collections.sort(topOngoingNovelList, (n1, n2) -> Integer.compare(Integer.parseInt(n2.getRating()), Integer.parseInt(n1.getRating())));
                Collections.sort(bestMangaYearlyNovelList, (n1, n2) -> Integer.compare(Integer.parseInt(n2.getRating()), Integer.parseInt(n1.getRating())));

                adapterNewsNovel.notifyDataSetChanged();
                adapterTopOngoingNovels.notifyDataSetChanged();
                adapterBestYearlyNovel.notifyDataSetChanged();

                progressBarNovel.setVisibility(View.GONE);
                mainContentNovel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar novelas", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean shouldUpdateTodayManga() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_mangas", Context.MODE_PRIVATE);
        String lastUpdate = prefs.getString("last_update_date", null);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return !today.equals(lastUpdate);
    }

    private void saveUpdateDateManga() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_mangas", Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("last_update_date", today).apply();
    }

    private boolean shouldUpdateTodayNovels() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_novels", Context.MODE_PRIVATE);
        String lastUpdate = prefs.getString("last_update_date", null);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return !today.equals(lastUpdate);
    }

    private void saveUpdateDateNovels() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_novels", Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("last_update_date", today).apply();
    }





}