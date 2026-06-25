package com.myk.emotionalHole.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserResponseDTO implements Serializable {

    private Long id;
    private String anonymousId;         // 匿名用户id
    private String openid;              // 用户唯一标识
    private String avatar;              // 用户头像URL
    private int userStatus;             // 1-正常，2-受限（禁止发布），3-封禁
    private String createTime;          // 创建时间
    private String updateTime;          // 更新时间
    private int contentCount;           // 发布内容数
    private int likeCount;              // 收到点赞数
}
