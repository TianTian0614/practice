package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.service.FavoriteService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService service;
    public FavoriteController(FavoriteService service) { this.service = service; }

    @PostMapping("/toggle")
    public Result<Map<String, Boolean>> toggle(@RequestBody Map<String, Object> body) {
        String type = (String) body.get("targetType");
        Long tid = ((Number) body.get("targetId")).longValue();
        boolean favored = service.toggle(currentUid(), type, tid);
        return Result.success(Map.of("favored", favored));
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam String targetType, @RequestParam Long targetId) {
        return Result.success(service.isFav(currentUid(), targetType, targetId));
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String targetType) {
        return Result.success(service.list(currentUid(), targetType));
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
