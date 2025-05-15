package com.example.myvault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.models.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TEXT = 0;
    private static final int VIEW_TYPE_CUSTOM_LIST = 1;

    private Context context;
    private List<Message> messages;

    public ChatAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if ("custom_list".equals(message.getType())) {
            return VIEW_TYPE_CUSTOM_LIST;
        } else {
            return VIEW_TYPE_TEXT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TEXT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_text, parent, false);
            return new TextMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_custom_list, parent, false);
            return new CustomListMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof TextMessageViewHolder) {
            ((TextMessageViewHolder) holder).bind(message);
        } else if(holder instanceof CustomListMessageViewHolder) {
            ((CustomListMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder para mensajes de texto
    public class TextMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;
        public TextMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
        public void bind(Message message) {
            tvMessage.setText(message.getMessage());
            tvTimestamp.setText(message.getMessageDate());
        }
    }

    // ViewHolder para mensajes con lista personalizada
    public class CustomListMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp, tvCustomListTitle;
        public CustomListMessageViewHolder(View itemView) {
            super(itemView);
            tvCustomListTitle = itemView.findViewById(R.id.tvCustomListTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
        public void bind(Message message) {
            if (message.getCustomList() != null) {
                tvCustomListTitle.setText(message.getCustomList().getListName());
            }
            tvMessage.setText(message.getMessage());
            tvTimestamp.setText(message.getMessageDate());
        }
    }
}

