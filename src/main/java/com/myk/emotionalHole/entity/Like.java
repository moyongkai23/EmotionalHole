package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Like implements Serializable {

    private Long id;
    private int targetType;         //点赞目标类型：1-内容点赞，2-评论点赞
    private Long targetId;          //点赞目标ID（content_id/comment_id）
    private String anonymousId;     //点赞者匿名ID
    private String createTime;
}
