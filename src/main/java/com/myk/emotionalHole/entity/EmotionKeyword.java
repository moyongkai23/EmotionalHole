package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class EmotionKeyword implements Serializable {

    private Long id;
    private String keyword;
    private Integer emotionType;
    private Double weight;
    private Integer status;
    private String createTime;
    private String updateTime;

}