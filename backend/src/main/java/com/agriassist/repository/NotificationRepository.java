package com.agriassist.repository;

import com.agriassist.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);
    List<Notification> findByUserIdAndStatus(Long userId, Integer status);
    long countByUserIdAndStatusAndIsRead(Long userId, Integer status, Integer isRead);
}
