package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.service.RiskAssessmentService;
import com.myk.emotionalHole.util.ExceptionUtils;
import com.myk.emotionalHole.util.PageUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 心理危机预警控制器
 *
 * 提供预警列表、预警详情、预警处理接口
 */
@RestController
@RequestMapping("/warning")
public class WarningController {

    private static final Logger log = LoggerFactory.getLogger(WarningController.class);

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private RiskAssessmentService riskAssessmentService;

    @GetMapping("/list")
    public Result<?> getWarningList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String warningStatus,
            @RequestParam(required = false) String riskLevel) {
        
        PageUtils.PageParam pageParam = PageUtils.createPageParam(page, pageSize);
        
        Integer warningStatusInt = parseInteger(warningStatus);
        Integer riskLevelInt = parseInteger(riskLevel);
        
        List<Content> warningList = contentMapper.getWarningList(
                pageParam.getOffset(),
                pageParam.getPageSize(),
                warningStatusInt,
                riskLevelInt
        );
        
        int total = contentMapper.countWarnings(warningStatusInt, riskLevelInt);
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", warningList);
        response.put("total", total);
        response.put("page", page);
        response.put("pageSize", pageSize);
        
        return Result.success(response);
    }
    
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/{id}")
    public Result<Content> getWarningDetail(@PathVariable Long id) {
        ExceptionUtils.checkNotNull(id, "预警ID不能为空");
        
        Content content = contentMapper.getContentById(id);
        if (content == null) {
            throw ExceptionUtils.createNotFoundException("预警记录不存在");
        }
        
        return Result.success(content);
    }

    @PutMapping("/{id}/process")
    public Result<String> processWarning(
            @PathVariable Long id,
            @RequestParam Integer warningStatus) {
        
        ExceptionUtils.checkNotNull(id, "预警ID不能为空");
        ExceptionUtils.checkNotNull(warningStatus, "预警状态不能为空");
        ExceptionUtils.checkCondition(warningStatus == 0 || warningStatus == 1, "预警状态值无效");
        
        Content content = contentMapper.getContentById(id);
        if (content == null) {
            throw ExceptionUtils.createNotFoundException("预警记录不存在");
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        
        int result = contentMapper.updateWarningStatus(id, warningStatus, now);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("处理预警失败");
        }
        
        return Result.success("处理成功");
    }

    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getWarningStatistics() {
        List<Map<String, Object>> statistics = contentMapper.getWarningStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/stats/date")
    public Result<List<Map<String, Object>>> getWarningStatisticsByDate(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        List<Map<String, Object>> statistics = contentMapper.getWarningStatisticsByDate(startTime, endTime);
        return Result.success(statistics);
    }

    @GetMapping("/top-risk")
    public Result<List<Content>> getTopRiskContents(
            @RequestParam(defaultValue = "2") Integer minRiskLevel,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Content> contents = contentMapper.getTopRiskContents(minRiskLevel, limit);
        return Result.success(contents);
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        int totalWarnings = contentMapper.countWarnings(null, null);
        int pendingWarnings = contentMapper.countWarnings(0, null);
        int completedWarnings = contentMapper.countWarnings(1, null);
        
        int highRiskCount = contentMapper.countWarnings(null, 3);
        int mediumRiskCount = contentMapper.countWarnings(null, 2);
        int lowRiskCount = contentMapper.countWarnings(null, 1);
        
        dashboard.put("totalWarnings", totalWarnings);
        dashboard.put("pendingWarnings", pendingWarnings);
        dashboard.put("completedWarnings", completedWarnings);
        dashboard.put("highRiskCount", highRiskCount);
        dashboard.put("mediumRiskCount", mediumRiskCount);
        dashboard.put("lowRiskCount", lowRiskCount);
        
        List<Content> recentWarnings = contentMapper.getTopRiskContents(1, 5);
        dashboard.put("recentWarnings", recentWarnings);
        
        return Result.success(dashboard);
    }

    /**
     * 重新评估所有内容的心理风险（回填历史数据）
     */
    @PostMapping("/reassess")
    public Result<Map<String, Object>> reassessAllContent() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 分页获取所有已发布内容
        int pageSize = 50;
        int offset = 0;
        int totalProcessed = 0;
        int riskFound = 0;
        List<Content> batch;

        do {
            batch = contentMapper.getContentList(offset, pageSize);
            if (batch == null || batch.isEmpty()) break;

            for (Content content : batch) {
                try {
                    RiskAssessmentService.AssessmentResult result =
                            riskAssessmentService.assessRisk(content.getContentText());

                    String now = LocalDateTime.now().format(formatter);
                    contentMapper.updateRiskInfo(
                            content.getId(),
                            result.getRiskLevel().getLevel(),
                            result.getRiskScore(),
                            result.getRiskKeywords() != null && !result.getRiskKeywords().isEmpty()
                                    ? String.join(",", result.getRiskKeywords()) : null,
                            result.getRiskLevel().getLevel() > 0 ? 0 : 1, // >0 risk → pending
                            now
                    );
                    totalProcessed++;
                    if (result.getRiskLevel().getLevel() > 0) {
                        riskFound++;
                    }
                } catch (Exception e) {
                    log.warn("重新评估内容 {} 失败: {}", content.getId(), e.getMessage());
                }
            }
            offset += pageSize;
        } while (batch.size() == pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("totalProcessed", totalProcessed);
        response.put("riskFound", riskFound);
        response.put("message", "已重新评估 " + totalProcessed + " 条内容，发现 " + riskFound + " 条有风险");
        return Result.success(response);
    }
}