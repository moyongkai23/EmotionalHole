package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.ReportResponseDTO;
import com.myk.emotionalHole.entity.Comment;
import com.myk.emotionalHole.entity.Report;
import com.myk.emotionalHole.mapper.CommentMapper;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.ReportMapper;
import com.myk.emotionalHole.service.CommentService;
import com.myk.emotionalHole.service.ContentService;
import com.myk.emotionalHole.service.ReportService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 举报服务实现类
 *
 * 提供举报提交、列表查询、举报处理功能
 * 流程：用户提交举报 → 管理员审核处理（下架/忽略）
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ContentService contentService;

    @Resource
    private CommentService commentService;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ContentMapper contentMapper;

    /** 管理端举报列表：支持搜索、状态筛选、目标类型过滤 */
    @Override
    public Result<?> getReportList(int page, int size, String search, Integer handleStatus, Integer targetType) {
        logger.info("获取举报列表，page: {}, size: {}, search: {}, handleStatus: {}, targetType: {}", 
                page, size, search, handleStatus, targetType);
        try {
            // 计算分页参数
            int offset = (page - 1) * size;
            int limit = size;
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("search", search);
            params.put("handleStatus", handleStatus);
            params.put("targetType", targetType);
            params.put("offset", offset);
            params.put("limit", limit);
            
            // 查询举报列表
            List<Map<String, Object>> reportMapList = reportMapper.getReportList(params);
            logger.info("查询举报列表完成，结果数量: {}", reportMapList.size());
            
            // 转换为 DTO
            List<ReportResponseDTO> reportList = new ArrayList<>();
            for (Map<String, Object> reportMap : reportMapList) {
                reportList.add(convertToDTO(reportMap));
            }
            
            // 查询举报总数
            int total = reportMapper.getReportCount(params);
            logger.info("查询举报总数完成，总数: {}", total);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("list", reportList);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("pages", (total + size - 1) / size);
            
            return Result.success(response);
        } catch (Exception e) {
            logger.error("获取举报列表失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "获取举报列表失败: " + e.getMessage());
        }
    }

    /** 获取举报详情 */
    @Override
    public Result<ReportResponseDTO> getReportDetail(Long id) {
        logger.info("获取举报详情，id: {}", id);
        try {
            // 查询举报详情
            Map<String, Object> reportMap = reportMapper.getReportById(id);
            if (reportMap == null || reportMap.isEmpty()) {
                logger.warn("举报不存在，id: {}", id);
                return Result.error(404, "举报不存在");
            }
            
            // 转换为 DTO
            ReportResponseDTO dto = convertToDTO(reportMap);
            logger.info("获取举报详情完成，举报信息: {}", dto);
            return Result.success(dto);
        } catch (Exception e) {
            logger.error("获取举报详情失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "获取举报详情失败: " + e.getMessage());
        }
    }

    /** 处理举报：ignore忽略 / delete删除被举报内容并级联清理 */
    @Override
    @Transactional
    public Result<?> handleReport(Long reportId, String handleAction, String handleResult) {
        logger.info("处理举报，reportId: {}, handleAction: {}, handleResult: {}", reportId, handleAction, handleResult);
        try {
            // 验证举报是否存在
            Map<String, Object> reportMap = reportMapper.getReportById(reportId);
            if (reportMap == null || reportMap.isEmpty()) {
                logger.warn("举报不存在，reportId: {}", reportId);
                return Result.error(404, "举报不存在");
            }
            
            // 构建更新参数
            Map<String, Object> params = new HashMap<>();
            params.put("id", reportId);
            params.put("handleStatus", "ignore".equals(handleAction) ? 2 : 1); // 1: 已处理, 2: 已忽略
            params.put("handleRemark", handleResult);
            params.put("handleTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 更新举报处理状态
            int result = reportMapper.updateReportStatus(params);
            if (result <= 0) {
                logger.warn("更新举报处理状态失败，reportId: {}", reportId);
                return Result.error(500, "更新举报处理状态失败");
            }
            
            // 如果是处理操作（下架），需要下架被举报的内容或评论
            if ("handle".equals(handleAction)) {
                Object targetTypeObj = reportMap.get("target_type");
                int targetType = targetTypeObj != null ? ((Number) targetTypeObj).intValue() : 0;
                Object targetIdObj = reportMap.get("target_id");
                Long targetId = targetIdObj != null ? ((Number) targetIdObj).longValue() : null;

                if (targetType == 1 && targetId != null) { // 1: 内容
                    logger.info("下架被举报的内容，targetId: {}", targetId);
                    contentService.removeContent(targetId);
                } else if (targetType == 2 && targetId != null) { // 2: 评论
                    logger.info("下架被举报的评论，targetId: {}", targetId);
                    commentService.updateCommentStatus(targetId, "hidden");
                }
            }
            
            logger.info("处理举报成功，reportId: {}, handleAction: {}", reportId, handleAction);
            return Result.success("处理举报成功");
        } catch (Exception e) {
            logger.error("处理举报失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "处理举报失败: " + e.getMessage());
        }
    }

    /** 用户提交举报（支持内容和评论两种目标类型） */
    @Override
    @Transactional
    public Result<?> submitReport(int targetType, Long targetId, String anonymousId, String reportType, String reportDesc) {
        logger.info("提交举报，targetType: {}, targetId: {}, anonymousId: {}, reportType: {}, reportDesc: {}", 
                targetType, targetId, anonymousId, reportType, reportDesc);
        try {
            // 创建举报对象
            Report report = new Report();
            report.setTargetType(targetType);
            report.setTargetId(targetId);
            report.setAnonymousId(anonymousId);
            report.setReportType(reportType);
            report.setReportDesc(reportDesc);
            report.setHandleStatus(0); // 0: 未处理
            report.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 保存举报记录
            int result = reportMapper.insertReport(report);
            if (result <= 0) {
                logger.warn("保存举报记录失败");
                return Result.error(500, "保存举报记录失败");
            }

            logger.info("提交举报成功");
            return Result.success("提交举报成功");
        } catch (Exception e) {
            logger.error("提交举报失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "提交举报失败: " + e.getMessage());
        }
    }

    /** 获取举报统计数据（总数/待处理/已核实） */
    @Override
    public Result<?> getReportStats() {
        logger.info("获取举报统计数据");
        try {
            // 查询统计数据
            int total = reportMapper.getReportTotalCount();
            int pending = reportMapper.getReportCountByStatus(0); // 0: 待处理
            int verified = reportMapper.getReportCountByStatus(1); // 1: 已核实
            
            // 构建响应数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("pending", pending);
            stats.put("verified", verified);
            
            logger.info("获取举报统计数据完成，total: {}, pending: {}, verified: {}", total, pending, verified);
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取举报统计数据失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "获取举报统计数据失败: " + e.getMessage());
        }
    }

    /** 重置举报状态为待处理（撤回已处理的举报） */
    @Override
    @Transactional
    public Result<?> resetReportStatus(Long reportId) {
        logger.info("重置举报状态，reportId: {}", reportId);
        try {
            // 验证举报是否存在
            Map<String, Object> reportMap = reportMapper.getReportById(reportId);
            if (reportMap == null || reportMap.isEmpty()) {
                logger.warn("举报不存在，reportId: {}", reportId);
                return Result.error(404, "举报不存在");
            }
            
            // 构建更新参数（重置为待处理状态）
            Map<String, Object> params = new HashMap<>();
            params.put("id", reportId);
            params.put("handleStatus", 0); // 0: 待处理
            params.put("handleRemark", null);
            params.put("handleTime", null);
            
            // 更新举报状态
            int result = reportMapper.updateReportStatus(params);
            if (result <= 0) {
                logger.warn("重置举报状态失败，reportId: {}", reportId);
                return Result.error(500, "重置举报状态失败");
            }
            
            logger.info("重置举报状态成功，reportId: {}", reportId);
            return Result.success("重置举报状态成功");
        } catch (Exception e) {
            logger.error("重置举报状态失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "重置举报状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 将数据库 Map 转换为 ReportResponseDTO
     */
    private ReportResponseDTO convertToDTO(Map<String, Object> reportMap) {
        ReportResponseDTO dto = new ReportResponseDTO();
        
        // 基础字段
        dto.setId(getLongValue(reportMap, "id"));
        dto.setTargetId(getLongValue(reportMap, "target_id"));
        
        // 目标类型转换（1 → content, 2 → comment）
        Object targetTypeObj = reportMap.get("target_type");
        int targetType = targetTypeObj != null ? ((Number) targetTypeObj).intValue() : 0;
        dto.setTargetType(targetType == 1 ? "content" : "comment");
        
        // 状态转换（0 → pending, 1 → verified, 2 → unverified）
        Object handleStatusObj = reportMap.get("handle_status");
        int handleStatus = handleStatusObj != null ? ((Number) handleStatusObj).intValue() : 0;
        String status = "pending";
        if (handleStatus == 1) {
            status = "verified";
        } else if (handleStatus == 2) {
            status = "unverified";
        }
        dto.setStatus(status);
        
        // 时间字段（去除 .0 后缀）
        String reportTime = getStringValue(reportMap, "create_time");
        if (reportTime != null && reportTime.endsWith(".0")) {
            reportTime = reportTime.substring(0, reportTime.length() - 2);
        }
        dto.setReportTime(reportTime);
        
        String handleTime = getStringValue(reportMap, "handle_time");
        if (handleTime != null && handleTime.endsWith(".0")) {
            handleTime = handleTime.substring(0, handleTime.length() - 2);
        }
        dto.setHandleTime(handleTime);
        
        // 举报者匿名ID
        dto.setReporterAnonymousId(getStringValue(reportMap, "anonymous_id"));
        
        // 举报类型和原因
        String reportType = getStringValue(reportMap, "report_type");
        String reportDesc = getStringValue(reportMap, "report_desc");
        dto.setReportType(reportType != null ? reportType : "");
        dto.setReportDesc(reportDesc != null ? reportDesc : "");
        
        StringBuilder reason = new StringBuilder();
        if (reportType != null && !reportType.isEmpty()) {
            reason.append(reportType);
        }
        if (reportDesc != null && !reportDesc.isEmpty()) {
            if (reason.length() > 0) {
                reason.append(" - ");
            }
            reason.append(reportDesc);
        }
        dto.setReportReason(reason.length() > 0 ? reason.toString() : "无");
        
        // 处理备注
        dto.setHandleRemark(getStringValue(reportMap, "handle_remark"));
        
        // 内容相关字段
        if (targetType == 1) {
            dto.setContentTitle(getStringValue(reportMap, "content_title"));
            dto.setContentText(getStringValue(reportMap, "content_text"));
            dto.setTargetAnonymousId(getStringValue(reportMap, "target_anonymous_id"));
        } else {
            // 评论举报时，用评论内容作为标题
            dto.setContentTitle(getStringValue(reportMap, "comment_text"));
            dto.setContentText(getStringValue(reportMap, "comment_text"));
            dto.setTargetAnonymousId(getStringValue(reportMap, "comment_anonymous_id"));
        }
        
        return dto;
    }
    
    /**
     * 安全获取 String 值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 安全获取 Long 值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
