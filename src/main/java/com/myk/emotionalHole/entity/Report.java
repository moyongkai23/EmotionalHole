package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Report implements Serializable {

    private Long id;
    private int targetType;             //举报目标类型：1-内容举报，2-评论举报
    private Long targetId;              //举报目标ID（content_id/comment_id）
    private String anonymousId;         //举报者匿名ID
    private String reportType;          //举报类型（如“辱骂攻击”“谣言”“广告”）
    private String reportDesc;          //举报描述（如“内容涉及敏感信息”“内容违规”“内容 spam”）
    private int handleStatus;           //处理状态：0-未处理，1-已核实（下架目标），2-未核实
    private String handleTime;          //处理时间（管理员处理后更新）
    private String handleRemark;        //处理备注（管理员填写）
    private String createTime;          //举报提交时间

}
