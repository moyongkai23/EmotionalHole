package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.ReportListRequestDTO;
import com.myk.emotionalHole.dto.SubmitReportRequestDTO;
import com.myk.emotionalHole.service.ReportService;
import com.myk.emotionalHole.util.TypeConverterUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 举报管理控制器
 *
 * 提供举报提交、举报列表、举报详情、举报处理接口
 */
@RestController
@RequestMapping("report")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @GetMapping("/list")
    public Result<?> getReportList(@Valid ReportListRequestDTO request) {
        logger.info("开始处理获取举报列表请求，参数: {}", request);
        try {
            Result<?> result = reportService.getReportList(
                    request.getPage(),
                    request.getSize(),
                    request.getSearch(),
                    request.getHandleStatus(),
                    request.getTargetType()
            );
            logger.info("获取举报列表请求处理完成，结果: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("获取举报列表请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    @GetMapping("/detail/{id}")
    public Result<?> getReportDetail(@PathVariable Long id) {
        logger.info("开始处理获取举报详情请求，id: {}", id);
        try {
            if (id == null || id < 1) {
                logger.warn("参数验证失败，id: {}", id);
                return Result.error(400, "举报ID必须大于0");
            }

            Result<?> result = reportService.getReportDetail(id);
            logger.info("获取举报详情请求处理完成，结果: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("获取举报详情请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    @PostMapping("/handle")
    public Result<?> handleReport(@RequestBody Map<String, Object> request) {
        logger.info("开始处理举报请求，请求参数: {}", request);
        try {
            Long reportId = TypeConverterUtil.toLong(request.get("reportId"), null);
            String handleAction = TypeConverterUtil.toString(request.get("handleAction"), null);
            String handleResult = TypeConverterUtil.toString(request.get("handleResult"), null);

            if (reportId == null || reportId < 1) {
                logger.warn("参数验证失败，reportId: {}", reportId);
                return Result.error(400, "举报ID必须大于0");
            }
            if (handleAction == null || !Arrays.asList("delete", "handle", "ignore").contains(handleAction)) {
                logger.warn("参数验证失败，handleAction: {}", handleAction);
                return Result.error(400, "处理操作必须为delete、handle或ignore");
            }

            Result<?> result = reportService.handleReport(reportId, handleAction, handleResult);
            logger.info("处理举报请求处理完成，结果: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("处理举报请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    @GetMapping("/stats")
    public Result<?> getReportStats() {
        logger.info("开始处理获取举报统计数据请求");
        try {
            Result<?> result = reportService.getReportStats();
            logger.info("获取举报统计数据请求处理完成，结果: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("获取举报统计数据请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    @PostMapping("/submit")
    public Result<?> submitReport(@Valid @RequestBody SubmitReportRequestDTO request, HttpServletRequest httpRequest) {
        try {
            if (request.getTargetType() != 1 && request.getTargetType() != 2) {
                return Result.error(400, "举报目标类型必须为1(内容)或2(评论)");
            }
            if (request.getTargetId() == null || request.getTargetId() < 1) {
                return Result.error(400, "举报目标ID必须大于0");
            }
            // 从JWT获取用户身份
            String anonymousId = (String) httpRequest.getAttribute("anonymousId");
            return reportService.submitReport(
                    request.getTargetType(),
                    request.getTargetId(),
                    anonymousId,
                    request.getReportType(),
                    request.getReportDesc()
            );
        } catch (Exception e) {
            logger.error("提交举报请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    @PutMapping("/{id}/reset")
    public Result<?> resetReportStatus(@PathVariable Long id) {
        logger.info("开始处理重置举报状态请求，id: {}", id);
        try {
            if (id == null || id < 1) {
                logger.warn("参数验证失败，id: {}", id);
                return Result.error(400, "举报ID必须大于0");
            }

            Result<?> result = reportService.resetReportStatus(id);
            logger.info("重置举报状态请求处理完成，结果: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("重置举报状态请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }
}