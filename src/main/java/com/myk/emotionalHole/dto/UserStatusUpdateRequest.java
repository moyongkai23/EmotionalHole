package com.myk.emotionalHole.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更新请求参数
 */
@Data
public class UserStatusUpdateRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "用户状态不能为空")
    private int userStatus;
}
