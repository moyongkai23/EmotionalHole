package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SensitiveWordMapper {

    /**
     * 查询所有敏感词
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
     * 根据ID查询敏感词
     */
    SensitiveWord getSensitiveWordById(Long id);
    
    /**
     * 根据敏感词内容和类型查询
     */
    SensitiveWord getSensitiveWordByWordAndType(@Param("word") String word, @Param("keywordType") int keywordType);
    
    /**
     * 根据关键词类型查询敏感词
     */
    List<SensitiveWord> getSensitiveWordsByType(int keywordType);
    
    /**
     * 按关键词和类型搜索敏感词（分页）
     */
    List<SensitiveWord> searchSensitiveWordsByKeywordAndType(@Param("keyword") String keyword, @Param("keywordType") int keywordType, @Param("start") int start, @Param("pageSize") int pageSize);
    
    /**
     * 按关键词和类型统计敏感词数量
     */
    int countSensitiveWordsByKeywordAndType(@Param("keyword") String keyword, @Param("keywordType") int keywordType);
    
    /**
     * 获取心理危机词（类型2和类型3）分页列表
     */
    List<SensitiveWord> getCrisisWordsByPage(@Param("start") int start, @Param("pageSize") int pageSize);
    
    /**
     * 统计心理危机词数量
     */
    int countCrisisWords();
    
    /**
     * 搜索心理危机词（类型2和类型3）分页列表
     */
    List<SensitiveWord> searchCrisisWordsByKeyword(@Param("keyword") String keyword, @Param("start") int start, @Param("pageSize") int pageSize);
    
    /**
     * 统计搜索心理危机词数量
     */
    int countCrisisWordsByKeyword(@Param("keyword") String keyword);
    
    /**
     * 批量插入敏感词
     */
    int batchInsertSensitiveWords(List<SensitiveWord> sensitiveWords);

    /**
     * 分页获取敏感词
     */
    List<SensitiveWord> getSensitiveWordsByPage(int offset, int limit);

    /**
     * 根据类型分页获取敏感词
     */
    List<SensitiveWord> getSensitiveWordsByTypeAndPage(@Param("keywordType") Integer keywordType, @Param("offset") int offset, @Param("limit") int limit);

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
    List<SensitiveWord> searchSensitiveWordsByKeyword(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据关键词统计敏感词数量
     */
    int countSensitiveWordsByKeyword(@Param("keyword") String keyword);
}
