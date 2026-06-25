package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.service.RecommendationEvaluationService;
import com.myk.emotionalHole.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 推荐系统评估Controller
 * 提供推荐效果评估相关的API接口
 */
@RestController
@RequestMapping("/api/recommendation/evaluation")
public class RecommendationEvaluationController {

    @Autowired
    private RecommendationEvaluationService evaluationService;

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 获取推荐内容的ID列表
     * @param anonymousId 用户匿名ID
     * @param limit 推荐数量限制
     * @return 推荐内容的ID列表
     */
    private List<Long> getRecommendationIds(String anonymousId, int limit) {
        List<Long> recommendations = new ArrayList<>();
        var contents = recommendationService.recommend(anonymousId, limit);
        for (var content : contents) {
            recommendations.add(content.getId());
        }
        return recommendations;
    }

    /**
     * 获取当前用户的推荐评估报告
     */
    @GetMapping("/report")
    public Result<Map<String, Object>> getEvaluationReport(
            @RequestParam("anonymousId") String anonymousId,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            List<Long> recommendations = getRecommendationIds(anonymousId, limit);

            Map<String, Double> metrics = evaluationService.generateEvaluationReport(anonymousId, recommendations);

            Map<String, Object> report = new HashMap<>();
            report.put("anonymousId", anonymousId);
            report.put("recommendationCount", recommendations.size());
            report.put("metrics", metrics);
            report.put("recommendations", recommendations);
            report.put("generateTime", new Date());

            return Result.success(report);
        } catch (Exception e) {
            return Result.error("生成评估报告失败: " + e.getMessage());
        }
    }

    /**
     * 计算准确率
     */
    @GetMapping("/precision")
    public Result<Map<String, Object>> calculatePrecision(
            @RequestParam("anonymousId") String anonymousId,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            List<Long> recommendations = getRecommendationIds(anonymousId, limit);
            double precision = evaluationService.calculatePrecision(anonymousId, recommendations);

            Map<String, Object> result = new HashMap<>();
            result.put("precision", precision);
            result.put("precisionPercent", String.format("%.2f%%", precision * 100));
            result.put("recommendationCount", recommendations.size());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("计算准确率失败: " + e.getMessage());
        }
    }

    /**
     * 计算召回率
     */
    @GetMapping("/recall")
    public Result<Map<String, Object>> calculateRecall(
            @RequestParam("anonymousId") String anonymousId,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            List<Long> recommendations = getRecommendationIds(anonymousId, limit);
            double recall = evaluationService.calculateRecall(anonymousId, recommendations);

            Map<String, Object> result = new HashMap<>();
            result.put("recall", recall);
            result.put("recallPercent", String.format("%.2f%%", recall * 100));
            result.put("recommendationCount", recommendations.size());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("计算召回率失败: " + e.getMessage());
        }
    }

    /**
     * 计算F1分数
     */
    @GetMapping("/f1")
    public Result<Map<String, Object>> calculateF1(
            @RequestParam("anonymousId") String anonymousId,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            List<Long> recommendations = getRecommendationIds(anonymousId, limit);
            double precision = evaluationService.calculatePrecision(anonymousId, recommendations);
            double recall = evaluationService.calculateRecall(anonymousId, recommendations);
            double f1Score = evaluationService.calculateF1Score(precision, recall);

            Map<String, Object> result = new HashMap<>();
            result.put("precision", precision);
            result.put("recall", recall);
            result.put("f1Score", f1Score);
            result.put("f1ScorePercent", String.format("%.2f%%", f1Score * 100));

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("计算F1分数失败: " + e.getMessage());
        }
    }

    /**
     * 计算多样性
     */
    @GetMapping("/diversity")
    public Result<Map<String, Object>> calculateDiversity(
            @RequestParam("anonymousId") String anonymousId,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            List<Long> recommendations = getRecommendationIds(anonymousId, limit);
            double diversity = evaluationService.calculateDiversity(recommendations);

            Map<String, Object> result = new HashMap<>();
            result.put("diversity", diversity);
            result.put("diversityPercent", String.format("%.2f%%", diversity * 100));
            result.put("recommendationCount", recommendations.size());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("计算多样性失败: " + e.getMessage());
        }
    }

    /**
     * 计算新颖度
     */
    @GetMapping("/novelty")
    public Result<Map<String, Object>> calculateNovelty(
            @RequestParam("anonymousId") String anonymousId,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            List<Long> recommendations = getRecommendationIds(anonymousId, limit);
            double novelty = evaluationService.calculateNovelty(anonymousId, recommendations);

            Map<String, Object> result = new HashMap<>();
            result.put("novelty", novelty);
            result.put("noveltyPercent", String.format("%.2f%%", novelty * 100));
            result.put("recommendationCount", recommendations.size());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("计算新颖度失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户喜欢的内容列表
     */
    @GetMapping("/liked-contents")
    public Result<Map<String, Object>> getUserLikedContents(
            @RequestParam("anonymousId") String anonymousId) {

        try {
            Set<Long> likedContents = evaluationService.getUserLikedContents(anonymousId);

            Map<String, Object> result = new HashMap<>();
            result.put("anonymousId", anonymousId);
            result.put("likedCount", likedContents.size());
            result.put("likedContentIds", likedContents);

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取用户喜欢内容失败: " + e.getMessage());
        }
    }

    /**
     * 系统整体评估报告（管理员接口）
     */
    @GetMapping("/system-report")
    public Result<Map<String, Object>> getSystemEvaluationReport(
            @RequestParam(defaultValue = "20") int sampleSize,
            @RequestParam(defaultValue = "20") int recommendationLimit) {

        try {
            // 注意：这里需要获取所有用户ID列表
            // 实际使用时应该从数据库查询活跃用户
            // 这里返回示例数据

            Map<String, Object> report = new HashMap<>();
            report.put("sampleSize", sampleSize);
            report.put("recommendationLimit", recommendationLimit);
            report.put("note", "系统整体评估需要批量用户数据，建议通过定时任务定期生成报告");
            report.put("generateTime", new Date());

            return Result.success(report);
        } catch (Exception e) {
            return Result.error("生成系统评估报告失败: " + e.getMessage());
        }
    }

}
