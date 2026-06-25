package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper {

    /**
     * 新增评论
     */
    int insertComment(Comment comment);

    /**
     * 删除评论
     */
    int deleteComment(Long id);

    /**
     * 根据内容ID查询评论
     */
    List<Comment> getCommentsByContentId(Long contentId);

    /**
     * 根据父评论ID查询子评论
     */
    List<Comment> getCommentsByParentId(Long parentId);

    /**
     * 根据ID查询评论
     */
    Comment getCommentById(Long id);

    /**
     * 根据匿名ID查询评论列表
     */
    List<Comment> getCommentsByAnonymousId(String anonymousId);

    /**
     * 根据匿名ID查询评论列表（支持分页）
     */
    List<Comment> getCommentsByAnonymousIdWithPage(
            @Param("anonymousId") String anonymousId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /**
     * 更新评论状态
     */
    int updateCommentStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新评论点赞数
     */
    int updateCommentLikeNum(@Param("id") Long id, @Param("likeNum") Integer likeNum);

    /**
     * 获取所有评论（支持分页和筛选）
     */
    List<Comment> getAllCommentsWithPage(
            @Param("offset") int offset,
            @Param("pageSize") int pageSize,
            @Param("status") String status,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    /**
     * 获取评论总数
     */
    int getCommentCount(
            @Param("status") String status,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    /**
     * 获取评论统计数据
     */
    List<java.util.Map<String, Object>> getCommentStatistics();

    /**
     * 批量获取评论数量
     */
    List<Map<String, Object>> batchGetCommentCount(@Param("contentIds") List<Long> contentIds);

    /**
     * 根据匿名ID批量更新评论状态（用于封禁用户时下架其评论）
     */
    int updateCommentStatusByAnonymousId(@Param("anonymousId") String anonymousId, @Param("status") Integer status);

    /**
     * 根据内容ID删除所有评论
     */
    int deleteByContentId(@Param("contentId") Long contentId);

}
