package com.myk.emotionalHole.service;

import com.myk.emotionalHole.entity.Content;

import java.util.List;

/**
 * 推荐服务接口
 * 基于标签的个性化推荐算法核心接口
 */
public interface RecommendationService {

    /**
     * 获取个性化推荐内容
     * 采用三路优先级填充策略：相似度 → 热门 → 随机
     *
     * @param anonymousId 用户匿名ID
     * @param limit 推荐数量
     * @return 推荐内容列表
     */
    List<Content> recommend(String anonymousId, int limit);

    /**
     * 获取个性化推荐内容（带分类筛选）
     *
     * @param anonymousId 用户匿名ID
     * @param categoryId 分类ID（可选）
     * @param limit 推荐数量
     * @return 推荐内容列表
     */
    List<Content> recommendByCategory(String anonymousId, Long categoryId, int limit);

    /**
     * 获取相关内容推荐
     *
     * @param contentId 当前内容ID
     * @param limit 推荐数量
     * @return 相关内容列表
     */
    List<Content> getRelatedContents(Long contentId, int limit);

    /**
     * 记录用户行为并更新画像
     *
     * @param anonymousId 用户匿名ID
     * @param contentId 内容ID
     * @param behaviorType 行为类型：1-浏览，2-点赞，3-评论，4-抱抱
     */
    void recordBehavior(String anonymousId, Long contentId, Integer behaviorType);

    /**
     * 获取推荐原因说明
     *
     * @param anonymousId 用户匿名ID
     * @param contentId 内容ID
     * @return 推荐原因文本
     */
    String getRecommendReason(String anonymousId, Long contentId);

}
