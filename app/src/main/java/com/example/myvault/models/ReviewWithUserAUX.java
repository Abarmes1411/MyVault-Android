package com.example.myvault.models;

public class ReviewWithUserAUX {
    private User user;
    private String comment;
    private double rating;
    private String reviewDate;

    public ReviewWithUserAUX(User user, String comment, double rating, String reviewDate) {
        this.user = user;
        this.comment = comment;
        this.rating = rating;
        this.reviewDate = reviewDate;
    }

    public User getUser() {
        return user;
    }

    public String getComment() {
        return comment;
    }

    public double getRating() {
        return rating;
    }

    public String getReviewDate() {
        return reviewDate;
    }
}

