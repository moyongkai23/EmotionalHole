package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为记录实体类
 * 用于记录用户的浏览、点赞、评论、抱抱等行为，为推荐算法提供数据基础
 */
@Data
public class UserBehavior implements Serializable {

    private Long id;
    private String anonymousId;           // 用户匿名ID
    private Long contentId;               // 内容ID
    private Integer behaviorType;         // 行为类型：1-浏览，2-点赞，3-评论，4-抱抱
    private Double behaviorWeight;        // 行为权重
    private LocalDateTime createTime;     // 行为发生时间

}
