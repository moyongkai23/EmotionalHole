package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.Like;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.LikeMapper;
import com.myk.emotionalHole.service.CommentService;
import com.myk.emotionalHole.service.LikeService;
import com.myk.emotionalHole.service.RecommendationService;
import com.myk.emotionalHole.util.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 点赞服务实现类
 *
 * 提供点赞/取消、状态查询、批量查询功能
 * 支持内容点赞和评论点赞两种类型
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private CommentService commentService;

    @Resource
    private RecommendationService recommendationService;

    /** 点赞：防重复 + 原子更新计数 + 记录用户行为 */
    @Override
    @Transactional
    public Result<Void> addLike(Like like) {
        // 参数校验
        ExceptionUtils.checkNotNull(like, "点赞信息不能为空");
        ExceptionUtils.checkNotNull(like.getTargetType(), "目标类型不能为空");
        ExceptionUtils.checkNotNull(like.getTargetId(), "目标ID不能为空");
        ExceptionUtils.checkNotNull(like.getAnonymousId(), "用户标识不能为空");
        ExceptionUtils.checkCondition(!like.getAnonymousId().trim().isEmpty(), "用户标识不能为空");

        // 检查是否已经点赞
        List<Like> existingLikes = likeMapper.getLikesByTarget(like.getTargetType(), like.getTargetId());
        boolean alreadyLiked = existingLikes.stream()
                .anyMatch(existingLike -> existingLike.getAnonymousId().equals(like.getAnonymousId()));
        if (alreadyLiked) {
            return Result.error(400, "已经点过赞了");
        }

        // 设置默认值
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        like.setCreateTime(LocalDateTime.now().format(formatter));

        // 插入数据库
        int result = likeMapper.insertLike(like);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("点赞失败，请重试");
        }

        // 原子更新对应内容/评论的点赞数
        int targetType = like.getTargetType();
        Long targetId = like.getTargetId();

        if (targetType == 1) {
            contentMapper.incrementLikeCount(targetId, 1);
            recommendationService.recordBehavior(like.getAnonymousId(), targetId, 2);
        } else if (targetType == 2) {
            commentService.updateCommentLikeNum(targetId, 1);
        }

        return Result.success();
    }

    /** 取消点赞：删除记录 + 原子减少计数 */
    @Override
    @Transactional
    public Result<Void> removeLike(int targetType, Long targetId, String anonymousId) {
        // 参数校验
        ExceptionUtils.checkNotNull(targetId, "目标ID不能为空");
        ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
        ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");

        // 执行删除
        int result = likeMapper.deleteLike(targetType, targetId, anonymousId);
        if (result <= 0) {
            return Result.error(400, "取消点赞失败，可能尚未点赞");
        }

        // 原子更新对应内容/评论的点赞数
        if (targetType == 1) {
            contentMapper.incrementLikeCount(targetId, -1);
            recommendationService.recordBehavior(anonymousId, targetId, -2);
        } else if (targetType == 2) {
            commentService.updateCommentLikeNum(targetId, -1);
        }

        return Result.success();
    }

    /** 查询当前用户是否已对目标点过赞 */
    @Override
    @Transactional(readOnly = true)
    public Result<Boolean> checkLikeStatus(int targetType, Long targetId, String anonymousId) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(targetId, "目标ID不能为空");
            ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
            ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");

            // 查询点赞记录
            List<Like> likes = likeMapper.getLikesByTarget(targetType, targetId);
            boolean liked = likes.stream()
                    .anyMatch(like -> like.getAnonymousId().equals(anonymousId));

            return Result.success(liked);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /** 获取目标内容的点赞总数 */
    @Override
    @Transactional(readOnly = true)
    public Result<Integer> getLikeCount(int targetType, Long targetId) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(targetId, "目标ID不能为空");

            // 查询点赞记录并统计数量
            List<Like> likes = likeMapper.getLikesByTarget(targetType, targetId);
            int count = likes.size();

            return Result.success(count);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /** 分页获取当前用户点过赞的内容列表 */
    @Override
    @Transactional(readOnly = true)
    public Result<List<Content>> getMyLikes(int page, int pageSize, String anonymousId) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
            ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");
            ExceptionUtils.checkCondition(page > 0, "页码必须大于0");
            ExceptionUtils.checkCondition(pageSize > 0 && pageSize <= 100, "每页大小必须在1-100之间");

            // 计算偏移量
            int offset = (page - 1) * pageSize;

            // 查询用户点赞的内容列表
            List<Content> contentList = likeMapper.getMyLikes(offset, pageSize, anonymousId);

            return Result.success(contentList);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /** 批量查询点赞状态（用于列表页展示） */
    @Override
    @Transactional(readOnly = true)
    public Result<Map<Long, Boolean>> batchCheckLikeStatus(int targetType, List<Long> targetIds, String anonymousId) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(targetIds, "目标ID列表不能为空");
            ExceptionUtils.checkCondition(!targetIds.isEmpty(), "目标ID列表不能为空");
            ExceptionUtils.checkCondition(targetIds.size() <= 50, "单次查询最多50个目标");
            ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
            ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");

            // 批量查询点赞记录
            List<Like> likes = likeMapper.batchGetLikeStatus(targetType, targetIds, anonymousId);

            // 构建结果映射
            Map<Long, Boolean> resultMap = new HashMap<>();
            for (Long targetId : targetIds) {
                resultMap.put(targetId, false);
            }
            for (Like like : likes) {
                resultMap.put(like.getTargetId(), true);
            }

            return Result.success(resultMap);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /** 批量获取点赞数量（用于列表页展示） */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Result<Map<Long, Integer>> batchGetLikeCount(int targetType, List<Long> targetIds) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(targetIds, "目标ID列表不能为空");
            ExceptionUtils.checkCondition(!targetIds.isEmpty(), "目标ID列表不能为空");
            ExceptionUtils.checkCondition(targetIds.size() <= 50, "单次查询最多50个目标");

            // 批量查询点赞数量（MyBatis返回List<Map>）
            List<Map<String, Object>> resultList = likeMapper.batchGetLikeCount(targetType, targetIds);

            // 构建结果映射
            Map<Long, Integer> likeCountMap = new HashMap<>();
            for (Long targetId : targetIds) {
                likeCountMap.put(targetId, 0);
            }
            
            // 遍历查询结果，填充数量
            for (Map<String, Object> row : resultList) {
                Long targetId = ((Number) row.get("targetId")).longValue();
                Integer count = ((Number) row.get("count")).intValue();
                likeCountMap.put(targetId, count);
            }

            return Result.success(likeCountMap);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

}