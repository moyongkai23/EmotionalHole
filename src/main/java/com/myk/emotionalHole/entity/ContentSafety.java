package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class ContentSafety implements Serializable {

    private Integer riskLevel;
    private Integer riskScore;
    private String detectedKeywords;
    private Integer warningStatus;
    private Integer emotionType;
    private Double emotionScore;

}
