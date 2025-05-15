package com.example.myvault.adapters;

import android.content.Context;
import android.util.Log;
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

public class AnimesAdapter extends RecyclerView.Adapter<AnimesAdapter.AnimeViewHolder> {

    private List<Content> animeList;
    private Context context;
    private OnItemClickListener listener;


    public AnimesAdapter(List<Content> animeList, Context context, OnItemClickListener listener) {
        this.animeList = animeList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnimesAdapter.AnimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anime, parent, false);
        return new AnimesAdapter.AnimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimesAdapter.AnimeViewHolder holder, int position) {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);

        Content anime = animeList.get(position);
        holder.tvTitle.setText(anime.getTitle());

        holder.tvDate.setText("Estreno: "+anime.getReleaseDate());

        if(anime.getCoverImage()==null || anime.getCoverImage().equals("")){
            holder.ivCover.setImageResource(R.drawable.no_image_available);
        }else{
            Picasso.get().load(anime.getCoverImage()).into(holder.ivCover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(animeList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    static class AnimeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvDate;

        public AnimeViewHolder(@NonNull View itemView) {
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
