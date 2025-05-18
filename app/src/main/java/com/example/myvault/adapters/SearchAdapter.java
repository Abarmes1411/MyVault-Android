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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Content> searchList;
    private Context context;
    private SearchAdapter.OnItemClickListener listener;


    public SearchAdapter(List<Content> gameList, Context context, SearchAdapter.OnItemClickListener listener) {
        this.searchList = gameList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
        return new SearchAdapter.SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchViewHolder holder, int position) {

        Content search = searchList.get(position);
        holder.tvTitle.setText(search.getTitle());
        holder.tvDate.setText(search.getReleaseDate());

        if(search.getCoverImage()==null || search.getCoverImage().equals("")){
            holder.ivCover.setImageResource(R.drawable.no_image_available);
        }else{
            Picasso.get().load(search.getCoverImage()).into(holder.ivCover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(search);
            }
        });
    }


    @Override
    public int getItemCount() {
        return searchList.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvDate;

        public SearchViewHolder(@NonNull View itemView) {
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
