package com.myk.emotionalHole.mapper;

import com.myk.emotionalHole.entity.EmotionKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmotionKeywordMapper {

    List<EmotionKeyword> getAllEnabledKeywords();

    int addKeyword(EmotionKeyword keyword);

    int updateKeyword(EmotionKeyword keyword);

    int deleteKeyword(Long id);

    EmotionKeyword getKeywordById(Long id);

    EmotionKeyword getKeywordByName(String keyword);

    List<EmotionKeyword> searchKeywords(String keyword);

    List<EmotionKeyword> searchKeywordsWithPagination(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    int countSearchKeywords(@Param("keyword") String keyword);

    List<EmotionKeyword> getKeywordsByTypeWithPagination(@Param("emotionType") Integer emotionType, @Param("offset") int offset, @Param("limit") int limit);

    int countKeywordsByType(@Param("emotionType") Integer emotionType);

    List<EmotionKeyword> getKeywordsWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    int countAllKeywords();

}