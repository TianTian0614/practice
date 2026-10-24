package com.agriassist.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String role;
    private String nickname;
}
