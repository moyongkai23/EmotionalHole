package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserProfile implements Serializable {

    private Long id;
    private String anonymousId;
    private String interestTags;
    private String createTime;
    private String updateTime;

}