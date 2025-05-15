package com.example.myvault.models;

public class ReviewWithContentAUX {
    private UserReview review;
    private Content content;

    public ReviewWithContentAUX(UserReview review, Content content) {
        this.review = review;
        this.content = content;
    }

    public UserReview getReview() {
        return review;
    }

    public Content getContent() {
        return content;
    }
}
