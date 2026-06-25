package com.myk.emotionalHole.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import com.myk.emotionalHole.dto.HotRankingResponseDTO;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.service.AtmosphereService;
import com.myk.emotionalHole.service.HotRankingService;
import com.myk.emotionalHole.service.PageType;
import com.myk.emotionalHole.util.PageUtils;

/**
 * 热榜服务实现类
 *
 * 热度公式：(点赞×2) + (抱抱×4) + (评论×5) - 时间衰减
 * 评论权重最高，鼓励用户互动；氛围权重加成优质内容
 */
@Service
public class HotRankingServiceImpl implements HotRankingService {

    private static final double LIKE_WEIGHT = 2.0;      // 点赞权重
    private static final double HUG_WEIGHT = 4.0;       // 抱抱权重（新增）
    private static final double COMMENT_WEIGHT = 5.0;   // 评论权重
    private static final double DECAY_FACTOR = 0.1;     // 时间衰减因子

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private AtmosphereService atmosphereService;

    /** 获取热榜：计算所有已发布内容的热度分→排序→分页返回 */
    @Override
    public Map<String, Object> getHotRanking(int pageNum, int pageSize) {
        PageUtils.PageParam pageParam = PageUtils.createPageParam(pageNum, pageSize);
        
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有已发布的内容（不按时间过滤，因为测试数据比较旧）
        List<Content> allContents = contentMapper.getContentListByStatus(0, 1000, 1);
        
        List<HotRankingResponseDTO> scoredList = new ArrayList<>();
        for (Content content : allContents) {
            if (content.getContentStatus() != 1) {
                continue;
            }
            
            double score = calculateScore(content, now);
            HotRankingResponseDTO dto = new HotRankingResponseDTO();
            dto.setId(content.getId());
            dto.setContentId(content.getId());
            dto.setContentText(content.getContentText());
            dto.setImageUrls(content.getImageUrls());
            dto.setAnonymousId(content.getAnonymousId());
            dto.setAvatar(content.getAvatar());
            dto.setCreateTime(content.getCreateTime());
            dto.setLikeCount(content.getLikeCount());
            dto.setHugCount(content.getHugCount());
            dto.setCommentCount(content.getCommentCount());
            dto.setScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP));
            dto.setTopicId(content.getTopicId());
            dto.setTopicName(content.getTopicName());
            scoredList.add(dto);
        }
        
        scoredList = scoredList.stream()
                .sorted(Comparator.comparing(HotRankingResponseDTO::getScore).reversed())
                .collect(Collectors.toList());
        
        for (int i = 0; i < scoredList.size(); i++) {
            scoredList.get(i).setRankingPosition(i + 1);
        }
        
        int offset = pageParam.getOffset();
        int end = Math.min(offset + pageParam.getSize(), scoredList.size());
        
        List<HotRankingResponseDTO> resultList = offset < end ? scoredList.subList(offset, end) : new ArrayList<>();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", resultList);
        result.put("total", scoredList.size());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return result;
    }

    /**
     * 计算热度分数
     * 公式：(点赞×2) + (抱抱×4) + (评论×5) - 时间衰减
     * 评论权重最高，鼓励用户互动
     */
    private double calculateScore(Content content, LocalDateTime calculateTime) {
        int likeCount = content.getLikeCount();
        int hugCount = content.getHugCount();
        int commentCount = content.getCommentCount();

        // 热度分 = (点赞 × 2) + (抱抱 × 4) + (评论 × 5) - 时间衰减
        double baseScore = likeCount * LIKE_WEIGHT
                         + hugCount * HUG_WEIGHT
                         + commentCount * COMMENT_WEIGHT;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime publishTime = LocalDateTime.parse(content.getCreateTime(), formatter);
            long hoursSincePublish = ChronoUnit.HOURS.between(publishTime, calculateTime);
            double decay = hoursSincePublish / 24.0 * DECAY_FACTOR;
            baseScore = Math.max(0, baseScore - decay);
        } catch (Exception e) {
        }

        // 应用氛围权重（热榜页系数0.3），直接使用 Content 对象避免 N+1 查询
        return atmosphereService.applyAtmosphereWeightFromContent(baseScore, content, PageType.HOT);
    }
}
