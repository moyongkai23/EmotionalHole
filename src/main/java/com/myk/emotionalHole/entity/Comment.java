package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Comment implements Serializable {

    private Long id;
    private Long contentId;      // 关联内容ID
    private Long parentId;       // 父评论ID，NULL表示直接对树洞内容的评论
    private String commentText;  // 评论内容，限制500字
    private String anonymousId;  // 匿名用户ID
    private String avatar;       // 用户头像URL
    private int likeCount;        // 评论点赞数
    private int commentStatus; // 评论状态：0-待审核，1-已发布，2-已下架
    private String createTime;   // 创建时间

}
