package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.UserBehavior;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.UserBehaviorMapper;
import com.myk.emotionalHole.service.UserBehaviorService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为记录服务实现类
 *
 * 记录用户浏览/点赞/评论/抱抱行为，用于个性化推荐算法
 * 行为权重：浏览0.1、点赞0.5、抱抱0.6、评论0.8
 */
@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Resource
    private UserBehaviorMapper userBehaviorMapper;
    
    @Resource
    private ContentMapper contentMapper;

    // 行为权重配置
    private static final Map<Integer, Double> BEHAVIOR_WEIGHTS = new HashMap<>();
    static {
        BEHAVIOR_WEIGHTS.put(BEHAVIOR_VIEW, 0.1);      // 浏览
        BEHAVIOR_WEIGHTS.put(BEHAVIOR_LIKE, 0.5);      // 点赞
        BEHAVIOR_WEIGHTS.put(BEHAVIOR_COMMENT, 0.8);   // 评论
        BEHAVIOR_WEIGHTS.put(BEHAVIOR_HUG, 0.6);       // 抱抱
    }

    /** 记录用户行为（已存在则更新时间，否则新建） */
    @Override
    @Transactional
    public boolean recordBehavior(String anonymousId, Long contentId, Integer behaviorType) {
        // 检查是否已存在相同的行为记录
        UserBehavior existingBehavior = userBehaviorMapper.selectByUserAndContent(
                anonymousId, contentId, behaviorType);
        
        if (existingBehavior != null) {
            // 已存在则更新记录时间
            existingBehavior.setCreateTime(LocalDateTime.now());
            return userBehaviorMapper.update(existingBehavior) > 0;
        }

        // 创建新的行为记录
        UserBehavior behavior = new UserBehavior();
        behavior.setAnonymousId(anonymousId);
        behavior.setContentId(contentId);
        behavior.setBehaviorType(behaviorType);
        behavior.setBehaviorWeight(getBehaviorWeight(behaviorType));
        behavior.setCreateTime(LocalDateTime.now());

        return userBehaviorMapper.insert(behavior) > 0;
    }

    /**
     * 获取行为权重
     */
    private Double getBehaviorWeight(Integer behaviorType) {
        return BEHAVIOR_WEIGHTS.getOrDefault(behaviorType, 0.1);
    }

    /** 获取用户最近浏览过的内容列表（用于推荐去重） */
    @Override
    public List<Content> getRecentViewedContents(String anonymousId, int limit) {
        List<Long> contentIds = userBehaviorMapper.selectRecentViewedContentIds(anonymousId, limit);
        
        if (contentIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        List<Content> contents = new java.util.ArrayList<>();
        for (Long contentId : contentIds) {
            Content content = contentMapper.getContentById(contentId);
            if (content != null) {
                contents.add(content);
            }
        }
        
        return contents;
    }

}
