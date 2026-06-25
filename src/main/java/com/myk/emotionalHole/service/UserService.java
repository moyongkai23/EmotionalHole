package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.LoginResponse;
import com.myk.emotionalHole.entity.User;


public interface UserService {

    Result<LoginResponse> userLogin(String code);

    Result<?> getUserStatistics(String anonymousId);

    /**
     * 获取用户列表（分页、搜索）
     */
    Result<?> getUserList(int page, int size, String search, Integer userStatus);

    /**
     * 获取用户详情
     */
    Result<User> getUserDetail(Long userId);

    /**
     * 更新用户状态（封禁/解封）
     */
    Result<?> updateUserStatus(Long userId, int userStatus);

    /**
     * 获取用户行为分析
     */
    Result<?> getUserBehaviorAnalysis(String anonymousId);

    /**
     * 修改匿名ID
     */
    Result<?> updateAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新用户头像
     */
    Result<?> updateAvatar(String anonymousId, String avatar);

    /**
     * 获取用户信息
     */
    Result<?> getUserInfo(String anonymousId);

}
