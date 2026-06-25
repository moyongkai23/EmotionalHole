package com.myk.emotionalHole.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.ContentResponseDTO;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.ContentSafety;
import com.myk.emotionalHole.entity.Topic;

import com.myk.emotionalHole.mapper.CommentMapper;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.HugMapper;
import com.myk.emotionalHole.mapper.LikeMapper;
import com.myk.emotionalHole.mapper.ReportMapper;
import com.myk.emotionalHole.mapper.TopicMapper;
import com.myk.emotionalHole.mapper.UserMapper;
import com.myk.emotionalHole.service.AtmosphereService;
import com.myk.emotionalHole.service.ContentService;
import com.myk.emotionalHole.service.EmotionAnalysisService;
import com.myk.emotionalHole.service.PageType;
import com.myk.emotionalHole.service.RiskAssessmentService;
import com.myk.emotionalHole.util.ExceptionUtils;
import com.myk.emotionalHole.util.PageUtils;
import com.myk.emotionalHole.util.SensitiveWordUtils;

/**
 * 内容服务实现类
 *
 * 提供树洞内容的发布、编辑、删除、搜索、分页查询等功能
 * 发布流程：参数校验→敏感词检测→风险评估→情绪分析→入库
 */
@Service
public class ContentServiceImpl implements ContentService {
    
    private static final int EXTRA_FETCH_COUNT = 4;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TopicMapper topicMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private HugMapper hugMapper;

    @Resource
    private ReportMapper reportMapper;
    
    @Resource
    private AtmosphereService atmosphereService;
    
    @Resource
    private RiskAssessmentService riskAssessmentService;
    
    @Resource
    private EmotionAnalysisService emotionAnalysisService;

    /** 校验内容是否存在，不存在则抛出NotFoundException */
    private Content requireContentExists(Long id) {
        Content content = contentMapper.getContentById(id);
        if (content == null) {
            throw ExceptionUtils.createNotFoundException("内容不存在");
        }
        return content;
    }

    /** 校验内容归属权，非本人操作则抛出ForbiddenException */
    private void validateContentOwner(Content content, String anonymousId, String errorMessage) {
        if (!content.getAnonymousId().equals(anonymousId)) {
            throw ExceptionUtils.createForbiddenException(errorMessage);
        }
    }

