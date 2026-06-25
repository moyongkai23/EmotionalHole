package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.dto.LoginRequest;
import com.myk.emotionalHole.dto.LoginResponse;
import com.myk.emotionalHole.dto.UserListRequest;
import com.myk.emotionalHole.dto.UserStatusUpdateRequest;
import com.myk.emotionalHole.entity.User;
import com.myk.emotionalHole.service.UserService;
import com.myk.emotionalHole.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     * @param request 登录请求，包含code参数
     * @return 登录结果
     */
    @PostMapping("/user/login")
    public Result<LoginResponse> userLogin(@RequestBody @Valid LoginRequest request) {
        logger.info("开始处理用户登录请求，请求参数: {}", request);
        try {
            // 调用service层获取登录结果
            String code = request.getCode();
            Result<LoginResponse> result = userService.userLogin(code);
            logger.info("用户登录请求处理完成，结果: {}", result);
            
            return result;
        } catch (Exception e) {
            logger.error("用户登录请求处理失败: {}", e.getMessage(), e);
            return Result.error("系统异常，请重试");
        }
    }

    /**
     * 刷新令牌接口
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    @PostMapping("/user/refreshToken")
    public Result<Map<String, String>> refreshToken(@RequestParam String refreshToken) {
        logger.info("开始处理令牌刷新请求");
        try {
            // 验证刷新令牌并生成新的访问令牌
            String newAccessToken = JwtUtils.refreshAccessToken(refreshToken);
            if (newAccessToken == null) {
                logger.warn("无效的刷新令牌");
                return Result.error(401, "无效的刷新令牌");
            }
            
            logger.info("令牌刷新成功");
            Map<String, String> response = new HashMap<>();
            response.put("token", newAccessToken);
            return Result.success(response);
        } catch (Exception e) {
            logger.error("令牌刷新失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 管理员用户列表接口
     * @param request 用户列表请求参数
     * @return 用户列表
     */
    @GetMapping("/admin/users")
    public Result<?> getUserList(@Valid UserListRequest request) {
        logger.info("开始处理管理员获取用户列表请求，请求参数: {}", request);
        try {
            Result<?> result = userService.getUserList(
                    request.getPage(),
                    request.getSize(),
                    request.getSearch(),
                    request.getUserStatus()
            );
            logger.info("管理员获取用户列表请求处理完成");
            return result;
        } catch (Exception e) {
            logger.error("管理员获取用户列表请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 管理员用户详情接口
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/admin/users/{userId}")
    public Result<User> getUserDetail(@PathVariable Long userId) {
        logger.info("开始处理管理员获取用户详情请求，用户ID: {}", userId);
        try {
            Result<User> result = userService.getUserDetail(userId);
            logger.info("管理员获取用户详情请求处理完成");
            return result;
        } catch (Exception e) {
            logger.error("管理员获取用户详情请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 管理员更新用户状态接口（封禁/解封）
     * @param request 用户状态更新请求参数
     * @return 更新结果
     */
    @PutMapping("/admin/users/status")
    public Result<?> updateUserStatus(@RequestBody @Valid UserStatusUpdateRequest request) {
        logger.info("开始处理管理员更新用户状态请求，请求参数: {}", request);
        try {
            Result<?> result = userService.updateUserStatus(
                    request.getUserId(),
                    request.getUserStatus()
            );
            logger.info("管理员更新用户状态请求处理完成");
            return result;
        } catch (Exception e) {
            logger.error("管理员更新用户状态请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 管理员获取用户行为分析接口
     * @param anonymousId 匿名用户ID
     * @return 用户行为分析
     */
    @GetMapping("/admin/users/behavior/{anonymousId}")
    public Result<?> getUserBehaviorAnalysis(@PathVariable String anonymousId) {
        logger.info("开始处理管理员获取用户行为分析请求，anonymousId: {}", anonymousId);
        try {
            Result<?> result = userService.getUserBehaviorAnalysis(anonymousId);
            logger.info("管理员获取用户行为分析请求处理完成");
            return result;
        } catch (Exception e) {
            logger.error("管理员获取用户行为分析请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 获取用户统计信息接口
     * @param anonymousId 匿名用户ID
     * @return 用户统计信息
     */
    @GetMapping("/user/statistics")
    public Result<?> getUserStatistics(HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return userService.getUserStatistics(anonymousId);
        } catch (Exception e) {
            logger.error("获取用户统计信息请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 修改匿名ID接口
     * @param request 修改匿名ID请求（包含当前匿名ID和新匿名ID）
     * @return 修改结果
     */
    @PutMapping("/user/anonymousId")
    public Result<?> updateAnonymousId(
            @RequestBody @Valid com.myk.emotionalHole.dto.request.UpdateAnonymousIdRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 从JWT获取当前用户身份，覆盖客户端传入的currentAnonymousId
            String currentAnonymousId = (String) httpRequest.getAttribute("anonymousId");
            Result<?> result = userService.updateAnonymousId(currentAnonymousId, request.getNewAnonymousId());
            return result;
        } catch (Exception e) {
            logger.error("修改匿名ID请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 获取用户信息接口
     * @param anonymousId 用户匿名ID
     * @return 用户信息
     */
    @GetMapping("/user/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return userService.getUserInfo(anonymousId);
        } catch (Exception e) {
            logger.error("获取用户信息请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 更新用户头像接口
     * @param anonymousId 用户匿名ID
     * @param avatar 头像URL
     * @return 更新结果
     */
    @PutMapping("/user/avatar")
    public Result<?> updateAvatar(
            @RequestParam String avatar,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return userService.updateAvatar(anonymousId, avatar);
        } catch (Exception e) {
            logger.error("更新用户头像请求处理失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }
}

