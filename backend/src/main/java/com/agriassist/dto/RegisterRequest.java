package com.agriassist.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String role; // FARMER / BUYER
    private String nickname;
    private String contact;
    private String region;
}
