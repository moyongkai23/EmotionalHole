package com.myk.emotionalHole.mapper;
import com.myk.emotionalHole.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    /**
     * 分页查询举报列表（带关联数据）
     */
    List<Map<String, Object>> getReportList(Map<String, Object> params);

    /**
     * 查询举报总数
     */
    int getReportCount(Map<String, Object> params);

    /**
     * 根据ID查询举报详情（带关联数据）
     */
    Map<String, Object> getReportById(Long id);

    /**
     * 更新举报处理状态
     */
    int updateReportStatus(Map<String, Object> params);

    /**
     * 插入举报记录
     */
    int insertReport(Report report);

    /**
     * 获取举报总数
     */
    int getReportTotalCount();

    /**
     * 根据状态获取举报数量
     */
    int getReportCountByStatus(int status);

    /**
     * 根据目标类型和目标ID删除所有举报
     */
    int deleteByTargetTypeAndTargetId(@Param("targetType") int targetType, @Param("targetId") Long targetId);
}
