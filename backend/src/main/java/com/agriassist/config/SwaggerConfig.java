package com.agriassist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI agriAssistOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("agri-assist 乡村数字化助农服务平台 API")
                .description("面向农户与采购方的助农服务平台，含 RAG 本地知识库智能咨询")
                .version("1.0.0"));
    }
}
