package com.myk.emotionalHole.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitReportRequestDTO {

    @NotNull(message = "举报目标类型不能为空")
    private Integer targetType;

    @NotNull(message = "举报目标ID不能为空")
    private Long targetId;

    @NotBlank(message = "举报者匿名ID不能为空")
    private String anonymousId;

    @NotBlank(message = "举报类型不能为空")
    private String reportType;

    private String reportDesc;
}