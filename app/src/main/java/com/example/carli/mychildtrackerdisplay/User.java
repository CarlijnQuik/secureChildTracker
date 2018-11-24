package com.example.carli.mychildtrackerdisplay;

public class User {
    String userId;
    String userType;

    public User(String userId, String userType) {
        this.userId = userId;
        this.userType = userType;

    }

    public String getUserId() {
        return userId;
    }

    public String getUserType(){
        return userType;

    }

}
