package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Message;
import com.myk.emotionalHole.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI聊天控制器
 *
 * 提供AI对话、聊天历史查询、历史清空接口
 * 基于百度千帆大模型实现情感陪伴
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private AiService aiService;

    /**
     * AI对话接口
     * @param anonymousId 匿名用户ID
     * @param userInput 用户输入内容
     * @return AI回复结果
     */
    @PostMapping("/chat")
    public Result<String> aiChat(@RequestBody String userInput, HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            if (userInput == null || userInput.trim().isEmpty()) {
                return Result.error(400, "用户输入内容不能为空");
            }
            return aiService.aiChat(anonymousId, userInput);
        } catch (Exception e) {
            logger.error("AI对话接口异常: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history")
    public Result<List<Message>> getChatHistory(HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return aiService.getChatHistory(anonymousId);
        } catch (Exception e) {
            logger.error("获取对话历史接口异常: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }

    /**
     * 删除对话历史
     */
    @DeleteMapping("/history")
    public Result<Boolean> deleteChatHistory(HttpServletRequest request) {
        try {
            String anonymousId = (String) request.getAttribute("anonymousId");
            return aiService.deleteChatHistory(anonymousId);
        } catch (Exception e) {
            logger.error("删除对话历史接口异常: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请重试");
        }
    }
}