package com.agriassist.service.ai;

import com.agriassist.entity.KnowledgeChunk;
import com.agriassist.repository.KnowledgeChunkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 知识库服务：负责农技语料的入库（向量化）与检索。
 * 向量化使用本地 LocalEmbedder，不依赖任何外部大模型，零内存/显存占用。
 */
@Service
public class KnowledgeService {

    private final KnowledgeChunkRepository repository;
    private final LocalEmbedder embedder;
    private final ObjectMapper mapper = new ObjectMapper();

    public KnowledgeService(KnowledgeChunkRepository repository, LocalEmbedder embedder) {
        this.repository = repository;
        this.embedder = embedder;
    }

    @Transactional
    public void ingest(String source, String content) {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setSource(source);
        chunk.setContent(content);
        double[] vec = embedder.embed(content);
        try {
            chunk.setEmbedding(mapper.writeValueAsString(vec));
        } catch (Exception e) {
            chunk.setEmbedding("[]");
        }
        repository.save(chunk);
    }

    /** 检索 topK 个最相似知识片段（仅返回内容） */
    public List<String> retrieve(String query, int topK) {
        return retrieveScored(query, topK).stream().map(r -> r.content).toList();
    }

    /** 检索 topK 个最相似知识片段，并附带余弦相似度分数（供阈值判定使用） */
    public List<RetrievedChunk> retrieveScored(String query, int topK) {
        double[] q = embedder.embed(query);
        List<KnowledgeChunk> all = repository.findAll();
        List<RetrievedChunk> scored = new ArrayList<>();
        for (KnowledgeChunk c : all) {
            double[] v = parse(c.getEmbedding());
            if (v.length == 0) continue;
            scored.add(new RetrievedChunk(c.getContent(), cosine(q, v)));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        return scored.subList(0, Math.min(topK, scored.size()));
    }

    public static class RetrievedChunk {
        public final String content;
        public final double score;
        public RetrievedChunk(String content, double score) {
            this.content = content;
            this.score = score;
        }
    }

    private double[] parse(String json) {
        try {
            List<Double> list = mapper.readValue(json, mapper.getTypeFactory()
                    .constructCollectionType(List.class, Double.class));
            double[] v = new double[list.size()];
            for (int i = 0; i < list.size(); i++) v[i] = list.get(i);
            return v;
        } catch (Exception e) {
            return new double[0];
        }
    }

    private double cosine(double[] a, double[] b) {
        if (a.length != b.length) return 0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return (na == 0 || nb == 0) ? 0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
