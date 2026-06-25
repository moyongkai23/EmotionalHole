package com.myk.emotionalHole.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ContentResponseDTO implements Serializable {

    private Long id;                    // 内容ID
    private String title;               // 内容标题
    private Integer status;             // 内容状态：0-待审核，1-已发布，2-已下架
    private String anonymousId;         // 发布者匿名ID
    private String avatar;              // 发布者头像
    private String publishTime;         // 发布时间
    private Integer viewCount;          // 浏览次数
    private Integer likeCount;          // 点赞次数
    private Integer commentCount;       // 评论次数
}
