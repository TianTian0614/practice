package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pre_sale")
public class PreSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(length = 1000)
    private String description;

    private java.math.BigDecimal expectPrice;

    private Integer totalQuota;

    private Integer reservedCount = 0;

    private java.math.BigDecimal price;

    private String region;

    private String category;

    private java.time.LocalDate harvestDate;

    private Integer minOrder = 1;

    private Integer status = 1; // 1 进行中 2 已结束 3 已取消

    private LocalDateTime endDate;

    private LocalDateTime createTime = LocalDateTime.now();
}
