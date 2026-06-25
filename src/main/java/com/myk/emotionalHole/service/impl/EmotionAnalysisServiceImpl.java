package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.entity.EmotionKeyword;
import com.myk.emotionalHole.mapper.EmotionKeywordMapper;
import com.myk.emotionalHole.service.EmotionAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 情绪分析服务实现类
 *
 * 基于关键词的情绪识别：正面/中性/负面
 * 启动时从数据库加载情绪关键词库到内存
 */
@Service
@Slf4j
public class EmotionAnalysisServiceImpl implements EmotionAnalysisService {

    @Autowired
    private EmotionKeywordMapper emotionKeywordMapper;

    private Set<String> positiveKeywords = new HashSet<>();
    private Set<String> negativeKeywords = new HashSet<>();
    private Map<String, Double> keywordWeights = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            loadKeywords();
            log.info("EmotionAnalysisService initialized with {} positive keywords and {} negative keywords",
                    positiveKeywords.size(), negativeKeywords.size());
        } catch (Exception e) {
            log.error("EmotionAnalysisService initialization failed: {}", e.getMessage());
        }
    }

    private void loadKeywords() {
        positiveKeywords.clear();
        negativeKeywords.clear();
        keywordWeights.clear();

        try {
            List<EmotionKeyword> keywords = emotionKeywordMapper.getAllEnabledKeywords();
            for (EmotionKeyword keyword : keywords) {
                if (keyword.getEmotionType() == 1) {
                    positiveKeywords.add(keyword.getKeyword());
                } else if (keyword.getEmotionType() == 2) {
                    negativeKeywords.add(keyword.getKeyword());
                }
                keywordWeights.put(keyword.getKeyword(), keyword.getWeight());
            }
        } catch (Exception e) {
            log.error("加载关键词失败: {}", e.getMessage());
        }
    }

    @Override
    public int analyzeContentEmotion(String contentText, String customTags) {
        double score = calculateEmotionScore(contentText, customTags);
        if (score >= 0.65) return 1;  // 正面
        if (score >= 0.35) return 2;  // 中性
        return 3;                      // 负面
    }

    @Override
    public double calculateEmotionScore(String contentText, String customTags) {
        double score = 0.5;

        if (contentText != null && !contentText.isEmpty()) {
            score += analyzeTextEmotion(contentText);
        }

        if (customTags != null && !customTags.isEmpty()) {
            score += analyzeTagsEmotion(customTags);
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    private double analyzeTextEmotion(String text) {
        double score = 0.0;
        int matchCount = 0;

        for (String keyword : positiveKeywords) {
            if (text.contains(keyword)) {
                score += keywordWeights.getOrDefault(keyword, 1.0) * 0.1;
                matchCount++;
            }
        }

        for (String keyword : negativeKeywords) {
            if (text.contains(keyword)) {
                score -= keywordWeights.getOrDefault(keyword, 1.0) * 0.15;
                matchCount++;
            }
        }

        if (matchCount > 0) {
            score /= matchCount;
        }

        return score;
    }

    private double analyzeTagsEmotion(String customTags) {
        double score = 0.0;
        int matchCount = 0;

        String[] tags = customTags.split(",");
        for (String tag : tags) {
            tag = tag.trim().replace("#", "");
            if (tag.isEmpty()) continue;

            for (String keyword : positiveKeywords) {
                if (tag.contains(keyword)) {
                    score += keywordWeights.getOrDefault(keyword, 1.0) * 0.15;
                    matchCount++;
                }
            }

            for (String keyword : negativeKeywords) {
                if (tag.contains(keyword)) {
                    score -= keywordWeights.getOrDefault(keyword, 1.0) * 0.2;
                    matchCount++;
                }
            }
        }

        if (matchCount > 0) {
            score /= matchCount;
        }

        return score;
    }

    @Override
    public void refreshKeywordCache() {
        loadKeywords();
        log.info("Emotion keyword cache refreshed");
    }

}