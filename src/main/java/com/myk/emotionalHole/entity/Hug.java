package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Hug implements Serializable {

    private Long id;
    private int targetType;         //抱抱目标类型：1-内容抱抱，2-评论抱抱
    private Long targetId;          //抱抱目标ID（content_id/comment_id）
    private String anonymousId;     //抱抱者匿名ID
    private String createTime;
}
