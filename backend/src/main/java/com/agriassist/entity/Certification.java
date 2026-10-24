package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "certification")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String type; // REALNAME / ORIGIN / COOP / EXPERT / QUALITY

    @Column(length = 1000)
    private String material; // 认证材料/说明

    @Column(length = 20)
    private String status = "PENDING"; // PENDING / APPROVED / REJECTED

    @Column(length = 500)
    private String rejectReason;

    private LocalDateTime applyTime = LocalDateTime.now();

    private LocalDateTime reviewTime;

    private Long reviewerId;
}
