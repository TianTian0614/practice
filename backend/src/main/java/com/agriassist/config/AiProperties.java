package com.agriassist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 模块配置属性，对应 application.yml 中的 agri.ai.*。
 *
 * mode 说明：
 *  - local  ：纯本地检索，不调用任何大模型（零内存/零外部依赖，返回检索到的知识片段）
 *  - cloud  ：本地检索 + 云端大模型生成（需配置 agri.ai.cloud.api-key）
 *  - ollama ：本地检索 + 本地 Ollama 模型生成（当前默认，需本机安装 Ollama 且内存充足）
 */
@Component
@ConfigurationProperties(prefix = "agri.ai")
@Data
public class AiProperties {

    private String mode = "ollama";

    private Ollama ollama = new Ollama();

    private Cloud cloud = new Cloud();

    @Data
    public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String model = "qwen2.5:7b";
        private String embeddingModel = "nomic-embed-text";
    }

    @Data
    public static class Cloud {
        private String baseUrl = "https://api.hunyuan.cloud.tencent.com/v1";
        private String apiKey = "";
        private String model = "hunyuan-standard";
        private String chatPath = "/chat/completions";
        private double temperature = 0.3;
    }
}
