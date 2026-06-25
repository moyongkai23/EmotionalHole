package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.service.AtmosphereService;
import com.myk.emotionalHole.service.PageType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 社区氛围服务实现类
 *
 * 提供社区支持率、互动热度等氛围指标计算
 */
@Service
public class AtmosphereServiceImpl implements AtmosphereService {

    @Resource
    private ContentMapper contentMapper;

    @Override
    public double calculateSupportScore(Long contentId) {
        // 根据内容ID查询内容信息
        Content content = contentMapper.getContentById(contentId);
        if (content == null) {
            return 0.0;
        }
        return calculateSupportScoreFromContent(content);
    }

    /**
     * 直接从 Content 对象计算支持分数，避免额外 DB 查询
     */
    private double calculateSupportScoreFromContent(Content content) {
        int likeCount = content.getLikeCount();
        int hugCount = content.getHugCount();
        int commentCount = content.getCommentCount();

        int totalInteractions = likeCount + hugCount + commentCount;

        if (totalInteractions == 0) {
            return 0.5;
        }

        return (double) (likeCount + hugCount) / totalInteractions;
    }

    @Override
    public double applyAtmosphereWeight(double baseScore, Long contentId, PageType pageType) {
        // 获取内容的支持率分数
        double supportScore = calculateSupportScore(contentId);
        // 根据页面类型获取氛围因子
        double atmosphereFactor = pageType.getAtmosphereFactor();

        return baseScore * (1 + atmosphereFactor * supportScore);
    }

    /**
     * 直接从 Content 对象应用氛围权重，避免 N+1 查询
     */
    @Override
    public double applyAtmosphereWeightFromContent(double baseScore, Content content, PageType pageType) {
        double supportScore = calculateSupportScoreFromContent(content);
        double atmosphereFactor = pageType.getAtmosphereFactor();

        return baseScore * (1 + atmosphereFactor * supportScore);
    }

    @Override
    public double getCommunitySupportRate() {
        // 获取所有已发布的内容
        List<Content> publishedContents = contentMapper.getContentListByStatus(0, 10000, 1);

        if (publishedContents.isEmpty()) {
            return 0.0;
        }

        // 累计所有内容的支持率
        double totalSupportScore = 0.0;
        for (Content content : publishedContents) {
            int likeCount = content.getLikeCount();
            int hugCount = content.getHugCount();
            int commentCount = content.getCommentCount();

            int totalInteractions = likeCount + hugCount + commentCount;
            if (totalInteractions > 0) {
                totalSupportScore += (double) (likeCount + hugCount) / totalInteractions;
            } else {
                totalSupportScore += 0.5;
            }
        }

        // 返回平均支持率
        return totalSupportScore / publishedContents.size();
    }
}