package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.dto.LoginRequest;
import com.agriassist.dto.LoginResponse;
import com.agriassist.dto.RegisterRequest;
import com.agriassist.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) { this.userService = userService; }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        return Result.success(userService.login(req));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest req) {
        userService.register(req);
        return Result.success();
    }
}
