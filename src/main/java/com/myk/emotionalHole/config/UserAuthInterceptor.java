package com.myk.emotionalHole.config;

import com.myk.emotionalHole.entity.User;
import com.myk.emotionalHole.mapper.UserMapper;
import com.myk.emotionalHole.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户端JWT认证拦截器
 * 从Authorization头中解析JWT，提取openid并查询对应的anonymousId设入request attribute
 */
@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtils.validateToken(token)) {
                String openid = JwtUtils.extractOpenid(token);
                if (openid != null) {
                    // 通过openid查询用户的真实anonymousId
                    User user = userMapper.getUserByOpenid(openid);
                    if (user != null) {
                        // 检查用户是否被封禁
                        if (user.getUserStatus() == 3) {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"code\":403,\"message\":\"账号已被禁用\",\"data\":null}");
                            return false;
                        }
                        request.setAttribute("anonymousId", user.getAnonymousId());
                        return true;
                    }
                }
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\",\"data\":null}");
        return false;
    }
}
