package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 可信溯源记录：一条记录 = 某批次农产品流转链上的一个环节。
 * 同一 batchNo 的多条记录按 occurredAt 升序排列即为完整溯源链。
 */
@Data
@Entity
@Table(name = "trace_record")
public class TraceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String batchNo; // 溯源批次号（溯源码）

    @Column(nullable = false, length = 50)
    private String productName; // 农产品名称

    @Column(nullable = false, length = 30)
    private String step; // 环节名称：种植/采摘/加工/质检/仓储/配送/上架等

    @Column(length = 500)
    private String detail; // 环节描述

    @Column(length = 50)
    private String location; // 发生地点

    @Column(length = 50)
    private String operator; // 经手人/主体

    @Column(nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now(); // 环节发生时间

    @Column(name = "is_demo", nullable = false)
    private Integer isDemo = 1; // 1=系统演示数据 0=真实业务录入

    private LocalDateTime createTime = LocalDateTime.now();
}
