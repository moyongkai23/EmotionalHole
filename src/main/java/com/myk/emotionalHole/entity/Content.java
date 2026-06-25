package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 内容实体类，对应数据库content表
 * 包含匿名用户标识、互动计数、内容状态等字段
 */
@Data
public class Content implements Serializable {

    private Long id;              // 内容ID
    private String contentText;   // 内容文本
    private String imageUrls;     // 图片URL（多个用逗号分隔）
    private String anonymousId;   // 匿名用户标识
    private String avatar;        // 匿名头像
    private Long topicId;         // 话题ID
    private String topicName;     // 话题名称
    private String customTopic;   // 自定义话题
    private int likeCount;        // 点赞数
    private int commentCount;     // 评论数
    private int hugCount;         // 抱抱数
    private int contentStatus;    // 内容状态：0待审核 1已发布 2已下架
    private String createTime;    // 创建时间
    private String updateTime;    // 更新时间

    private ContentSafety safety; // 内容安全检测结果

}