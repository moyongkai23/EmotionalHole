package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.UserBehavior;

import com.myk.emotionalHole.mapper.UserBehaviorMapper;
import com.myk.emotionalHole.service.RecommendationEvaluationService;
import com.myk.emotionalHole.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 推荐系统评估服务实现类
 * 实现各种离线评估指标的计算
 */
@Service
public class RecommendationEvaluationServiceImpl implements RecommendationEvaluationService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 行为类型常量
     */
    private static final int BEHAVIOR_LIKE = 2;

    /**
     * 计算准确率 (Precision)
     * Precision = 推荐中用户喜欢的内容数 / 总推荐数
     */
    @Override
    public double calculatePrecision(String anonymousId, List<Long> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return 0.0;
        }

        Set<Long> userLikedContents = getUserLikedContents(anonymousId);

        int hitCount = 0;
        for (Long contentId : recommendations) {
            if (userLikedContents.contains(contentId)) {
                hitCount++;
            }
        }

        return (double) hitCount / recommendations.size();
    }

    /**
     * 计算召回率 (Recall)
     * Recall = 推荐中用户喜欢的内容数 / 用户喜欢的总内容数
     */
    @Override
    public double calculateRecall(String anonymousId, List<Long> recommendations) {
        Set<Long> userLikedContents = getUserLikedContents(anonymousId);

        if (userLikedContents.isEmpty()) {
            return 0.0;
        }

        int hitCount = 0;
        for (Long contentId : recommendations) {
            if (userLikedContents.contains(contentId)) {
                hitCount++;
            }
        }

        return (double) hitCount / userLikedContents.size();
    }

    /**
     * 计算F1分数
     * F1 = 2 × (Precision × Recall) / (Precision + Recall)
     */
    @Override
    public double calculateF1Score(double precision, double recall) {
        if (precision + recall == 0) {
            return 0.0;
        }
        return 2 * (precision * recall) / (precision + recall);
    }

    /**
     * 计算多样性 (Diversity)
     * 基于内容数量的简单多样性计算
     */
    @Override
    public double calculateDiversity(List<Long> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return 0.0;
        }

        // 简单计算：推荐内容的数量越多，多样性越高
        // 这里返回一个基于推荐数量的简单值
        return Math.min(1.0, (double) recommendations.size() / 10.0);
    }

    /**
     * 计算新颖度 (Novelty)
     * 基于用户历史行为计算推荐内容的新颖程度
     * 推荐内容越不在用户历史行为中，新颖度越高
     */
    @Override
    public double calculateNovelty(String anonymousId, List<Long> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return 0.0;
        }

        // 获取用户历史行为涉及的所有内容
        List<UserBehavior> userBehaviors = userBehaviorMapper.selectByAnonymousId(anonymousId);
        Set<Long> historicalContents = new HashSet<>();
        for (UserBehavior behavior : userBehaviors) {
            historicalContents.add(behavior.getContentId());
        }

        // 计算新颖度：推荐内容中不在历史行为中的比例
        int novelCount = 0;
        for (Long contentId : recommendations) {
            if (!historicalContents.contains(contentId)) {
                novelCount++;
            }
        }

        return (double) novelCount / recommendations.size();
    }

    /**
     * 获取用户喜欢的内容ID集合
     * 基于点赞行为
     */
    @Override
    public Set<Long> getUserLikedContents(String anonymousId) {
        Set<Long> likedContents = new HashSet<>();
        List<UserBehavior> behaviors = userBehaviorMapper.selectByAnonymousIdAndType(anonymousId, BEHAVIOR_LIKE);

        for (UserBehavior behavior : behaviors) {
            likedContents.add(behavior.getContentId());
        }

        return likedContents;
    }

    /**
     * 综合评估报告
     * 一次性计算所有评估指标
     */
    @Override
    public Map<String, Double> generateEvaluationReport(String anonymousId, List<Long> recommendations) {
        Map<String, Double> report = new HashMap<>();

        // 计算各项指标
        double precision = calculatePrecision(anonymousId, recommendations);
        double recall = calculateRecall(anonymousId, recommendations);
        double f1Score = calculateF1Score(precision, recall);
        double diversity = calculateDiversity(recommendations);
        double novelty = calculateNovelty(anonymousId, recommendations);

        // 存入报告
        report.put("precision", precision);
        report.put("recall", recall);
        report.put("f1Score", f1Score);
        report.put("diversity", diversity);
        report.put("novelty", novelty);

        return report;
    }

    /**
     * 批量评估多个用户的推荐效果
     * 用于系统整体效果评估
     */
    public Map<String, Double> batchEvaluate(List<String> anonymousIds, int recommendationLimit) {
        Map<String, Double> avgMetrics = new HashMap<>();
        double totalPrecision = 0.0;
        double totalRecall = 0.0;
        double totalF1 = 0.0;
        double totalDiversity = 0.0;
        double totalNovelty = 0.0;

        int validUserCount = 0;

        for (String anonymousId : anonymousIds) {
            // 获取用户的真实推荐列表
            List<Long> recommendations = getRecommendations(anonymousId, recommendationLimit);

            if (recommendations != null && !recommendations.isEmpty()) {
                Map<String, Double> userReport = generateEvaluationReport(anonymousId, recommendations);

                totalPrecision += userReport.get("precision");
                totalRecall += userReport.get("recall");
                totalF1 += userReport.get("f1Score");
                totalDiversity += userReport.get("diversity");
                totalNovelty += userReport.get("novelty");

                validUserCount++;
            }
        }

        if (validUserCount > 0) {
            avgMetrics.put("avgPrecision", totalPrecision / validUserCount);
            avgMetrics.put("avgRecall", totalRecall / validUserCount);
            avgMetrics.put("avgF1Score", totalF1 / validUserCount);
            avgMetrics.put("avgDiversity", totalDiversity / validUserCount);
            avgMetrics.put("avgNovelty", totalNovelty / validUserCount);
        }

        return avgMetrics;
    }

    /**
     * 获取推荐列表
     * 调用 RecommendationService 获取真实的个性化推荐
     */
    private List<Long> getRecommendations(String anonymousId, int limit) {
        List<Content> contents = recommendationService.recommend(anonymousId, limit);
        List<Long> recommendations = new ArrayList<>();

        for (Content content : contents) {
            recommendations.add(content.getId());
        }

        return recommendations;
    }

}
