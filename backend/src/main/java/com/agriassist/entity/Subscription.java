package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 50)
    private String keyword; // 订阅的品类/关键词

    @Column(length = 50)
    private String region;  // 订阅地区（可选）

    private LocalDateTime createTime = LocalDateTime.now();
}
