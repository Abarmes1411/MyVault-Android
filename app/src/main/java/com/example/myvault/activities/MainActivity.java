package com.example.myvault.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;


import com.example.myvault.R;
import com.example.myvault.fragments.AnimesFragment;
import com.example.myvault.fragments.CustomListsFragment;
import com.example.myvault.fragments.GamesFragment;
import com.example.myvault.fragments.HomeFragment;
import com.example.myvault.fragments.MangasFragment;
import com.example.myvault.fragments.MoviesFragment;
import com.example.myvault.fragments.MyVaultFragment;
import com.example.myvault.fragments.ProfileFragment;
import com.example.myvault.fragments.RequestsFragment;
import com.example.myvault.fragments.SearchFragment;
import com.example.myvault.fragments.FriendsFragment;
import com.example.myvault.fragments.SettingsFragment;
import com.example.myvault.fragments.ShowsFragment;
import com.example.myvault.fragments.UserReviewsFragment;
import com.example.myvault.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout, btnMoviesPage, btnShowsPage, btnGamesPage, btnAnimePage, btnMangaPage;
    private DrawerLayout drawerLayout;
    private ImageView hamburgerIcon, searchButton, arrowDown, arrowUp;
    private NavigationView lateral_view;
    private Fragment selectedFragment;
    private TextView tvHeader, tvMyVault;
    private BottomNavigationView bottomMenu;
    private DatabaseReference reference;
    private ImageView ivProfileImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference("users").child(uid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {


                        tvHeader.setText(user.getUsername());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_ERROR", "Error: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
        tvHeader.setText("Usuario Ejemplo");

        hamburgerIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        searchButton.setOnClickListener(v -> {
            searchButton.setSelected(!searchButton.isSelected());

            if (searchButton.isSelected()) {
                arrowDown.setVisibility(View.GONE);
                arrowUp.setVisibility(View.VISIBLE);

                selectedFragment = new SearchFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.containerFrame, selectedFragment)
                        .addToBackStack(null)
                        .commit();

            } else {
                arrowDown.setVisibility(View.VISIBLE);
                arrowUp.setVisibility(View.GONE);
                getSupportFragmentManager().popBackStack();
            }
        });



        lateral_view.setNavigationItemSelectedListener(menuItem -> {
            Fragment selectedFragment = null;
            int id = menuItem.getItemId();

            if (id == R.id.movies_title) {
                selectedFragment = new MoviesFragment();
                tvMyVault.setText("Películas");
            } else if (id == R.id.shows_title) {
                selectedFragment = new ShowsFragment();
                tvMyVault.setText("Series");
            } else if (id == R.id.games_title) {
                selectedFragment = new GamesFragment();
                tvMyVault.setText("VideoJuegos");
            }else if (id == R.id.anime_title) {
                selectedFragment = new AnimesFragment();
                tvMyVault.setText("Animes");
            } else if (id == R.id.manga_novel_title) {
                selectedFragment = new MangasFragment();
                tvMyVault.setText("Manga y Novelas");
            } else if (id == R.id.logout_title) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
            } else if (id == R.id.your_reviews_title) {
                selectedFragment = new UserReviewsFragment();
                tvMyVault.setText("Reseñas");
            }else if (id == R.id.settings_title) {
                selectedFragment = new SettingsFragment();
                tvMyVault.setText("Configuración");

            }else if (id == R.id.requests_title) {
                selectedFragment = new RequestsFragment();
                tvMyVault.setText("Peticiones de Amistad");
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.containerFrame, selectedFragment)
                    .addToBackStack(null)
                    .commit();

            bottomMenu.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < bottomMenu.getMenu().size(); i++) {
                bottomMenu.getMenu().getItem(i).setChecked(false);
            }
            bottomMenu.getMenu().setGroupCheckable(0, true, true);

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        bottomMenu.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                tvMyVault.setText("MyVault");
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                tvMyVault.setText("Perfil");
            } else if (itemId == R.id.nav_myvault) {
                selectedFragment = new MyVaultFragment();
                tvMyVault.setText("Mi Baúl");
            } else if (itemId == R.id.customLists_title) {
                selectedFragment = new CustomListsFragment();
                tvMyVault.setText("Listas");
            } else if (itemId == R.id.friends_title) {
                selectedFragment = new FriendsFragment();
                tvMyVault.setText("Amigos");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().popBackStackImmediate(null, getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.containerFrame, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });


        if (savedInstanceState == null) {
            selectedFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrame, selectedFragment)
                    .commit();
            bottomMenu.setSelectedItemId(R.id.nav_home);
            tvMyVault.setText("MyVault");
        }

    }

    private void loadComponents() {
        selectedFragment = new Fragment();
        drawerLayout = findViewById(R.id.drawer_layout);
        hamburgerIcon = findViewById(R.id.ivHamburger);
        lateral_view = findViewById(R.id.lateral_view);
        searchButton = findViewById(R.id.search_button);
        arrowDown = findViewById(R.id.arrow_down);
        arrowUp = findViewById(R.id.arrow_up);

        arrowUp.setVisibility(View.GONE);
        View view = lateral_view.getHeaderView(0);
        tvHeader = view.findViewById(R.id.tvUserName);
        tvMyVault = findViewById(R.id.tvMyVault);
        bottomMenu = findViewById(R.id.bottom_menu);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);

    }
}
