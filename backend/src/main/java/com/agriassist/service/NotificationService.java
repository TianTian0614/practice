package com.agriassist.service;

import com.agriassist.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Notification create(Long userId, String type, String title, String content, String link);
    Page<Notification> list(Long userId, Pageable pageable);
    long unreadCount(Long userId);
    void markRead(Long id, Long userId);
    void markAllRead(Long userId);
    void delete(Long id, Long userId);
}
