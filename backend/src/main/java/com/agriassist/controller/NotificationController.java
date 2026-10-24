package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.Notification;
import com.agriassist.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public Result<Page<Notification>> list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(service.list(currentUid(), PageRequest.of(page, size)));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(service.unreadCount(currentUid()));
    }

    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        service.markRead(id, currentUid());
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        service.markAllRead(currentUid());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id, currentUid());
        return Result.success();
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
