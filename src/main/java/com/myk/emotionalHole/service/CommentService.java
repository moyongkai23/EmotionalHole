package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Comment;

import java.util.List;
import java.util.Map;

public interface CommentService {

    /**
     * 发布评论
     */
    Result<Comment> publishComment(Comment comment);

    /**
     * 获取内容的评论列表
     */
    Result<List<Comment>> getCommentsByContentId(Long contentId);

    /**
     * 获取评论详情
     */
    Result<Comment> getCommentById(Long id);

    /**
     * 获取用户发布的评论列表（支持分页）
     */
    Result<List<Comment>> getCommentsByAnonymousId(String anonymousId, int page, int pageSize);

    /**
     * 获取评论的回复列表
     */
    Result<List<Comment>> getCommentsByParentId(Long parentId);

    /**
     * 删除评论
     */
    Result<Void> deleteComment(Long id, String anonymousId);

    /**
     * 更新评论状态
     */
    Result<Void> updateCommentStatus(Long id, String status);

    /**
     * 更新评论点赞数
     */
    Result<Void> updateCommentLikeNum(Long id, Integer likeNum);

    /**
     * 获取所有评论（管理员使用，支持分页和筛选）
     */
    Result<java.util.Map<String, Object>> getAllComments(int page, int pageSize, String status, String startTime, String endTime);

    /**
     * 管理员删除评论（无权限限制）
     */
    Result<Void> adminDeleteComment(Long id);

    /**
     * 获取评论统计分析
     */
    Result<java.util.Map<String, Object>> getCommentStatistics();

    /**
     * 批量获取评论数量
     */
    Result<Map<Long, Integer>> batchGetCommentCount(List<Long> contentIds);

}
