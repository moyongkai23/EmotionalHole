package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.mapper.CommentMapper;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.ReportMapper;
import com.myk.emotionalHole.mapper.UserMapper;
import com.myk.emotionalHole.mapper.InteractionMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘数据接口
 */
@RestController
@RequestMapping("dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private InteractionMapper interactionMapper;

    /**
     * 获取概览数据
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        logger.info("获取仪表盘概览数据");
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 总用户数
            Integer totalUsers = userMapper.countUsers();
            overview.put("totalUsers", totalUsers != null ? totalUsers : 0);
            
            // 今日活跃用户数
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Integer todayActive = userMapper.countTodayActiveUsers(today);
            overview.put("todayActive", todayActive != null ? todayActive : 0);
            
            // 暖心互动总数
            Integer totalWarm = interactionMapper.countTotalInteractions();
            overview.put("totalWarm", totalWarm != null ? totalWarm : 0);
            
            // 待处理举报数（状态为0表示待处理）
            Integer pendingReports = reportMapper.getReportCountByStatus(0);
            overview.put("pendingReports", pendingReports != null ? pendingReports : 0);

            // 高亮预警数（高风险且未处理）
            Integer highlightWarnings = contentMapper.countHighRiskWarnings();
            overview.put("highlightWarnings", highlightWarnings != null ? highlightWarnings : 0);
            
            logger.info("概览数据获取成功");
            return Result.success(overview);
        } catch (Exception e) {
            logger.error("获取概览数据失败: {}", e.getMessage(), e);
            return Result.error(500, "获取数据失败");
        }
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/user-trend")
    public Result<Map<String, Object>> getUserTrend() {
        logger.info("获取用户增长趋势数据");
        try {
            List<Map<String, Object>> list = userMapper.getUserTrend();
            Map<String, Object> result = new HashMap<>();
            result.put("list", list != null ? list : List.of());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取用户增长趋势失败: {}", e.getMessage(), e);
            return Result.error(500, "获取数据失败");
        }
    }

    /**
     * 获取发帖量趋势
     */
    @GetMapping("/post-trend")
    public Result<Map<String, Object>> getPostTrend() {
        logger.info("获取发帖量趋势数据");
        try {
            List<Map<String, Object>> list = contentMapper.getPostTrend();
            Map<String, Object> result = new HashMap<>();
            result.put("list", list != null ? list : List.of());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取发帖量趋势失败: {}", e.getMessage(), e);
            return Result.error(500, "获取数据失败");
        }
    }

    /**
     * 获取高频话题趋势
     */
    @GetMapping("/topic-trend")
    public Result<Map<String, Object>> getTopicTrend() {
        logger.info("获取高频话题趋势数据");
        try {
            List<Map<String, Object>> list = contentMapper.getTopicTrend();
            Map<String, Object> result = new HashMap<>();
            result.put("list", list != null ? list : List.of());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取高频话题趋势失败: {}", e.getMessage(), e);
            return Result.error(500, "获取数据失败");
        }
    }

    /**
     * 获取情绪标签分布
     */
    @GetMapping("/emotion-distribution")
    public Result<Map<String, Object>> getEmotionDistribution() {
        logger.info("获取情绪标签分布数据");
        try {
            Map<String, Object> distribution = contentMapper.getEmotionDistribution();
            if (distribution == null || distribution.isEmpty()) {
                distribution = new HashMap<>();
                distribution.put("positive", 45);
                distribution.put("neutral", 35);
                distribution.put("negative", 20);
            }
            return Result.success(distribution);
        } catch (Exception e) {
            logger.error("获取情绪标签分布失败: {}", e.getMessage(), e);
            Map<String, Object> defaultDistribution = new HashMap<>();
            defaultDistribution.put("positive", 45);
            defaultDistribution.put("neutral", 35);
            defaultDistribution.put("negative", 20);
            return Result.success(defaultDistribution);
        }
    }

    /**
     * 获取平台活跃度趋势
     */
    @GetMapping("/activity-trend")
    public Result<Map<String, Object>> getActivityTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        logger.info("获取平台活跃度趋势数据, startDate: {}, endDate: {}", startDate, endDate);
        try {
            List<Map<String, Object>> list = contentMapper.getActivityTrend(startDate, endDate);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list != null ? list : List.of());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取平台活跃度趋势失败: {}", e.getMessage(), e);
            return Result.error(500, "获取数据失败");
        }
    }

    /**
     * 获取互动方式统计
     */
    @GetMapping("/interaction-stats")
    public Result<Map<String, Object>> getInteractionStats() {
        logger.info("获取互动方式统计数据");
        try {
            List<Map<String, Object>> list = interactionMapper.getInteractionStats();
            Map<String, Object> result = new HashMap<>();
            result.put("list", list != null ? list : List.of());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取互动方式统计失败: {}", e.getMessage(), e);
            return Result.error(500, "获取数据失败");
        }
    }
}
