package com.myk.emotionalHole.service;

import java.util.List;

/**
 * 用户行为记录Service接口
 */
public interface UserBehaviorService {

    /**
     * 行为类型常量
     */
    int BEHAVIOR_VIEW = 1;      // 浏览
    int BEHAVIOR_LIKE = 2;      // 点赞
    int BEHAVIOR_COMMENT = 3;   // 评论
    int BEHAVIOR_HUG = 4;       // 抱抱

    /**
     * 记录用户行为
     * @param anonymousId 用户匿名ID
     * @param contentId 内容ID
     * @param behaviorType 行为类型
     * @return 是否记录成功
     */
    boolean recordBehavior(String anonymousId, Long contentId, Integer behaviorType);

    /**
     * 获取用户最近浏览的内容列表
     * @param anonymousId 用户匿名ID
     * @param limit 数量限制
     * @return 内容列表
     */
    List<com.myk.emotionalHole.entity.Content> getRecentViewedContents(String anonymousId, int limit);

}
