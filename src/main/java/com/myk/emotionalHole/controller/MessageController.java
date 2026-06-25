package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Message;
import com.myk.emotionalHole.service.MessageService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 私信消息控制器
 */
@RestController
@RequestMapping("message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /**
     * 发送私信
     */
    @PostMapping("/send")
    public Result<Message> sendMessage(@RequestBody Message message, HttpServletRequest request) {
        try {
            if (message == null || message.getMessageText() == null || message.getMessageText().trim().isEmpty()) {
                return Result.error(400, "消息内容不能为空");
            }
            if (message.getReceiverAnonymousId() == null || message.getReceiverAnonymousId().trim().isEmpty()) {
                return Result.error(400, "接收者标识不能为空");
            }
            // 从JWT获取发送者身份
            String anonymousId = (String) request.getAttribute("anonymousId");
            message.setSenderAnonymousId(anonymousId);
            return messageService.sendMessage(message);
        } catch (Exception e) {
            return Result.error(500, "发送消息失败，请重试");
        }
    }

    /**
     * 获取与特定用户的对话消息列表
     */
    @GetMapping("/conversation")
    public Result<List<Message>> getConversation(
            @RequestParam String targetAnonymousId,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer pageSize,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            if (targetAnonymousId == null || targetAnonymousId.trim().isEmpty()) {
                return Result.error(400, "目标用户标识不能为空");
            }
            return messageService.getConversation(anonymousId, targetAnonymousId, offset, pageSize);
        } catch (Exception e) {
            return Result.error(500, "获取对话列表失败");
        }
    }

    /**
     * 获取用户的对话列表（最近联系人）
     */
    @GetMapping("/conversations")
    public Result<List<Message>> getConversations(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return messageService.getConversations(anonymousId, offset, pageSize);
        } catch (Exception e) {
            return Result.error(500, "获取对话列表失败");
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteMessage(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            if (id == null) {
                return Result.error(400, "消息ID不能为空");
            }
            String anonymousId = (String) request.getAttribute("anonymousId");
            return messageService.deleteMessage(id, anonymousId);
        } catch (Exception e) {
            return Result.error(500, "删除消息失败");
        }
    }
}