    /** 发布树洞：校验→敏感词检测→风险评估→情绪分析→自动创建话题→入库 */
    @Override
    @Transactional
    public Result<Content> publishContent(Content content) {
        // 参数校验
        ExceptionUtils.checkNotNull(content, "内容不能为空");
        ExceptionUtils.checkNotNull(content.getContentText(), "内容文本不能为空");
        ExceptionUtils.checkCondition(!content.getContentText().trim().isEmpty(), "内容文本不能为空");
        ExceptionUtils.checkNotNull(content.getAnonymousId(), "用户标识不能为空");
        ExceptionUtils.checkCondition(!content.getAnonymousId().trim().isEmpty(), "用户标识不能为空");
        // 使用标准日期格式，线程安全的DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        // 敏感词检测：检测到敏感词直接拒绝发布
        String contentText = content.getContentText();
        if (SensitiveWordUtils.containsSensitiveWord(contentText)) {
            throw ExceptionUtils.createParamException("您的内容包含敏感词，请修改后重新发布");
        }
        // 智能内容过滤与风险分级（用于后台风险评估和预警）
        int status;
        String message;
        // 使用风险评估服务进行综合评估（主要用于心理危机关键词检测）
        RiskAssessmentService.AssessmentResult assessmentResult = riskAssessmentService.assessRisk(contentText);
        switch (assessmentResult.getAction()) {
            case "REJECT":
                // 高风险内容入库待审核，管理员可在预警面板查看并处理
                status = 0;
                message = "内容已提交，正在审核中";
                break;
            case "PENDING":
                // 中风险，进入待审核
                status = 0;
                message = "内容已提交，正在审核中";
                break;
            case "PUBLISH":
            default:
                // 低风险或无风险，直接发布
                status = 1;
                message = "发布成功";
                break;
        }
        // 处理自定义话题：如果有自定义话题且没有选择预设话题，先创建话题
        if (content.getCustomTopic() != null && !content.getCustomTopic().trim().isEmpty() && content.getTopicId() == null) {
            String customTopicName = content.getCustomTopic().trim();
            // 检查话题是否已存在
            Topic existingTopic = topicMapper.getTopicByName(customTopicName);
            if (existingTopic == null) {
                // 创建新话题
                Topic newTopic = new Topic();
                newTopic.setTopicName(customTopicName);
                newTopic.setIsActive(1);
                newTopic.setCreateTime(now);
                topicMapper.addTopic(newTopic);
                // 获取刚创建的话题（假设数据库支持自增ID返回）
                existingTopic = topicMapper.getTopicByName(customTopicName);
            }
            // 设置话题ID
            if (existingTopic != null) {
                content.setTopicId(existingTopic.getId());
            }
        }
        // 设置默认值
        content.setLikeCount(0);
        content.setCommentCount(0);
        content.setHugCount(0);
        content.setContentStatus(status);
        content.setCreateTime(now);
        content.setUpdateTime(now);
        // 设置预警相关字段
        ContentSafety safety = new ContentSafety();
        safety.setRiskLevel(assessmentResult.getRiskLevel().getLevel());
        safety.setRiskScore(assessmentResult.getRiskScore());
        List<String> riskKeywords = assessmentResult.getRiskKeywords();
        safety.setDetectedKeywords(riskKeywords != null ? String.join(",", riskKeywords) : null);
        safety.setWarningStatus(assessmentResult.getRiskLevel().getLevel() > 0 ? 0 : 1);
        // 情绪分析（不影响发布，仅用于后续推荐情绪保护）
        int emotionType = emotionAnalysisService.analyzeContentEmotion(contentText, null);
        double emotionScore = emotionAnalysisService.calculateEmotionScore(contentText, null);
        safety.setEmotionType(emotionType);
        safety.setEmotionScore(emotionScore);
        content.setSafety(safety);
        // 插入数据库
        int result = contentMapper.insertContent(content);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("发布失败，请重试");
        }
        return Result.success(message, content);
    }

    /** 首页内容列表：氛围权重排序，多取EXTRA_FETCH_COUNT条后截取 */
    @Override
    public Result<List<Content>> getContentList(int page, int pageSize) {
        PageUtils.PageParam pageParam = PageUtils.createPageParam(page, pageSize);
        
        int actualOffset = pageParam.getOffset();
        int actualSize = pageParam.getSize() + EXTRA_FETCH_COUNT;
        
        List<Content> contentList = contentMapper.getContentList(actualOffset, actualSize);
        
        if (contentList.isEmpty()) {
            return Result.success(contentList);
        }
        
        List<Content> scoredList = new ArrayList<>();
        for (Content content : contentList) {
            Content scoredContent = new Content();
            scoredContent.setId(content.getId());
            scoredContent.setContentText(content.getContentText());
            scoredContent.setContentStatus(content.getContentStatus());
            scoredContent.setCreateTime(content.getCreateTime());
            scoredContent.setLikeCount(content.getLikeCount());
            scoredContent.setHugCount(content.getHugCount());
            scoredContent.setCommentCount(content.getCommentCount());
            scoredContent.setAnonymousId(content.getAnonymousId());
            scoredContent.setAvatar(content.getAvatar());
            scoredContent.setImageUrls(content.getImageUrls());
            scoredContent.setTopicId(content.getTopicId());
            scoredContent.setTopicName(content.getTopicName());
            scoredContent.setUpdateTime(content.getUpdateTime());
            
            scoredList.add(scoredContent);
        }
        
        scoredList.sort((c1, c2) -> {
            double score1 = atmosphereService.applyAtmosphereWeight(1.0, c1.getId(), PageType.NEW);
            double score2 = atmosphereService.applyAtmosphereWeight(1.0, c2.getId(), PageType.NEW);
            int timeCompare = c2.getCreateTime().compareTo(c1.getCreateTime());
            if (timeCompare != 0) {
                return timeCompare;
            }
            return Double.compare(score2, score1);
        });
        
        int start = 0;
        int end = Math.min(pageParam.getSize(), scoredList.size());
        List<Content> resultList = scoredList.subList(start, end);
        
        return Result.success(resultList);
    }

    /** 根据ID获取内容详情 */
    @Override
    public Result<Content> getContentById(Long id) {
        ExceptionUtils.checkNotNull(id, "内容ID不能为空");
        
        Content content = requireContentExists(id);
        return Result.success(content);
    }

    /** 分页获取指定用户发布的内容（我的发布） */
    @Override
    public Result<List<Content>> getContentByAnonymousId(int page, int pageSize, String anonymousId) {
        ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
        ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");
        ExceptionUtils.checkCondition(page > 0, "页码必须大于0");
        ExceptionUtils.checkCondition(pageSize > 0 && pageSize <= 100, "每页大小必须在1-100之间");
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        List<Content> contentList = contentMapper.getContentByAnonymousIdWithPage(offset, pageSize, anonymousId);
        return Result.success(contentList);
    }

    /** 编辑内容：敏感词检测→权限校验→更新 */
    @Override
    @Transactional
    public Result<Content> updateContent(Content content) {
        // 参数校验
        ExceptionUtils.checkNotNull(content, "内容不能为空");
        ExceptionUtils.checkNotNull(content.getId(), "内容ID不能为空");
        ExceptionUtils.checkNotNull(content.getContentText(), "内容文本不能为空");
        ExceptionUtils.checkCondition(!content.getContentText().trim().isEmpty(), "内容文本不能为空");
        ExceptionUtils.checkNotNull(content.getAnonymousId(), "用户标识不能为空");
        ExceptionUtils.checkCondition(!content.getAnonymousId().trim().isEmpty(), "用户标识不能为空");

        // 敏感词检测
        if (SensitiveWordUtils.containsSensitiveWord(content.getContentText())) {
            throw ExceptionUtils.createParamException("内容包含敏感词，请修改后重新发布");
        }

        // 验证内容是否存在且属于当前用户
        Content existingContent = requireContentExists(content.getId());
        if (!existingContent.getAnonymousId().equals(content.getAnonymousId())) {
            throw ExceptionUtils.createForbiddenException("无权修改此内容");
        }

        // 更新时间，使用线程安全的DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        content.setUpdateTime(LocalDateTime.now().format(formatter));

        // 执行更新
        int result = contentMapper.updateContent(content);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("更新内容失败");
        }

        return Result.success(content);
    }

    /** 用户删除自己的内容：权限校验→级联删除关联数据→删除 */
    @Override
    @Transactional
    public Result<Void> deleteContent(Long id, String anonymousId) {
        // 参数校验
        ExceptionUtils.checkNotNull(id, "内容ID不能为空");
        ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
        ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");

        // 验证内容是否存在且属于当前用户
        Content existingContent = requireContentExists(id);
        validateContentOwner(existingContent, anonymousId, "无权删除此内容");

        // 级联删除关联数据
        cascadeDeleteRelatedData(id);

        // 执行删除
        int result = contentMapper.deleteContent(id);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("删除内容失败");
        }

        return Result.success();
    }

    /** 更新内容状态（审核通过/拒绝/待审核） */
    @Override
    @Transactional
    public Result<Void> updateContentStatus(Long id, Integer status) {
        // 参数校验
        ExceptionUtils.checkNotNull(id, "内容ID不能为空");
        ExceptionUtils.checkNotNull(status, "状态值不能为空");
        ExceptionUtils.checkCondition(status == 0 || status == 1 || status == 2, "状态值无效");
        
        // 验证内容是否存在
        requireContentExists(id);

        // 执行更新
        int result = contentMapper.updateContentStatus(id, status);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("更新内容状态失败");
        }

        return Result.success();
    }

    /** 按关键词搜索内容 */
    @Override
    public Result<List<Content>> searchContent(String keyword, int page, int pageSize) {
        // 参数校验
        ExceptionUtils.checkNotNull(keyword, "搜索关键词不能为空");
        ExceptionUtils.checkCondition(!keyword.trim().isEmpty(), "搜索关键词不能为空");
        
        // 处理分页参数
        PageUtils.PageParam pageParam = PageUtils.createPageParam(page, pageSize);
        
        // 执行搜索
        List<Content> contentList = contentMapper.searchContent(keyword.trim(), pageParam.getOffset(), pageParam.getPageSize());
        
        return Result.success(contentList);
    }

    /** Content实体转管理端响应DTO（标题截取20字+省略号） */
    private ContentResponseDTO convertToDTO(Content content) {
        ContentResponseDTO dto = new ContentResponseDTO();
        dto.setId(content.getId());
        
        // title 从 contentText 截取前 20 个字符，超出部分加省略号
        String contentText = content.getContentText();
        if (contentText != null && contentText.length() > 20) {
            dto.setTitle(contentText.substring(0, 20) + "...");
        } else {
            dto.setTitle(contentText);
        }
        
        dto.setStatus(content.getContentStatus());
        
        dto.setAnonymousId(content.getAnonymousId());
        dto.setPublishTime(content.getCreateTime());
        dto.setViewCount(0);
        dto.setLikeCount(content.getLikeCount());
        dto.setCommentCount(content.getCommentCount());
        
        return dto;
    }

    /** 管理端内容列表：支持状态筛选、关键词搜索、时间范围过滤 */
    @Override
    public Result<?> getAdminContentList(int page, int pageSize, Integer status, String keyword, String startTime, String endTime) {
        // 处理分页参数
        PageUtils.PageParam pageParam = PageUtils.createPageParam(page, pageSize);
        
        // 执行查询
        List<Content> contentList = contentMapper.adminGetContentList(
            pageParam.getOffset(), 
            pageParam.getPageSize(), 
            status, 
            keyword != null ? keyword.trim() : null, 
            startTime, 
            endTime
        );
        
        // 转换为 DTO
        List<ContentResponseDTO> dtoList = new ArrayList<>();
        for (Content content : contentList) {
            dtoList.add(convertToDTO(content));
        }
        
        // 统计总数
        int total = contentMapper.adminCountContent(
            status, 
            keyword != null ? keyword.trim() : null, 
            startTime, 
            endTime
        );
        
        // 构建响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("list", dtoList);
        response.put("total", total);
        
        return Result.success(response);
    }

    /** 管理端获取内容详情 */
    @Override
    public Result<Content> getAdminContentDetail(Long id) {
        // 参数校验
        ExceptionUtils.checkNotNull(id, "内容ID不能为空");
        
        // 执行查询
        Content content = requireContentExists(id);
        
        return Result.success(content);
    }

    /** 管理端审核内容：通过或拒绝 */
    @Override
    @Transactional
    public Result<Void> auditContent(Long id, Integer status) {
        // 参数校验
        ExceptionUtils.checkNotNull(id, "内容ID不能为空");
        ExceptionUtils.checkNotNull(status, "状态值不能为空");
        ExceptionUtils.checkCondition(status == 1 || status == 2, "状态值无效，1-通过，2-拒绝");
        
        // 验证内容是否存在
        requireContentExists(id);

        // 执行审核
        int result = contentMapper.updateContentStatus(id, status);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("审核操作失败");
        }

        return Result.success();
    }

    /** 管理端下架内容（状态改为拒绝） */
    @Override
    @Transactional
    public Result<Void> removeContent(Long id) {
        // 参数校验
        ExceptionUtils.checkNotNull(id, "内容ID不能为空");
        
        // 验证内容是否存在
        requireContentExists(id);

        // 执行下架（状态改为2-拒绝）
        int result = contentMapper.updateContentStatus(id, 2);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("下架操作失败");
        }

        return Result.success();
    }

    /** 获取内容统计数据（用于管理端仪表盘） */
    @Override
    public Result<List<java.util.Map<String, Object>>> getContentStatistics(String startTime, String endTime) {
        // 执行统计
        List<java.util.Map<String, Object>> statistics = contentMapper.getContentStatistics(startTime, endTime);
        return Result.success(statistics);
    }

    /** 按日期统计内容发布趋势 */
    @Override
    public Result<List<java.util.Map<String, Object>>> getContentStatisticsByDate(String startTime, String endTime) {
        // 执行统计
        List<java.util.Map<String, Object>> statistics = contentMapper.getContentStatisticsByDate(startTime, endTime);
        return Result.success(statistics);
    }

    /** 管理端删除内容：级联删除关联数据后物理删除 */
    @Override
    @Transactional
    public Result<Void> deleteContentByAdmin(Long id) {
        // 参数校验
        ExceptionUtils.checkNotNull(id, "内容ID不能为空");

        // 验证内容是否存在
        requireContentExists(id);

        // 级联删除关联数据
        cascadeDeleteRelatedData(id);

        // 执行删除
        int result = contentMapper.deleteContent(id);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("删除内容失败");
        }

        return Result.success();
    }

    /**
     * 级联删除内容关联的所有数据
     */
    private void cascadeDeleteRelatedData(Long contentId) {
        // 删除内容下的所有评论
        commentMapper.deleteByContentId(contentId);
        // 删除内容的点赞记录（target_type=1表示内容）
        likeMapper.deleteByTargetTypeAndTargetId(1, contentId);
        // 删除内容的抱抱记录（target_type=1表示内容）
        hugMapper.deleteByTargetTypeAndTargetId(1, contentId);
        // 删除内容的举报记录（target_type=1表示内容）
        reportMapper.deleteByTargetTypeAndTargetId(1, contentId);
    }

    /** 统计全部内容数量 */
    @Override
    public Result<Integer> countAllContent() {
        // 执行统计
        int total = contentMapper.countAll();
        return Result.success(total);
    }

    /** 按话题分类获取内容列表 */
    @Override
    public Result<List<Content>> getContentListByCategory(int page, int pageSize, Long topicId) {
        // 参数校验
        ExceptionUtils.checkNotNull(topicId, "话题ID不能为空");
        
        // 处理分页参数
        PageUtils.PageParam pageParam = PageUtils.createPageParam(page, pageSize);
        
        // 执行查询
        List<Content> contentList = contentMapper.getContentListByCategory(
            pageParam.getOffset(), 
            pageParam.getPageSize(), 
            topicId
        );
        
        return Result.success(contentList);
    }

}