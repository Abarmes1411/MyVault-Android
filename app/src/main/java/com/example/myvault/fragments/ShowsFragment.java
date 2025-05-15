package com.example.myvault.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.activities.DetailContentActivity;
import com.example.myvault.adapters.ShowAdapter;
import com.example.myvault.models.Content;
import com.example.myvault.services.ContentService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShowsFragment extends Fragment {

    private RecyclerView rvNewShows, rvPopularShows, rvUpcomingShows;
    private ShowAdapter adapter;
    private ShowAdapter popularAdapter;
    private ShowAdapter upcomingAdapter;
    private List<Content> showsList = new ArrayList<>();
    private List<Content> popularShowsList = new ArrayList<>();
    private List<Content> upcomingShowsList = new ArrayList<>();
    private DatabaseReference dbRef;
    private ContentService contentService;
    private View mainContent;
    private View progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_shows, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents(view);
        loadDB();

        return view;
    }

    private void loadComponents(View view) {
        mainContent = view.findViewById(R.id.main);
        progressBar = view.findViewById(R.id.progressBarMovies);

        // Mostrar solo el ProgressBar al principio
        mainContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);


        rvNewShows = view.findViewById(R.id.rvNewShows);
        rvNewShows.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvPopularShows = view.findViewById(R.id.rvPopularShows);
        rvPopularShows.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvUpcomingShows = view.findViewById(R.id.rvUpcomingShows);
        rvUpcomingShows.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapter = new ShowAdapter(showsList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentTMDBTVID", content.getTmdbTVID());
            startActivity(intent);
        });

        popularAdapter = new ShowAdapter(popularShowsList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentTMDBTVID", content.getTmdbTVID());
            startActivity(intent);
        });

        upcomingAdapter = new ShowAdapter(upcomingShowsList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentTMDBTVID", content.getTmdbTVID());
            startActivity(intent);
        });

        rvNewShows.setAdapter(adapter);
        rvPopularShows.setAdapter(popularAdapter);
        rvUpcomingShows.setAdapter(upcomingAdapter);

        contentService = new ContentService(getContext());
        if (shouldUpdateToday()) {
            Log.d("ShowsFragment", "Actualizando datos...");
            contentService.fetchRecentShowsAndSave(requireContext(), () -> {
                contentService.fetchPopularShowsAndSave(requireContext(), () -> {
                    contentService.fetchUpcomingShowsAndSave(requireContext(), () -> {
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
                showsList.clear();
                popularShowsList.clear();
                upcomingShowsList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Content content = child.getValue(Content.class);
                    if (content != null && "cat_2".equals(content.getCategoryID())) {
                        String origins = content.getOrigin();
                        if (origins != null) {
                            if (origins.contains("recent_tv")) {
                                showsList.add(content);
                            }
                            if (origins.contains("popular_tv")) {
                                popularShowsList.add(content);
                            }
                            if (origins.contains("upcoming_tv")) {
                                upcomingShowsList.add(content);
                            }
                        }
                    }
                }

                Collections.sort(showsList, (a, b) ->
                        Double.compare(Double.parseDouble(b.getRating()), Double.parseDouble(a.getRating())));
                Collections.sort(popularShowsList, (a, b) ->
                        Double.compare(Double.parseDouble(b.getRating()), Double.parseDouble(a.getRating())));

                adapter.notifyDataSetChanged();
                popularAdapter.notifyDataSetChanged();
                upcomingAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
                mainContent.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error cargando series", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean shouldUpdateToday() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_shows", Context.MODE_PRIVATE);
        String lastUpdate = prefs.getString("last_update_date", null);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return !today.equals(lastUpdate);
    }

    private void saveUpdateDate() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_shows", Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("last_update_date", today).apply();
    }
}
