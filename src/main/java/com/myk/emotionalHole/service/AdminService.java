package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface AdminService {
    /**
     * 管理员登录接口
     * @param adminAccount 管理员账号
     * @param adminPassword 管理员密码
     * @param operateIp 操作IP
     * @return 登录结果
     */
    Result<LoginResponse> adminLogin(String adminAccount, String adminPassword, String operateIp);

    /**
     * 刷新管理员访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    Result<Map<String, String>> refreshToken(String refreshToken);

    /**
     * 记录管理员操作日志
     * @param adminAccount 管理员账号
     * @param operateType 操作类型
     * @param operateContent 操作内容
     * @param operateIp 操作IP
     */
    void recordAdminLog(String adminAccount, String operateType, String operateContent, String operateIp);

    /**
     * 获取管理员信息
     * @param request HttpServletRequest对象
     * @return 管理员信息
     */
    Result<?> getAdminInfo(HttpServletRequest request);

    /**
     * 修改管理员密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param request HttpServletRequest对象
     * @return 修改结果
     */
    Result<?> changePassword(String oldPassword, String newPassword, HttpServletRequest request);

    /**
     * 获取用户统计信息（管理员专用）
     * @param anonymousId 用户匿名ID
     * @return 用户统计信息
     */
    Result<?> getUserStatistics(String anonymousId);

    /**
     * 获取管理员列表（支持分页）
     * @param page 页码
     * @param size 每页大小
     * @return 管理员列表
     */
    Result<?> getAdminList(Integer page, Integer size);

    /**
     * 添加管理员
     * @param adminAccount 管理员账号
     * @param adminPassword 管理员密码
     * @return 添加结果
     */
    Result<?> addAdmin(String adminAccount, String adminPassword);

    /**
     * 删除管理员
     * @param id 管理员ID
     * @return 删除结果
     */
    Result<?> deleteAdmin(Long id);
}
