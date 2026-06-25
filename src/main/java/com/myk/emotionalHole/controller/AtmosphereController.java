package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.service.AtmosphereService;
import com.myk.emotionalHole.service.PageType;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 社区氛围控制器
 *
 * 提供社区支持率、互动热度等氛围指标数据
 */
@RestController
@RequestMapping("/api/atmosphere")
public class AtmosphereController {

    @Resource
    private AtmosphereService atmosphereService;

    @GetMapping("/support-rate")
    public Result<Map<String, Object>> getCommunitySupportRate() {
        double supportRate = atmosphereService.getCommunitySupportRate();
        
        Map<String, Object> result = new HashMap<>();
        result.put("supportRate", Math.round(supportRate * 100) / 100.0);
        result.put("description", "社区整体支持度（暖心+抱抱占总互动的比例）");
        
        return Result.success(result);
    }

    @GetMapping("/factors")
    public Result<Map<String, Object>> getAtmosphereFactors() {
        Map<String, Object> factors = new HashMap<>();
        
        for (PageType pageType : PageType.values()) {
            Map<String, Object> factorInfo = new HashMap<>();
            factorInfo.put("description", pageType.getDescription());
            factorInfo.put("factor", pageType.getAtmosphereFactor());
            factors.put(pageType.name(), factorInfo);
        }
        
        return Result.success(factors);
    }

    @GetMapping("/content/{contentId}")
    public Result<Map<String, Object>> getContentSupportScore(@PathVariable Long contentId) {
        double supportScore = atmosphereService.calculateSupportScore(contentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("contentId", contentId);
        result.put("supportScore", Math.round(supportScore * 100) / 100.0);
        result.put("description", "该内容的支持度得分（暖心+抱抱占总互动的比例）");
        
        return Result.success(result);
    }

    @GetMapping("/score/{contentId}/{pageType}")
    public Result<Map<String, Object>> getWeightedScore(
            @PathVariable Long contentId, 
            @PathVariable String pageType,
            @RequestParam(defaultValue = "1.0") double baseScore) {
        
        PageType type = PageType.fromString(pageType);
        double weightedScore = atmosphereService.applyAtmosphereWeight(baseScore, contentId, type);
        
        Map<String, Object> result = new HashMap<>();
        result.put("contentId", contentId);
        result.put("pageType", type.name());
        result.put("pageDescription", type.getDescription());
        result.put("atmosphereFactor", type.getAtmosphereFactor());
        result.put("baseScore", baseScore);
        result.put("weightedScore", Math.round(weightedScore * 100) / 100.0);
        
        return Result.success(result);
    }
}