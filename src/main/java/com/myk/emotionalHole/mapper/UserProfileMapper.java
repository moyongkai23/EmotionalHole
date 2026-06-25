package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper {

    UserProfile selectByAnonymousId(String anonymousId);

    int insert(UserProfile userProfile);

    int update(UserProfile userProfile);

}