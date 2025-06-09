package com.example.myvault.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.activities.DetailContentActivity;
import com.example.myvault.adapters.DetailListAdapter;
import com.example.myvault.adapters.SearchAdapter;
import com.example.myvault.enums.Categories;
import com.example.myvault.models.Content;
import com.example.myvault.services.SearchService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SearchAdapter.OnItemClickListener {

    private EditText searchBar;
    private Spinner categorySpinner;
    private Button btnSearch;
    private ProgressBar progressBar;
    private RecyclerView rvSearch;
    private SearchAdapter adapter;
    private TextView tvWait;

    private List<Content> allResults = new ArrayList<>();
    private DatabaseReference firebaseRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = view.findViewById(R.id.etSearch);
        categorySpinner = view.findViewById(R.id.spinnerSpeciality);
        btnSearch = view.findViewById(R.id.btnSearch);
        progressBar = view.findViewById(R.id.progressBarSearch);
        rvSearch = view.findViewById(R.id.rvSearch);
        tvWait = view.findViewById(R.id.tvWait);
        rvSearch.setLayoutManager(new GridLayoutManager(getContext(), 2));

        firebaseRef = FirebaseDatabase.getInstance().getReference("content");

        ArrayAdapter<Categories> spinnerAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, Categories.values());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        adapter = new SearchAdapter(allResults, getContext(), this);
        rvSearch.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(getContext(), "Escribe algún titulo", Toast.LENGTH_SHORT).show();
                return;
            }
            Categories selected = (Categories) categorySpinner.getSelectedItem();
            performSearch(query, selected);
        });

        return view;
    }

    private void performSearch(String query, Categories category) {
        progressBar.setVisibility(View.VISIBLE);
        allResults.clear();
        adapter.notifyDataSetChanged();

        SearchService.searchMinimal(query, category, new SearchService.Callback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> results) {
                progressBar.setVisibility(View.GONE);
                tvWait.setVisibility(View.GONE);
                allResults.addAll(results);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                tvWait.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error en búsqueda: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, getContext());

    }

    @Override
    public void onItemClick(Content content) {
        if (content.getCategoryID() == null) {
            Toast.makeText(getContext(), "Error: categoryID es null para este contenido", Toast.LENGTH_SHORT).show();
            return;
        }
        SearchService.fetchDetails(content, new SearchService.Callback<Content>() {
            @Override
            public void onSuccess(Content detailedContent) {
                Intent intent = new Intent(getContext(), DetailContentActivity.class);

                switch (detailedContent.getCategoryID()) {
                    case "cat_1":
                        intent.putExtra("contentTMDBID", detailedContent.getTmdbID());
                        break;
                    case "cat_2":
                        intent.putExtra("contentTMDBTVID", detailedContent.getTmdbTVID());
                        break;
                    case "cat_3":
                        intent.putExtra("contentGamesID", detailedContent.getGameID());
                        break;
                    case "cat_4":
                        intent.putExtra("contentAnimeID", detailedContent.getAnimeID());
                        break;
                    case "cat_5":
                        intent.putExtra("contentMangaID", detailedContent.getMangaID());
                        break;
                    case "cat_6":
                        intent.putExtra("contentNovelID", detailedContent.getMangaID());
                        break;
                }

                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error al obtener detalles: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, getContext());
    }



}





