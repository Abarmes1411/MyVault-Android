package com.example.myvault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.models.Content;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Content> movieList;
    private Context context;

    private OnItemClickListener listener;

    public MovieAdapter(List<Content> movieList, Context context, OnItemClickListener listener) {
        this.movieList = movieList;
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);

        Content movie = movieList.get(position);
        holder.tvTitle.setText(movie.getTitle());

        holder.tvDate.setText(movie.getReleaseDate());

        if(movie.getCoverImage()==null || movie.getCoverImage().equals("")){
            holder.ivCover.setImageResource(R.drawable.no_image_available);
        }else{
            Picasso.get().load(movie.getCoverImage()).into(holder.ivCover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(movieList.get(holder.getAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvDate;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Content content);
    }

}

