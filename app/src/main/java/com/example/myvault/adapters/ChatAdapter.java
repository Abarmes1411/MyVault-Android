package com.example.myvault.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.activities.DetailItemCustomListsActivity;
import com.example.myvault.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TEXT_SENT = 0;
    private static final int VIEW_TYPE_TEXT_RECEIVED = 1;
    private static final int VIEW_TYPE_CUSTOM_LIST = 2;

    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public ChatAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if ("custom_list".equals(message.getType())) {
            return VIEW_TYPE_CUSTOM_LIST;
        } else if (message.getUserID().equals(currentUserId)) {
            return VIEW_TYPE_TEXT_SENT;
        } else {
            return VIEW_TYPE_TEXT_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_TEXT_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new TextMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_TEXT_RECEIVED) {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new TextMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_custom_list, parent, false);
            return new CustomListMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof TextMessageViewHolder) {
            ((TextMessageViewHolder) holder).bind(message);
        } else if (holder instanceof CustomListMessageViewHolder) {
            CustomListMessageViewHolder customHolder = (CustomListMessageViewHolder) holder;
            customHolder.bind(message);

            customHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailItemCustomListsActivity.class);
                intent.putExtra("listName", message.getCustomList().getListName());
                intent.putExtra("senderUid", message.getUserID());
                context.startActivity(intent);
            });
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
            tvTimestamp.setText(message.getTimestamp());
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
            tvTimestamp.setText(message.getTimestamp());
        }
    }
}
