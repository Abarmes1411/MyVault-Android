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

public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ShowViewHolder> {

    private List<Content> showList;
    private Context context;
    private OnItemClickListener listener;

    public ShowAdapter(List<Content> showList, Context context, OnItemClickListener listener) {
        this.showList = showList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShowAdapter.ShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_show, parent, false);
        return new ShowAdapter.ShowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowAdapter.ShowViewHolder holder, int position) {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);

        Content show = showList.get(position);
        holder.tvTitle.setText(show.getTitle());

        holder.tvDate.setText(show.getReleaseDate());

        if(show.getCoverImage()==null || show.getCoverImage().equals("")){
            holder.ivCover.setImageResource(R.drawable.no_image_available);
        }else{
            Picasso.get().load(show.getCoverImage()).into(holder.ivCover);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(showList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return showList.size();
    }

    static class ShowViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvDate;

        public ShowViewHolder(@NonNull View itemView) {
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
