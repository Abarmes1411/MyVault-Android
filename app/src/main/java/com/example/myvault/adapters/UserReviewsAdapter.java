package com.example.myvault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myvault.R;
import com.example.myvault.models.Content;
import com.example.myvault.models.ReviewWithContentAUX;
import com.example.myvault.models.UserReview;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserReviewsAdapter extends ArrayAdapter<ReviewWithContentAUX> {

    private Context context;
    private List<ReviewWithContentAUX> items;

    public UserReviewsAdapter(@NonNull Context context, List<ReviewWithContentAUX> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ReviewWithContentAUX item = getItem(position);
        UserReview review = item.getReview();
        Content content = item.getContent();

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_userreview, parent, false);
        }

        TextView tvContentName = convertView.findViewById(R.id.tvContentName);
        TextView tvUserReview = convertView.findViewById(R.id.tvUserReview);
        TextView tvCreatedAt = convertView.findViewById(R.id.tvCreatedAt);
        ImageView coverReview = convertView.findViewById(R.id.coverReview);

        ImageView[] stars = new ImageView[] {
                convertView.findViewById(R.id.star1),
                convertView.findViewById(R.id.star2),
                convertView.findViewById(R.id.star3),
                convertView.findViewById(R.id.star4),
                convertView.findViewById(R.id.star5)
        };

        tvContentName.setText(content.getTitle());
        tvUserReview.setText(review.getComment());
        tvCreatedAt.setText(review.getReviewDate());

        Picasso.get().load(content.getCoverImage()).into(coverReview);

        int rating = (int) Math.round(review.getRating());
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < rating ? R.drawable.full_star : R.drawable.empty_star);
        }

        return convertView;
    }
}
