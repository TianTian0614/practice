package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.Notification;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.NotificationRepository;
import com.agriassist.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Notification create(Long userId, String type, String title, String content, String link) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setLink(link);
        n.setIsRead(0);
        n.setStatus(1);
        return repository.save(n);
    }

    @Override
    public Page<Notification> list(Long userId, Pageable pageable) {
        return repository.findByUserIdAndStatus(userId, 1, pageable);
    }

    @Override
    public long unreadCount(Long userId) {
        return repository.countByUserIdAndStatusAndIsRead(userId, 1, 0);
    }

    @Override
    @Transactional
    public void markRead(Long id, Long userId) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "通知不存在"));
        if (!n.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该通知");
        }
        n.setIsRead(1);
        repository.save(n);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        repository.findByUserIdAndStatus(userId, 1).forEach(n -> {
            n.setIsRead(1);
            repository.save(n);
        });
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "通知不存在"));
        if (!n.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该通知");
        }
        n.setStatus(0);
        repository.save(n);
    }
}
