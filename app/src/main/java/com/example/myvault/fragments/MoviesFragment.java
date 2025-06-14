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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.activities.DetailContentActivity;
import com.example.myvault.adapters.MovieAdapter;
import com.example.myvault.models.Content;
import com.example.myvault.services.ContentService;
import com.example.myvault.services.MoviesService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoviesFragment extends Fragment {

    private RecyclerView rvNewMovies, rvPopularMovies, rvUpcomingMovies;
    private MovieAdapter adapter;
    private MovieAdapter popularAdapter;
    private MovieAdapter upcomingAdapter;
    private List<Content> movieList = new ArrayList<>();
    private List<Content> popularMoviesList = new ArrayList<>();
    private List<Content> upcomingMoviesList = new ArrayList<>();
    private DatabaseReference dbRef;
    private ContentService contentService;
    private MoviesService moviesService;
    private View mainContent;
    private View progressBar;

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);




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


        rvNewMovies = view.findViewById(R.id.rvNewMovies);
        rvNewMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvPopularMovies = view.findViewById(R.id.rvPopularMovies);
        rvPopularMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvUpcomingMovies = view.findViewById(R.id.rvUpcomingMovies);
        rvUpcomingMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapter = new MovieAdapter(movieList, requireContext(), content -> {
            Intent intent = new Intent(requireContext(), DetailContentActivity.class);
            intent.putExtra("contentTMDBID", content.getTmdbID());
            startActivity(intent);
        });

        popularAdapter = new MovieAdapter(popularMoviesList, requireContext(), content -> {
            Intent intent = new Intent(requireContext(), DetailContentActivity.class);
            intent.putExtra("contentTMDBID", content.getTmdbID());
            startActivity(intent);
        });

        upcomingAdapter = new MovieAdapter(upcomingMoviesList, requireContext(), content -> {
            Intent intent = new Intent(requireContext(), DetailContentActivity.class);
            intent.putExtra("contentTMDBID", content.getTmdbID());
            startActivity(intent);
        });

        rvNewMovies.setAdapter(adapter);
        rvPopularMovies.setAdapter(popularAdapter);
        rvUpcomingMovies.setAdapter(upcomingAdapter);

        moviesService = new MoviesService(getContext());
        if (shouldUpdateToday()) {
            Log.d("MoviesFragment", "Actualizando datos...");
            moviesService.fetchRecentMoviesAndSave(requireContext(), () -> {
                moviesService.fetchPopularMoviesAndSave(requireContext(), () -> {
                    moviesService.fetchUpcomingMoviesAndSave(requireContext(), () -> {
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
                long startTime = System.currentTimeMillis();

                movieList.clear();
                popularMoviesList.clear();
                upcomingMoviesList.clear();

                List<Content> tempPopular = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Content content = child.getValue(Content.class);
                    if (content != null && "cat_1".equals(content.getCategoryID())) {
                        String origins = content.getOrigin();
                        if (origins != null) {
                            if (origins.contains("recent")) movieList.add(content);
                            if (origins.contains("popular")) tempPopular.add(content);
                            if (origins.contains("upcoming")) upcomingMoviesList.add(content);
                        }
                    }
                }

                // Ordenar por rating descendente
                Collections.sort(movieList, (m1, m2) -> Double.compare(Double.parseDouble(m2.getRating()), Double.parseDouble(m1.getRating())));

                // Ordenar populares por fecha de estreno descendente
                Collections.sort(tempPopular, (m1, m2) -> {
                    try {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date d1 = df.parse(m1.getReleaseDate());
                        Date d2 = df.parse(m2.getReleaseDate());
                        return d2.compareTo(d1);
                    } catch (Exception e) {
                        return 0;
                    }
                });

                // Limitar a máximo 20 películas populares
                popularMoviesList.addAll(tempPopular.subList(0, Math.min(20, tempPopular.size())));

                adapter.notifyDataSetChanged();
                popularAdapter.notifyDataSetChanged();
                upcomingAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
                mainContent.setVisibility(View.VISIBLE);
                long endTime = System.currentTimeMillis();
                Log.d("Performance", "Tiempo de carga: " + (endTime - startTime) + " ms");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error cargando películas", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean shouldUpdateToday() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_movies", Context.MODE_PRIVATE);
        String lastUpdate = prefs.getString("last_update_date", null);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return !today.equals(lastUpdate);
    }

    private void saveUpdateDate() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs_movies", Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("last_update_date", today).apply();
    }

}
