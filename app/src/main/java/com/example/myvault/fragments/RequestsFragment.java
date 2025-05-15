package com.example.myvault.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myvault.R;
import com.example.myvault.adapters.UserListAdapter;
import com.example.myvault.enums.Mode;
import com.example.myvault.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsFragment extends Fragment {
    private View view;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestsFragment newInstance(String param1, String param2) {
        RequestsFragment fragment = new RequestsFragment();
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
        view = inflater.inflate(R.layout.fragment_requests, container, false);

        ListView listView = view.findViewById(R.id.lvRequests);
        List<User> requestUsers = new ArrayList<>();
        UserListAdapter adapter = new UserListAdapter(getContext(), requestUsers, Mode.REQUESTS);
        listView.setAdapter(adapter);

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("friend_requests");
        Log.d("RequestsFragment", "Current UID: " + currentUid);
        Log.d("RequestsFragment", "Request Reference: " + requestRef.toString());

        requestRef.child(currentUid).get().addOnSuccessListener(snapshot -> {
            Log.d("RequestsFragment", "Snapshot exists: " + snapshot.exists());
            if (snapshot.exists()) {
                for (DataSnapshot request : snapshot.getChildren()) {
                    String status = request.getValue(String.class);
                    String senderUid = request.getKey();
                    Log.d("RequestsFragment", "Sender UID: " + senderUid + " | Status: " + status);
                    if ("pending".equals(status)) {
                        FirebaseDatabase.getInstance().getReference("users").child(senderUid)
                                .get().addOnSuccessListener(userSnap -> {
                                    User user = userSnap.getValue(User.class);
                                    Log.d("RequestsFragment", "User loaded: " + user);
                                    if (user != null) {
                                        user.setId(senderUid);
                                        requestUsers.add(user);
                                        adapter.notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e("RequestsFragment", "Error loading user: " + e.getMessage());
                                });
                    }
                }
            } else {
                toggleEmptyMessage(requestUsers.isEmpty());
                Log.d("RequestsFragment", "No friend requests found.");
            }
        }).addOnFailureListener(e -> {
            Log.e("RequestsFragment", "Error getting friend_requests: " + e.getMessage());
        });


        return view;
    }

    private void toggleEmptyMessage(boolean show) {
        TextView emptyMessage = view.findViewById(R.id.tvEmptyMessage);
        emptyMessage.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}