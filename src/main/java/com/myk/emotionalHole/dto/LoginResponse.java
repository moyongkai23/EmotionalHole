package com.myk.emotionalHole.dto;

/**
 * 登录响应数据
 */
public class LoginResponse {
    private String anonymousId;
    private int userStatus;
    private String token;
    private String refreshToken;
    private String avatar;

    /**
     * 无参构造函数
     */
    public LoginResponse() {
    }

    /**
     * 带参数的构造函数（管理员登录使用）
     */
    public LoginResponse(String adminName, int adminStatus, String token, String refreshToken, String avatar) {
        this.anonymousId = adminName;
        this.userStatus = adminStatus;
        this.token = token;
        this.refreshToken = refreshToken;
        this.avatar = avatar;
    }

    public String getAnonymousId() {
        return anonymousId;
    }

    public void setAnonymousId(String anonymousId) {
        this.anonymousId = anonymousId;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}