
package com.myk.emotionalHole.service;

import java.util.Map;

public interface HotRankingService {

    Map<String, Object> getHotRanking(int pageNum, int pageSize);
}
