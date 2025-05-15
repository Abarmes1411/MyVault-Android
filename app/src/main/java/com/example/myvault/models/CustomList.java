package com.example.myvault.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomList extends DomainEntity implements Serializable {


    private String userID;
    private String listName;
    private Map<String, String> items;

    public CustomList() {
    }

    public CustomList(String userID, String listName, Map<String, String> items) {
        super();
        this.userID = userID;
        this.listName = listName;
        this.items = items;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }
}
