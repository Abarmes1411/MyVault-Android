package com.example.myvault.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Chat extends DomainEntity implements Serializable {

    private List<User> users;
    private List<Message> messages;

    public Chat() {
        this.users = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public Chat(List<User> users, List<Message> messages) {
        super();
        this.users = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
