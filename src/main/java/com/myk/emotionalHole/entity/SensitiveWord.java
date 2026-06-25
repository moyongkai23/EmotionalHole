package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class SensitiveWord implements Serializable {

    private Integer id;
    private String word;                //敏感词内容（如辱骂性词汇、违规词汇）
    private int keywordType;            //关键词类型：1-普通敏感词，2-心理危机关键词（严重），3-心理危机关键词（中等）
    private String createTime;
}
