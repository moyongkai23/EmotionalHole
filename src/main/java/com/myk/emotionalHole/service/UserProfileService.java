package com.myk.emotionalHole.service;

import java.util.Map;

public interface UserProfileService {

    boolean isNewUser(String anonymousId);

    Map<String, Double> getInterestTags(String anonymousId);

    void updateProfileByBehavior(String anonymousId, Long contentId, Integer behaviorType);

}