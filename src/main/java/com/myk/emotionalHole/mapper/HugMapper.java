package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.Hug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface HugMapper {

    /**
     * 新增抱抱
     */
    int insertHug(Hug hug);

    /**
     * 删除抱抱
     */
    int deleteHug(@Param("targetType") int targetType, @Param("targetId") Long targetId, @Param("anonymousId") String anonymousId);

    /**
     * 根据目标ID和类型查询抱抱
     */
    List<Hug> getHugsByTarget(@Param("targetType") int targetType, @Param("targetId") Long targetId);

    /**
     * 获取用户抱抱的内容列表
     */
    List<Content> getMyHugs(@Param("offset") int offset, @Param("pageSize") int pageSize, @Param("anonymousId") String anonymousId);

    /**
     * 批量查询抱抱状态
     */
    List<Hug> batchGetHugStatus(@Param("targetType") int targetType, @Param("targetIds") List<Long> targetIds, @Param("anonymousId") String anonymousId);

    /**
     * 批量获取抱抱数量
     */
    List<Map<String, Object>> batchGetHugCount(@Param("targetType") int targetType, @Param("targetIds") List<Long> targetIds);

    /**
     * 根据目标类型和目标ID删除所有抱抱
     */
    int deleteByTargetTypeAndTargetId(@Param("targetType") int targetType, @Param("targetId") Long targetId);
}
