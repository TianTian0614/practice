package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "favorite", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_type", "target_id"}))
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType; // SUPPLY / ARTICLE / PRESALE / COMMUNITY

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    private LocalDateTime createTime = LocalDateTime.now();
}
