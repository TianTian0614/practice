package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.dto.UserPublicVO;
import com.agriassist.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/me")
    public Result<UserPublicVO> me() {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(userService.getPublicById(uid, true));
    }

    // 公开资料：仅登录用户可见联系方式（且脱敏），未登录返回空，不含密码
    @GetMapping("/public/{id}")
    public Result<UserPublicVO> publicInfo(@PathVariable Long id) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = auth != null && auth.getPrincipal() instanceof Long;
        return Result.success(userService.getPublicById(id, loggedIn));
    }

    @PutMapping("/me")
    public Result<UserPublicVO> update(@RequestBody Map<String, String> body) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(userService.updateProfile(
                uid, body.get("nickname"), body.get("contact"), body.get("region")));
    }
}
