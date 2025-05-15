package com.example.myvault.models;

import java.io.Serializable;

public class UserReview extends DomainEntity implements Serializable {

    private String contentID;
    private String userID;
    private Double rating;
    private String comment;
    private String reviewDate;

    public UserReview() {
    }

    public UserReview(String contentID, String userID, Double rating, String comment, String reviewDate) {
        super();
        this.contentID = contentID;
        this.userID = userID;
        this.rating = rating;
        this.comment = comment;
    }


    public String getContentID() {
        return contentID;
    }

    public void setContentID(String contentID) {
        this.contentID = contentID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }


    @Override
    public String toString() {
        return "UserReview{" +
                "contentID='" + contentID + '\'' +
                ", userID='" + userID + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", reviewDate='" + reviewDate + '\'' +
                '}';
    }
}
