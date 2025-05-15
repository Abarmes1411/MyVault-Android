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

    public SearchAdapter(List<Content> gameList, Context context) {
        this.searchList = gameList;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
        return new SearchAdapter.SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchViewHolder holder, int position) {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);

        Content search = searchList.get(position);
        holder.tvTitle.setText(search.getTitle());

        holder.tvDate.setText(search.getReleaseDate());

        Picasso.get().load(search.getCoverImage()).into(holder.ivCover);
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
}
