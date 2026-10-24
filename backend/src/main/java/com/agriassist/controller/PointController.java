package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.service.PointService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/point")
public class PointController {

    private final PointService service;
    public PointController(PointService service) { this.service = service; }

    @GetMapping("/my")
    public Result<Map<String, Object>> my() {
        return Result.success(service.myPoints(currentUid()));
    }

    @GetMapping("/rank")
    public Result<List<Map<String, Object>>> rank(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(service.rank(limit));
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
