package com.myk.emotionalHole.service;

import com.myk.emotionalHole.entity.SensitiveWord;
import com.myk.emotionalHole.util.ContentAuditUtils;
import com.myk.emotionalHole.util.CrisisDetectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容风险评估服务
 * 三级评估：关键词检测（心理危机/否定词/求助信号）→ AI内容审核 → 综合打分
 * 风险等级：NONE(0) / LOW(1-3) / MEDIUM(4-6) / HIGH(7-10)
 * 输出动作：PUBLISH / PENDING / REJECT
 */
@Service
public class RiskAssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(RiskAssessmentService.class);

    @Autowired
    private ContentAuditUtils contentAuditUtils;

    @Resource
    private SensitiveWordService sensitiveWordService;

    private List<String> severeCrisisKeywords = new ArrayList<>();
    private List<String> moderateCrisisKeywords = new ArrayList<>();

    public static final int KEYWORD_TYPE_CRISIS_SEVERE = 2;
    public static final int KEYWORD_TYPE_CRISIS_MODERATE = 3;
    public static final int KEYWORD_TYPE_NEGATION = 4;
    public static final int KEYWORD_TYPE_HELP = 5;

    @PostConstruct
    public void init() {
        loadCrisisKeywords();
    }

    /** 从数据库加载心理危机关键词到CrisisDetectionUtils（严重级/中等级/否定词/求助信号） */
    public void loadCrisisKeywords() {
        try {
            severeCrisisKeywords = sensitiveWordService.getSensitiveWordsByType(KEYWORD_TYPE_CRISIS_SEVERE)
                    .stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());

            moderateCrisisKeywords = sensitiveWordService.getSensitiveWordsByType(KEYWORD_TYPE_CRISIS_MODERATE)
                    .stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());

            List<String> negationWords = sensitiveWordService.getSensitiveWordsByType(KEYWORD_TYPE_NEGATION)
                    .stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());

            List<String> helpSignals = sensitiveWordService.getSensitiveWordsByType(KEYWORD_TYPE_HELP)
                    .stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());

            CrisisDetectionUtils.loadAllKeywords(severeCrisisKeywords, moderateCrisisKeywords, 
                                                negationWords, helpSignals);

            logger.info("心理危机关键词加载成功 - 严重级: {} 个, 中等级: {} 个, 否定词: {} 个, 求助信号: {} 个",
                    severeCrisisKeywords.size(), moderateCrisisKeywords.size(), 
                    negationWords.size(), helpSignals.size());
        } catch (Exception e) {
            logger.error("加载心理危机关键词失败: {}", e.getMessage());
        }
    }

    /** 风险等级枚举：NONE→LOW→MEDIUM→HIGH */
    public enum RiskLevel {
        NONE(0),
        LOW(1),
        MEDIUM(2),
        HIGH(3);

        private final int level;

        RiskLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /** 评估结果：风险分数、等级、检测到的关键词、动作指令 */
    public static class AssessmentResult {
        private int riskScore;
        private RiskLevel riskLevel;
        private List<String> riskKeywords;
        private String action;

        public int getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(int riskScore) {
            this.riskScore = riskScore;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
        }

        public List<String> getRiskKeywords() {
            return riskKeywords;
        }

        public void setRiskKeywords(List<String> riskKeywords) {
            this.riskKeywords = riskKeywords;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }

    /** 综合风险评估：关键词检测 + AI审核 → 风险分数 → 等级 → 动作决策 */
    public AssessmentResult assessRisk(String content) {
        AssessmentResult result = new AssessmentResult();
        List<String> riskKeywords = new ArrayList<>();
        int riskScore;

        if (content == null || content.isEmpty()) {
            result.setRiskScore(0);
            result.setRiskLevel(RiskLevel.NONE);
            result.setRiskKeywords(riskKeywords);
            result.setAction("PUBLISH");
            return result;
        }

        // 使用 CrisisDetectionUtils 进行风险检测（包含否定词分析）
        CrisisDetectionUtils.DetectionResult detectionResult = CrisisDetectionUtils.detectCrisis(content);
        
        // 获取检测到的关键词
        riskKeywords = detectionResult.getAllDetectedKeywords();
        riskScore = detectionResult.getRiskScore();

        // AI内容审核
        ContentAuditUtils.AuditResult auditResult = contentAuditUtils.auditContent(content);
        if (auditResult == ContentAuditUtils.AuditResult.SUSPECT) {
            riskScore += 3;
            riskKeywords.add("AI疑似违规");
        } else if (auditResult == ContentAuditUtils.AuditResult.REJECT) {
            riskScore += 5;
            riskKeywords.add("AI确认违规");
        }

        // 确保分数在 0-10 范围内
        int finalScore = Math.min(Math.max(riskScore, 0), 10);
        
        // 根据分数确定风险等级
        RiskLevel riskLevel;
        if (finalScore == 0) {
            riskLevel = RiskLevel.NONE;
        } else if (finalScore <= 3) {
            riskLevel = RiskLevel.LOW;
        } else if (finalScore <= 6) {
            riskLevel = RiskLevel.MEDIUM;
        } else {
            riskLevel = RiskLevel.HIGH;
        }

        result.setRiskScore(finalScore);
        result.setRiskLevel(riskLevel);
        result.setRiskKeywords(riskKeywords);
        result.setAction(getAction(riskLevel));

        return result;
    }

    /** 根据风险等级返回动作指令 */
    private String getAction(RiskLevel riskLevel) {
        switch (riskLevel) {
            case MEDIUM:
                return "PENDING";
            case HIGH:
                return "REJECT";
            default:
                return "PUBLISH";
        }
    }
}