package com.myk.emotionalHole.service;

import java.util.List;
import java.util.Set;

/**
 * 推荐系统评估服务接口
 * 用于计算推荐算法的离线评估指标
 */
public interface RecommendationEvaluationService {

    /**
     * 计算准确率 (Precision)
     * Precision = 推荐中用户喜欢的内容数 / 总推荐数
     *
     * @param anonymousId 用户匿名ID
     * @param recommendations 推荐内容ID列表
     * @return 准确率 (0-1之间)
     */
    double calculatePrecision(String anonymousId, List<Long> recommendations);

    /**
     * 计算召回率 (Recall)
     * Recall = 推荐中用户喜欢的内容数 / 用户喜欢的总内容数
     *
     * @param anonymousId 用户匿名ID
     * @param recommendations 推荐内容ID列表
     * @return 召回率 (0-1之间)
     */
    double calculateRecall(String anonymousId, List<Long> recommendations);

    /**
     * 计算F1分数
     * F1 = 2 × (Precision × Recall) / (Precision + Recall)
     *
     * @param precision 准确率
     * @param recall 召回率
     * @return F1分数
     */
    double calculateF1Score(double precision, double recall);

    /**
     * 计算多样性 (Diversity)
     * 基于标签的多样性计算
     *
     * @param recommendations 推荐内容ID列表
     * @return 多样性分数
     */
    double calculateDiversity(List<Long> recommendations);

    /**
     * 计算新颖度 (Novelty)
     * 推荐内容的新颖程度
     *
     * @param anonymousId 用户匿名ID
     * @param recommendations 推荐内容ID列表
     * @return 新颖度分数
     */
    double calculateNovelty(String anonymousId, List<Long> recommendations);

    /**
     * 获取用户喜欢的内容ID集合
     * 基于点赞行为
     *
     * @param anonymousId 用户匿名ID
     * @return 喜欢的内容ID集合
     */
    Set<Long> getUserLikedContents(String anonymousId);

    /**
     * 综合评估报告
     * 一次性计算所有评估指标
     *
     * @param anonymousId 用户匿名ID
     * @param recommendations 推荐内容ID列表
     * @return 评估结果Map
     */
    java.util.Map<String, Double> generateEvaluationReport(String anonymousId, List<Long> recommendations);

}
