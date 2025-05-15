package com.example.myvault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myvault.R;
import com.example.myvault.models.CustomListItem;
import com.example.myvault.models.User;

import java.util.List;

public class FriendsAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> userList;

    public FriendsAdapter(Context context, List<User> userList) {
        super(context, 0, userList);
        this.context = context;
        this.userList = userList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_friends, parent, false);
        }

        User user = userList.get(position);

        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvNameSurname = convertView.findViewById(R.id.tvNameSurname);

        tvUsername.setText(user.getUsername());
        tvNameSurname.setText(user.getName()+" "+user.getSurname());

        return convertView;
    }
}