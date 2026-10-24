package com.agriassist.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Ollama 本地大模型客户端（仅在使用 agri.ai.mode=ollama 时启用）。
 * 调用本地 Ollama 的 /api/generate 与 /api/embeddings 接口。
 */
@Component
public class OllamaClient {

    @Value("${agri.ai.ollama.base-url:http://localhost:11434}")
    private String baseUrl;
    @Value("${agri.ai.ollama.model:qwen2.5:7b}")
    private String model;
    @Value("${agri.ai.ollama.embedding-model:nomic-embed-text}")
    private String embedModel;

    private RestClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        this.client = RestClient.create(baseUrl);
    }

    /** 调用生成接口，返回模型回答文本 */
    public String generate(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );
        try {
            String resp = client.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode node = mapper.readTree(resp);
            return node.path("response").asText();
        } catch (Exception e) {
            return "模型调用失败：" + e.getMessage();
        }
    }

    /** 调用嵌入接口，返回向量 */
    public double[] embed(String text) {
        Map<String, Object> body = Map.of("model", embedModel, "prompt", text);
        try {
            String resp = client.post()
                    .uri("/api/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode arr = mapper.readTree(resp).path("embedding");
            double[] v = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) v[i] = arr.get(i).asDouble();
            return v;
        } catch (Exception e) {
            return new double[0];
        }
    }
}
