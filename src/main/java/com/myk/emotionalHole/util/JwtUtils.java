package com.myk.emotionalHole.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户端JWT工具类
 * 实现AccessToken（2小时）+ RefreshToken（7天）双令牌机制
 * 与AdminJwtUtils分离，用户端和管理端使用独立的认证体系
 */
@Component
public class JwtUtils {

    private static String SECRET_KEY;
    private static long ACCESS_TOKEN_EXPIRATION;
    private static long REFRESH_TOKEN_EXPIRATION;
    
    @Value("${jwt.secret}")
    public void setSecretKey(String secretKey) {
        JwtUtils.SECRET_KEY = secretKey;
    }
    
    @Value("${jwt.access-token-expiration:7200000}")
    public void setAccessTokenExpiration(long accessTokenExpiration) {
        JwtUtils.ACCESS_TOKEN_EXPIRATION = accessTokenExpiration;
    }
    
    @Value("${jwt.refresh-token-expiration:604800000}")
    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        JwtUtils.REFRESH_TOKEN_EXPIRATION = refreshTokenExpiration;
    }
    
    /**
     * 生成访问令牌
     * @param openid 用户唯一标识
     * @return 访问令牌
     */
    public static String generateAccessToken(String openid) {
        // 设置过期时间
        Date expirationDate = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION);
        
        // 生成JWT令牌
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", openid);
        payload.put("exp", expirationDate.getTime() / 1000);
        payload.put("type", "access");
        
        return JWTUtil.createToken(payload, SECRET_KEY.getBytes());
    }
    
    /**
     * 生成刷新令牌
     * @param openid 用户唯一标识
     * @return 刷新令牌
     */
    public static String generateRefreshToken(String openid) {
        // 设置过期时间
        Date expirationDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);
        
        // 生成JWT令牌
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", openid);
        payload.put("exp", expirationDate.getTime() / 1000);
        payload.put("type", "refresh");
        
        return JWTUtil.createToken(payload, SECRET_KEY.getBytes());
    }
    
    /**
     * 校验JWT令牌
     * @param token JWT令牌
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            if (!jwt.setKey(SECRET_KEY.getBytes()).verify()) {
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
     * 从JWT令牌中提取openid
     * @param token JWT令牌
     * @return openid
     */
    public static String extractOpenid(String token) {
        try {
            // 解析令牌
            JWT jwt = JWTUtil.parseToken(token);
            if (jwt.setKey(SECRET_KEY.getBytes()).verify()) {
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
    public static String extractTokenType(String token) {
        try {
            // 解析令牌
            JWT jwt = JWTUtil.parseToken(token);
            if (jwt.setKey(SECRET_KEY.getBytes()).verify()) {
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
    public static String refreshAccessToken(String refreshToken) {
        try {
            JWT jwt = JWTUtil.parseToken(refreshToken);
            if (jwt.setKey(SECRET_KEY.getBytes()).verify()) {
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
                    String openid = jwt.getPayload("sub").toString();
                    return generateAccessToken(openid);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}