package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Message;
import com.myk.emotionalHole.mapper.MessageMapper;
import com.myk.emotionalHole.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 私信消息服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Resource
    private MessageMapper messageMapper;

    /** 发送私信：自动识别消息类型（普通/AI/管理员） */
    @Override
    @Transactional
    public Result<Message> sendMessage(Message message) {
        try {
            message.setMessageStatus(1);
            
            // 根据发送者设置消息类型：管理员发送的消息标记为管理员消息
            if (message.getMessageType() == null) {
                if ("admin_system".equals(message.getSenderAnonymousId())) {
                    message.setMessageType(1); // 管理员消息
                } else {
                    message.setMessageType(0); // 普通私信
                }
            }
            
            messageMapper.insert(message);
            
            if (message.getId() != null && message.getId() > 0) {
                return Result.success(message);
            }
            return Result.error(500, "发送失败");
        } catch (Exception e) {
            log.error("发送消息失败: sender={}, receiver={}", 
                message.getSenderAnonymousId(), message.getReceiverAnonymousId(), e);
            return Result.error(500, "发送消息失败: " + e.getMessage());
        }
    }

    /** 获取两个用户之间的对话消息列表 */
    @Override
    public Result<List<Message>> getConversation(String anonymousId, String targetAnonymousId, Integer offset, Integer pageSize) {
        try {
            log.debug("获取对话消息: anonymousId={}, targetAnonymousId={}, offset={}, pageSize={}",
                anonymousId, targetAnonymousId, offset, pageSize);
            List<Message> messages = messageMapper.getConversation(anonymousId, targetAnonymousId, offset, pageSize);
            log.debug("获取对话消息结果: count={}", messages != null ? messages.size() : 0);
            if (messages != null && log.isDebugEnabled()) {
                for (Message msg : messages) {
                    log.debug("消息详情: id={}, sender={}, receiver={}, text={}",
                        msg.getId(), msg.getSenderAnonymousId(), msg.getReceiverAnonymousId(), msg.getMessageText());
                }
            }
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取对话消息失败: anonymousId={}, targetAnonymousId={}", anonymousId, targetAnonymousId, e);
            return Result.error(500, "获取对话失败");
        }
    }

    /** 获取用户的会话列表（最近联系人） */
    @Override
    public Result<List<Message>> getConversations(String anonymousId, Integer offset, Integer pageSize) {
        try {
            log.debug("获取对话列表: anonymousId={}, offset={}, pageSize={}",
                anonymousId, offset, pageSize);
            List<Message> conversations = messageMapper.getConversations(anonymousId, offset, pageSize);
            log.debug("获取对话列表结果: count={}", conversations != null ? conversations.size() : 0);
            if (conversations != null && !conversations.isEmpty() && log.isDebugEnabled()) {
                for (Message msg : conversations) {
                    log.debug("对话消息: sender={}, receiver={}, text={}",
                        msg.getSenderAnonymousId(), msg.getReceiverAnonymousId(), msg.getMessageText());
                }
            }
            return Result.success(conversations);
        } catch (Exception e) {
            log.error("获取对话列表失败: anonymousId={}", anonymousId, e);
            return Result.error(500, "获取对话列表失败: " + e.getMessage());
        }
    }

    /** 删除消息（发送者或接收者均可操作） */
    @Override
    public Result<Void> deleteMessage(Long id, String anonymousId) {
        try {
            Message message = messageMapper.selectById(id);
            if (message == null) {
                return Result.error(404, "消息不存在");
            }
            
            if (!message.getSenderAnonymousId().equals(anonymousId) && 
                !message.getReceiverAnonymousId().equals(anonymousId)) {
                return Result.error(403, "无权删除该消息");
            }

            int rows = messageMapper.delete(id);
            if (rows > 0) {
                return Result.success(null);
            }
            return Result.error(500, "删除失败");
        } catch (Exception e) {
            log.error("删除消息失败: id={}, anonymousId={}", id, anonymousId, e);
            return Result.error(500, "删除消息失败");
        }
    }
}