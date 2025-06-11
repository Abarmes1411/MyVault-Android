package com.example.myvault.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvault.R;
import com.example.myvault.activities.DetailContentActivity;
import com.example.myvault.adapters.GamesAdapter;
import com.example.myvault.models.Content;
import com.example.myvault.services.ContentService;
import com.example.myvault.services.GamesService;
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

public class GamesFragment extends Fragment {

    private RecyclerView rvNewGames, rvPopularGames, rvUpcomingGames;
    private TextView titleNewGames, titlePopularGames, titleUpcomingGames;
    private GamesAdapter popularGamesAdapter;
    private GamesAdapter newGamesAdapter;
    private GamesAdapter upcomingGamesAdapter;
    private List<Content> popularGamesList = new ArrayList<>();
    private List<Content> newGamesList = new ArrayList<>();
    private List<Content> upcomingGamesList = new ArrayList<>();
    private ContentService contentService;
    private GamesService gamesService;
    private DatabaseReference dbRef;
    private View mainContent;
    private View progressBar;

    public GamesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);
        loadComponents(view);
        loadDB();
        return view;
    }

    private void loadComponents(View view) {
        mainContent = view.findViewById(R.id.main);
        progressBar = view.findViewById(R.id.progressBarMovies);

        mainContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        rvNewGames = view.findViewById(R.id.rvRecentGames);
        rvPopularGames = view.findViewById(R.id.rvPopularGames);
        rvUpcomingGames = view.findViewById(R.id.rvUpcomingGames);
        titleNewGames = view.findViewById(R.id.titleRecent);
        titlePopularGames = view.findViewById(R.id.titlePopular);
        titleUpcomingGames = view.findViewById(R.id.titleUpcoming);

        rvNewGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopularGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUpcomingGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        newGamesAdapter = new GamesAdapter(newGamesList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentGamesID", content.getGameID());
            startActivity(intent);
        });

        popularGamesAdapter = new GamesAdapter(popularGamesList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentGamesID", content.getGameID());
            startActivity(intent);
        });

        upcomingGamesAdapter = new GamesAdapter(upcomingGamesList, getContext(), content -> {
            Intent intent = new Intent(getContext(), DetailContentActivity.class);
            intent.putExtra("contentGamesID", content.getGameID());
            startActivity(intent);
        });

        rvNewGames.setAdapter(newGamesAdapter);
        rvPopularGames.setAdapter(popularGamesAdapter);
        rvUpcomingGames.setAdapter(upcomingGamesAdapter);

        contentService = new ContentService(getContext());
        gamesService = new GamesService(getContext());

        progressBar.setVisibility(View.VISIBLE);

        if (shouldUpdateToday()) {
            Log.d("GamesFragment", "Actualizando datos...");
            gamesService.fetchRecentGamesAndSave(requireContext(), () -> {
                gamesService.fetchPopularGamesAndSave(requireContext(), () -> {
                    gamesService.fetchUpcomingGamesAndSave(requireContext(), () -> {
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
                newGamesList.clear();
                popularGamesList.clear();
                upcomingGamesList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Content content = child.getValue(Content.class);
                    if (content != null && "cat_3".equals(content.getCategoryID())) {
                        String origins = content.getOrigin();
                        if (origins != null) {
                            if (origins.contains("recent_game")) newGamesList.add(content);
                            if (origins.contains("popular_game")) popularGamesList.add(content);
                            if (origins.contains("upcoming_game")) upcomingGamesList.add(content);
                        }
                    }
                }

                sortAndNotifyAdapters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error cargando juegos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortAndNotifyAdapters() {
        Collections.sort(newGamesList, (a, b) -> Integer.compare(Integer.parseInt(b.getAdded()), Integer.parseInt(a.getAdded())));
        Collections.sort(popularGamesList, (a, b) -> Integer.compare(Integer.parseInt(b.getAdded()), Integer.parseInt(a.getAdded())));
        Collections.sort(upcomingGamesList, (a, b) -> Integer.compare(Integer.parseInt(b.getAdded()), Integer.parseInt(a.getAdded())));

        newGamesAdapter.notifyDataSetChanged();
        popularGamesAdapter.notifyDataSetChanged();
        upcomingGamesAdapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);

    }


    private boolean shouldUpdateToday() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_games", Context.MODE_PRIVATE);
        String lastUpdate = prefs.getString("last_update_date", null);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return !today.equals(lastUpdate);
    }

    private void saveUpdateDate() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_games", Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("last_update_date", today).apply();
    }
}
