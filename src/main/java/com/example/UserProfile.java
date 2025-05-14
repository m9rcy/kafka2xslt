package com.example;


import lombok.Data;

@Data
public class UserProfile {
    private String userId;
    private String name;
    private String email;

    public UserProfile(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}