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

public class DetailListAdapter extends RecyclerView.Adapter<DetailListAdapter.DetailListViewHolder> {

    private List<Content> detailList;
    private Context context;

    private DetailListAdapter.OnItemClickListener listener;

    public DetailListAdapter(List<Content> movieList, Context context, DetailListAdapter.OnItemClickListener listener) {
        this.detailList = movieList;
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public DetailListAdapter.DetailListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_content, parent, false);
        return new DetailListAdapter.DetailListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailListAdapter.DetailListViewHolder holder, int position) {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);

        Content detail = detailList.get(position);
        holder.tvTitle.setText(detail.getTitle());

        holder.tvDate.setText(detail.getReleaseDate());

        Picasso.get().load(detail.getCoverImage()).into(holder.ivCover);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(detailList.get(holder.getAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return detailList.size();
    }

    static class DetailListViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvDate;

        public DetailListViewHolder(@NonNull View itemView) {
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
