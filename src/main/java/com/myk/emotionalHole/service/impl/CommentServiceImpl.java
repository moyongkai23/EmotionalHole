package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Comment;
import com.myk.emotionalHole.mapper.CommentMapper;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.HugMapper;
import com.myk.emotionalHole.mapper.LikeMapper;
import com.myk.emotionalHole.mapper.ReportMapper;
import com.myk.emotionalHole.service.CommentService;
import com.myk.emotionalHole.service.ContentService;
import com.myk.emotionalHole.service.RecommendationService;
import com.myk.emotionalHole.service.RiskAssessmentService;
import com.myk.emotionalHole.util.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 评论服务实现类
 *
 * 提供评论发布、删除、查询、批量统计等功能
 * 发布流程：参数校验→风险评估→入库→原子更新评论数
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private HugMapper hugMapper;

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ContentService contentService;

    @Resource
    private RiskAssessmentService riskAssessmentService;

    @Resource
    private RecommendationService recommendationService;

    /** 发布评论：风险评估 + 原子更新评论数 + 记录行为 */
    @Override
    @Transactional
    public Result<Comment> publishComment(Comment comment) {
        // 参数校验
        ExceptionUtils.checkNotNull(comment, "评论信息不能为空");
        ExceptionUtils.checkNotNull(comment.getCommentText(), "评论内容不能为空");
        ExceptionUtils.checkCondition(!comment.getCommentText().trim().isEmpty(), "评论内容不能为空");

        // 如果是回复评论，验证父评论是否存在
        if (comment.getParentId() != null) {
            Comment parentComment = commentMapper.getCommentById(comment.getParentId());
            ExceptionUtils.checkNotNull(parentComment, "回复的评论不存在");
            comment.setContentId(parentComment.getContentId());
        } else {
            ExceptionUtils.checkNotNull(comment.getContentId(), "内容ID不能为空");
        }

        // 风险评估与审核
        RiskAssessmentService.AssessmentResult assessmentResult = riskAssessmentService.assessRisk(comment.getCommentText());
        String status;
        switch (assessmentResult.getAction()) {
            case "REJECT":
                throw ExceptionUtils.createParamException("评论内容包含违规信息，请修改后重新发布");
            case "PENDING":
                status = "0";
                break;
            default:
                status = "1";
                break;
        }

        comment.setLikeCount(0);
        comment.setCommentStatus(Integer.parseInt(status));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        comment.setCreateTime(LocalDateTime.now().format(formatter));

        int result = commentMapper.insertComment(comment);
        ExceptionUtils.checkCondition(result > 0, 500, "发布评论失败，请重试");

        contentMapper.incrementCommentCount(comment.getContentId(), 1);

        // 记录评论行为（用于推荐算法）
        recommendationService.recordBehavior(comment.getAnonymousId(), comment.getContentId(), 3);

        String message = "1".equals(status) ? "评论成功" : "评论已提交，正在审核中";
        return Result.success(message, comment);
    }

    /** 获取指定内容的评论列表 */
    @Override
    public Result<List<Comment>> getCommentsByContentId(Long contentId) {
        ExceptionUtils.checkNotNull(contentId, "内容ID不能为空");

        List<Comment> commentList = commentMapper.getCommentsByContentId(contentId);
        List<Comment> validComments = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                if (comment != null) {
                    if (comment.getCommentText() == null) {
                        comment.setCommentText("");
                    }
                    validComments.add(comment);
                }
            }
        }
        return Result.success(validComments);
    }

    /** 根据ID获取单条评论 */
    @Override
    public Result<Comment> getCommentById(Long id) {
        ExceptionUtils.checkNotNull(id, "评论ID不能为空");

        Comment comment = commentMapper.getCommentById(id);
        ExceptionUtils.checkNotNull(comment, "评论不存在");
        return Result.success(comment);
    }

    /** 获取指定父评论下的子评论列表 */
    @Override
    public Result<List<Comment>> getCommentsByParentId(Long parentId) {
        ExceptionUtils.checkNotNull(parentId, "父评论ID不能为空");

        List<Comment> commentList = commentMapper.getCommentsByParentId(parentId);
        return Result.success(commentList);
    }

    /** 用户删除自己的评论：权限校验→级联删除子评论→原子更新评论数 */
    @Override
    @Transactional
    public Result<Void> deleteComment(Long id, String anonymousId) {
        ExceptionUtils.checkNotNull(id, "评论ID不能为空");
        ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");

        Comment existingComment = commentMapper.getCommentById(id);
        ExceptionUtils.checkNotNull(existingComment, "评论不存在");
        ExceptionUtils.checkCondition(existingComment.getAnonymousId().equals(anonymousId), 403, "无权删除此评论");

        // 级联删除子评论的关联数据
        cascadeDeleteCommentRelatedData(id);

        int result = commentMapper.deleteComment(id);
        ExceptionUtils.checkCondition(result > 0, 500, "删除评论失败");

        contentMapper.incrementCommentCount(existingComment.getContentId(), -1);
        recommendationService.recordBehavior(anonymousId, existingComment.getContentId(), -3);

        return Result.success();
    }

    /** 更新评论状态（审核通过/拒绝） */
    @Override
    @Transactional
    public Result<Void> updateCommentStatus(Long id, String status) {
        ExceptionUtils.checkNotNull(id, "评论ID不能为空");

        Comment existingComment = commentMapper.getCommentById(id);
        ExceptionUtils.checkNotNull(existingComment, "评论不存在");

        int result = commentMapper.updateCommentStatus(id, status);
        ExceptionUtils.checkCondition(result > 0, 500, "更新评论状态失败");

        return Result.success();
    }

    /** 更新评论点赞数（原子操作） */
    @Override
    @Transactional
    public Result<Void> updateCommentLikeNum(Long id, Integer likeNum) {
        ExceptionUtils.checkNotNull(id, "评论ID不能为空");

        Comment existingComment = commentMapper.getCommentById(id);
        ExceptionUtils.checkNotNull(existingComment, "评论不存在");

        int result = commentMapper.updateCommentLikeNum(id, likeNum);
        ExceptionUtils.checkCondition(result > 0, 500, "更新评论点赞数失败");

        return Result.success();
    }

    /** 分页获取指定用户的评论列表（我的评论） */
    @Override
    public Result<List<Comment>> getCommentsByAnonymousId(String anonymousId, int page, int pageSize) {
        ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");

        int offset = (page - 1) * pageSize;
        List<Comment> commentList = commentMapper.getCommentsByAnonymousIdWithPage(anonymousId, offset, pageSize);

        return Result.success(commentList);
    }

    /** 管理端评论列表：支持状态筛选、时间范围过滤 */
    @Override
    public Result<Map<String, Object>> getAllComments(int page, int pageSize, String status, String startTime, String endTime) {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        int offset = (page - 1) * pageSize;
        List<Comment> commentList = commentMapper.getAllCommentsWithPage(offset, pageSize, status, startTime, endTime);
        int total = commentMapper.getCommentCount(status, startTime, endTime);

        Map<String, Object> result = new HashMap<>();
        result.put("comments", commentList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("pages", (total + pageSize - 1) / pageSize);

        return Result.success(result);
    }

    /** 管理端删除评论：级联删除子评论及关联数据 */
    @Override
    @Transactional
    public Result<Void> adminDeleteComment(Long id) {
        ExceptionUtils.checkNotNull(id, "评论ID不能为空");

        Comment existingComment = commentMapper.getCommentById(id);
        ExceptionUtils.checkNotNull(existingComment, "评论不存在");

        // 级联删除子评论的关联数据
        cascadeDeleteCommentRelatedData(id);

        int result = commentMapper.deleteComment(id);
        ExceptionUtils.checkCondition(result > 0, 500, "删除评论失败");

        contentMapper.incrementCommentCount(existingComment.getContentId(), -1);

        return Result.success();
    }

    /** 获取评论统计数据（按状态分组） */
    @Override
    public Result<Map<String, Object>> getCommentStatistics() {
        List<Map<String, Object>> statistics = commentMapper.getCommentStatistics();

        Map<String, Object> result = new HashMap<>();
        result.put("statistics", statistics);

        long total = statistics.stream()
                .mapToLong(map -> Long.parseLong(map.get("count").toString()))
                .sum();
        result.put("total", total);

        return Result.success(result);
    }

    /**
     * 级联删除评论关联的所有数据（子评论、点赞、抱抱、举报）
     */
    private void cascadeDeleteCommentRelatedData(Long commentId) {
        // 先递归删除子评论的关联数据
        List<Comment> childComments = commentMapper.getCommentsByParentId(commentId);
        if (childComments != null) {
            for (Comment child : childComments) {
                cascadeDeleteCommentRelatedData(child.getId());
                // 删除子评论的点赞/抱抱/举报
                likeMapper.deleteByTargetTypeAndTargetId(2, child.getId());
                hugMapper.deleteByTargetTypeAndTargetId(2, child.getId());
                reportMapper.deleteByTargetTypeAndTargetId(2, child.getId());
            }
            // 批量删除子评论
            for (Comment child : childComments) {
                commentMapper.deleteComment(child.getId());
            }
        }
        // 删除当前评论的点赞/抱抱/举报
        likeMapper.deleteByTargetTypeAndTargetId(2, commentId);
        hugMapper.deleteByTargetTypeAndTargetId(2, commentId);
        reportMapper.deleteByTargetTypeAndTargetId(2, commentId);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Result<Map<Long, Integer>> batchGetCommentCount(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Result.success(new HashMap<>());
        }

        List<Map<String, Object>> resultList = commentMapper.batchGetCommentCount(contentIds);

        Map<Long, Integer> commentCountMap = new HashMap<>();
        for (Long contentId : contentIds) {
            commentCountMap.put(contentId, 0);
        }
        if (resultList != null) {
            for (Map<String, Object> row : resultList) {
                Long contentId = ((Number) row.get("contentId")).longValue();
                Integer count = ((Number) row.get("commentCount")).intValue();
                commentCountMap.put(contentId, count);
            }
        }

        return Result.success(commentCountMap);
    }

}
