package com.myk.emotionalHole.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class HotRankingResponseDTO implements Serializable {

    private Long id;
    private Long contentId;
    private String contentText;
    private String imageUrls;
    private String anonymousId;
    private String avatar;
    private Integer likeCount;
    private Integer hugCount;
    private Integer commentCount;
    private BigDecimal score;
    private Integer rankingPosition;
    private String createTime;
    private Long topicId;
    private String topicName;
}
