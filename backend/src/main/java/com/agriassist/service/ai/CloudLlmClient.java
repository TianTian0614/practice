package com.agriassist.service.ai;

import com.agriassist.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 云端大模型客户端（OpenAI 兼容协议）。
 * 默认对接腾讯混元 OpenAI 兼容端点，亦可替换为 DeepSeek、智谱等任意兼容服务，
 * 只需修改 application.yml 中的 agri.ai.cloud.base-url / model / api-key。
 *
 * 注：本类仅负责“生成”环节，知识检索仍在本地完成（见 KnowledgeService）。
 * API Key 通过环境变量 HUNYUAN_API_KEY 注入，避免明文写在配置文件中。
 */
@Component
public class CloudLlmClient {

    private final AiProperties props;
    private final RestClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public CloudLlmClient(AiProperties props) {
        this.props = props;
        this.client = RestClient.create(props.getCloud().getBaseUrl());
    }

    /** 是否已配置可用的 API Key */
    public boolean isConfigured() {
        String key = props.getCloud().getApiKey();
        return key != null && !key.isBlank();
    }

    /** 调用云端大模型生成回答 */
    public String generate(String system, String user) {
        if (!isConfigured()) {
            return "云端模型未配置 API Key，无法生成回答。";
        }
        Map<String, Object> body = Map.of(
                "model", props.getCloud().getModel(),
                "temperature", props.getCloud().getTemperature(),
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                )
        );
        try {
            String resp = client.post()
                    .uri(props.getCloud().getChatPath())
                    .header("Authorization", "Bearer " + props.getCloud().getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode node = mapper.readTree(resp);
            return node.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "云端模型调用失败：" + e.getMessage();
        }
    }
}
