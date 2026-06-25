package com.myk.emotionalHole.controller;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.SensitiveWord;
import com.myk.emotionalHole.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 敏感词管理控制器
 */
@RestController
@RequestMapping("/admin/sensitive-words")
@Tag(name = "敏感词管理", description = "敏感词的增删改查接口")
public class SensitiveWordController {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordController.class);

    @Autowired
    private SensitiveWordService sensitiveWordService;

    /**
     * 获取所有敏感词
     */
    @GetMapping
    @Operation(summary = "获取所有敏感词", description = "获取系统中所有的敏感词列表")
    public Result<List<SensitiveWord>> getAllSensitiveWords() {
        logger.info("开始处理获取所有敏感词请求");
        try {
            List<SensitiveWord> sensitiveWords = sensitiveWordService.getAllSensitiveWords();
            logger.info("获取敏感词成功，共 {} 个", sensitiveWords.size());
            return Result.success(sensitiveWords);
        } catch (Exception e) {
            logger.error("获取敏感词失败: {}", e.getMessage(), e);
            return Result.error(500, "获取敏感词失败");
        }
    }

    /**
     * 分页获取心理危机词列表（类型2和类型3）
     */
    @GetMapping("/crisis/list")
    @Operation(summary = "分页获取心理危机词列表", description = "分页获取心理危机词列表（包含严重级和中等级）")
    public Result<java.util.Map<String, Object>> getCrisisWordList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer keywordType,
            @RequestParam(required = false) String keyword) {
        logger.info("开始处理分页获取心理危机词列表请求，page: {}, pageSize: {}, keywordType: {}, keyword: {}", page, pageSize, keywordType, keyword);
        try {
            List<SensitiveWord> sensitiveWords;
            int total;
            
            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            boolean hasType = keywordType != null;
            
            if (hasKeyword && hasType) {
                sensitiveWords = sensitiveWordService.searchSensitiveWordsByKeywordAndType(keyword.trim(), keywordType, page, pageSize);
                total = sensitiveWordService.countSensitiveWordsByKeywordAndType(keyword.trim(), keywordType);
            } else if (hasKeyword) {
                sensitiveWords = sensitiveWordService.searchCrisisWordsByKeyword(keyword.trim(), page, pageSize);
                total = sensitiveWordService.countCrisisWordsByKeyword(keyword.trim());
            } else if (hasType) {
                sensitiveWords = sensitiveWordService.getSensitiveWordsByTypeAndPage(keywordType, page, pageSize);
                total = sensitiveWordService.getSensitiveWordCountByType(keywordType);
            } else {
                sensitiveWords = sensitiveWordService.getCrisisWordsByPage(page, pageSize);
                total = sensitiveWordService.countCrisisWords();
            }
            
            logger.info("获取心理危机词成功，共 {} 个", sensitiveWords.size());
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", sensitiveWords);
            response.put("total", total);
            
            return Result.success(response);
        } catch (Exception e) {
            logger.error("获取心理危机词失败: {}", e.getMessage(), e);
            return Result.error(500, "获取心理危机词失败");
        }
    }

    /**
     * 分页获取敏感词列表（兼容前端路径）
     */
    @GetMapping("/list")
    @Operation(summary = "分页获取敏感词列表", description = "分页获取系统中的敏感词列表，支持按类型筛选和关键词搜索")
    public Result<java.util.Map<String, Object>> getSensitiveWordList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer keywordType,
            @RequestParam(required = false) String keyword) {
        logger.info("开始处理分页获取敏感词列表请求，page: {}, pageSize: {}, keywordType: {}, keyword: {}", page, pageSize, keywordType, keyword);
        try {
            List<SensitiveWord> sensitiveWords;
            int total;
            
            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            boolean hasType = keywordType != null;
            
            if (hasKeyword && hasType) {
                sensitiveWords = sensitiveWordService.searchSensitiveWordsByKeywordAndType(keyword.trim(), keywordType, page, pageSize);
                total = sensitiveWordService.countSensitiveWordsByKeywordAndType(keyword.trim(), keywordType);
            } else if (hasKeyword) {
                sensitiveWords = sensitiveWordService.searchSensitiveWordsByKeyword(keyword.trim(), page, pageSize);
                total = sensitiveWordService.countSensitiveWordsByKeyword(keyword.trim());
            } else if (hasType) {
                sensitiveWords = sensitiveWordService.getSensitiveWordsByTypeAndPage(keywordType, page, pageSize);
                total = sensitiveWordService.getSensitiveWordCountByType(keywordType);
            } else {
                sensitiveWords = sensitiveWordService.getSensitiveWordsByPage(page, pageSize);
                total = sensitiveWordService.getSensitiveWordCount();
            }
            
            logger.info("获取敏感词成功，共 {} 个", sensitiveWords.size());
            
            // 构建响应数据
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", sensitiveWords);
            response.put("total", total);
            response.put("page", page);
            response.put("pageSize", pageSize);
            
            return Result.success(response);
        } catch (Exception e) {
            logger.error("获取敏感词失败: {}", e.getMessage(), e);
            return Result.error(500, "获取敏感词失败");
        }
    }

    /**
     * 根据关键词类型获取敏感词
     */
    @GetMapping("/by-type")
    @Operation(summary = "根据关键词类型获取敏感词", description = "根据关键词类型获取敏感词列表：1-普通敏感词，2-严重危机关键词，3-中等危机关键词")
    public Result<List<SensitiveWord>> getSensitiveWordsByType(@RequestParam int keywordType) {
        logger.info("开始处理根据关键词类型获取敏感词请求，类型: {}", keywordType);
        try {
            List<SensitiveWord> sensitiveWords = sensitiveWordService.getSensitiveWordsByType(keywordType);
            logger.info("获取类型 {} 敏感词成功，共 {} 个", keywordType, sensitiveWords.size());
            return Result.success(sensitiveWords);
        } catch (Exception e) {
            logger.error("获取敏感词失败: {}", e.getMessage(), e);
            return Result.error(500, "获取敏感词失败");
        }
    }

    /**
     * 添加敏感词
     */
    @PostMapping
    @Operation(summary = "添加敏感词", description = "添加新的敏感词")
    public Result<Void> addSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        logger.info("开始处理添加敏感词请求: {}", sensitiveWord);
        try {
            if (sensitiveWord.getWord() == null || sensitiveWord.getWord().trim().isEmpty()) {
                return Result.error(400, "敏感词不能为空");
            }
            
            SensitiveWord existing = sensitiveWordService.getSensitiveWordByWordAndType(sensitiveWord.getWord().trim(), sensitiveWord.getKeywordType());
            if (existing != null) {
                logger.warn("该类型下敏感词已存在: word={}, type={}", sensitiveWord.getWord(), sensitiveWord.getKeywordType());
                return Result.error(400, "该关键词在当前类型下已存在");
            }
            
            int result = sensitiveWordService.addSensitiveWord(sensitiveWord);
            if (result > 0) {
                logger.info("添加敏感词成功: {}", sensitiveWord.getWord());
                return Result.success();
            } else {
                logger.warn("添加敏感词失败: {}", sensitiveWord.getWord());
                return Result.error(500, "添加敏感词失败");
            }
        } catch (Exception e) {
            logger.error("添加敏感词失败: {}", e.getMessage(), e);
            return Result.error(500, "添加敏感词失败");
        }
    }

    /**
     * 更新敏感词
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新敏感词", description = "更新已存在的敏感词")
    public Result<Void> updateSensitiveWord(@PathVariable Integer id, @RequestBody SensitiveWord sensitiveWord) {
        logger.info("开始处理更新敏感词请求: id={}, word={}", id, sensitiveWord.getWord());
        try {
            sensitiveWord.setId(id);
            int result = sensitiveWordService.updateSensitiveWord(sensitiveWord);
            if (result > 0) {
                logger.info("更新敏感词成功: {}", sensitiveWord.getWord());
                return Result.success();
            } else {
                logger.warn("更新敏感词失败: {}", sensitiveWord.getWord());
                return Result.error(500, "更新敏感词失败");
            }
        } catch (Exception e) {
            logger.error("更新敏感词失败: {}", e.getMessage(), e);
            return Result.error(500, "更新敏感词失败");
        }
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除敏感词", description = "根据ID删除敏感词")
    public Result<Void> deleteSensitiveWord(@PathVariable Long id) {
        logger.info("开始处理删除敏感词请求，ID: {}", id);
        try {
            int result = sensitiveWordService.deleteSensitiveWord(id);
            if (result > 0) {
                logger.info("删除敏感词成功，ID: {}", id);
                return Result.success();
            } else {
                logger.warn("删除敏感词失败，ID: {}", id);
                return Result.error(500, "删除敏感词失败");
            }
        } catch (Exception e) {
            logger.error("删除敏感词失败: {}", e.getMessage(), e);
            return Result.error(500, "删除敏感词失败");
        }
    }

    /**
     * 重新加载敏感词
     */
    @PostMapping("/reload")
    @Operation(summary = "重新加载敏感词", description = "从数据库重新加载敏感词到内存")
    public Result<Void> reloadSensitiveWords() {
        logger.info("开始处理重新加载敏感词请求");
        try {
            sensitiveWordService.loadSensitiveWords();
            logger.info("重新加载敏感词成功");
            return Result.success();
        } catch (Exception e) {
            logger.error("重新加载敏感词失败: {}", e.getMessage(), e);
            return Result.error(500, "重新加载敏感词失败");
        }
    }

    /**
     * 批量导入敏感词
     */
    @PostMapping("/batch-import")
    @Operation(summary = "批量导入敏感词", description = "通过上传.txt文件批量导入敏感词，文件格式为每行一个敏感词，格式为\"敏感词,关键词类型\"")
    public Result<String> batchImportSensitiveWords(@RequestParam("file") MultipartFile file) {
        logger.info("开始处理批量导入敏感词请求，文件名: {}", file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                logger.warn("上传文件为空");
                return Result.error(400, "上传文件为空");
            }

            if (!file.getOriginalFilename().endsWith(".txt")) {
                logger.warn("上传文件格式错误，仅支持.txt文件");
                return Result.error(400, "上传文件格式错误，仅支持.txt文件");
            }

            // 解析文件内容
            List<SensitiveWord> sensitiveWords = new ArrayList<>();
            int successCount = 0;
            int errorCount = 0;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
                String line;
                int lineNum = 1;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue; // 跳过空行和注释行
                    }

                    try {
                        // 解析行内容：敏感词,关键词类型
                        String[] parts = line.split(",");
                        if (parts.length != 2) {
                            logger.warn("第 {} 行格式错误: {}", lineNum, line);
                            errorCount++;
                            continue;
                        }

                        String word = parts[0].trim();
                        int keywordType = Integer.parseInt(parts[1].trim());

                        // 验证关键词类型
                        if (keywordType < 1 || keywordType > 3) {
                            logger.warn("第 {} 行关键词类型错误，必须为1、2或3: {}", lineNum, line);
                            errorCount++;
                            continue;
                        }

                        // 创建敏感词对象
                        SensitiveWord sensitiveWord = new SensitiveWord();
                        sensitiveWord.setWord(word);
                        sensitiveWord.setKeywordType(keywordType);
                        sensitiveWords.add(sensitiveWord);
                        successCount++;
                    } catch (NumberFormatException e) {
                        logger.warn("第 {} 行数字格式错误: {}", lineNum, line);
                        errorCount++;
                    } catch (Exception e) {
                        logger.warn("第 {} 行解析错误: {}", lineNum, line);
                        errorCount++;
                    }
                    lineNum++;
                }
            }

            if (sensitiveWords.isEmpty()) {
                logger.warn("没有有效的敏感词可以导入");
                return Result.error(400, "没有有效的敏感词可以导入");
            }

            // 调用服务层批量导入
            int importCount = sensitiveWordService.batchImportSensitiveWords(sensitiveWords);
            logger.info("批量导入敏感词完成，成功: {}，失败: {}", importCount, errorCount);

            return Result.success("批量导入敏感词成功，共处理 " + (successCount + errorCount) + " 行，成功导入 " + importCount + " 个敏感词，失败 " + errorCount + " 个");
        } catch (IOException e) {
            logger.error("读取上传文件失败: {}", e.getMessage(), e);
            return Result.error(500, "读取上传文件失败");
        } catch (Exception e) {
            logger.error("批量导入敏感词失败: {}", e.getMessage(), e);
            return Result.error(500, "批量导入敏感词失败");
        }
    }
}
