package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信消息数据访问层
 */
@Mapper
public interface MessageMapper {

    /**
     * 插入消息
     */
    int insert(Message message);

    /**
     * 删除消息
     */
    int delete(Long id);

    /**
     * 根据ID查询消息
     */
    Message selectById(Long id);

    /**
     * 获取与特定用户的对话消息列表
     */
    List<Message> getConversation(
            @Param("anonymousId") String anonymousId,
            @Param("targetAnonymousId") String targetAnonymousId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    /**
     * 获取用户的对话列表（最近联系人）
     */
    List<Message> getConversations(
            @Param("anonymousId") String anonymousId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);
}