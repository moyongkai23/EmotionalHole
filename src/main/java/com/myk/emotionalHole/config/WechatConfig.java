package com.myk.emotionalHole.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置类
 * 使用@ConfigurationProperties绑定application.yml中的wechat配置
 */
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {
    private String appId;              // 小程序AppID
    private String appSecret;          // 小程序AppSecret
    private String jscode2sessionUrl;  // 微信登录凭证校验接口

    // getter和setter方法
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getJscode2sessionUrl() {
        return jscode2sessionUrl;
    }

    public void setJscode2sessionUrl(String jscode2sessionUrl) {
        this.jscode2sessionUrl = jscode2sessionUrl;
    }
}
