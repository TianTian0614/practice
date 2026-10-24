package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "negotiation")
public class Negotiation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplyDemandId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(length = 1000)
    private String message;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING / ACCEPTED / REJECTED / DONE

    private LocalDateTime createTime = LocalDateTime.now();
}
