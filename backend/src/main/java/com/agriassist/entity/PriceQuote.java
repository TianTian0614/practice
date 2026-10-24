package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 农产品价格行情报价：某农产品在某市场/渠道某日的价格。
 * 多市场同日价格对比即构成"比价"。
 */
@Data
@Entity
@Table(name = "price_quote")
public class PriceQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String productName; // 农产品名称

    @Column(nullable = false, length = 50)
    private String market; // 市场/渠道：产地直销、批发市场、商超、电商等

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // 价格

    @Column(nullable = false, length = 20)
    private String unit; // 计价单位：元/斤、元/箱等

    @Column(nullable = false)
    private LocalDate quoteDate; // 报价日期

    // —— 真实数据溯源字段（从农业农村部官方市场行情库采集，毕设须可溯源）——
    @Column(length = 500)
    private String sourceTitle; // 来源行情综述标题

    @Column(length = 200)
    private String sourceMarket; // 来源批发市场名称

    @Column(length = 500)
    private String sourceUrl; // 来源链接（可溯源）

    @Column(precision = 10, scale = 2)
    private BigDecimal priceLow; // 真实价格区间下限（元/斤）

    @Column(precision = 10, scale = 2)
    private BigDecimal priceHigh; // 真实价格区间上限（元/斤）

    @Column(nullable = false)
    private Boolean isReal = true; // 是否为真实采集数据（true=官方溯源）

    private LocalDateTime createTime = LocalDateTime.now();
}
