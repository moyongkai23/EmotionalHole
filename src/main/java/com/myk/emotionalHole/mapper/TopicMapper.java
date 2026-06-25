package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.Topic;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TopicMapper {

    List<Topic> getAllTopics();

    Topic getTopicByName(String name);

    int addTopic(Topic topic);

}