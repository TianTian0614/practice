package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.service.RecommendService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService service;
    public RecommendController(RecommendService service) { this.service = service; }

    @GetMapping
    public Result<Map<String, Object>> recommend(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(service.recommend(currentUidOrNull(), limit));
    }

    private Long currentUidOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
