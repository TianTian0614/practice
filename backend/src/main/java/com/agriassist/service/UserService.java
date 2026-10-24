package com.agriassist.service;

import com.agriassist.dto.LoginRequest;
import com.agriassist.dto.LoginResponse;
import com.agriassist.dto.RegisterRequest;
import com.agriassist.dto.UserPublicVO;

public interface UserService {
    LoginResponse login(LoginRequest req);
    void register(RegisterRequest req);
    com.agriassist.entity.User getById(Long id);
    UserPublicVO getPublicById(Long id, boolean exposeContact);
    UserPublicVO updateProfile(Long uid, String nickname, String contact, String region);
}
