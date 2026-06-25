package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Message;

import java.util.List;

/**
 * 私信消息服务接口
 */
public interface MessageService {

    /**
     * 发送私信
     */
    Result<Message> sendMessage(Message message);

    /**
     * 获取与特定用户的对话消息列表
     */
    Result<List<Message>> getConversation(String anonymousId, String targetAnonymousId, Integer offset, Integer pageSize);

    /**
     * 获取用户的对话列表（最近联系人）
     */
    Result<List<Message>> getConversations(String anonymousId, Integer offset, Integer pageSize);

    /**
     * 删除消息
     */
    Result<Void> deleteMessage(Long id, String anonymousId);
}