package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trade_order")
public class TradeOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplyId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(length = 100)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deposit;

    private Integer quantity = 1;

    /** 0 待付定金 1 已付定金 2 已发货 3 已完成 4 已取消 5 已退款 */
    @Column(nullable = false)
    private Integer status = 0;

    @Column(length = 60)
    private String expressNo;

    @Column(length = 500)
    private String remark;

    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
}
