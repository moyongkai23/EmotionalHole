package com.myk.emotionalHole.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员JWT工具类
 */
@Component
public class AdminJwtUtils {

    @Value("${jwt.admin-secret}")
    private String secretKey;

    /**
     * 访问令牌过期时间：2小时（毫秒）
     */
    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000;

    /**
     * 刷新令牌过期时间：7天（毫秒）
     */
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成访问令牌
     * @param adminAccount 管理员账号
     * @return 访问令牌
     */
    public String generateAccessToken(String adminAccount) {
        Date expirationDate = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", adminAccount);
        payload.put("exp", expirationDate.getTime() / 1000);
        payload.put("type", "access");
        payload.put("role", "admin");

        return JWTUtil.createToken(payload, secretKey.getBytes());
    }

    /**
     * 生成刷新令牌
     * @param adminAccount 管理员账号
     * @return 刷新令牌
     */
    public String generateRefreshToken(String adminAccount) {
        Date expirationDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", adminAccount);
        payload.put("exp", expirationDate.getTime() / 1000);
        payload.put("type", "refresh");
        payload.put("role", "admin");

        return JWTUtil.createToken(payload, secretKey.getBytes());
    }

    /**
     * 校验JWT令牌（验证签名和过期时间）
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            if (!jwt.setKey(secretKey.getBytes()).verify()) {
                return false;
            }
            // 显式验证过期时间
            Object exp = jwt.getPayload("exp");
            if (exp != null) {
                long expTime = Long.parseLong(exp.toString());
                if (System.currentTimeMillis() / 1000 > expTime) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从JWT令牌中提取管理员账号
     * @param token JWT令牌
     * @return 管理员账号
     */
    public String extractAdminAccount(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            if (jwt.setKey(secretKey.getBytes()).verify()) {
                return jwt.getPayload("sub").toString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从JWT令牌中提取令牌类型
     * @param token JWT令牌
     * @return 令牌类型
     */
    public String extractTokenType(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            if (jwt.setKey(secretKey.getBytes()).verify()) {
                return jwt.getPayload("type").toString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌，如果刷新令牌无效则返回null
     */
    public String refreshAccessToken(String refreshToken) {
        try {
            JWT jwt = JWTUtil.parseToken(refreshToken);
            if (jwt.setKey(secretKey.getBytes()).verify()) {
                // 显式验证过期时间
                Object exp = jwt.getPayload("exp");
                if (exp != null) {
                    long expTime = Long.parseLong(exp.toString());
                    if (System.currentTimeMillis() / 1000 > expTime) {
                        return null;
                    }
                }
                String type = jwt.getPayload("type").toString();
                if ("refresh".equals(type)) {
                    String adminAccount = jwt.getPayload("sub").toString();
                    return generateAccessToken(adminAccount);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
