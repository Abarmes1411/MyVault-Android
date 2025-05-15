package com.example.myvault.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.activities.DetailContentActivity;
import com.example.myvault.adapters.AnimesAdapter;
import com.example.myvault.models.Content;
import com.example.myvault.services.ContentService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnimesFragment extends Fragment {

    private RecyclerView rvSeasonedAnimes, rvPopularAnimes, rvBestAnimesYearly;
    private TextView tvSeasonedAnimes;
    private AnimesAdapter adapterSeasoned, adapterPopular, adapterBestYearly;
    private List<Content> seasonalAnimesList = new ArrayList<>();
    private List<Content> popularAnimesList = new ArrayList<>();
    private List<Content> bestAnimesYearlyList = new ArrayList<>();
    private DatabaseReference dbRef;
    private ContentService contentService;
    private View mainContent;
    private View progressBar;

    public AnimesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animes, container, false);

        try {
            loadComponents(view);
            loadDB();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return view;
    }

    private void loadComponents(View view) throws JSONException {

        mainContent = view.findViewById(R.id.animeRoot);
        progressBar = view.findViewById(R.id.progressBarAnime);

        // Mostrar solo el ProgressBar al principio
        mainContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        tvSeasonedAnimes = view.findViewById(R.id.title);
        rvSeasonedAnimes = view.findViewById(R.id.rvSeasonedAnimes);
        rvSeasonedAnimes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvPopularAnimes = view.findViewById(R.id.rvPopularAnimes);
        rvPopularAnimes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvBestAnimesYearly = view.findViewById(R.id.rvBestAnimesYearly);
        rvBestAnimesYearly.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapterSeasoned = new AnimesAdapter(seasonalAnimesList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentAnimeID", content.getAnimeID());
            startActivity(intent);
        });
        adapterPopular = new AnimesAdapter(popularAnimesList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentAnimeID", content.getAnimeID());
            startActivity(intent);
        });
        adapterBestYearly = new AnimesAdapter(bestAnimesYearlyList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentAnimeID", content.getAnimeID());
            startActivity(intent);
        });

        rvSeasonedAnimes.setAdapter(adapterSeasoned);
        rvPopularAnimes.setAdapter(adapterPopular);
        rvBestAnimesYearly.setAdapter(adapterBestYearly);

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;

        String season;
        if (month >= 1 && month <= 3) {
            season = "'INVIERNO'";
        } else if (month >= 4 && month <= 6) {
            season = "'PRIMAVERA'";
        } else if (month >= 7 && month <= 9) {
            season = "'VERANO'";
        } else {
            season = "'OTOÑO'";
        }

        tvSeasonedAnimes.setText("Temporada de " + season);

        contentService = new ContentService(getContext());

        progressBar.setVisibility(View.VISIBLE);

        if (shouldUpdateToday()) {
            Log.d("GamesFragment", "Actualizando datos...");
            contentService.fetchSeasonedAnimeAndSave(requireContext(), () -> {
                contentService.fetchPopularAnimeAndSave(requireContext(), () -> {
                    contentService.fetchBestAnimeYearlyAndSave(requireContext(), () -> {
                        saveUpdateDate();
                        loadDB();
                    });
                });
            });
        } else {
            loadDB();
        }
    }

    private void loadDB() {
        dbRef = FirebaseDatabase.getInstance().getReference("content");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seasonalAnimesList.clear();
                popularAnimesList.clear();
                bestAnimesYearlyList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Content content = child.getValue(Content.class);
                    if (content != null && "cat_4".equals(content.getCategoryID())) {
                        String origins = content.getOrigin();
                        if (origins != null) {
                            if (origins.contains("seasonal")) {
                                seasonalAnimesList.add(content);
                            }
                            if (origins.contains("popular")) {
                                popularAnimesList.add(content);
                            }
                            if (origins.contains("best")) {
                                bestAnimesYearlyList.add(content);
                            }
                        }
                    }
                }

                Collections.sort(seasonalAnimesList, (a1, a2) -> Integer.compare(
                        Integer.parseInt(a2.getRating()), Integer.parseInt(a1.getRating())));
                Collections.sort(popularAnimesList, (a1, a2) -> Integer.compare(
                        Integer.parseInt(a2.getRating()), Integer.parseInt(a1.getRating())));
                Collections.sort(bestAnimesYearlyList, (a1, a2) -> Integer.compare(
                        Integer.parseInt(a2.getRating()), Integer.parseInt(a1.getRating())));

                adapterSeasoned.notifyDataSetChanged();
                adapterPopular.notifyDataSetChanged();
                adapterBestYearly.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
                mainContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error cargando animes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean shouldUpdateToday() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_animes", Context.MODE_PRIVATE);
        String lastUpdate = prefs.getString("last_update_date", null);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return !today.equals(lastUpdate);
    }

    private void saveUpdateDate() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_animes", Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("last_update_date", today).apply();
    }
}
