package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "point_log")
public class PointLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Integer delta; // 积分变动值（社区激励为正）

    @Column(length = 50)
    private String reason; // 发帖 / 评论 / 最佳答案

    @Column(length = 20)
    private String refType; // POST / COMMENT

    private Long refId;

    private LocalDateTime createTime = LocalDateTime.now();
}
