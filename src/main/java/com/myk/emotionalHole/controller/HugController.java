package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.BatchHugStatusRequest;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.Hug;
import com.myk.emotionalHole.service.HugService;
import com.myk.emotionalHole.util.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 抱抱管理控制器
 */
@RestController
@RequestMapping("hug")
public class HugController {

    @Resource
    private HugService hugService;

    /**
     * 添加抱抱
     */
    @PostMapping("/add")
    public Result<Void> addHug(@RequestBody Hug hug, HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            hug.setAnonymousId(anonymousId);
            return hugService.addHug(hug);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 取消抱抱
     */
    @DeleteMapping("/remove")
    public Result<Void> removeHug(
            @RequestParam int targetType,
            @RequestParam Long targetId,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return hugService.removeHug(targetType, targetId, anonymousId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 检查抱抱状态
     */
    @GetMapping("/status")
    public Result<Boolean> checkHugStatus(
            @RequestParam int targetType,
            @RequestParam Long targetId,
            @RequestParam String anonymousId) {
        try {
            return hugService.checkHugStatus(targetType, targetId, anonymousId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取抱抱数
     */
    @GetMapping("/count")
    public Result<Integer> getHugCount(
            @RequestParam int targetType,
            @RequestParam Long targetId) {
        try {
            return hugService.getHugCount(targetType, targetId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 获取用户抱抱列表
     */
    @GetMapping("/my-hugs")
    public Result<List<Content>> getMyHugs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return hugService.getMyHugs(page, pageSize, anonymousId);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 批量查询抱抱状态
     */
    @PostMapping("/batch-status")
    public Result<Map<Long, Boolean>> batchCheckHugStatus(@RequestBody BatchHugStatusRequest request) {
        try {
            return hugService.batchCheckHugStatus(request.getTargetType(), request.getTargetIds(), request.getAnonymousId());
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /**
     * 批量获取抱抱数量
     */
    @PostMapping("/batch-count")
    public Result<Map<Long, Integer>> batchGetHugCount(@RequestBody BatchHugStatusRequest request) {
        try {
            return hugService.batchGetHugCount(request.getTargetType(), request.getTargetIds());
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

}
