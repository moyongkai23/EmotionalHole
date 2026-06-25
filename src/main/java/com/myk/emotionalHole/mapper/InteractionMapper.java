package com.myk.emotionalHole.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface InteractionMapper {

    /**
     * 统计总互动数
     */
    Integer countTotalInteractions();

    /**
     * 获取互动方式统计
     */
    List<Map<String, Object>> getInteractionStats();

}