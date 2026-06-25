package com.myk.emotionalHole.service;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Message;

import java.util.List;

public interface AiService {

    /**
     * AI对话接口
     * @param anonymousId 匿名用户ID
     * @param userInput 用户输入内容
     * @return AI回复结果
     */
    Result<String> aiChat(String anonymousId, String userInput);

    /**
     * 获取对话历史
     * @param anonymousId 匿名用户ID
     * @return 对话历史列表
     */
    Result<List<Message>> getChatHistory(String anonymousId);

    /**
     * 删除对话历史
     * @param anonymousId 匿名用户ID
     * @return 删除结果
     */
    Result<Boolean> deleteChatHistory(String anonymousId);
}