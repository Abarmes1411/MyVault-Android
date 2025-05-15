package com.example.myvault.models;

public class CustomListItem {
    private String title;
    private int itemCount;

    public CustomListItem() {}

    public CustomListItem(String title, int itemCount) {
        this.title = title;
        this.itemCount = itemCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}