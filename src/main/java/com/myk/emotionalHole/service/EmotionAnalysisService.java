package com.myk.emotionalHole.service;

public interface EmotionAnalysisService {

    int analyzeContentEmotion(String contentText, String customTags);

    double calculateEmotionScore(String contentText, String customTags);

    void refreshKeywordCache();

}