package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "supply_demand")
public class SupplyDemand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String type; // SUPPLY / DEMAND

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 20)
    private String region;

    private java.math.BigDecimal price;

    @Column(length = 2000)
    private String brandStory;

    private Integer status = 1; // 0 审核中 1 已通过 2 已驳回

    private LocalDateTime createTime = LocalDateTime.now();
}
