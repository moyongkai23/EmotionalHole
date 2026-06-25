package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContentMapper {

    int insertContent(Content content);

    List<Content> getContentList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    Content getContentById(Long id);

    List<Content> getContentByAnonymousId(String anonymousId);

    List<Content> getContentByAnonymousIdWithPage(@Param("offset") int offset, @Param("pageSize") int pageSize, @Param("anonymousId") String anonymousId);

    int updateContent(Content content);

    int deleteContent(Long id);

    int updateContentStatus(@Param("id") Long id, @Param("status") Integer status);

    List<Content> getContentListByStatus(@Param("offset") int offset, @Param("pageSize") int pageSize, @Param("status") Integer status);

    /** 原子增减点赞数（delta 传 1 或 -1） */
    int incrementLikeCount(@Param("id") Long id, @Param("delta") int delta);

    /** 原子增减评论数 */
    int incrementCommentCount(@Param("id") Long id, @Param("delta") int delta);

    /** 原子增减抱抱数 */
    int incrementHugCount(@Param("id") Long id, @Param("delta") int delta);

    List<Content> searchContent(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    List<Content> adminGetContentList(@Param("offset") int offset, @Param("pageSize") int pageSize,
                                     @Param("status") Integer status, @Param("keyword") String keyword,
                                     @Param("startTime") String startTime, @Param("endTime") String endTime);

    int adminCountContent(@Param("status") Integer status, @Param("keyword") String keyword,
                          @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<java.util.Map<String, Object>> getContentStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<java.util.Map<String, Object>> getContentStatisticsByDate(@Param("startTime") String startTime, @Param("endTime") String endTime);

    int countAll();

    List<Content> getContentListByCategory(@Param("offset") int offset, @Param("pageSize") int pageSize, @Param("topicId") Long topicId);

    List<Content> getWarningList(@Param("offset") int offset, @Param("pageSize") int pageSize,
                                 @Param("warningStatus") Integer warningStatus, @Param("riskLevel") Integer riskLevel);

    int countWarnings(@Param("warningStatus") Integer warningStatus, @Param("riskLevel") Integer riskLevel);

    int updateWarningStatus(@Param("id") Long id, @Param("warningStatus") Integer warningStatus, @Param("updateTime") String updateTime);

    int updateRiskInfo(@Param("id") Long id, @Param("riskLevel") Integer riskLevel,
                       @Param("riskScore") Integer riskScore, @Param("detectedKeywords") String detectedKeywords,
                       @Param("warningStatus") Integer warningStatus, @Param("updateTime") String updateTime);

    List<java.util.Map<String, Object>> getWarningStatistics();

    List<java.util.Map<String, Object>> getWarningStatisticsByDate(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<Content> getTopRiskContents(@Param("minRiskLevel") Integer minRiskLevel, @Param("limit") int limit);

    /** 统计高亮预警数（risk_level=3 且 warning_status=0） */
    int countHighRiskWarnings();

    /**
     * 统计帖子总数
     */
    Integer countPosts();

    /**
     * 获取发帖量趋势
     */
    List<java.util.Map<String, Object>> getPostTrend();

    /**
     * 获取高频话题趋势
     */
    List<java.util.Map<String, Object>> getTopicTrend();

    /**
     * 根据匿名ID批量更新内容状态（用于封禁用户时下架其内容）
     */
    int updateContentStatusByAnonymousId(@Param("anonymousId") String anonymousId, @Param("status") Integer status, @Param("updateTime") String updateTime);

    /**
     * 获取情绪标签分布
     */
    java.util.Map<String, Object> getEmotionDistribution();

    /**
     * 获取平台活跃度趋势
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    List<java.util.Map<String, Object>> getActivityTrend(
            @Param("startDate") String startDate, 
            @Param("endDate") String endDate);

}
