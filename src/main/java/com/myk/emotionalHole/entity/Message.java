
package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Message implements Serializable {

    private Long id;
    private String senderAnonymousId;
    private String receiverAnonymousId;
    private String messageText;
    private Integer messageStatus;
    private String createTime;
    private Integer isAdmin;
    
    /**
     * 消息类型：0-普通私信，1-管理员消息，2-AI消息
     */
    private Integer messageType;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 接收者头像
     */
    private String receiverAvatar;
}
