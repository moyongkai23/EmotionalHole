package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.UserProfile;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.UserProfileMapper;
import com.myk.emotionalHole.service.UserProfileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户画像服务实现类
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private ContentMapper contentMapper;

    // 分类标签映射：分类ID对应标签列表
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

    @Override
    public boolean isNewUser(String anonymousId) {
        UserProfile profile = userProfileMapper.selectByAnonymousId(anonymousId);
        return profile == null;
    }

    @Override
    public Map<String, Double> getInterestTags(String anonymousId) {
        UserProfile profile = userProfileMapper.selectByAnonymousId(anonymousId);
        if (profile == null) {
            return new HashMap<>();
        }

        Map<String, Double> tags = new HashMap<>();
        String interestTags = profile.getInterestTags();
        if (interestTags != null && !interestTags.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                tags = mapper.readValue(interestTags, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Double>>() {});
            } catch (Exception e) {
                // JSON解析失败，说明是旧格式数据，直接返回空Map
                // 这样新的写入会使用正确的JSON格式
                return new HashMap<>();
            }
        }

        return tags;
    }

    @Override
    @Transactional
    public void updateProfileByBehavior(String anonymousId, Long contentId, Integer behaviorType) {
        // 获取内容信息，了解其分类
        Content content = contentMapper.getContentById(contentId);
        if (content == null) {
            return;
        }

        // 获取用户当前画像
        UserProfile profile = userProfileMapper.selectByAnonymousId(anonymousId);
        Map<String, Double> interestTags = getInterestTags(anonymousId);

        // 根据行为类型计算权重增量
        double weightIncrement = getWeightIncrement(behaviorType);

        // 更新预设话题标签权重
        Long topicId = content.getTopicId();
        String[] topicTags = CATEGORY_TAGS.getOrDefault(topicId, new String[]{});
        for (String tag : topicTags) {
            double currentWeight = interestTags.getOrDefault(tag, 0.0);
            double newWeight = Math.min(currentWeight + weightIncrement, 1.0);
            interestTags.put(tag, newWeight);
        }

        // 构建JSON格式的标签字符串
        String tagsString = "{}";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            tagsString = mapper.writeValueAsString(interestTags);
        } catch (Exception e) {
            // JSON序列化失败，使用旧格式作为降级方案
            StringBuilder tagsBuilder = new StringBuilder();
            for (Map.Entry<String, Double> entry : interestTags.entrySet()) {
                if (tagsBuilder.length() > 0) {
                    tagsBuilder.append(",");
                }
                tagsBuilder.append(entry.getKey()).append(":").append(entry.getValue());
            }
            tagsString = tagsBuilder.toString();
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (profile == null) {
            // 新用户，创建画像
            profile = new UserProfile();
            profile.setAnonymousId(anonymousId);
            profile.setInterestTags(tagsString);
            profile.setCreateTime(now);
            profile.setUpdateTime(now);
            userProfileMapper.insert(profile);
        } else {
            // 老用户，更新画像
            profile.setInterestTags(tagsString);
            profile.setUpdateTime(now);
            userProfileMapper.update(profile);
        }
    }

    /**
     * 根据行为类型获取权重增量
     * @param behaviorType 行为类型：1-浏览, 2-点赞, 3-评论, 4-抱抱, -2-取消点赞, -3-删除评论, -4-取消抱抱
     */
    private double getWeightIncrement(Integer behaviorType) {
        switch (behaviorType) {
            case 1: // 浏览
                return 0.05;
            case 2: // 点赞
                return 0.1;
            case -2: // 取消点赞
                return -0.1;
            case 3: // 评论
                return 0.15;
            case -3: // 删除评论
                return -0.15;
            case 4: // 抱抱
                return 0.12;
            case -4: // 取消抱抱
                return -0.12;
            default:
                return 0.05;
        }
    }

}