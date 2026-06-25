package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.LoginResponse;
import com.myk.emotionalHole.entity.Admin;
import com.myk.emotionalHole.mapper.AdminMapper;
import com.myk.emotionalHole.mapper.UserMapper;
import com.myk.emotionalHole.service.AdminService;
import com.myk.emotionalHole.util.AdminJwtUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务实现类
 * 提供管理员登录认证、管理员CRUD、用户管理功能
 * 认证：账号密码 → Admin JWT Token（独立于用户Token体系）
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private AdminJwtUtils adminJwtUtils;

    /** 管理员登录：账号密码校验→生成Admin JWT双Token */
    @Override
    @Transactional
    public Result<LoginResponse> adminLogin(String adminAccount, String adminPassword, String operateIp) {
        logger.info("开始处理管理员登录请求，账号: {}", adminAccount);
        try {
            if (adminAccount == null || adminAccount.trim().isEmpty()) {
                logger.warn("管理员账号为空");
                recordAdminLog("未知", "登录失败", "管理员账号为空", operateIp);
                return Result.error(400, "管理员账号不能为空");
            }
            if (adminPassword == null || adminPassword.trim().isEmpty()) {
                logger.warn("管理员密码为空");
                recordAdminLog(adminAccount, "登录失败", "管理员密码为空", operateIp);
                return Result.error(400, "管理员密码不能为空");
            }

            Admin admin = adminMapper.getAdminByAccount(adminAccount);
            logger.info("查询管理员信息结果: {}", admin != null ? "管理员存在" : "管理员不存在");

            if (admin == null) {
                logger.warn("管理员账号不存在: {}", adminAccount);
                recordAdminLog(adminAccount, "登录失败", "管理员账号不存在", operateIp);
                return Result.error(401, "账号或密码错误");
            }

            if (admin.getAdminStatus() != 1) {
                logger.warn("管理员账号被禁用: {}", adminAccount);
                recordAdminLog(adminAccount, "登录失败", "管理员账号被禁用", operateIp);
                return Result.error(403, "账号已被禁用");
            }

            if (!adminPassword.equals(admin.getAdminPassword())) {
                logger.warn("管理员密码错误: {}", adminAccount);
                recordAdminLog(adminAccount, "登录失败", "密码错误", operateIp);
                return Result.error(401, "账号或密码错误");
            }

            String accessToken = adminJwtUtils.generateAccessToken(adminAccount);
            String refreshToken = adminJwtUtils.generateRefreshToken(adminAccount);
            logger.info("管理员登录成功，生成令牌完成: {}", adminAccount);

            recordAdminLog(adminAccount, "登录成功", "管理员登录系统", operateIp);

            LoginResponse response = new LoginResponse(
                    admin.getAdminAvatar(),
                    admin.getAdminStatus(),
                    accessToken,
                    refreshToken,
                    null
            );

            logger.info("管理员登录请求处理完成，结果: 成功");
            return Result.success(response);
        } catch (Exception e) {
            logger.error("管理员登录请求处理失败: {}", e.getMessage(), e);
            recordAdminLog(adminAccount, "登录失败", "系统异常: " + e.getMessage(), operateIp);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /** 刷新Admin AccessToken（RefreshToken换新AccessToken） */
    @Override
    public Result<Map<String, String>> refreshToken(String refreshToken) {
        logger.info("开始处理管理员令牌刷新请求");
        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                logger.warn("刷新令牌为空");
                return Result.error(400, "刷新令牌不能为空");
            }

            String newAccessToken = adminJwtUtils.refreshAccessToken(refreshToken);
            if (newAccessToken == null) {
                logger.warn("无效的刷新令牌");
                return Result.error(401, "无效的刷新令牌");
            }

            logger.info("管理员令牌刷新成功");
            Map<String, String> response = new HashMap<>();
            response.put("token", newAccessToken);
            return Result.success(response);
        } catch (Exception e) {
            logger.error("管理员令牌刷新失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /** 记录管理员操作日志（当前仅控制台输出） */
    @Override
    public void recordAdminLog(String adminAccount, String operateType, String operateContent, String operateIp) {
        logger.info("管理员操作日志，账号: {}, 操作类型: {}, 操作内容: {}, IP: {}", 
                adminAccount, operateType, operateContent, operateIp);
    }

    /** 从请求中解析管理员信息（拦截器已验证Token） */
    @Override
    public Result<?> getAdminInfo(HttpServletRequest request) {
        logger.info("开始处理获取管理员信息请求");
        try {
            String adminAccount = (String) request.getAttribute("adminAccount");
            if (adminAccount == null) {
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    adminAccount = adminJwtUtils.extractAdminAccount(token);
                    logger.info("从token中解析出管理员账号: {}", adminAccount);
                }
            } else {
                logger.info("从请求属性中获取管理员账号: {}", adminAccount);
            }

            if (adminAccount == null) {
                logger.warn("获取管理员信息失败：未找到管理员账号");
                return Result.error(401, "未授权访问");
            }

            Admin admin = adminMapper.getAdminByAccount(adminAccount);
            if (admin == null) {
                logger.warn("管理员账号不存在: {}", adminAccount);
                return Result.error(404, "管理员不存在");
            }

            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", admin.getId());
            adminInfo.put("username", admin.getAdminAccount());
            adminInfo.put("createdAt", admin.getCreateTime());

            logger.info("获取管理员信息完成，账号: {}", adminAccount);
            return Result.success(adminInfo);
        } catch (Exception e) {
            logger.error("获取管理员信息失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /** 修改管理员密码：校验旧密码→更新新密码 */
    @Override
    @Transactional
    public Result<?> changePassword(String oldPassword, String newPassword, HttpServletRequest request) {
        logger.info("开始处理修改管理员密码请求");
        try {
            String adminAccount = (String) request.getAttribute("adminAccount");
            if (adminAccount == null) {
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    adminAccount = adminJwtUtils.extractAdminAccount(token);
                    logger.info("从token中解析出管理员账号: {}", adminAccount);
                }
            } else {
                logger.info("从请求属性中获取管理员账号: {}", adminAccount);
            }

            if (adminAccount == null) {
                logger.warn("修改密码失败：未找到管理员账号");
                return Result.error(401, "未授权访问");
            }

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                logger.warn("旧密码为空，账号: {}", adminAccount);
                return Result.error(400, "旧密码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                logger.warn("新密码为空，账号: {}", adminAccount);
                return Result.error(400, "新密码不能为空");
            }

            Admin admin = adminMapper.getAdminByAccount(adminAccount);
            if (admin == null) {
                logger.warn("管理员账号不存在: {}", adminAccount);
                return Result.error(404, "管理员不存在");
            }

            if (!oldPassword.equals(admin.getAdminPassword())) {
                logger.warn("旧密码错误，账号: {}", adminAccount);
                return Result.error(400, "旧密码错误");
            }

            if (newPassword.length() < 6) {
                logger.warn("新密码长度不足，账号: {}", adminAccount);
                return Result.error(400, "新密码长度至少6位");
            }

            admin.setAdminPassword(newPassword);
            int result = adminMapper.updateAdmin(admin);
            if (result <= 0) {
                logger.warn("密码更新失败，账号: {}", adminAccount);
                return Result.error(500, "密码更新失败");
            }

            String operateIp = getClientIp(request);
            logger.info("获取客户端IP: {}", operateIp);
            recordAdminLog(adminAccount, "修改密码", "管理员修改密码", operateIp);

            logger.info("修改管理员密码成功，账号: {}", adminAccount);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            logger.error("修改管理员密码失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            String[] ips = ip.split(",");
            for (String s : ips) {
                if (!"unknown".equalsIgnoreCase(s)) {
                    ip = s.trim();
                    break;
                }
            }
        }
        return ip;
    }

    /** 获取用户行为统计（发布/点赞/评论数量） */
    @Override
    public Result<?> getUserStatistics(String anonymousId) {
        logger.info("获取用户统计信息，anonymousId: {}", anonymousId);
        try {
            if (anonymousId == null || anonymousId.trim().isEmpty()) {
                logger.warn("用户匿名ID为空");
                return Result.error(400, "用户ID不能为空");
            }

            Map<String, Object> stats = userMapper.getUserBehaviorStats(anonymousId);
            if (stats == null) {
                stats = new HashMap<>();
                stats.put("publishCount", 0);
                stats.put("likeCount", 0);
                stats.put("commentCount", 0);
            }

            logger.info("获取用户统计信息完成，统计信息: {}", stats);
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取用户统计信息失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "获取用户统计信息失败: " + e.getMessage());
        }
    }

    /** 分页获取管理员列表 */
    @Override
    public Result<?> getAdminList(Integer page, Integer size) {
        logger.info("获取管理员列表，page: {}, size: {}", page, size);
        try {
            List<Admin> admins = adminMapper.getAllAdmins();
            
            // 分页处理
            int total = admins.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            
            List<Admin> pageAdmins;
            if (start >= total) {
                pageAdmins = new ArrayList<>();
            } else {
                pageAdmins = admins.subList(start, end);
            }
            
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Admin admin : pageAdmins) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", admin.getId());
                item.put("username", admin.getAdminAccount());
                item.put("adminAvatar", admin.getAdminAvatar());
                item.put("adminStatus", admin.getAdminStatus());
                item.put("createTime", admin.getCreateTime());
                resultList.add(item);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", resultList);
            result.put("total", total);
            
            logger.info("获取管理员列表完成，共{}条记录，当前页{}条", total, resultList.size());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取管理员列表失败: {}", e.getMessage(), e);
            return Result.error(500, "获取管理员列表失败: " + e.getMessage());
        }
    }

    /** 添加管理员：查重→创建账号 */
    @Override
    @Transactional
    public Result<?> addAdmin(String adminAccount, String adminPassword) {
        logger.info("添加管理员，账号: {}", adminAccount);
        try {
            if (adminAccount == null || adminAccount.trim().isEmpty()) {
                return Result.error(400, "管理员账号不能为空");
            }
            if (adminPassword == null || adminPassword.trim().isEmpty()) {
                return Result.error(400, "管理员密码不能为空");
            }

            Admin existing = adminMapper.getAdminByAccount(adminAccount);
            if (existing != null) {
                return Result.error(400, "管理员账号已存在");
            }

            Admin admin = new Admin();
            admin.setAdminAccount(adminAccount);
            admin.setAdminPassword(adminPassword);
            admin.setAdminAvatar("/images/admin-default-avatar.png");
            admin.setAdminStatus(1);
            admin.setCreateTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            adminMapper.insertAdmin(admin);
            logger.info("添加管理员成功，账号: {}", adminAccount);
            return Result.success("添加管理员成功");
        } catch (Exception e) {
            logger.error("添加管理员失败: {}", e.getMessage(), e);
            return Result.error(500, "添加管理员失败: " + e.getMessage());
        }
    }

    /** 删除管理员（超级管理员不可删除） */
    @Override
    @Transactional
    public Result<?> deleteAdmin(Long id) {
        logger.info("删除管理员，ID: {}", id);
        try {
            if (id == 1) {
                logger.warn("尝试删除超级管理员，ID: {}", id);
                return Result.error(403, "超级管理员不能被删除");
            }
            
            Admin admin = adminMapper.getAdminById(id);
            if (admin == null) {
                return Result.error(404, "管理员不存在");
            }

            adminMapper.deleteAdmin(id);
            logger.info("删除管理员成功，ID: {}", id);
            return Result.success("删除管理员成功");
        } catch (Exception e) {
            logger.error("删除管理员失败: {}", e.getMessage(), e);
            return Result.error(500, "删除管理员失败: " + e.getMessage());
        }
    }
}
