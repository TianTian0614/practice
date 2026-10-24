package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String targetType; // SUPPLY_DEMAND / ARTICLE / POST

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 20)
    private String action; // APPROVE / REJECT

    private Long auditorId;

    @Column(length = 500)
    private String note;

    private LocalDateTime createTime = LocalDateTime.now();
}
