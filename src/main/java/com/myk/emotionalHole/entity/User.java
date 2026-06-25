package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class User implements Serializable {

    private Long id;
    private String anonymousId;     //匿名用户id,如树洞用户_89757
    private String openid;          //用户唯一标识
    private String avatar;          //用户头像URL
    private int userStatus;         //1-正常，2-受限（禁止发布），3-封禁
    private String createTime;
    private String updateTime;
}
