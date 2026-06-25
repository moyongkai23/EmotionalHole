package com.myk.emotionalHole.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ReportResponseDTO implements Serializable {

    private Long id;
    private String targetType;              // 举报目标类型：content 或 comment
    private Long targetId;                  // 举报目标ID
    private String reporterAnonymousId;     // 举报者匿名ID
    private String reportType;              // 举报类型
    private String reportDesc;               // 举报描述
    private String reportReason;            // 举报原因（reportType + reportDesc）
    private String status;                  // 处理状态：pending、processed、ignored
    private String reportTime;              // 举报时间
    private String handleTime;              // 处理时间
    private String handleRemark;            // 处理备注
    private String contentTitle;            // 内容标题（内容举报）
    private String contentText;             // 内容文本
    private String targetAnonymousId;       // 被举报用户匿名ID
}
