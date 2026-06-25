package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户行为记录Mapper接口
 */
@Mapper
public interface UserBehaviorMapper {

    /**
     * 插入用户行为记录
     */
    int insert(UserBehavior userBehavior);

    /**
     * 根据用户ID查询行为记录
     */
    List<UserBehavior> selectByAnonymousId(@Param("anonymousId") String anonymousId);

    /**
     * 根据用户ID和行为类型查询
     */
    List<UserBehavior> selectByAnonymousIdAndType(@Param("anonymousId") String anonymousId,
                                                   @Param("behaviorType") Integer behaviorType);

    /**
     * 查询用户对某内容的行为记录
     */
    UserBehavior selectByUserAndContent(@Param("anonymousId") String anonymousId, 
                                        @Param("contentId") Long contentId,
                                        @Param("behaviorType") Integer behaviorType);

    /**
     * 更新行为记录
     */
    int update(UserBehavior userBehavior);

    /**
     * 获取用户最近浏览的内容ID列表
     */
    List<Long> selectRecentViewedContentIds(@Param("anonymousId") String anonymousId, @Param("limit") int limit);

}
