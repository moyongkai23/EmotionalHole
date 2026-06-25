package com.myk.emotionalHole.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改匿名ID请求DTO
 */
@Data
public class UpdateAnonymousIdRequest {

    /**
     * 当前匿名ID
     */
    @NotBlank(message = "当前匿名ID不能为空")
    private String currentAnonymousId;

    /**
     * 新的匿名ID
     */
    @NotBlank(message = "匿名ID不能为空")
    @Size(min = 2, max = 50, message = "匿名ID长度必须在2-50个字符之间")
    private String newAnonymousId;
}