package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.service.AtmosphereService;
import com.myk.emotionalHole.service.EmotionAnalysisService;
import com.myk.emotionalHole.service.PageType;
import com.myk.emotionalHole.service.RecommendationService;
import com.myk.emotionalHole.service.UserBehaviorService;
import com.myk.emotionalHole.service.UserProfileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 * 基于标签的个性化推荐算法核心实现
 * 采用三路优先级填充策略：相似度 → 热门 → 随机
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private UserBehaviorService userBehaviorService;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private AtmosphereService atmosphereService;

    @Resource
    private EmotionAnalysisService emotionAnalysisService;

    // 分类标签映射：分类ID对应标签列表（与 UserProfileServiceImpl 保持一致）
    private static final Map<Long, String[]> CATEGORY_TAGS = new HashMap<>();
    static {
        CATEGORY_TAGS.put(1L, new String[]{"#心情吐槽", "#情绪低落"});           // 心情吐槽
        CATEGORY_TAGS.put(2L, new String[]{"#学业焦虑", "#压力"});              // 学业困惑
        CATEGORY_TAGS.put(3L, new String[]{"#情感心事", "#友谊困惑", "#恋爱心事"}); // 情感心事
        CATEGORY_TAGS.put(4L, new String[]{"#校园趣事", "#日常生活"});           // 校园趣事
        CATEGORY_TAGS.put(5L, new String[]{"#日常分享", "#未来展望"});           // 日常分享
        CATEGORY_TAGS.put(6L, new String[]{"#求助建议", "#就业迷茫"});           // 求助建议
        CATEGORY_TAGS.put(7L, new String[]{"#自定义话题"});                         // 自定义话题
    }

    // 情绪保护相关常量
    private static final double NEGATIVE_CONTENT_RATIO_LIMIT = 0.3;          // 正常用户消极内容上限30%
    private static final double NEGATIVE_CONTENT_RATIO_LIMIT_INTERVENTION = 0.2; // 干预中用户消极内容上限20%
    private static final double EMOTION_INTERVENTION_THRESHOLD = 0.6;        // 情绪干预阈值：消极占比>60%
    private static final int INTERVENTION_POSITIVE_COUNT = 2;                // 干预时插入积极内容数量

    /** 个性化推荐入口 */
    @Override
    public List<Content> recommend(String anonymousId, int limit) {
        return recommendByCategory(anonymousId, null, limit);
    }

    /** 按分类推荐，包含新老用户策略和情绪保护 */
    @Override
    public List<Content> recommendByCategory(String anonymousId, Long categoryId, int limit) {
        // 1. 判断用户类型（新用户/老用户）
        boolean isNewUser = userProfileService.isNewUser(anonymousId);

        // 2. 生成候选内容池
        List<Content> candidates = generateCandidates(categoryId);

        // 3. 获取用户情绪状态（用于情绪保护）
        EmotionState userEmotionState = analyzeUserEmotionState(anonymousId);

        // 4. 根据用户类型选择推荐策略
        List<Content> recommendations;
        if (isNewUser) {
            // 新用户：热门推荐 + 随机推荐（带情绪保护）
            recommendations = recommendForNewUserWithEmotionProtection(candidates, limit, userEmotionState);
        } else {
            // 老用户：个性化推荐（三路优先级填充 + 情绪保护）
            recommendations = recommendForExistingUserWithEmotionProtection(anonymousId, candidates, limit, userEmotionState);
        }

        // 5. 情绪干预处理
        if (userEmotionState.needIntervention) {
            recommendations = injectPositiveContents(recommendations, candidates, userEmotionState);
        }

        // 6. 控制消极内容比例
        recommendations = controlNegativeContentRatio(recommendations, userEmotionState);

        // 7. 去重并返回
        return recommendations.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /** 相关内容推荐：基于分类相似度 */
    @Override
    public List<Content> getRelatedContents(Long contentId, int limit) {
        // 获取当前内容
        Content currentContent = contentMapper.getContentById(contentId);
        if (currentContent == null) {
            return Collections.emptyList();
        }

        // 获取所有已发布内容（排除当前内容）
        List<Content> allContents = getAllPublishedContents();
        allContents.removeIf(c -> c.getId().equals(contentId));

        // 基于分类相似度排序
        return allContents.stream()
                .map(content -> {
                    double similarity = calculateCategorySimilarity(currentContent, content);
                    return new AbstractMap.SimpleEntry<>(content, similarity);
                })
                .sorted(Map.Entry.<Content, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /** 记录用户行为并更新画像（点赞/浏览/评论等） */
    @Override
    @Transactional
    public void recordBehavior(String anonymousId, Long contentId, Integer behaviorType) {
        // 1. 记录用户行为
        userBehaviorService.recordBehavior(anonymousId, contentId, behaviorType);

        // 2. 更新用户画像
        userProfileService.updateProfileByBehavior(anonymousId, contentId, behaviorType);
    }

    /** 生成推荐理由（如"根据你的兴趣推荐：#学业焦虑"） */
    @Override
    public String getRecommendReason(String anonymousId, Long contentId) {
        // 获取用户画像
        Map<String, Double> userTags = userProfileService.getInterestTags(anonymousId);

        // 获取内容标签
        Content content = contentMapper.getContentById(contentId);
        if (content == null) {
            return "热门推荐";
        }

        // 获取内容对应的标签
        String[] contentTags = CATEGORY_TAGS.getOrDefault(content.getTopicId(), new String[]{});

        // 找出匹配的标签
        List<String> matchedTags = new ArrayList<>();
        for (String tag : contentTags) {
            if (userTags.containsKey(tag) && userTags.get(tag) > 0) {
                matchedTags.add(tag);
            }
        }

        if (!matchedTags.isEmpty()) {
            return "根据你的兴趣推荐：" + String.join("、", matchedTags);
        }

        // 检查是否为热门内容
        if (isHotContent(content)) {
            return "热门内容";
        }

        return "猜你喜欢";
    }

    /**
     * 生成候选内容池
     */
    private List<Content> generateCandidates(Long categoryId) {
        List<Content> allContents = getAllPublishedContents();

        // 如果指定了分类，进行筛选
        if (categoryId != null) {
            return allContents.stream()
                    .filter(c -> categoryId.equals(c.getTopicId()))
                    .collect(Collectors.toList());
        }

        return allContents;
    }

    private static final int MAX_CANDIDATE_COUNT = 1000;

    /**
     * 获取所有已发布内容
     */
    private List<Content> getAllPublishedContents() {
        // 这里简化处理，实际应该分页查询
        return contentMapper.getContentListByStatus(0, MAX_CANDIDATE_COUNT, 1);
    }

    /**
     * 计算内容与用户画像的相似度（余弦相似度简化版）
     */
    private double calculateContentSimilarity(Content content, Map<String, Double> userTags) {
        // 获取内容对应的标签
        String[] contentTags = CATEGORY_TAGS.getOrDefault(content.getTopicId(), new String[]{});

        if (contentTags.length == 0 || userTags.isEmpty()) {
            return 0.0;
        }

        // 计算匹配分数
        double score = 0.0;
        for (String tag : contentTags) {
            if (userTags.containsKey(tag)) {
                score += userTags.get(tag);
            }
        }

        // 归一化
        return score / (contentTags.length * 10.0); // 假设最大权重为10
    }

    /**
     * 计算热度分数
     * 热度 = 点赞×1 + 评论×2 + 抱抱×1.5 + 时间衰减 + 氛围权重
     */
    private double calculateHotScore(Content content) {
        int likeCount = content.getLikeCount();
        int commentCount = content.getCommentCount();
        int hugCount = content.getHugCount();

        double baseScore = likeCount * 1.0
                + commentCount * 2.0
                + hugCount * 1.5;

        // 时间衰减
        double timeDecay = calculateTimeDecay(content.getCreateTime());
        baseScore = baseScore * timeDecay;

        // 应用氛围权重（推荐页系数0.2），直接使用 Content 对象避免 N+1 查询
        return atmosphereService.applyAtmosphereWeightFromContent(baseScore, content, PageType.RECOMMEND);
    }

    /**
     * 计算时间衰减
     * 越新的内容权重越高
     */
    private double calculateTimeDecay(String createTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime createTime = LocalDateTime.parse(createTimeStr, formatter);
            long hoursSincePublish = ChronoUnit.HOURS.between(createTime, LocalDateTime.now());

            // 24小时内：不衰减
            // 超过24小时：指数衰减
            if (hoursSincePublish < 24) {
                return 1.5; // 新内容加权
            } else {
                return Math.exp(-hoursSincePublish / 24.0 * 0.1);
            }
        } catch (Exception e) {
            return 1.0;
        }
    }

    /**
     * 判断是否为热门内容
     */
    private boolean isHotContent(Content content) {
        return calculateHotScore(content) > 50; // 阈值可调整
    }

    /**
     * 计算分类相似度
     */
    private double calculateCategorySimilarity(Content content1, Content content2) {
        if (content1.getTopicId() == null || content2.getTopicId() == null) {
            return 0.0;
        }

        // 相同分类：相似度1.0
        if (content1.getTopicId().equals(content2.getTopicId())) {
            return 1.0;
        }

        // 检查是否有共同标签
        String[] tags1 = CATEGORY_TAGS.getOrDefault(content1.getTopicId(), new String[]{});
        String[] tags2 = CATEGORY_TAGS.getOrDefault(content2.getTopicId(), new String[]{});

        int commonTags = 0;
        for (String tag1 : tags1) {
            for (String tag2 : tags2) {
                if (tag1.equals(tag2)) {
                    commonTags++;
                }
            }
        }

        return commonTags * 0.3; // 每有一个共同标签，相似度+0.3
    }

    /**
     * 用户情绪状态类
     */
    private static class EmotionState {

        boolean needIntervention;

        EmotionState(double positiveRatio, double neutralRatio, double negativeRatio) {
            this.needIntervention = negativeRatio > EMOTION_INTERVENTION_THRESHOLD;
        }
    }

    /**
     * 分析用户情绪状态
     */
    private EmotionState analyzeUserEmotionState(String anonymousId) {
        //获取该用户最近浏览的10条内容
        List<Content> recentContents = userBehaviorService.getRecentViewedContents(anonymousId, 10);
        //为空时，默认情绪状态
        if (recentContents.isEmpty()) {
            return new EmotionState(0.3, 0.5, 0.2);
        }

        int positiveCount = 0;
        int neutralCount = 0;
        int negativeCount = 0;

        for (Content content : recentContents) {
            // 获取内容的情绪类型（从安全分析结果中获取）
            Integer emotionType = content.getSafety() != null ? content.getSafety().getEmotionType() : null;
            if (emotionType == null) {
                emotionType = 2;
            }

            switch (emotionType) {
                case 1: positiveCount++; break;
                case 2: neutralCount++; break;
                case 3: negativeCount++; break;
                default: neutralCount++;
            }
        }

        int total = positiveCount + neutralCount + negativeCount;
        return new EmotionState(
                (double) positiveCount / total,
                (double) neutralCount / total,
                (double) negativeCount / total
        );
    }

    /**
     * 为新用户推荐（带情绪保护）
     */
    private List<Content> recommendForNewUserWithEmotionProtection(List<Content> candidates, int limit, EmotionState emotionState) {
        List<Content> recommendations = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>();

        // 1. 热门内容（70%）
        int hotCount = (int) (limit * 0.7);
        List<Content> hotContents = getHotContentsWithEmotion(candidates, hotCount * 2, emotionState);
        for (Content content : hotContents) {
            if (recommendations.size() >= hotCount) break;
            if (!selectedIds.contains(content.getId())) {
                recommendations.add(content);
                selectedIds.add(content.getId());
            }
        }

        // 2. 积极内容（30%）- 用于情绪保护
        int positiveCount = limit - recommendations.size();
        List<Content> positiveContents = getPositiveContents(candidates, positiveCount * 2, selectedIds);
        for (Content content : positiveContents) {
            if (recommendations.size() >= limit) break;
            if (!selectedIds.contains(content.getId())) {
                recommendations.add(content);
                selectedIds.add(content.getId());
            }
        }

        return recommendations;
    }

    /**
     * 为老用户推荐（带情绪保护）
     */
    private List<Content> recommendForExistingUserWithEmotionProtection(String anonymousId, List<Content> candidates, int limit, EmotionState emotionState) {
        List<Content> recommendations = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>();

        // 获取用户画像标签
        Map<String, Double> userTags = userProfileService.getInterestTags(anonymousId);

        // 第一优先级：相似度推荐（带情绪保护）
        List<Content> similarContents = getSimilarContentsWithEmotion(candidates, userTags, limit, emotionState);
        for (Content content : similarContents) {
            if (recommendations.size() >= limit) break;
            if (!selectedIds.contains(content.getId())) {
                recommendations.add(content);
                selectedIds.add(content.getId());
            }
        }

        // 第二优先级：热门推荐（补充填充）
        if (recommendations.size() < limit) {
            List<Content> hotContents = getHotContentsWithEmotion(candidates, limit * 2, emotionState);
            for (Content content : hotContents) {
                if (recommendations.size() >= limit) break;
                if (!selectedIds.contains(content.getId())) {
                    recommendations.add(content);
                    selectedIds.add(content.getId());
                }
            }
        }

        // 第三优先级：积极内容（兜底填充，用于情绪保护）
        if (recommendations.size() < limit) {
            List<Content> positiveContents = getPositiveContents(candidates, limit * 2, selectedIds);
            for (Content content : positiveContents) {
                if (recommendations.size() >= limit) break;
                if (!selectedIds.contains(content.getId())) {
                    recommendations.add(content);
                    selectedIds.add(content.getId());
                }
            }
        }

        return recommendations;
    }

    /**
     * 获取相似内容（带情绪保护）
     */
    private List<Content> getSimilarContentsWithEmotion(List<Content> candidates, Map<String, Double> userTags, int limit, EmotionState emotionState) {
        return candidates.stream()
                .map(content -> {
                    double similarity = calculateContentSimilarity(content, userTags);
                    double emotionFactor = calculateEmotionFactor(content, emotionState);
                    double finalScore = similarity * emotionFactor;
                    return new AbstractMap.SimpleEntry<>(content, finalScore);
                })
                .sorted(Map.Entry.<Content, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门内容（带情绪保护）
     */
    private List<Content> getHotContentsWithEmotion(List<Content> candidates, int limit, EmotionState emotionState) {
        return candidates.stream()
                .map(content -> {
                    double hotScore = calculateHotScore(content);
                    double emotionFactor = calculateEmotionFactor(content, emotionState);
                    double finalScore = hotScore * emotionFactor;
                    return new AbstractMap.SimpleEntry<>(content, finalScore);
                })
                .sorted(Map.Entry.<Content, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 计算情绪因子（用于调整推荐权重）
     */
    private double calculateEmotionFactor(Content content, EmotionState emotionState) {
        Integer emotionType = content.getSafety() != null ? content.getSafety().getEmotionType() : null;
        if (emotionType == null) {
            emotionType = 2;
        }

        // 用户情绪正常时，正常推荐
        if (!emotionState.needIntervention) {
            return 1.0;
        }

        // 用户情绪需要干预时，调整权重
        switch (emotionType) {
            case 1: return 1.5;  // 积极内容：加权
            case 2: return 1.0;  // 中性内容：正常
            case 3: return 0.5;  // 消极内容：降权
            default: return 1.0;
        }
    }

    /**
     * 获取积极内容
     */
    private List<Content> getPositiveContents(List<Content> candidates, int limit, Set<Long> excludeIds) {
        return candidates.stream()
                .filter(c -> !excludeIds.contains(c.getId()))
                .filter(c -> {
                    Integer emotionType = c.getSafety() != null ? c.getSafety().getEmotionType() : null;
                    return emotionType != null && emotionType == 1;
                })
                .map(content -> {
                    double hotScore = calculateHotScore(content);
                    return new AbstractMap.SimpleEntry<>(content, hotScore);
                })
                .sorted(Map.Entry.<Content, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 情绪干预：插入积极内容
     */
    private List<Content> injectPositiveContents(List<Content> recommendations, List<Content> candidates, EmotionState emotionState) {
        Set<Long> existingIds = recommendations.stream()
                .map(Content::getId)
                .collect(Collectors.toSet());

        List<Content> positiveContents = candidates.stream()
                .filter(c -> !existingIds.contains(c.getId()))
                .filter(c -> c.getSafety() != null && c.getSafety().getEmotionType() != null && c.getSafety().getEmotionType() == 1)
                .limit(INTERVENTION_POSITIVE_COUNT)
                .collect(Collectors.toList());

        // 将积极内容插入到列表开头
        recommendations.addAll(0, positiveContents);
        return recommendations;
    }

    /**
     * 控制消极内容比例
     */
    private List<Content> controlNegativeContentRatio(List<Content> recommendations, EmotionState emotionState) {
        double limit = emotionState.needIntervention 
                ? NEGATIVE_CONTENT_RATIO_LIMIT_INTERVENTION 
                : NEGATIVE_CONTENT_RATIO_LIMIT;

        int maxNegativeCount = (int) (recommendations.size() * limit);
        int currentNegativeCount = 0;
        List<Content> result = new ArrayList<>();

        for (Content content : recommendations) {
            Integer emotionType = content.getSafety() != null ? content.getSafety().getEmotionType() : null;
            boolean isNegative = emotionType != null && emotionType == 3;

            if (isNegative) {
                if (currentNegativeCount < maxNegativeCount) {
                    result.add(content);
                    currentNegativeCount++;
                }
            } else {
                result.add(content);
            }
        }

        return result;
    }

}
