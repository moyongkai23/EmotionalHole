package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;

import com.myk.emotionalHole.entity.Content;

import java.util.List;

/**
 * 内容服务接口
 * 定义内容的增删改查及管理员审核方法
 */
public interface ContentService {

    /**
     * 发布内容
     */
    Result<Content> publishContent(Content content);

    /**
     * 获取内容列表
     */
    Result<List<Content>> getContentList(int page, int pageSize);

    /**
     * 获取内容详情
     */
    Result<Content> getContentById(Long id);

    /**
     * 获取用户发布的内容列表（分页）
     */
    Result<List<Content>> getContentByAnonymousId(int page, int pageSize, String anonymousId);

    /**
     * 更新内容
     */
    Result<Content> updateContent(Content content);

    /**
     * 删除内容
     */
    Result<Void> deleteContent(Long id, String anonymousId);

    /**
     * 更新内容状态
     */
    Result<Void> updateContentStatus(Long id, Integer status);

    /**
     * 搜索内容（模糊查询）
     */
    Result<List<Content>> searchContent(String keyword, int page, int pageSize);

    /**
     * 管理员内容列表（支持多条件筛选）
     */
    Result<?> getAdminContentList(int page, int pageSize, Integer status, String keyword, String startTime, String endTime);

    /**
     * 管理员内容详情
     */
    Result<Content> getAdminContentDetail(Long id);

    /**
     * 内容审核
     */
    Result<Void> auditContent(Long id, Integer status);

    /**
     * 内容下架
     */
    Result<Void> removeContent(Long id);

    /**
     * 管理员删除内容
     */
    Result<Void> deleteContentByAdmin(Long id);

    /**
     * 内容统计分析
     */
    Result<List<java.util.Map<String, Object>>> getContentStatistics(String startTime, String endTime);

    /**
     * 按日期统计内容发布数量
     */
    Result<List<java.util.Map<String, Object>>> getContentStatisticsByDate(String startTime, String endTime);

    /**
     * 统计全量内容总数
     */
    Result<Integer> countAllContent();

    /**
     * 按分类获取内容列表
     */
    Result<List<Content>> getContentListByCategory(int page, int pageSize, Long categoryId);

}
