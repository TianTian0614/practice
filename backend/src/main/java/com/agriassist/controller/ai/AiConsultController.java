package com.agriassist.controller.ai;

import com.agriassist.common.Result;
import com.agriassist.config.AiProperties;
import com.agriassist.entity.Consultation;
import com.agriassist.service.ai.AiConsultService;
import com.agriassist.service.ai.CloudLlmClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiConsultController {
    private final AiConsultService service;
    private final AiProperties aiProperties;
    private final CloudLlmClient cloudLlmClient;

    public AiConsultController(AiConsultService service, AiProperties aiProperties, CloudLlmClient cloudLlmClient) {
        this.service = service;
        this.aiProperties = aiProperties;
        this.cloudLlmClient = cloudLlmClient;
    }

    @PostMapping("/consult")
    public Result<Consultation> consult(@RequestParam String question) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(service.ask(uid, question));
    }

    @PostMapping("/knowledge/ingest")
    public Result<Void> ingest(@RequestParam String source, @RequestParam String content) {
        service.ingest(source, content);
        return Result.success();
    }

    /** 查看当前 AI 模式与云端配置状态（调试用） */
    @GetMapping("/mode")
    public Result<Map<String, Object>> mode() {
        Map<String, Object> m = new HashMap<>();
        m.put("mode", aiProperties.getMode());
        m.put("cloudConfigured", cloudLlmClient.isConfigured());
        return Result.success(m);
    }
}
