package com.myk.emotionalHole.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * AI工具类，集成百度千帆大模型
 * 包含用户聊天Agent和情感分析功能
 * AI服务不可用时降级到基于关键词的本地分析
 */
@Component
public class AiUtils {

    private static final Logger logger = LoggerFactory.getLogger(AiUtils.class);

    public AiUtils() {
    }

    @Value("${ai.api-key:}")
    private String apiKey;

    // Agent配置
    @Value("${ai.chat-agent-id:}")
    private String agentId;

    @Value("${ai.agent-api-url:}")
    private String agentApiUrl;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 情感分析结果类
     */
    public static class EmotionAnalysisResult {
        private String emotion; // 情感类型：positive, negative, neutral
        private double score; // 情感得分：0-1
        private String suggestion; // 建议

        public EmotionAnalysisResult(String emotion, double score, String suggestion) {
            this.emotion = emotion;
            this.score = score;
            this.suggestion = suggestion;
        }

        public double getScore() {
            return score;
        }
    }

    /**
     * 调用Agent API获取回复和情感分析
     * @param userInput 用户输入内容
     * @return Agent回复内容和情感分析结果
     */
    public Map<String, Object> getAiResponseWithEmotion(String userInput) {
        Map<String, Object> result = new HashMap<>();

        try {
            EmotionAnalysisResult emotionResult = analyzeEmotion(userInput);
            String aiResponse = getAiResponse(userInput, emotionResult);
            result.put("response", aiResponse);
        } catch (Exception e) {
            logger.error("Agent处理失败: {}", e.getMessage(), e);
            result.put("response", "抱歉，AI服务暂时不可用，请稍后再试。");
        }

        return result;
    }

