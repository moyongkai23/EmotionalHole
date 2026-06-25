package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Comment;
import com.myk.emotionalHole.service.CommentService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 评论管理控制器
 */
@RestController
@RequestMapping("comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 发布评论
     */
    @PostMapping("/publish")
    public Result<Comment> publishComment(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            if (comment == null || comment.getCommentText() == null || comment.getCommentText().trim().isEmpty()) {
                return Result.error(400, "评论内容不能为空");
            }
            if (comment.getContentId() == null) {
                return Result.error(400, "内容ID不能为空");
            }
            // 从JWT获取用户身份
            String anonymousId = (String) request.getAttribute("anonymousId");
            comment.setAnonymousId(anonymousId);
            return commentService.publishComment(comment);
        } catch (Exception e) {
            return Result.error(500, "发布评论失败，请重试");
        }
    }

    /**
     * 获取内容的评论列表
     */
    @GetMapping("/list/{contentId}")
    public Result<List<Comment>> getCommentsByContentId(@PathVariable Long contentId) {
        try {
            // 参数校验
            if (contentId == null) {
                return Result.error(400, "内容ID不能为空");
            }

            // 调用service层获取评论列表
            return commentService.getCommentsByContentId(contentId);
        } catch (Exception e) {
            return Result.error(500, "获取评论列表失败");
        }
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/detail/{id}")
    public Result<Comment> getCommentById(@PathVariable Long id) {
        try {
            // 调用service层获取评论详情
            return commentService.getCommentById(id);
        } catch (Exception e) {
            return Result.error(500, "获取评论详情失败");
        }
    }



    /**
     * 获取评论的回复列表
     */
    @GetMapping("/replies/{parentId}")
    public Result<List<Comment>> getCommentsByParentId(@PathVariable Long parentId) {
        try {
            // 参数校验
            if (parentId == null) {
                return Result.error(400, "父评论ID不能为空");
            }

            // 调用service层获取回复列表
            return commentService.getCommentsByParentId(parentId);
        } catch (Exception e) {
            return Result.error(500, "获取回复列表失败");
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            if (id == null) {
                return Result.error(400, "评论ID不能为空");
            }
            String anonymousId = (String) request.getAttribute("anonymousId");
            return commentService.deleteComment(id, anonymousId);
        } catch (Exception e) {
            return Result.error(500, "删除评论失败");
        }
    }

    /**
     * 更新评论状态（管理员使用）
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateCommentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            // 参数校验
            if (id == null) {
                return Result.error(400, "评论ID不能为空");
            }
            if (status == null || (!"0".equals(status) && !"1".equals(status) && !"2".equals(status))) {
                return Result.error(400, "状态值无效");
            }

            // 调用service层更新评论状态
            return commentService.updateCommentStatus(id, status);
        } catch (Exception e) {
            return Result.error(500, "更新评论状态失败");
        }
    }

    /**
     * 更新评论点赞数
     */
    @PutMapping("/like/{id}")
    public Result<Void> updateCommentLikeNum(
            @PathVariable Long id,
            @RequestParam Integer likeNum) {
        try {
            // 参数校验
            if (id == null) {
                return Result.error(400, "评论ID不能为空");
            }
            if (likeNum == null || likeNum < 0) {
                return Result.error(400, "点赞数无效");
            }

            // 调用service层更新评论点赞数
            return commentService.updateCommentLikeNum(id, likeNum);
        } catch (Exception e) {
            return Result.error(500, "更新评论点赞数失败");
        }
    }

    /**
     * 获取用户发布的评论列表（支持分页）
     */
    @GetMapping("/my-comments")
    public Result<List<Comment>> getMyComments(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }
            return commentService.getCommentsByAnonymousId(anonymousId, page, pageSize);
        } catch (Exception e) {
            return Result.error(500, "获取评论列表失败");
        }
    }

    /**
     * 获取所有评论（管理员使用，支持分页和筛选）
     */
    @GetMapping("/admin/list")
    public Result<java.util.Map<String, Object>> getAllComments(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            // 参数校验
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 调用service层获取所有评论
            return commentService.getAllComments(page, pageSize, status, startTime, endTime);
        } catch (Exception e) {
            return Result.error(500, "获取评论列表失败");
        }
    }

    /**
     * 管理员删除评论（无权限限制）
     */
    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> adminDeleteComment(@PathVariable Long id) {
        try {
            // 参数校验
            if (id == null) {
                return Result.error(400, "评论ID不能为空");
            }

            // 调用service层删除评论
            return commentService.adminDeleteComment(id);
        } catch (Exception e) {
            return Result.error(500, "删除评论失败");
        }
    }

    /**
     * 获取评论统计分析
     */
    @GetMapping("/admin/statistics")
    public Result<java.util.Map<String, Object>> getCommentStatistics() {
        try {
            // 调用service层获取评论统计分析
            return commentService.getCommentStatistics();
        } catch (Exception e) {
            return Result.error(500, "获取评论统计失败");
        }
    }

    /**
     * 批量获取评论数量
     */
    @PostMapping("/batch-count")
    public Result<Map<Long, Integer>> batchGetCommentCount(@RequestBody List<Long> contentIds) {
        try {
            return commentService.batchGetCommentCount(contentIds);
        } catch (Exception e) {
            return Result.error(500, "批量获取评论数量失败");
        }
    }

}
