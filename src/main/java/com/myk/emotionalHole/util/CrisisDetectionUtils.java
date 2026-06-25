package com.myk.emotionalHole.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 心理危机信号检测工具类
 * 基于关键词匹配识别用户内容中的危机信号
 * 支持否定词消歧和求助信号加权，降低误判率
 */
public class CrisisDetectionUtils {

    private static final Set<String> severeCrisisKeywords = new HashSet<>();
    private static final Set<String> moderateCrisisKeywords = new HashSet<>();
    
    private static final Set<String> negationWords = new HashSet<>();
    private static final Set<String> helpSignals = new HashSet<>();

    public static final int KEYWORD_TYPE_CRISIS_SEVERE = 2;
    public static final int KEYWORD_TYPE_CRISIS_MODERATE = 3;
    public static final int KEYWORD_TYPE_NEGATION = 4;
    public static final int KEYWORD_TYPE_HELP = 5;

    /** 加载全部危机关键词（严重级/中等级/否定词/求助信号），启动时由RiskAssessmentService调用 */
    public static void loadAllKeywords(List<String> severeWords, List<String> moderateWords,
                                      List<String> negationList, List<String> helpList) {
        severeCrisisKeywords.clear();
        moderateCrisisKeywords.clear();
        negationWords.clear();
        helpSignals.clear();
        
        if (severeWords != null) {
            severeCrisisKeywords.addAll(severeWords);
        }
        if (moderateWords != null) {
            moderateCrisisKeywords.addAll(moderateWords);
        }
        if (negationList != null) {
            negationWords.addAll(negationList);
        }
        if (helpList != null) {
            helpSignals.addAll(helpList);
        }
    }

    /** 检测文本中的心理危机信号：关键词匹配→否定词消歧→求助信号加权→计算风险分 */
    public static DetectionResult detectCrisis(String text) {
        DetectionResult result = new DetectionResult();
        List<String> detectedSevere = new ArrayList<>();
        List<String> detectedModerate = new ArrayList<>();
        boolean hasNegation = false;
        boolean hasHelpSignal = false;

        if (text == null || text.isEmpty()) {
            result.setHasCrisis(false);
            result.setRiskScore(0);
            result.setRiskLevel(RiskLevel.NONE);
            return result;
        }

        for (String keyword : negationWords) {
            if (text.contains(keyword)) {
                hasNegation = true;
                break;
            }
        }

        for (String signal : helpSignals) {
            if (text.contains(signal)) {
                hasHelpSignal = true;
                break;
            }
        }

        for (String keyword : severeCrisisKeywords) {
            if (text.contains(keyword)) {
                detectedSevere.add(keyword);
            }
        }

        for (String keyword : moderateCrisisKeywords) {
            if (text.contains(keyword)) {
                detectedModerate.add(keyword);
            }
        }

        int score = calculateScore(detectedSevere.size(), detectedModerate.size(), hasNegation, hasHelpSignal);
        
        result.setDetectedSevereKeywords(detectedSevere);
        result.setDetectedModerateKeywords(detectedModerate);
        result.setHasNegation(hasNegation);
        result.setHasHelpSignal(hasHelpSignal);
        result.setRiskScore(score);
        result.setRiskLevel(determineRiskLevel(score));
        result.setHasCrisis(score > 0);

        return result;
    }

    /** 计算风险分：严重×5 + 中等×2 - 否定词减3 + 求助信号加2，上限10 */
    private static int calculateScore(int severeCount, int moderateCount, boolean hasNegation, boolean hasHelpSignal) {
        int score = 0;
        
        score += severeCount * 5;
        score += moderateCount * 2;

        if (hasNegation) {
            score = Math.max(0, score - 3);
        }

        if (hasHelpSignal) {
            score += 2;
        }

        return Math.min(10, Math.max(0, score));
    }

    /** 根据分数判定风险等级：≥8高 / ≥5中 / >0低 / 0无 */
    private static RiskLevel determineRiskLevel(int score) {
        if (score >= 8) {
            return RiskLevel.HIGH;
        } else if (score >= 5) {
            return RiskLevel.MEDIUM;
        } else if (score > 0) {
            return RiskLevel.LOW;
        } else {
            return RiskLevel.NONE;
        }
    }

    public static class DetectionResult {
        private boolean hasCrisis;
        private int riskScore;
        private RiskLevel riskLevel;
        private List<String> detectedSevereKeywords;
        private List<String> detectedModerateKeywords;
        private boolean hasNegation;
        private boolean hasHelpSignal;

        public void setHasCrisis(boolean hasCrisis) { this.hasCrisis = hasCrisis; }

        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

        public void setDetectedSevereKeywords(List<String> detectedSevereKeywords) { this.detectedSevereKeywords = detectedSevereKeywords; }

        public void setDetectedModerateKeywords(List<String> detectedModerateKeywords) { this.detectedModerateKeywords = detectedModerateKeywords; }

        public void setHasNegation(boolean hasNegation) { this.hasNegation = hasNegation; }

        public void setHasHelpSignal(boolean hasHelpSignal) { this.hasHelpSignal = hasHelpSignal; }

        public List<String> getAllDetectedKeywords() {
            List<String> all = new ArrayList<>();
            if (detectedSevereKeywords != null) all.addAll(detectedSevereKeywords);
            if (detectedModerateKeywords != null) all.addAll(detectedModerateKeywords);
            return all;
        }
    }

    public enum RiskLevel {
        NONE(0, "无风险"),
        LOW(1, "低风险"),
        MEDIUM(2, "中风险"),
        HIGH(3, "高风险");

        private final int level;
        private final String description;

        RiskLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }

}