package com.myk.emotionalHole.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.config.WechatConfig;
import com.myk.emotionalHole.dto.LoginResponse;
import com.myk.emotionalHole.dto.UserResponseDTO;
import com.myk.emotionalHole.entity.User;
import com.myk.emotionalHole.mapper.UserMapper;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.CommentMapper;
import com.myk.emotionalHole.service.UserService;
import com.myk.emotionalHole.util.JwtUtils;
import com.myk.emotionalHole.util.ExceptionUtils;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ContentMapper contentMapper;
    
    @Resource
    private CommentMapper commentMapper;
    
    @Autowired
    private WechatConfig wechatConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 微信登录主流程
     * code → openid → 查询/创建用户 → 生成JWT → 返回登录信息
     */
    @Override
    @Transactional
    public Result<LoginResponse> userLogin(String code) {
        logger.info("开始处理用户登录请求，code: {}", code);
        try {
            // 1. 调用微信接口获取openid
            String openid = getOpenidFromWechat(code);
            logger.info("获取openid完成，openid: {}", openid);
            // 2. 查询或创建用户
            UserInfo userInfo = createOrGetUser(openid);
            // 3. 校验用户状态
            checkUserStatus(userInfo.userStatus, openid);
            // 4. 生成JWT令牌
            TokenInfo tokens = generateTokens(openid);
            // 5. 构建响应数据
            LoginResponse response = buildLoginResponse(userInfo, tokens);
            logger.info("登录成功");
            return Result.success(response);
        } catch (Exception e) {
            logger.error("登录失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试: " + e.getMessage());
        }
    }

    /**
     * 查询或创建用户
     */
    private UserInfo createOrGetUser(String openid) {
        User user = userMapper.getUserByOpenid(openid);
        
        if (user != null) {
            logger.info("用户已存在，获取用户信息");
            return new UserInfo(user.getAnonymousId(), user.getUserStatus(), user.getAvatar());
        }
        
        logger.info("用户不存在，创建新用户");
        String anonymousId = generateAnonymousId();
        int userStatus = 1; // 1-正常状态
        String avatar = null;
        
        User newUser = new User();
        newUser.setAnonymousId(anonymousId);
        newUser.setOpenid(openid);
        newUser.setAvatar(avatar);
        newUser.setUserStatus(userStatus);
        
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        newUser.setCreateTime(currentTime);
        newUser.setUpdateTime(currentTime);
        
        int result = userMapper.insertUser(newUser);
        if (result <= 0) {
            throw new RuntimeException("创建用户失败");
        }
        
        return new UserInfo(anonymousId, userStatus, avatar);
    }

    /**
     * 校验用户状态
     */
    private void checkUserStatus(int userStatus, String openid) {
        if (userStatus == 3) { // 3-封禁
            logger.warn("用户账号被禁用，openid: {}", openid);
            throw ExceptionUtils.createForbiddenException("账号已被禁用");
        }
    }

    /**
     * 生成JWT令牌
     */
    private TokenInfo generateTokens(String openid) {
        String accessToken = JwtUtils.generateAccessToken(openid);
        String refreshToken = JwtUtils.generateRefreshToken(openid);
        return new TokenInfo(accessToken, refreshToken);
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(UserInfo userInfo, TokenInfo tokens) {
        return new LoginResponse(
                userInfo.anonymousId,
                userInfo.userStatus,
                tokens.accessToken,
                tokens.refreshToken,
                userInfo.avatar
        );
    }

    /**
     * 用户信息内部类
     */
    private static class UserInfo {
        String anonymousId;
        int userStatus;
        String avatar;

        UserInfo(String anonymousId, int userStatus, String avatar) {
            this.anonymousId = anonymousId;
            this.userStatus = userStatus;
            this.avatar = avatar;
        }
    }

    /**
     * 令牌信息内部类
     */
    private static class TokenInfo {
        String accessToken;
        String refreshToken;

        TokenInfo(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
    
    /**
     * 调用微信接口获取openid
     */
    private String getOpenidFromWechat(String code) throws Exception {
        // 构建完整的URL，包含所有参数
        String url = wechatConfig.getJscode2sessionUrl() + "?appid=" + wechatConfig.getAppId() + "&secret=" + wechatConfig.getAppSecret() + "&js_code=" + code + "&grant_type=authorization_code";
        logger.info("微信接口请求URL: {}", url);
        
        // 发送GET请求
        String response = restTemplate.getForObject(url, String.class);
        logger.info("微信接口响应内容: {}", response);
        
        // 解析响应
        Map<String, Object> resultMap = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        logger.info("解析后的响应数据: {}", resultMap);
        
        // 检查是否有错误
        if (resultMap.containsKey("errcode")) {
            Integer errcode = (Integer) resultMap.get("errcode");
            String errmsg = (String) resultMap.get("errmsg");
            logger.error("微信接口调用失败，错误码: {}, 错误信息: {}", errcode, errmsg);
            throw new RuntimeException("微信接口调用失败：" + errmsg);
        }
        
        // 获取openid
        String openid = (String) resultMap.get("openid");
        logger.info("从响应中获取到openid: {}", openid);
        if (openid == null || openid.isEmpty()) {
            logger.error("微信接口调用失败：未获取到openid");
            throw new RuntimeException("微信接口调用失败：未获取到openid");
        }
        
        return openid;
    }

    /**
     * 生成唯一的anonymousId
     * 格式：可爱昵称 + 随机数字（如"小鲸鱼123"）
     */
    private String generateAnonymousId() {
        // 可爱的前缀词汇
        String[] prefixes = {
            "小鲸鱼", "小星星", "小月亮", "小太阳", "小云朵",
            "小花朵", "小蝴蝶", "小蜜蜂", "小松鼠", "小兔子",
            "小猫咪", "小狗狗", "小熊猫", "小企鹅", "小海豚",
            "小鲸鱼", "小贝壳", "小海星", "小树叶", "小雪花",
            "小彩虹", "小水滴", "小树苗", "小草儿", "小萤火虫"
        };
        
        // 随机选择前缀
        int prefixIndex = (int) (Math.random() * prefixes.length);
        String prefix = prefixes[prefixIndex];
        
        // 生成3-4位随机数字
        int randomNum = 100 + (int) (Math.random() * 9000);
        
        return prefix + randomNum;
    }

    /**
     * 获取用户统计信息
     */
    @Override
    public Result<?> getUserStatistics(String anonymousId) {
        logger.info("获取用户统计信息，anonymousId: {}", anonymousId);
        try {
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

    /**
     * 获取用户列表（分页、搜索）
     */
    @Override
    public Result<?> getUserList(int page, int size, String search, Integer userStatus) {
        logger.info("获取用户列表，page: {}, size: {}, search: {}, userStatus: {}", 
                page, size, search, userStatus);
        try {
            // 计算分页参数
            int offset = (page - 1) * size;
            int limit = size;
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("search", search);
            params.put("userStatus", userStatus);
            params.put("offset", offset);
            params.put("limit", limit);
            
            // 查询用户列表
            List<User> userList = userMapper.getUserList(params);
            logger.info("查询用户列表完成，结果数量: {}", userList.size());
            
            // 转换为 DTO 并补充统计数据
            List<UserResponseDTO> dtoList = new ArrayList<>();
            for (User user : userList) {
                dtoList.add(convertToDTO(user));
            }
            
            // 查询用户总数
            int total = userMapper.getUserCount(params);
            logger.info("查询用户总数完成，总数: {}", total);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("list", dtoList);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("pages", (total + size - 1) / size);
            
            return Result.success(response);
        } catch (Exception e) {
            logger.error("获取用户列表失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @Override
    public Result<User> getUserDetail(Long userId) {
        logger.info("获取用户详情，userId: {}", userId);
        try {
            // 查询用户详情
            User user = userMapper.getUserById(userId);
            if (user == null) {
                logger.warn("用户不存在，userId: {}", userId);
                return Result.error(404, "用户不存在");
            }
            
            logger.info("获取用户详情完成，用户信息: {}", user);
            return Result.success(user);
        } catch (Exception e) {
            logger.error("获取用户详情失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "获取用户详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态（封禁/解封）
     */
    @Override
    @Transactional
    public Result<?> updateUserStatus(Long userId, int userStatus) {
        logger.info("更新用户状态，userId: {}, userStatus: {}", userId, userStatus);
        try {
            // 验证用户是否存在
            User user = userMapper.getUserById(userId);
            if (user == null) {
                logger.warn("用户不存在，userId: {}", userId);
                return Result.error(404, "用户不存在");
            }
            String anonymousId = user.getAnonymousId();
            String updateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // 如果是封禁操作（状态3），需要下架用户的所有内容和评论
            if (userStatus == 3) {
                logger.info("执行用户封禁操作，将下架用户 {} 的所有内容和评论", anonymousId);
                // 下架用户发布的所有内容（设置为待审核状态2）
                int contentCount = contentMapper.updateContentStatusByAnonymousId(anonymousId, 2, updateTime);
                logger.info("已下架用户 {} 的 {} 条内容", anonymousId, contentCount);
                // 下架用户发布的所有评论（设置为待审核状态0）
                int commentCount = commentMapper.updateCommentStatusByAnonymousId(anonymousId, 0);
                logger.info("已下架用户 {} 的 {} 条评论", anonymousId, commentCount);
            } else if (userStatus == 1) {
                // 如果是解封操作（状态1），需要恢复用户的所有内容和评论
                logger.info("执行用户解封操作，将恢复用户 {} 的所有内容和评论", anonymousId);
                // 恢复用户发布的所有内容（设置为正常状态1）
                int contentCount = contentMapper.updateContentStatusByAnonymousId(anonymousId, 1, updateTime);
                logger.info("已恢复用户 {} 的 {} 条内容", anonymousId, contentCount);
                // 恢复用户发布的所有评论（设置为正常状态1）
                int commentCount = commentMapper.updateCommentStatusByAnonymousId(anonymousId, 1);
                logger.info("已恢复用户 {} 的 {} 条评论", anonymousId, commentCount);
            }
            // 构建更新参数
            Map<String, Object> params = new HashMap<>();
            params.put("id", userId);
            params.put("userStatus", userStatus);
            params.put("updateTime", updateTime);
            // 更新用户状态
            int result = userMapper.updateUserStatus(params);
            if (result <= 0) {
                logger.warn("更新用户状态失败，userId: {}", userId);
                return Result.error(500, "更新用户状态失败");
            }
            logger.info("更新用户状态成功，userId: {}, userStatus: {}", userId, userStatus);
            return Result.success("更新用户状态成功");
        } catch (Exception e) {
            logger.error("更新用户状态失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户行为分析
     */
    @Override
    public Result<?> getUserBehaviorAnalysis(String anonymousId) {
        logger.info("获取用户行为分析，anonymousId: {}", anonymousId);
        try {
            // 获取用户行为统计信息
            Map<String, Object> stats = userMapper.getUserBehaviorStats(anonymousId);
            if (stats == null) {
                stats = new HashMap<>();
                stats.put("publishCount", 0);
                stats.put("likeCount", 0);
                stats.put("commentCount", 0);
            }
            
            logger.info("获取用户行为分析完成，统计信息: {}", stats);
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取用户行为分析失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "获取用户行为分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 将 User 实体转换为 UserResponseDTO
     */
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setAnonymousId(user.getAnonymousId());
        dto.setOpenid(user.getOpenid());
        dto.setAvatar(user.getAvatar());
        dto.setUserStatus(user.getUserStatus());
        
        // 时间处理（去除 .0 后缀）
        String createTime = user.getCreateTime();
        if (createTime != null && createTime.endsWith(".0")) {
            createTime = createTime.substring(0, createTime.length() - 2);
        }
        dto.setCreateTime(createTime);
        
        String updateTime = user.getUpdateTime();
        if (updateTime != null && updateTime.endsWith(".0")) {
            updateTime = updateTime.substring(0, updateTime.length() - 2);
        }
        dto.setUpdateTime(updateTime);
        
        // 查询统计数据
        String anonymousId = user.getAnonymousId();
        try {
            int contentCount = userMapper.getUserContentCount(anonymousId);
            dto.setContentCount(contentCount);
        } catch (Exception e) {
            logger.warn("查询用户内容数失败，anonymousId: {}", anonymousId, e);
            dto.setContentCount(0);
        }
        
        try {
            int likeCount = userMapper.getUserReceivedLikeCount(anonymousId);
            dto.setLikeCount(likeCount);
        } catch (Exception e) {
            logger.warn("查询用户点赞数失败，anonymousId: {}", anonymousId, e);
            dto.setLikeCount(0);
        }
        
        return dto;
    }

    @Override
    @Transactional
    public Result<?> updateAnonymousId(String oldAnonymousId, String newAnonymousId) {
        logger.info("修改匿名ID，oldAnonymousId: {}, newAnonymousId: {}", oldAnonymousId, newAnonymousId);
        
        try {
            // 1. 验证新匿名ID
            Result<?> validationResult = validateNewAnonymousId(newAnonymousId);
            if (validationResult != null) {
                return validationResult;
            }
            
            // 2. 执行跨表更新
            updateAnonymousIdInAllTables(oldAnonymousId, newAnonymousId);
            
            logger.info("修改匿名ID成功");
            return Result.success("修改匿名ID成功");
            
        } catch (Exception e) {
            logger.error("修改匿名ID失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "修改匿名ID失败: " + e.getMessage());
        }
    }

    /**
     * 验证新匿名ID
     */
    private Result<?> validateNewAnonymousId(String newAnonymousId) {
        User existingUser = userMapper.findByAnonymousId(newAnonymousId);
        if (existingUser != null) {
            return Result.error(400, "该匿名ID已被使用");
        }
        return null;
    }

    /**
     * 在所有相关表中更新匿名ID
     * 使用事务保证更新的原子性，由于anonymousId是业务字段而非主键，无需禁用外键约束
     */
    private void updateAnonymousIdInAllTables(String oldAnonymousId, String newAnonymousId) {
        // 更新 user 表
        updateUserTable(oldAnonymousId, newAnonymousId);
        // 更新所有关联表
        updateContentTable(oldAnonymousId, newAnonymousId);
        updateCommentTable(oldAnonymousId, newAnonymousId);
        updateLikeTable(oldAnonymousId, newAnonymousId);
        updateHugTable(oldAnonymousId, newAnonymousId);
        updateMessageTable(oldAnonymousId, newAnonymousId);
        updateReportTable(oldAnonymousId, newAnonymousId);
    }

    private void updateUserTable(String oldId, String newId) {
        int updated = userMapper.updateAnonymousId(oldId, newId);
        logger.info("更新 user 表完成，影响行数: {}", updated);
    }

    private void updateContentTable(String oldId, String newId) {
        int updated = userMapper.updateContentAnonymousId(oldId, newId);
        logger.info("更新 content 表完成，影响行数: {}", updated);
    }

    private void updateCommentTable(String oldId, String newId) {
        int updated = userMapper.updateCommentAnonymousId(oldId, newId);
        logger.info("更新 comment 表完成，影响行数: {}", updated);
    }

    private void updateLikeTable(String oldId, String newId) {
        int updated = userMapper.updateLikeAnonymousId(oldId, newId);
        logger.info("更新 like 表完成，影响行数: {}", updated);
    }

    private void updateHugTable(String oldId, String newId) {
        int updated = userMapper.updateHugAnonymousId(oldId, newId);
        logger.info("更新 hug 表完成，影响行数: {}", updated);
    }

    private void updateMessageTable(String oldId, String newId) {
        int senderUpdated = userMapper.updateMessageSenderAnonymousId(oldId, newId);
        logger.info("更新 message 表发送者完成，影响行数: {}", senderUpdated);

        int receiverUpdated = userMapper.updateMessageReceiverAnonymousId(oldId, newId);
        logger.info("更新 message 表接收者完成，影响行数: {}", receiverUpdated);
    }

    private void updateReportTable(String oldId, String newId) {
        int updated = userMapper.updateReportAnonymousId(oldId, newId);
        logger.info("更新 report 表完成，影响行数: {}", updated);
    }

    @Override
    @Transactional
    public Result<?> updateAvatar(String anonymousId, String avatar) {
        logger.info("更新用户头像，anonymousId: {}, avatar: {}", anonymousId, avatar);
        try {
            // 验证参数
            if (anonymousId == null || anonymousId.trim().isEmpty()) {
                logger.warn("参数验证失败，匿名ID不能为空");
                return Result.error(400, "匿名ID不能为空");
            }
            
            // 构建更新参数
            Map<String, Object> params = new HashMap<>();
            params.put("anonymousId", anonymousId);
            params.put("avatar", avatar);
            params.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 更新头像
            int result = userMapper.updateAvatar(params);
            if (result <= 0) {
                logger.warn("更新用户头像失败，可能用户不存在，anonymousId: {}", anonymousId);
                return Result.error(500, "更新头像失败");
            }
            
            logger.info("更新用户头像成功，anonymousId: {}", anonymousId);
            return Result.success("更新头像成功");
        } catch (Exception e) {
            logger.error("更新用户头像失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "更新头像失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getUserInfo(String anonymousId) {
        logger.info("获取用户信息，anonymousId: {}", anonymousId);
        try {
            User user = userMapper.getUserByAnonymousId(anonymousId);
            if (user == null) {
                logger.warn("用户不存在，anonymousId: {}", anonymousId);
                return Result.error(404, "用户不存在");
            }
            
            // 返回用户信息（只返回必要字段，不返回敏感信息）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("anonymousId", user.getAnonymousId());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("userStatus", user.getUserStatus());
            
            return Result.success(userInfo);
        } catch (Exception e) {
            logger.error("获取用户信息失败，异常信息: {}", e.getMessage(), e);
            return Result.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }

}
