package com.agriassist.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "knowledge_chunk")
public class KnowledgeChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String source;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 向量以 JSON 数组字符串存储（演示用；生产建议 PGVector / Chroma）
    @Column(columnDefinition = "TEXT")
    private String embedding;

    private LocalDateTime createTime = LocalDateTime.now();
}
