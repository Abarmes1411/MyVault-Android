package com.example.myvault.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myvault.R;
import com.example.myvault.adapters.UserListAdapter;
import com.example.myvault.enums.Mode;
import com.example.myvault.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserListActivity extends AppCompatActivity {

    private ListView lvUserlist;
    private List<User> userList = new ArrayList<>();
    private UserListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();


    }


    private void loadComponents(){
        lvUserlist = findViewById(R.id.lvUserlist);

        lvUserlist = findViewById(R.id.lvUserlist);
        adapter = new UserListAdapter(this, userList, Mode.DEFAULT);
        lvUserlist.setAdapter(adapter);
        loadDB();

    }

    private void loadDB() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference friendsRef = usersRef.child(currentUid).child("friends");

        friendsRef.get().addOnCompleteListener(friendTask -> {
            Set<String> friendIds = new HashSet<>();

            if (friendTask.isSuccessful() && friendTask.getResult().exists()) {
                for (DataSnapshot snapshot : friendTask.getResult().getChildren()) {
                    friendIds.add(snapshot.getValue(String.class));
                }
            }

            usersRef.get().addOnCompleteListener(usersTask -> {
                if (usersTask.isSuccessful() && usersTask.getResult().exists()) {
                    userList.clear();

                    for (DataSnapshot snapshot : usersTask.getResult().getChildren()) {
                        String uid = snapshot.getKey();

                        if (!uid.equals(currentUid) && !friendIds.contains(uid)) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                userList.add(user);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("loadDB", "Error al obtener usuarios", usersTask.getException());
                }
            });
        });
    }


}