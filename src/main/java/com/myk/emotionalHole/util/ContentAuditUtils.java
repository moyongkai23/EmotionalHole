package com.myk.emotionalHole.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 内容审核工具类
 * 调用百度千帆Agent进行AI审核，不可用时跳过审核
 * 返回PASS/SUSPECT/REJECT三级审核结果
 */
@Component
public class ContentAuditUtils {

    private static final Logger logger = LoggerFactory.getLogger(ContentAuditUtils.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.audit-agent-id:}")
    private String auditAgentId;

    @Value("${ai.agent-api-url:}")
    private String agentApiUrl;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public enum AuditResult {
        PASS,
        SUSPECT,
        REJECT
    }

    /** AI审核入口：配置完整走千帆Agent，否则跳过敏感词检测 */
    public AuditResult auditContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return AuditResult.PASS;
        }

        // 检查AI配置是否完整
        if (!isAiConfigured()) {
            logger.info("AI审核配置不完整，跳过AI审核");
            return AuditResult.PASS;
        }

        try {
            return callAgentAPI(content);
        } catch (Exception e) {
            logger.warn("AI审核失败，跳过AI审核: {}", e.getMessage());
            return AuditResult.PASS;
        }
    }

    /** 检查AI审核配置（apiKey/auditAgentId/agentApiUrl）是否完整 */
    private boolean isAiConfigured() {
        return apiKey != null && !apiKey.isEmpty() &&
               auditAgentId != null && !auditAgentId.isEmpty() &&
               agentApiUrl != null && !agentApiUrl.isEmpty();
    }

    /** 调用百度千帆Agent进行AI内容审核，解析返回PASS/SUSPECT/REJECT */
    private AuditResult callAgentAPI(String content) {
        try {
            // 构建审核提示
            String auditPrompt = "请你作为内容审核专家，判断以下用户发布的内容是否合规。\n\n" +
                "审核规则：\n" +
                "1. 如果内容包含脏话、侮辱性语言、暴力、色情、政治敏感等禁止级内容，请回复REJECT\n" +
                "2. 如果内容包含轻微负面词汇、广告推销等警告级内容，请回复SUSPECT\n" +
                "3. 如果内容正常、积极健康，请回复PASS\n\n" +
                "待审核内容：\n" + content;
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("app_id", auditAgentId);
            requestBody.put("stream", false);
            requestBody.put("query", auditPrompt);
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            // 构建请求实体
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(agentApiUrl, HttpMethod.POST, entity, String.class);
            // 解析响应体
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                // 从JSON响应中提取审核结果
                String result = "";
                if (jsonNode.has("answer")) {
                    result = jsonNode.get("answer").asText().trim();
                } else if (jsonNode.has("result")) {
                    result = jsonNode.get("result").asText().trim();
                }
                // 解析审核结果
                if (result.contains("REJECT")) {
                    return AuditResult.REJECT;
                } else if (result.contains("SUSPECT")) {
                    return AuditResult.SUSPECT;
                } else if (result.contains("PASS")) {
                    return AuditResult.PASS;
                }
            }
        } catch (Exception e) {
            logger.error("Agent审核API调用失败: {}", e.getMessage());
        }
        return AuditResult.PASS;
    }
}