package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String role; // FARMER / BUYER / ADMIN

    @Column(length = 50)
    private String nickname;

    @Column(length = 100)
    private String contact;

    @Column(length = 50)
    private String region;

    @Column(columnDefinition = "int default 0")
    private Integer points = 0; // 社区激励积分

    private LocalDateTime createTime = LocalDateTime.now();
}
