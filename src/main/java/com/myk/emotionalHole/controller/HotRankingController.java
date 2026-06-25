package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.service.HotRankingService;
import com.myk.emotionalHole.util.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 热榜控制器
 *
 * 提供热榜排行查询、手动触发计算、计算状态查询接口
 */
@RestController
@RequestMapping("/hot-ranking")
public class HotRankingController {

    @Resource
    private HotRankingService hotRankingService;

    @GetMapping("/list")
    public Result<List<?>> getHotRanking() {
        try {
            Map<String, Object> page = hotRankingService.getHotRanking(1, 50);
            List<?> list = (List<?>) page.get("list");
            return Result.success(list);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }
}
