package com.example.myvault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.myvault.R;
import com.example.myvault.models.ReviewWithUserAUX;
import com.example.myvault.models.UserReview;
import java.util.List;
import androidx.annotation.Nullable;


public class UserReviewsInDetailContentAdapter extends ArrayAdapter<ReviewWithUserAUX> {

    private Context context;
    private List<ReviewWithUserAUX> reviews;

    public UserReviewsInDetailContentAdapter(@NonNull Context context, List<ReviewWithUserAUX> reviews) {
        super(context, 0, reviews);
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ReviewWithUserAUX review = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_userreview_in_detail_content, parent, false);
        }

        TextView tvUserName = convertView.findViewById(R.id.tvUserName);
        TextView tvUserReview = convertView.findViewById(R.id.tvUserReview);
        TextView tvCreatedAt = convertView.findViewById(R.id.tvCreatedAt);

        ImageView[] stars = new ImageView[]{
                convertView.findViewById(R.id.star1),
                convertView.findViewById(R.id.star2),
                convertView.findViewById(R.id.star3),
                convertView.findViewById(R.id.star4),
                convertView.findViewById(R.id.star5)
        };

        tvUserName.setText(review.getUser().getUsername());
        tvUserReview.setText(review.getComment());
        tvCreatedAt.setText(review.getReviewDate());

        int rating = (int) Math.round(review.getRating()); // De 0 a 5
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.full_star);
            } else {
                stars[i].setImageResource(R.drawable.empty_star);
            }
        }

        return convertView;
    }
}
