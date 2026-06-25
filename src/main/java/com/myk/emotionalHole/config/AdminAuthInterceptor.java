package com.myk.emotionalHole.config;

import com.myk.emotionalHole.entity.Admin;
import com.myk.emotionalHole.mapper.AdminMapper;
import com.myk.emotionalHole.util.AdminJwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理端JWT认证拦截器
 *
 * 从Authorization头中解析Admin JWT，验证管理员身份并注入adminId
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AdminJwtUtils adminJwtUtils;

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (adminJwtUtils.validateToken(token)) {
                // 将管理员账号设入 request attribute，供后续使用
                String adminAccount = adminJwtUtils.extractAdminAccount(token);
                if (adminAccount != null) {
                    // 检查管理员状态
                    Admin admin = adminMapper.getAdminByAccount(adminAccount);
                    if (admin != null && admin.getAdminStatus() == 2) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("{\"code\":403,\"message\":\"管理员账号已被禁用\",\"data\":null}");
                        return false;
                    }
                    request.setAttribute("adminAccount", adminAccount);
                }
                return true;
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\",\"data\":null}");
        return false;
    }
}
