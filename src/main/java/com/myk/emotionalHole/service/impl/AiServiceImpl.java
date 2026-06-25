package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Message;
import com.myk.emotionalHole.service.AiService;
import com.myk.emotionalHole.service.MessageService;
import com.myk.emotionalHole.util.AiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI聊天服务实现类
 * 基于百度千帆大模型实现情感陪伴对话
 * 流程：用户消息 → 调用千帆API → 保存对话记录 → 返回AI回复
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceImpl.class);

    @Autowired
    private AiUtils aiUtils;

    @Autowired
    private MessageService messageService;

    // AI对话：调用千帆大模型获取回复并保存对话记录
    @Override
    public Result<String> aiChat(String anonymousId, String userInput) {
        try {
            Map<String, Object> aiResult = aiUtils.getAiResponseWithEmotion(userInput);
            String aiResponse = (String) aiResult.get("response");

            saveChatHistory(anonymousId, userInput, aiResponse);

            logger.info("AI对话成功，匿名用户ID: {}, 用户输入: {}, AI回复: {}", anonymousId, userInput, aiResponse);
            return Result.success(aiResponse);
        } catch (Exception e) {
            logger.error("AI对话失败: {}", e.getMessage(), e);
            return Result.error(500, "AI对话失败，请重试");
        }
    }

    // 获取用户与AI的对话历史
    @Override
    public Result<List<Message>> getChatHistory(String anonymousId) {
        try {
            Result<List<Message>> result = messageService.getConversation(anonymousId, "ai_assistant", 0, 100);
            logger.info("获取AI对话历史成功，匿名用户ID: {}, 历史记录数: {}", anonymousId, result.getData() != null ? result.getData().size() : 0);
            return result;
        } catch (Exception e) {
            logger.error("获取AI对话历史失败: {}", e.getMessage(), e);
            return Result.error(500, "获取对话历史失败，请重试");
        }
    }

    // 清空用户与AI的对话历史
    @Override
    public Result<Boolean> deleteChatHistory(String anonymousId) {
        try {
            Result<List<Message>> result = messageService.getConversation(anonymousId, "ai_assistant", 0, 1000);
            if (result.getCode() == 200 && result.getData() != null) {
                for (Message msg : result.getData()) {
                    messageService.deleteMessage(msg.getId(), anonymousId);
                }
            }
            logger.info("删除AI对话历史成功，匿名用户ID: {}", anonymousId);
            return Result.success(true);
        } catch (Exception e) {
            logger.error("删除AI对话历史失败: {}", e.getMessage(), e);
            return Result.error(500, "删除对话历史失败，请重试");
        }
    }

    // 保存对话记录到数据库（用户消息+AI回复）
    private void saveChatHistory(String anonymousId, String userInput, String aiResponse) {
        try {
            if (anonymousId == null || anonymousId.isEmpty()) {
                logger.info("用户ID无效，跳过对话历史存储");
                return;
            }

            Message userMessage = new Message();
            userMessage.setSenderAnonymousId(anonymousId);
            userMessage.setReceiverAnonymousId("ai_assistant");
            userMessage.setMessageText(userInput);
            userMessage.setMessageStatus(1);
            userMessage.setMessageType(2);
            messageService.sendMessage(userMessage);

            Message aiMessage = new Message();
            aiMessage.setSenderAnonymousId("ai_assistant");
            aiMessage.setReceiverAnonymousId(anonymousId);
            aiMessage.setMessageText(aiResponse);
            aiMessage.setMessageStatus(1);
            aiMessage.setMessageType(2);
            messageService.sendMessage(aiMessage);

        } catch (Exception e) {
            logger.error("保存对话历史失败: {}", e.getMessage(), e);
        }
    }
}