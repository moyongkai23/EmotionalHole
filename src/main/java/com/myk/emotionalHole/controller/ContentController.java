package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;

import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.service.*;
import com.myk.emotionalHole.util.ExceptionUtils;
import com.myk.emotionalHole.util.PageUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 内容管理控制器
 * 处理树洞内容的发布、查询、删除等操作
 * 匿名ID从JWT中获取，确保用户身份安全
 */
@RestController
@RequestMapping("content")
public class ContentController {

    @Resource
    private ContentService contentService;

    @Resource
    private LikeService likeService;

    @Resource
    private HugService hugService;

    @Resource
    private CommentService commentService;

    /**
     * 发布内容
     */
    @PostMapping("/publish")
    public Result<Content> publishContent(@RequestBody Content content, HttpServletRequest request) {
        try {
            // 从JWT中获取用户身份，覆盖客户端传入的anonymousId
            String anonymousId = (String) request.getAttribute("anonymousId");
            content.setAnonymousId(anonymousId);
            return contentService.publishContent(content);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取内容列表
     */
    @GetMapping("/list")
    public Result<List<Content>> getContentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            // 使用PageUtils处理分页参数
            PageUtils.PageParam pageParam = PageUtils.createPageParam(page, pageSize);
            // 调用service层获取内容列表
            return contentService.getContentList(pageParam.getPageNum(), pageParam.getPageSize());
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取内容详情
     */
    @GetMapping("/detail/{id}")
    public Result<Content> getContentById(@PathVariable Long id) {
        try {
            // 调用service层获取内容详情
            return contentService.getContentById(id);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 更新内容
     */
    @PutMapping("/update")
    public Result<Content> updateContent(@RequestBody Content content, HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            content.setAnonymousId(anonymousId);
            return contentService.updateContent(content);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 删除内容
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return contentService.deleteContent(id, anonymousId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 更新内容状态（管理员使用）
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateContentStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            // 调用service层更新内容状态
            return contentService.updateContentStatus(id, status);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 搜索内容（模糊查询）
     */
    @GetMapping("/search")
    public Result<List<Content>> searchContent(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            // 调用service层搜索内容
            return contentService.searchContent(keyword, page, pageSize);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取用户发布的内容列表（分页）
     */
    @GetMapping("/my-posts")
    public Result<List<Content>> getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return contentService.getContentByAnonymousId(page, pageSize, anonymousId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 管理员内容列表（支持多条件筛选）
     */
    @GetMapping("/admin/list")
    public Result<?> getAdminContentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            // 调用service层获取管理员内容列表
            return contentService.getAdminContentList(page, pageSize, status, keyword, startTime, endTime);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 管理员内容详情
     */
    @GetMapping("/admin/detail/{id}")
    public Result<Content> getAdminContentDetail(@PathVariable Long id) {
        try {
            // 调用service层获取管理员内容详情
            return contentService.getAdminContentDetail(id);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 内容审核
     */
    @PutMapping("/admin/audit/{id}")
    public Result<Void> auditContent(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            // 调用service层进行内容审核
            return contentService.auditContent(id, status);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 内容下架
     */
    @PutMapping("/admin/remove/{id}")
    public Result<Void> removeContent(@PathVariable Long id) {
        try {
            // 调用service层进行内容下架
            return contentService.removeContent(id);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 管理员删除内容（兼容前端路径）
     */
    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> deleteContentByAdmin(@PathVariable Long id) {
        try {
            // 调用service层删除内容
            return contentService.deleteContentByAdmin(id);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 管理员更新内容状态（兼容前端路径）
     */
    @PutMapping("/admin/updateStatus")
    public Result<Void> updateContentStatusByAdmin(@RequestBody java.util.Map<String, Object> request) {
        try {
            Long contentId = ((Number) request.get("contentId")).longValue();
            Integer status = (Integer) request.get("status");
            
            // 调用service层更新内容状态
            return contentService.updateContentStatus(contentId, status);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 内容统计分析
     */
    @GetMapping("/admin/statistics")
    public Result<List<java.util.Map<String, Object>>> getContentStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            // 调用service层获取内容统计分析
            return contentService.getContentStatistics(startTime, endTime);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 按日期统计内容发布数量
     */
    @GetMapping("/admin/statistics/date")
    public Result<List<java.util.Map<String, Object>>> getContentStatisticsByDate(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            // 调用service层获取按日期统计的内容发布数量
            return contentService.getContentStatisticsByDate(startTime, endTime);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 统计全量内容总数
     */
    @GetMapping("/admin/total")
    public Result<Integer> getContentTotal() {
        try {
            // 调用service层统计全量内容总数
            return contentService.countAllContent();
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 按话题获取内容列表
     */
    @GetMapping("/list/category")
    public Result<List<Content>> getContentListByCategory(
            @RequestParam Long topicId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            return contentService.getContentListByCategory(page, pageSize, topicId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 批量获取内容的互动数据（合并点赞、抱抱、评论查询）
     */
    @PostMapping("/batch-interaction")
    public Result<Map<String, Object>> batchGetInteraction(@RequestBody Map<String, Object> body, HttpServletRequest httpRequest) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> idList = (List<Integer>) body.get("contentIds");
            // 优先从JWT获取，回退到请求体（公开接口兼容）
            String anonymousId = (String) httpRequest.getAttribute("anonymousId");
            if (anonymousId == null) {
                anonymousId = (String) body.get("anonymousId");
            }

            if (idList == null || idList.isEmpty()) {
                return Result.success(new HashMap<>());
            }

            List<Long> contentIds = new ArrayList<>();
            for (Integer id : idList) {
                contentIds.add(id.longValue());
            }

            Map<Long, Boolean> likeStatus = likeService.batchCheckLikeStatus(1, contentIds, anonymousId).getData();
            Map<Long, Integer> likeCount = likeService.batchGetLikeCount(1, contentIds).getData();
            Map<Long, Boolean> hugStatus = hugService.batchCheckHugStatus(1, contentIds, anonymousId).getData();
            Map<Long, Integer> hugCount = hugService.batchGetHugCount(1, contentIds).getData();
            Map<Long, Integer> commentCount = commentService.batchGetCommentCount(contentIds).getData();

            Map<String, Object> result = new HashMap<>();
            result.put("likeStatus", likeStatus);
            result.put("likeCount", likeCount);
            result.put("hugStatus", hugStatus);
            result.put("hugCount", hugCount);
            result.put("commentCount", commentCount);
            return Result.success(result);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

}