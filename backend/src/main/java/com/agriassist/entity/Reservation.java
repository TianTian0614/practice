package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long preSaleId;

    @Column(nullable = false)
    private Long userId;

    private Integer quantity = 1;

    @Column(length = 50)
    private String name;     // 联系人

    @Column(length = 50)
    private String phone;    // 联系电话

    @Column(length = 20)
    private String status = "RESERVED"; // RESERVED 已预约 / SHIPPED 已发货 / CONFIRMED 已确认 / CANCELLED 已取消

    private LocalDateTime createTime = LocalDateTime.now();
}
