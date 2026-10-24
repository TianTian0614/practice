package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rating")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fromUserId;   // 评价人

    @Column(nullable = false)
    private Long toUserId;     // 被评价人

    @Column(nullable = false)
    private Long targetId;     // 关联对象（供需/预售/协商 id）

    @Column(nullable = false, length = 20)
    private String targetType; // SUPPLY / PRESALE / NEGOTIATION

    private Integer score;     // 1~5 分

    @Column(length = 1000)
    private String comment;

    private Integer status = 1; // 1 有效 0 已删除

    private LocalDateTime createTime = LocalDateTime.now();
}
