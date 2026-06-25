package com.myk.emotionalHole.service;

import com.myk.emotionalHole.entity.Content;

public interface AtmosphereService {

    double calculateSupportScore(Long contentId);

    double applyAtmosphereWeight(double baseScore, Long contentId, PageType pageType);

    /**
     * 直接从 Content 对象应用氛围权重，避免 N+1 查询
     */
    double applyAtmosphereWeightFromContent(double baseScore, Content content, PageType pageType);

    double getCommunitySupportRate();
}