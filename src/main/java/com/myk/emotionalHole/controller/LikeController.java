package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.Like;
import com.myk.emotionalHole.service.LikeService;
import com.myk.emotionalHole.util.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

import com.myk.emotionalHole.dto.BatchHugStatusRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 点赞管理控制器
 */
@RestController
@RequestMapping("like")
public class LikeController {

    @Resource
    private LikeService likeService;

    /**
     * 添加点赞
     */
    @PostMapping("/add")
    public Result<Void> addLike(@RequestBody Like like, HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            like.setAnonymousId(anonymousId);
            return likeService.addLike(like);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/remove")
    public Result<Void> removeLike(
            @RequestParam int targetType,
            @RequestParam Long targetId,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return likeService.removeLike(targetType, targetId, anonymousId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 检查点赞状态
     */
    @GetMapping("/status")
    public Result<Boolean> checkLikeStatus(
            @RequestParam int targetType,
            @RequestParam Long targetId,
            @RequestParam String anonymousId) {
        try {
            // 调用service层检查点赞状态
            return likeService.checkLikeStatus(targetType, targetId, anonymousId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取点赞数
     */
    @GetMapping("/count")
    public Result<Integer> getLikeCount(
            @RequestParam int targetType,
            @RequestParam Long targetId) {
        try {
            // 调用service层获取点赞数
            return likeService.getLikeCount(targetType, targetId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取用户点赞列表
     */
    @GetMapping("/my-likes")
    public Result<List<Content>> getMyLikes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return likeService.getMyLikes(page, pageSize, anonymousId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 批量查询点赞状态
     */
    @PostMapping("/batch-status")
    public Result<Map<Long, Boolean>> batchCheckLikeStatus(@RequestBody BatchHugStatusRequest request) {
        try {
            return likeService.batchCheckLikeStatus(request.getTargetType(), request.getTargetIds(), request.getAnonymousId());
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 批量获取点赞数量
     */
    @PostMapping("/batch-count")
    public Result<Map<Long, Integer>> batchGetLikeCount(@RequestBody BatchHugStatusRequest request) {
        try {
            return likeService.batchGetLikeCount(request.getTargetType(), request.getTargetIds());
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

}