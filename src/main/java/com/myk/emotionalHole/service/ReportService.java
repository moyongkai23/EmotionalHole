package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.ReportResponseDTO;

public interface ReportService {

    /**
     * 获取举报列表（分页、筛选）
     */
    Result<?> getReportList(int page, int size, String search, Integer handleStatus, Integer targetType);

    /**
     * 获取举报详情
     */
    Result<ReportResponseDTO> getReportDetail(Long id);

    /**
     * 处理举报
     */
    Result<?> handleReport(Long reportId, String handleAction, String handleResult);

    /**
     * 提交举报
     */
    Result<?> submitReport(int targetType, Long targetId, String anonymousId, String reportType, String reportDesc);

    /**
     * 获取举报统计数据
     */
    Result<?> getReportStats();

    /**
     * 重置举报状态为待处理
     */
    Result<?> resetReportStatus(Long reportId);
}
