package com.myk.emotionalHole.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    @NotBlank(message = "登录凭证不能为空")
    private String code;
}