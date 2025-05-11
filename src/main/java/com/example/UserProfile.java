package com.example;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserProfile {
    private String userId;
    private String name;
    private String email;
}