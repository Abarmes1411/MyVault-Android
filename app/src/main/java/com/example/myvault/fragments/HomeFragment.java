package com.example.myvault.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.myvault.R;
import com.example.myvault.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private ImageView shortcut_movies, shortcut_shows, shortcut_games, shortcut_animes, shortcut_manganovel;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context context;
    private View view;


    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

        view = inflater.inflate(R.layout.fragment_home, container, false);

        shortcut_movies = view.findViewById(R.id.shortcut_movies);
        shortcut_shows = view.findViewById(R.id.shortcut_shows);
        shortcut_games = view.findViewById(R.id.shortcut_games);
        shortcut_animes = view.findViewById(R.id.shortcut_animes);
        shortcut_manganovel = view.findViewById(R.id.shortcut_manganovel);


        shortcut_movies.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrame, new MoviesFragment())
                    .addToBackStack(null)
                    .commit();
        });

        shortcut_shows.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrame, new ShowsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        shortcut_games.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrame, new GamesFragment())
                    .addToBackStack(null)
                    .commit();
        });

        shortcut_animes.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrame, new AnimesFragment())
                    .addToBackStack(null)
                    .commit();
        });

        shortcut_manganovel.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrame, new MangasFragment())
                    .addToBackStack(null)
                    .commit();
        });


        // Inflate the layout for this fragment
        return view;
    }
}