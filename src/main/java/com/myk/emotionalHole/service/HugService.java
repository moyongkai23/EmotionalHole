package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.Hug;

import java.util.List;
import java.util.Map;

public interface HugService {

    /**
     * 添加抱抱
     */
    Result<Void> addHug(Hug hug);

    /**
     * 取消抱抱
     */
    Result<Void> removeHug(int targetType, Long targetId, String anonymousId);

    /**
     * 检查抱抱状态
     */
    Result<Boolean> checkHugStatus(int targetType, Long targetId, String anonymousId);

    /**
     * 获取抱抱数
     */
    Result<Integer> getHugCount(int targetType, Long targetId);

    /**
     * 获取用户抱抱列表
     */
    Result<List<Content>> getMyHugs(int page, int pageSize, String anonymousId);

    /**
     * 批量查询抱抱状态
     */
    Result<Map<Long, Boolean>> batchCheckHugStatus(int targetType, List<Long> targetIds, String anonymousId);

    /**
     * 批量获取抱抱数量
     */
    Result<Map<Long, Integer>> batchGetHugCount(int targetType, List<Long> targetIds);

}
