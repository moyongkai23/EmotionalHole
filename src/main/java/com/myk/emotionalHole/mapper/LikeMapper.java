package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LikeMapper {

    /**
     * 新增点赞
     */
    int insertLike(Like like);

    /**
     * 删除点赞
     */
    int deleteLike(int targetType, Long targetId, String anonymousId);

    /**
     * 根据目标ID和类型查询点赞
     */
    List<Like> getLikesByTarget(int targetType, Long targetId);

    /**
     * 获取用户点赞的内容列表
     */
    List<Content> getMyLikes(int offset, int pageSize, String anonymousId);

    /**
     * 批量查询点赞状态
     */
    List<Like> batchGetLikeStatus(int targetType, @Param("targetIds") List<Long> targetIds, String anonymousId);

    /**
     * 批量获取点赞数量
     */
    List<Map<String, Object>> batchGetLikeCount(int targetType, @Param("targetIds") List<Long> targetIds);

    /**
     * 根据目标类型和目标ID删除所有点赞
     */
    int deleteByTargetTypeAndTargetId(@Param("targetType") int targetType, @Param("targetId") Long targetId);

}
