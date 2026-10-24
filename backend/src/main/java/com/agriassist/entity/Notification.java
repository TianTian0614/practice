package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;      // 接收人

    @Column(nullable = false, length = 20)
    private String type;      // SYSTEM / TRADE / INTERACTION

    @Column(length = 200)
    private String title;

    @Column(length = 1000)
    private String content;

    @Column(length = 200)
    private String link;      // 跳转地址

    private Integer isRead = 0; // 0 未读 1 已读

    private Integer status = 1; // 1 有效 0 已删除

    private LocalDateTime createTime = LocalDateTime.now();
}
