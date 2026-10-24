package com.agriassist.service.ai;

import com.agriassist.config.AiProperties;
import com.agriassist.entity.Consultation;
import com.agriassist.repository.ConsultationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 智能咨询业务服务。
 *
 * 采用“本地检索 + 生成”架构：
 *  1) 先用本地向量检索从知识库找出最相关的农技片段（零模型、零内存占用）；
 *  2) 再按 agri.ai.mode 决定如何生成最终回答：
 *     - local  ：直接返回检索到的知识片段（不调用大模型）
 *     - cloud  ：将检索片段作为上下文，调用云端大模型生成自然语言回答
 *     - ollama ：将检索片段作为上下文，调用本地 Ollama 模型生成回答
 *
 * 当最高相似度低于阈值时，返回友好兜底提示，而非硬塞不相关内容。
 */
@Service
public class AiConsultService {

    private final KnowledgeService knowledgeService;
    private final CloudLlmClient cloudLlmClient;
    private final OllamaClient ollamaClient;
    private final AiProperties aiProperties;
    private final ConsultationRepository consultationRepository;

    public AiConsultService(KnowledgeService knowledgeService,
                            CloudLlmClient cloudLlmClient,
                            OllamaClient ollamaClient,
                            AiProperties aiProperties,
                            ConsultationRepository consultationRepository) {
        this.knowledgeService = knowledgeService;
        this.cloudLlmClient = cloudLlmClient;
        this.ollamaClient = ollamaClient;
        this.aiProperties = aiProperties;
        this.consultationRepository = consultationRepository;
    }

    /** 相似度低于该阈值判定为“未检索到相关内容” */
    private static final double SIM_THRESHOLD = 0.12;
    private static final String SYSTEM_PROMPT =
            "你是面向农户与采购方的农业智能助手。请仅依据用户提供的农业知识片段作答，" +
                    "若知识不足请如实说明，不要编造。";
    private static final String DISCLAIMER =
            "\n\n——温馨提示：以上内容由 AI 基于本地农技知识库生成，仅供参考，不构成专业农事决策依据；" +
            "用药、施肥等关键环节请务必咨询当地农技推广站或拨打 12316 农业服务热线。";

    @Transactional
    public Consultation ask(Long userId, String question) {
        List<KnowledgeService.RetrievedChunk> hits = knowledgeService.retrieveScored(question, 3);
        double best = hits.isEmpty() ? 0 : hits.get(0).score;
        List<String> refs = hits.stream().map(h -> h.content).toList();

        String answer;
        if (best < SIM_THRESHOLD) {
            answer = "抱歉，本地知识库未检索到与您问题高度相关的内容。建议：①换一种表述再试；" +
                    "②咨询当地农技推广站；③联系平台管理员补充相关知识。";
        } else {
            String mode = aiProperties.getMode();
            switch (mode) {
                case "local":
                    answer = "根据本地知识库，为您找到以下相关内容：\n\n" + String.join("\n\n", refs);
                    break;
                case "ollama":
                    try {
                        answer = ollamaClient.generate(buildPrompt(refs, question));
                    } catch (Exception e) {
                        answer = "（AI 生成服务暂时不可用，已为您返回本地知识库检索结果）\n\n"
                                + String.join("\n\n", refs);
                    }
                    break;
                case "cloud":
                default:
                    if (!cloudLlmClient.isConfigured()) {
                        answer = "（提示：未配置云端模型 API Key，已返回本地检索结果）\n\n"
                                + String.join("\n\n", refs);
                    } else {
                        try {
                            answer = cloudLlmClient.generate(SYSTEM_PROMPT, buildPrompt(refs, question));
                        } catch (Exception e) {
                            answer = "（AI 生成服务暂时不可用，已为您返回本地知识库检索结果）\n\n"
                                    + String.join("\n\n", refs);
                        }
                    }
            }
        }

        answer = answer + DISCLAIMER;

        Consultation c = new Consultation();
        c.setUserId(userId);
        c.setQuestion(question);
        c.setAnswer(answer);
        c.setSourceRefs(String.join("\n", refs));
        return consultationRepository.save(c);
    }

    /** 知识入库（供管理员/定时任务灌入农技语料） */
    public void ingest(String source, String content) {
        knowledgeService.ingest(source, content);
    }

    private String buildPrompt(List<String> refs, String question) {
        StringBuilder ctx = new StringBuilder();
        for (String r : refs) {
            ctx.append("- ").append(r).append("\n");
        }
        return "【知识片段】\n" + ctx + "\n【用户问题】" + question + "\n【回答】";
    }
}
