package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.service.RecommendationService;
import com.myk.emotionalHole.util.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 推荐功能控制器
 * 提供个性化推荐、热门推荐、相关推荐等接口
 */
@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    @Resource
    private RecommendationService recommendationService;

    /**
     * 获取个性化推荐内容
     * 采用三路优先级填充策略：相似度 → 热门 → 随机
     *
     * @param anonymousId 用户匿名ID（从请求头或参数获取）
     * @param limit 推荐数量（默认20条）
     * @return 推荐内容列表
     */
    @GetMapping("/personalized")
    public Result<List<Content>> getPersonalizedRecommendations(
            @RequestParam String anonymousId,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Content> recommendations = recommendationService.recommend(anonymousId, limit);
            return Result.success(recommendations);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 按分类获取个性化推荐内容
     *
     * @param anonymousId 用户匿名ID
     * @param categoryId 分类ID
     * @param limit 推荐数量（默认20条）
     * @return 推荐内容列表
     */
    @GetMapping("/personalized/category")
    public Result<List<Content>> getPersonalizedRecommendationsByCategory(
            @RequestParam String anonymousId,
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Content> recommendations = recommendationService.recommendByCategory(anonymousId, categoryId, limit);
            return Result.success(recommendations);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取相关内容推荐
     * 基于当前内容的分类相似度推荐
     *
     * @param contentId 当前内容ID
     * @param limit 推荐数量（默认10条）
     * @return 相关内容列表
     */
    @GetMapping("/related/{contentId}")
    public Result<List<Content>> getRelatedContents(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Content> relatedContents = recommendationService.getRelatedContents(contentId, limit);
            return Result.success(relatedContents);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 记录用户行为
     * 用于收集用户反馈，优化推荐效果
     *
     * @param request 请求体，包含 anonymousId, contentId, behaviorType
     * @return 操作结果
     */
    @PostMapping("/behavior")
    public Result<Void> recordBehavior(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            Long contentId = ((Number) body.get("contentId")).longValue();
            Integer behaviorType = (Integer) body.get("behaviorType");
            recommendationService.recordBehavior(anonymousId, contentId, behaviorType);
            return Result.success();
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 批量记录用户行为
     * 用于前端批量上报用户浏览行为
     */
    @PostMapping("/behavior/batch")
    public Result<Void> recordBehaviorsBatch(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> behaviors = (List<Map<String, Object>>) body.get("behaviors");

            if (behaviors != null) {
                for (Map<String, Object> behavior : behaviors) {
                    Long contentId = ((Number) behavior.get("contentId")).longValue();
                    Integer behaviorType = (Integer) behavior.get("behaviorType");
                    recommendationService.recordBehavior(anonymousId, contentId, behaviorType);
                }
            }

            return Result.success();
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取推荐原因说明
     * 用于前端展示为什么推荐此内容
     *
     * @param anonymousId 用户匿名ID
     * @param contentId 内容ID
     * @return 推荐原因文本
     */
    @GetMapping("/reason")
    public Result<String> getRecommendReason(
            @RequestParam String anonymousId,
            @RequestParam Long contentId) {
        try {
            String reason = recommendationService.getRecommendReason(anonymousId, contentId);
            return Result.success(reason);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

}
