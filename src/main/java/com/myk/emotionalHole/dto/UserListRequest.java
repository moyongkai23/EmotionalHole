package com.myk.emotionalHole.dto;

import lombok.Data;

/**
 * 用户列表请求参数
 */
@Data
public class UserListRequest {
    private int page = 1;
    private int size = 10;
    private String search;
    private Integer userStatus;
}
