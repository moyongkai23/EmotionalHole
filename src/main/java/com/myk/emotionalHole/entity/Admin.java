package com.myk.emotionalHole.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Admin implements Serializable {

    private Integer id;
    private String adminAccount;    //管理员账号
    private String adminPassword;
    private String adminAvatar;     //管理员头像URL
    private int adminStatus;        //管理员状态，1-正常，2-禁用（无法登录）
    private String createTime;

}
