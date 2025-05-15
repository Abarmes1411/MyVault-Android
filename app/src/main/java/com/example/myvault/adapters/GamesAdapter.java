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

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GamesViewHolder> {

    private List<Content> gameList;
    private Context context;
    private OnItemClickListener listener;


    public GamesAdapter(List<Content> gameList, Context context, OnItemClickListener listener) {
        this.gameList = gameList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GamesAdapter.GamesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GamesAdapter.GamesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GamesAdapter.GamesViewHolder holder, int position) {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);

        Content game = gameList.get(position);
        holder.tvTitle.setText(game.getTitle());

        holder.tvDate.setText(game.getReleaseDate());


        if(game.getCoverImage()==null || game.getCoverImage().equals("")){
            holder.ivCover.setImageResource(R.drawable.no_image_available);
        }else{
            Picasso.get().load(game.getCoverImage()).into(holder.ivCover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(gameList.get(holder.getAdapterPosition()));
            }
        });    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    static class GamesViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvDate;

        public GamesViewHolder(@NonNull View itemView) {
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
