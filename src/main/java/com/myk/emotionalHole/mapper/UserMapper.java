package com.myk.emotionalHole.mapper;
import com.myk.emotionalHole.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据anonymousId查询用户
     */
    User getUserByAnonymousId(String anonymousId);

    /**
     * 插入新用户
     */
    int insertUser(User user);

    /**
     * 根据openid查询用户
     */
    User getUserByOpenid(String openid);

    /**
     * 分页查询用户列表
     */
    List<User> getUserList(Map<String, Object> params);

    /**
     * 查询用户总数
     */
    int getUserCount(Map<String, Object> params);

    /**
     * 根据ID查询用户详情
     */
    User getUserById(Long id);

    /**
     * 更新用户状态
     */
    int updateUserStatus(Map<String, Object> params);

    /**
     * 获取用户行为统计信息
     */
    Map<String, Object> getUserBehaviorStats(String anonymousId);
    
    /**
     * 获取用户发布内容数
     */
    int getUserContentCount(String anonymousId);
    
    /**
     * 获取用户收到的点赞数
     */
    int getUserReceivedLikeCount(String anonymousId);

    /**
     * 根据匿名ID查询用户
     */
    User findByAnonymousId(String anonymousId);

    /**
     * 更新用户表中的匿名ID
     */
    int updateAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新content表中的匿名ID
     */
    int updateContentAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新comment表中的匿名ID
     */
    int updateCommentAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新like表中的匿名ID
     */
    int updateLikeAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新hug表中的匿名ID
     */
    int updateHugAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新message表中的发送者匿名ID
     */
    int updateMessageSenderAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新message表中的接收者匿名ID
     */
    int updateMessageReceiverAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新report表中的匿名ID
     */
    int updateReportAnonymousId(String oldAnonymousId, String newAnonymousId);

    /**
     * 更新用户头像
     */
    int updateAvatar(Map<String, Object> params);

    /**
     * 统计用户总数
     */
    Integer countUsers();

    /**
     * 获取用户增长趋势
     */
    List<Map<String, Object>> getUserTrend();

    /**
     * 统计今日活跃用户数
     */
    Integer countTodayActiveUsers(String today);
}
