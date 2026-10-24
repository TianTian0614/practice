package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.Subscription;
import com.agriassist.service.SubscriptionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscribe")
public class SubscriptionController {

    private final SubscriptionService service;
    public SubscriptionController(SubscriptionService service) { this.service = service; }

    @PostMapping
    public Result<Subscription> subscribe(@RequestBody Map<String, Object> body) {
        String keyword = (String) body.get("keyword");
        String region = (String) body.get("region");
        return Result.success(service.subscribe(currentUid(), keyword, region));
    }

    @DeleteMapping("/{id}")
    public Result<Void> unsubscribe(@PathVariable Long id) {
        service.unsubscribe(id, currentUid());
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<Subscription>> my() {
        return Result.success(service.list(currentUid()));
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
