package com.example.myvault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myvault.R;
import com.example.myvault.models.CustomListItem;

import java.util.List;

public class CustomListsAdapter extends ArrayAdapter<CustomListItem> {

    private Context context;
    private List<CustomListItem> customListItems;

    public CustomListsAdapter(Context context, List<CustomListItem> customListItems) {
        super(context, 0, customListItems);
        this.context = context;
        this.customListItems = customListItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_lists, parent, false);
        }

        CustomListItem item = customListItems.get(position);

        TextView tvListTitle = convertView.findViewById(R.id.tvListTitle);
        TextView tvNumberElements = convertView.findViewById(R.id.tvNumberElements);

        tvListTitle.setText(item.getTitle());
        tvNumberElements.setText(item.getItemCount() + " elementos");

        return convertView;
    }
}