    /**
     * 调用AI API获取回复
     * @param userInput 用户输入内容
     * @return AI回复内容
     */
    public String getAiResponse(String userInput, EmotionAnalysisResult emotionResult) {
        try {
            // 使用自定义Agent API
            if (agentId == null || agentId.isEmpty()) {
                logger.error("Agent ID未配置");
                return "抱歉，Agent ID未配置，请联系管理员。";
            }
            
            if (agentApiUrl == null || agentApiUrl.isEmpty()) {
                logger.error("Agent API URL未配置");
                return "抱歉，Agent API URL未配置，请联系管理员。";
            }
            
            return getAgentResponse(userInput);
        } catch (Exception e) {
            logger.error("AI API调用失败: {}", e.getMessage(), e);
            // 失败时返回错误信息
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    /**
     * 调用自定义Agent API获取回复
     * @param userInput 用户输入内容
     * @return AI回复内容
     */
    private String getAgentResponse(String userInput) {
        try {
            // 验证Agent配置
            if (agentApiUrl == null || agentApiUrl.isEmpty()) {
                logger.error("Agent API URL未配置");
                throw new RuntimeException("Agent API URL未配置");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("百度API Key未配置");
                throw new RuntimeException("百度API Key未配置");
            }
            if (agentId == null || agentId.isEmpty()) {
                logger.error("Agent ID未配置");
                throw new RuntimeException("Agent ID未配置");
            }
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("app_id", agentId);
            requestBody.put("stream", false);
            requestBody.put("query", userInput);
            
            // 构建请求头
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            // 使用Bearer认证方式
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 构建请求实体
            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            // 发送请求
            try {
                String response = restTemplate.postForObject(agentApiUrl, requestEntity, String.class);
                
                // 解析响应
                Map<String, Object> responseMap = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
                
                if (responseMap.containsKey("answer")) {
                    String result = (String) responseMap.get("answer");
                    return result;
                } else if (responseMap.containsKey("result")) {
                    String result = (String) responseMap.get("result");
                    return result;
                } else if (responseMap.containsKey("error")) {
                    // 处理错误响应
                    Map<?, ?> errorMap = (Map<?, ?>) responseMap.get("error");
                    String errorMessage = errorMap.containsKey("message") ? (String) errorMap.get("message") : "未知错误";
                    logger.error("API返回错误: {}", errorMessage);
                    throw new RuntimeException("API调用失败: " + errorMessage);
                } else {
                    logger.error("响应格式不正确，没有answer、result或error字段");
                    throw new RuntimeException("获取AI回复失败，响应格式不正确");
                }
            } catch (Exception e) {
                logger.error("API调用过程中发生异常: {}", e.getMessage(), e);
                throw e;
            }

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("网络连接失败: {}", e.getMessage(), e);
            throw new RuntimeException("网络连接失败，请检查网络设置或Agent API地址", e);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("HTTP请求错误: {}", e.getStatusCode() + " - " + e.getStatusText(), e);
            
            if (e.getStatusCode().value() == 401) {
                logger.error("认证失败，请检查API Key是否正确");
                throw new RuntimeException("认证失败，请检查API Key是否正确", e);
            } else if (e.getStatusCode().value() == 403) {
                logger.error("权限不足，请检查API Key是否有调用Agent的权限");
                throw new RuntimeException("权限不足，请检查API Key是否有调用Agent的权限", e);
            } else if (e.getStatusCode().value() == 404) {
                logger.error("Agent API地址不存在，请检查Agent API URL是否正确");
                throw new RuntimeException("Agent API地址不存在，请检查Agent API URL是否正确", e);
            } else {
                throw new RuntimeException("HTTP请求错误: " + e.getStatusCode() + " - " + e.getStatusText(), e);
            }
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            logger.error("服务器错误: {}", e.getStatusCode() + " - " + e.getStatusText(), e);
            throw new RuntimeException("服务器错误: " + e.getStatusCode() + " - " + e.getStatusText(), e);
        } catch (Exception e) {
            logger.error("Agent API调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("Agent API调用失败", e);
        }
    }

    /**
     * 情感分析
     * @param text 待分析文本
     * @return 情感分析结果
     */
    public EmotionAnalysisResult analyzeEmotion(String text) {
        try {
            // 集成百度智能云情感分析API
            // 暂时使用基于关键词的情感分析
            return analyzeEmotionBasedOnKeywords(text);
            
        } catch (Exception e) {
            logger.error("情感分析失败: {}", e.getMessage(), e);
            // 失败时返回中性情感
            return new EmotionAnalysisResult("neutral", 0.5, "保持积极的心态");
        }
    }

    /**
     * 基于关键词的情感分析
     * @param text 待分析文本
     * @return 情感分析结果
     */
    private EmotionAnalysisResult analyzeEmotionBasedOnKeywords(String text) {
        // 基于关键词的情感分析逻辑
        text = text.toLowerCase();
        
        // 消极词汇
        String[] negativeWords = {"难过", "伤心", "痛苦", "绝望", "沮丧", "焦虑", "压力", "孤独", "害怕", "生气", "愤怒", "失望", "悲伤"};
        // 积极词汇
        String[] positiveWords = {"开心", "快乐", "高兴", "幸福", "兴奋", "激动", "感激", "希望", "满足", "自信", "温暖", "美好"};
        
        int negativeCount = 0;
        int positiveCount = 0;
        
        for (String word : negativeWords) {
            if (text.contains(word)) {
                negativeCount++;
            }
        }
        
        for (String word : positiveWords) {
            if (text.contains(word)) {
                positiveCount++;
            }
        }
        
        String emotion;
        double score;
        String suggestion;
        
        if (negativeCount > positiveCount) {
            emotion = "negative";
            score = Math.min(1.0, 0.5 + negativeCount * 0.1);
            suggestion = "我理解你现在的感受，一切都会好起来的。建议你可以尝试深呼吸、听音乐或者与朋友交流来缓解情绪。";
        } else if (positiveCount > negativeCount) {
            emotion = "positive";
            score = Math.min(1.0, 0.5 + positiveCount * 0.1);
            suggestion = "很高兴看到你这么积极！保持这种良好的心态，继续享受美好的生活。";
        } else {
            emotion = "neutral";
            score = 0.5;
            suggestion = "保持平衡的心态，积极面对生活中的各种挑战。";
        }
        
        return new EmotionAnalysisResult(emotion, score, suggestion);
    }


}