
package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Announcement implements Serializable {

    private Long id;
    private Integer adminId;
    private String title;
    private String contentText;
    private Integer announcementType;
    private Integer isPinned;
    private Integer announcementStatus;
    private String createTime;
    private String updateTime;
}
