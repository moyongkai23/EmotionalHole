package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.Like;

import java.util.List;
import java.util.Map;

public interface LikeService {

    /**
     * 添加点赞
     */
    Result<Void> addLike(Like like);

    /**
     * 取消点赞
     */
    Result<Void> removeLike(int targetType, Long targetId, String anonymousId);

    /**
     * 检查点赞状态
     */
    Result<Boolean> checkLikeStatus(int targetType, Long targetId, String anonymousId);

    /**
     * 获取点赞数
     */
    Result<Integer> getLikeCount(int targetType, Long targetId);

    /**
     * 获取用户点赞列表
     */
    Result<List<Content>> getMyLikes(int page, int pageSize, String anonymousId);

    /**
     * 批量查询点赞状态
     */
    Result<Map<Long, Boolean>> batchCheckLikeStatus(int targetType, List<Long> targetIds, String anonymousId);

    /**
     * 批量获取点赞数量
     */
    Result<Map<Long, Integer>> batchGetLikeCount(int targetType, List<Long> targetIds);

}