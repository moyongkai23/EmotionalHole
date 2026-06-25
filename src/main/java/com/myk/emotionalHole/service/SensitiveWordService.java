package com.myk.emotionalHole.service;

import com.myk.emotionalHole.entity.SensitiveWord;

import java.util.List;

/**
 * 敏感词服务接口
 */
public interface SensitiveWordService {

    /**
     * 加载所有敏感词到内存
     */
    void loadSensitiveWords();

    /**
     * 获取所有敏感词
     */
    List<SensitiveWord> getAllSensitiveWords();

    /**
     * 添加敏感词
     */
    int addSensitiveWord(SensitiveWord sensitiveWord);
    
    /**
     * 更新敏感词
     */
    int updateSensitiveWord(SensitiveWord sensitiveWord);
    
    /**
     * 删除敏感词
     */
    int deleteSensitiveWord(Long id);
    
    /**
     * 根据敏感词内容和类型获取敏感词
     */
    SensitiveWord getSensitiveWordByWordAndType(String word, int keywordType);
    
    /**
     * 根据关键词类型获取敏感词
     */
    List<SensitiveWord> getSensitiveWordsByType(int keywordType);
    
    /**
     * 按关键词和类型搜索敏感词（分页）
     */
    List<SensitiveWord> searchSensitiveWordsByKeywordAndType(String keyword, int keywordType, int page, int pageSize);
    
    /**
     * 按关键词和类型统计敏感词数量
     */
    int countSensitiveWordsByKeywordAndType(String keyword, int keywordType);
    
    /**
     * 获取心理危机词（类型2和类型3）分页列表
     */
    List<SensitiveWord> getCrisisWordsByPage(int page, int pageSize);
    
    /**
     * 统计心理危机词数量
     */
    int countCrisisWords();
    
    /**
     * 搜索心理危机词（类型2和类型3）分页列表
     */
    List<SensitiveWord> searchCrisisWordsByKeyword(String keyword, int page, int pageSize);
    
    /**
     * 统计搜索心理危机词数量
     */
    int countCrisisWordsByKeyword(String keyword);
    
    /**
     * 批量导入敏感词
     */
    int batchImportSensitiveWords(List<SensitiveWord> sensitiveWords);

    /**
     * 分页获取敏感词
     */
    List<SensitiveWord> getSensitiveWordsByPage(int page, int pageSize);

    /**
     * 根据类型分页获取敏感词
     */
    List<SensitiveWord> getSensitiveWordsByTypeAndPage(Integer keywordType, int page, int pageSize);

    /**
     * 获取敏感词总数
     */
    int getSensitiveWordCount();

    /**
     * 根据类型获取敏感词总数
     */
    int getSensitiveWordCountByType(Integer keywordType);

    /**
     * 根据关键词搜索敏感词（分页）
     */
    List<SensitiveWord> searchSensitiveWordsByKeyword(String keyword, int page, int pageSize);

    /**
     * 根据关键词搜索敏感词数量
     */
    int countSensitiveWordsByKeyword(String keyword);
}
