package com.example.myvault.models;

import java.io.Serializable;

public class Message extends DomainEntity implements Serializable {


    private String type;
    private String chatID;
    private String userID;
    private String message;
    private String timestamp;
    private CustomList customList;

    public Message() {
    }


    public Message(String type, String chatID, String userID, String message, String timestamp) {
        super();
        this.type = type;
        this.chatID = chatID;
        this.userID = userID;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CustomList getCustomList() {
        return customList;
    }

    public void setCustomList(CustomList customList) {
        this.customList = customList;
    }
}
