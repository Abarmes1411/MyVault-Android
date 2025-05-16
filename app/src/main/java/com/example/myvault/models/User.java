package com.example.myvault.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User extends DomainEntity implements Serializable {
    private String name;
    private String surname;
    private String username;
    private String email;
    private String profilePic;
    private Map<String, CustomList> customLists;
    private Map<String, Boolean> friends;
    public Map<String, UserReview> userReviews;
    public Map<String, String> myVault;


    public User() {
    }

    public User(String name, String surname, String email, String username, String profilePic, Map<String, CustomList> customLists, Map<String, Boolean> friends) {
        super();
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.username = username;
        this.profilePic = profilePic;
        this.customLists  = customLists;
        this.friends = friends;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        return profilePic;
    }


    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Map<String, CustomList> getCustomLists() {
        return customLists;
    }

    public void setCustomLists(Map<String, CustomList> customLists) {
        this.customLists = customLists;
    }

    public Map<String, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Boolean> friends) {
        this.friends = friends;
    }

    public Map<String, UserReview> getUserReviews() {
        return userReviews;
    }

    public void setUserReviews(Map<String, UserReview> userReviews) {
        this.userReviews = userReviews;
    }

    public Map<String, String> getMyVault() {
        return myVault;
    }

    public void setMyVault(Map<String, String> myVault) {
        this.myVault = myVault;
    }
}
