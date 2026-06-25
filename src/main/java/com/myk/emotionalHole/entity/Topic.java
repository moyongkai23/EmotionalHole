package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Topic implements Serializable {

    private Long id;
    private String topicName;
    private Integer isActive;
    private String createTime;

}