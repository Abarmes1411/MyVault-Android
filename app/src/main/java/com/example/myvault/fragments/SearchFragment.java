package com.example.myvault.fragments;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.adapters.SearchAdapter;
import com.example.myvault.models.Content;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchBar;
    private RecyclerView rvSearch;
    private SearchAdapter adapter;
    private List<Content> allContent = new ArrayList<>();
    private List<Content> filteredContent = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = view.findViewById(R.id.etSearch);
        rvSearch = view.findViewById(R.id.rvSearch);
        rvSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SearchAdapter(filteredContent, getContext());
        rvSearch.setAdapter(adapter);

        loadAllContent();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadAllContent() {
        Log.d("SearchFragment", "Iniciando carga de contenido desde Realtime Database...");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("content");

        allContent.clear();
        reference.get().addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                try {
                    String title = snapshot.child("title").getValue(String.class);
                    String releaseDate = snapshot.child("releaseDate").getValue(String.class);
                    String coverImage = snapshot.child("coverImage").getValue(String.class);

                    Log.d("SearchFragment", "Doc ID: " + snapshot.getKey() + ", title: " + title);

                    if (title == null || releaseDate == null || coverImage == null) {
                        Log.w("SearchFragment", "Documento con campos nulos: " + snapshot.getKey());
                        continue;
                    }

                    Content content = new Content();
                    content.setTitle(title);
                    content.setReleaseDate(releaseDate);
                    content.setCoverImage(coverImage);

                    allContent.add(content);
                    Log.d("SearchFragment", "Contenido añadido: " + title);
                } catch (Exception e) {
                    Log.e("SearchFragment", "Error al parsear documento: " + e.getMessage(), e);
                }
            }

            Log.d("SearchFragment", "Contenido cargado correctamente. Total: " + allContent.size());
            filter(searchBar.getText().toString());
        }).addOnFailureListener(e -> {
            Log.e("SearchFragment", "Error al cargar contenido: " + e.getMessage(), e);
        });
    }





    private void filter(String text) {
        String normalizedText = text.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        Log.d("SearchFragment", "Filtrando por: " + normalizedText);
        filteredContent.clear();
        for (Content c : allContent) {
            if (c.getTitle() != null) {
                // Normaliza el título de cada contenido también
                String normalizedTitle = c.getTitle().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                Log.d("SearchFragment", "Título actual: " + normalizedTitle);
                if (normalizedTitle.contains(normalizedText)) {
                    filteredContent.add(c);
                }
            }
        }
        Log.d("SearchFragment", "Resultados filtrados: " + filteredContent.size());
        adapter.notifyDataSetChanged();
    }


}

