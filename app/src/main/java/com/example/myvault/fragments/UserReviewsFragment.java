package com.example.myvault.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myvault.R;
import com.example.myvault.activities.AddOrEditReviewActivity;
import com.example.myvault.adapters.UserReviewsAdapter;
import com.example.myvault.models.Content;
import com.example.myvault.models.ReviewWithContentAUX;
import com.example.myvault.models.User;
import com.example.myvault.models.UserReview;
import com.example.myvault.services.UserReviewService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserReviewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserReviewsFragment extends Fragment {

    private ListView lvYourReviews;
    private UserReviewsAdapter adapter;
    private List<ReviewWithContentAUX> reviewList = new ArrayList<>();
    private View view;
    boolean hasItems = false;
    UserReviewService userReviewService;
    String category_ID;
    String selectedUserId;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserReviewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserReviewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserReviewsFragment newInstance(String param1, String param2) {
        UserReviewsFragment fragment = new UserReviewsFragment();
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
            selectedUserId = getArguments().getString("selectedUserId");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_reviews, container, false); // se asigna a la variable de clase

        lvYourReviews = view.findViewById(R.id.lvYourReviews);
        adapter = new UserReviewsAdapter(requireContext(), reviewList);
        lvYourReviews.setAdapter(adapter);

        userReviewService = new UserReviewService(requireContext());
        Log.d("UserReviewsFragment", "selectedUserId: " + selectedUserId);
        if (selectedUserId == null) {
            selectedUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadUserReviews(selectedUserId);
        }else{
            loadUserReviews(selectedUserId);
        }


        lvYourReviews.setOnItemClickListener((parent, view1, position, id) -> {
            ReviewWithContentAUX selectedReview = reviewList.get(position);

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (!selectedUserId.equals(currentUserId)) {
                return;
            }

            String[] opciones = {"Editar Review", "Eliminar Review"};
            new AlertDialog.Builder(requireContext())
                    .setTitle("Opciones")
                    .setItems(opciones, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                Log.d("EditarReview", "Editar Review de : " + selectedReview.getContent().getTitle());
                                Intent intent = new Intent(requireContext(), AddOrEditReviewActivity.class);

                                startActivity(intent);
                                break;
                            case 1:
                                Log.d("EliminarReview", "Eliminar Review de: " + selectedReview.getContent().getTitle());
                                String reviewId = selectedReview.getReview() != null ? selectedReview.getReview().getId() : null;
                                String userID = selectedReview.getReview() != null ? selectedReview.getReview().getUserID() : null;
                                category_ID = selectedReview.getContent().getCategoryID();
                                Log.d("EliminarReview", "ReviewID: " + reviewId + ", CategoryID: " + category_ID + ", UserID: " + userID);
                                Log.d("EliminarReview", "Id content" + selectedReview.getContent().getId());
                                String contentId = selectedReview.getContent().getId();
                                if (reviewId != null  && userID != null) {
                                    userReviewService.delete(userID ,reviewId, contentId);
                                    refresh();
                                } else {
                                    Log.e("EliminarReview", "ID nulo: contentId o reviewId es null o userID es null");
                                }
                                break;
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });


        return view;
    }

    private void loadUserReviews(String uid) {
        DatabaseReference contentRef = FirebaseDatabase.getInstance().getReference("content");

        reviewList.clear();
        hasItems = false;

        contentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot contentSnapshot : task.getResult().getChildren()) {
                    Content content = contentSnapshot.getValue(Content.class);

                    if (content != null && contentSnapshot.hasChild("userReviews")) {
                        for (DataSnapshot reviewSnapshot : contentSnapshot.child("userReviews").getChildren()) {
                            String userId = reviewSnapshot.child("userID").getValue(String.class);
                            if (uid.equals(userId)) {
                                UserReview review = reviewSnapshot.getValue(UserReview.class);
                                ReviewWithContentAUX item = new ReviewWithContentAUX(review, content);
                                reviewList.add(item);
                                hasItems = true;
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                toggleEmptyMessage(!hasItems);
            } else {
                adapter.notifyDataSetChanged();
                toggleEmptyMessage(true);
            }
        });
    }




    private void toggleEmptyMessage(boolean show) {
        TextView emptyMessage = view.findViewById(R.id.tvEmptyMessage);
        emptyMessage.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private void refresh() {
        loadUserReviews(selectedUserId);
    }




}