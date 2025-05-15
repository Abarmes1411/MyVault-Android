package com.example.myvault.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myvault.R;
import com.example.myvault.enums.Mode;
import com.example.myvault.models.Content;
import com.example.myvault.models.ReviewWithContentAUX;
import com.example.myvault.models.User;
import com.example.myvault.models.UserReview;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> items;
    private Mode mode = Mode.DEFAULT;

    public UserListAdapter(@NonNull Context context, List<User> items, Mode mode) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        this.mode = mode;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User item = getItem(position);

        if (item == null || item.getId() == null) {
            View emptyView = convertView != null ? convertView :
                    LayoutInflater.from(context).inflate(R.layout.item_userlist, parent, false);

            TextView tvUsername = emptyView.findViewById(R.id.tvUsername);
            TextView tvNameSurname = emptyView.findViewById(R.id.tvNameSurname);
            ImageView sendRequest = emptyView.findViewById(R.id.send_request);
            ImageView pendingRequest = emptyView.findViewById(R.id.pending_request);
            ImageView acceptRequest = emptyView.findViewById(R.id.accept_request);
            ImageView refuseRequest = emptyView.findViewById(R.id.refuse_request);

            sendRequest.setVisibility(View.GONE);
            pendingRequest.setVisibility(View.GONE);
            acceptRequest.setVisibility(View.GONE);
            refuseRequest.setVisibility(View.GONE);

            tvUsername.setText("Usuario no válido");
            tvNameSurname.setText("");

            Log.e("UserListAdapter", "Item o ID nulo en posición: " + position);
            return emptyView;
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_userlist, parent, false);
        }

        // Referencias a vistas
        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvNameSurname = convertView.findViewById(R.id.tvNameSurname);
        ImageView sendRequest = convertView.findViewById(R.id.send_request);
        ImageView pendingRequest = convertView.findViewById(R.id.pending_request);
        ImageView acceptRequest = convertView.findViewById(R.id.accept_request);
        ImageView refuseRequest = convertView.findViewById(R.id.refuse_request);

        sendRequest.setVisibility(View.GONE);
        pendingRequest.setVisibility(View.GONE);
        acceptRequest.setVisibility(View.GONE);
        refuseRequest.setVisibility(View.GONE);

        tvUsername.setText(item.getUsername());
        tvNameSurname.setText(item.getName() + " " + item.getSurname());

        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        String targetUid = item != null ? item.getId() : null;

        if (currentUid == null || targetUid == null) {
            Log.e("UserListAdapter", "UID nulo - currentUid: " + currentUid + ", targetUid: " + targetUid);
            return convertView;
        }

        if (mode == Mode.DEFAULT) {
            DatabaseReference requestRef = FirebaseDatabase.getInstance()
                    .getReference("friend_requests")
                    .child(targetUid).child(currentUid);

            requestRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    pendingRequest.setVisibility(View.VISIBLE);
                } else {
                    sendRequest.setVisibility(View.VISIBLE);
                    sendRequest.setOnClickListener(v -> {
                        requestRef.setValue("pending").addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                sendRequest.setVisibility(View.GONE);
                                pendingRequest.setVisibility(View.VISIBLE);
                            }
                        });
                    });
                }
            });
        } else if (mode == Mode.REQUESTS) {
            acceptRequest.setVisibility(View.VISIBLE);
            refuseRequest.setVisibility(View.VISIBLE);

            acceptRequest.setOnClickListener(v -> {
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                usersRef.child(currentUid).child("friends").child(targetUid).setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            // Insertar currentUid en la lista de amigos del target
                            usersRef.child(targetUid).child("friends").child(currentUid).setValue(true)
                                    .addOnSuccessListener(aVoid2 -> {
                                        // Eliminar la solicitud correctamente
                                        FirebaseDatabase.getInstance()
                                                .getReference("friend_requests")
                                                .child(targetUid).child(currentUid)
                                                .removeValue();
                                        FirebaseDatabase.getInstance()
                                                .getReference("friend_requests")
                                                .child(currentUid).child(targetUid)
                                                .removeValue();

                                        // Quitar visualmente de la lista
                                        items.remove(position);
                                        notifyDataSetChanged();
                                    });
                        });
            });


            refuseRequest.setOnClickListener(v -> {
                FirebaseDatabase.getInstance()
                        .getReference("friend_requests")
                        .child(currentUid).child(targetUid).removeValue();
                items.remove(position);
                notifyDataSetChanged();
            });
        }

        return convertView;
    }
}

