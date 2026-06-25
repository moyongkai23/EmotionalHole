package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Topic;
import com.myk.emotionalHole.mapper.TopicMapper;
import com.myk.emotionalHole.util.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 话题分类控制器
 *
 * 提供话题列表、话题详情查询接口
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private TopicMapper topicMapper;

    @GetMapping("/list")
    public Result<List<Topic>> getCategoryList() {
        try {
            List<Topic> topics = topicMapper.getAllTopics();
            return Result.success(topics);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

}