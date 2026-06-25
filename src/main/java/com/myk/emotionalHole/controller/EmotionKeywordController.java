package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.EmotionKeyword;
import com.myk.emotionalHole.mapper.EmotionKeywordMapper;
import com.myk.emotionalHole.service.EmotionAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/emotion-keyword")
@Slf4j
public class EmotionKeywordController {

    @Autowired
    private EmotionKeywordMapper emotionKeywordMapper;

    @Autowired
    private EmotionAnalysisService emotionAnalysisService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer emotionType) {
        List<EmotionKeyword> keywords;
        int total;

        if (keyword != null && !keyword.trim().isEmpty()) {
            keywords = emotionKeywordMapper.searchKeywordsWithPagination(keyword.trim(), (page - 1) * size, size);
            total = emotionKeywordMapper.countSearchKeywords(keyword.trim());
        } else if (emotionType != null) {
            keywords = emotionKeywordMapper.getKeywordsByTypeWithPagination(emotionType, (page - 1) * size, size);
            total = emotionKeywordMapper.countKeywordsByType(emotionType);
        } else {
            keywords = emotionKeywordMapper.getKeywordsWithPagination((page - 1) * size, size);
            total = emotionKeywordMapper.countAllKeywords();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", keywords);
        result.put("total", total);

        return Result.success(result);
    }

    @GetMapping("/search")
    public Result<List<EmotionKeyword>> search(@RequestParam String keyword) {
        List<EmotionKeyword> keywords = emotionKeywordMapper.searchKeywords(keyword);
        return Result.success(keywords);
    }

    @GetMapping("/{id}")
    public Result<EmotionKeyword> getById(@PathVariable Long id) {
        EmotionKeyword keyword = emotionKeywordMapper.getKeywordById(id);
        if (keyword == null) {
            return Result.error("关键词不存在");
        }
        return Result.success(keyword);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody EmotionKeyword keyword) {
        log.info("开始添加情绪关键词: {}", keyword);
        
        try {
            if (keyword.getKeyword() == null || keyword.getKeyword().isEmpty()) {
                log.warn("关键词为空");
                return Result.error(400, "关键词不能为空");
            }
            if (keyword.getEmotionType() == null || (keyword.getEmotionType() != 1 && keyword.getEmotionType() != 2)) {
                log.warn("情绪类型无效: {}", keyword.getEmotionType());
                return Result.error(400, "情绪类型必须为1（积极）或2（消极）");
            }
            
            if (keyword.getWeight() == null) {
                keyword.setWeight(1.0);
            } else if (keyword.getWeight() <= 0 || keyword.getWeight() > 9.99) {
                log.warn("权重值超出范围: {}", keyword.getWeight());
                return Result.error(400, "权重值必须在 0.01 到 9.99 之间");
            }

            EmotionKeyword existing = emotionKeywordMapper.getKeywordByName(keyword.getKeyword());
            if (existing != null) {
                log.warn("关键词已存在: {}", keyword.getKeyword());
                return Result.error(400, "该关键词已存在，请输入其他关键词");
            }

            if (keyword.getStatus() == null) {
                keyword.setStatus(1);
            }

            log.info("准备插入关键词: keyword={}, emotionType={}, weight={}, status={}", 
                    keyword.getKeyword(), keyword.getEmotionType(), keyword.getWeight(), keyword.getStatus());
            
            int result = emotionKeywordMapper.addKeyword(keyword);
            log.info("插入结果: {}", result);
            
            try {
                emotionAnalysisService.refreshKeywordCache();
                log.info("缓存刷新成功");
            } catch (Exception e) {
                log.error("刷新关键词缓存失败: {}", e.getMessage());
            }

            return Result.success("添加成功");
        } catch (Exception e) {
            log.error("添加情绪关键词失败: {}", e.getMessage(), e);
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody EmotionKeyword keyword) {
        try {
            log.info("开始更新情绪关键词: {}", keyword);
            
            if (keyword.getId() == null) {
                return Result.error(400, "ID不能为空");
            }

            EmotionKeyword existing = emotionKeywordMapper.getKeywordById(keyword.getId());
            if (existing == null) {
                return Result.error(400, "关键词不存在");
            }

            if (keyword.getEmotionType() != null && (keyword.getEmotionType() != 1 && keyword.getEmotionType() != 2)) {
                return Result.error(400, "情绪类型必须为1（积极）或2（消极）");
            }
            
            if (keyword.getWeight() != null && (keyword.getWeight() <= 0 || keyword.getWeight() > 9.99)) {
                return Result.error(400, "权重值必须在 0.01 到 9.99 之间");
            }

            int result = emotionKeywordMapper.updateKeyword(keyword);
            log.info("更新结果: {}", result);
            
            try {
                emotionAnalysisService.refreshKeywordCache();
                log.info("缓存刷新成功");
            } catch (Exception e) {
                log.error("刷新关键词缓存失败: {}", e.getMessage());
            }

            return Result.success("修改成功");
        } catch (Exception e) {
            log.error("更新情绪关键词失败: {}", e.getMessage(), e);
            return Result.error(500, "系统异常，请稍后重试");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        EmotionKeyword existing = emotionKeywordMapper.getKeywordById(id);
        if (existing == null) {
            return Result.error("关键词不存在");
        }

        emotionKeywordMapper.deleteKeyword(id);
        
        try {
            emotionAnalysisService.refreshKeywordCache();
        } catch (Exception e) {
            log.error("刷新关键词缓存失败: {}", e.getMessage());
        }

        return Result.success("删除成功");
    }

    @PostMapping("/refresh")
    public Result<String> refresh() {
        try {
            emotionAnalysisService.refreshKeywordCache();
            return Result.success("缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新关键词缓存失败: {}", e.getMessage());
            return Result.error("缓存刷新失败");
        }
    }

}