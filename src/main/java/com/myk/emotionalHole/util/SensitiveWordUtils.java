package com.myk.emotionalHole.util;

import com.myk.emotionalHole.entity.SensitiveWord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 敏感词工具类 - DFA（确定性有限状态自动机）实现
 * 将所有敏感词构建成状态转移表，匹配时文本只扫描一遍，时间复杂度 O(m)
 */
public class SensitiveWordUtils {

    /** 状态转移表：当前状态 → (输入字符 → 下一状态) */
    private static final Map<Integer, Map<Character, Integer>> stateTable = new HashMap<>();

    /** 终止状态集合，到达这些状态表示命中敏感词 */
    private static final Set<Integer> endStates = new HashSet<>();

    /** 敏感词原始集合（用于替换等操作） */
    private static final Set<String> allSensitiveWords = new HashSet<>();

    /** 状态计数器，0 为初始状态 */
    private static int stateCount = 1;

    /**
     * 从数据库加载敏感词并构建 DFA
     * @param sensitiveWordList 从数据库查询的敏感词列表
     */
    public static void loadSensitiveWordsFromDatabase(List<SensitiveWord> sensitiveWordList) {
        if (sensitiveWordList == null || sensitiveWordList.isEmpty()) {
            return;
        }
        buildDFA(sensitiveWordList);
    }

    /**
     * 构建 DFA 状态转移表
     * 将所有敏感词逐个插入，共享相同前缀的状态
     */
    private static void buildDFA(List<SensitiveWord> wordList) {
        stateTable.clear();
        endStates.clear();
        allSensitiveWords.clear();
        stateCount = 1;

        for (SensitiveWord sw : wordList) {
            if (sw.getWord() == null || sw.getWord().trim().isEmpty()) continue;
            String word = sw.getWord().trim();
            allSensitiveWords.add(word);
            insertWord(word);
        }
    }

    /**
     * 将单个敏感词插入 DFA
     * 从状态0开始，逐字符创建或复用状态转移
     */
    private static void insertWord(String word) {
        int currentState = 0;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Map<Character, Integer> transitions = stateTable.computeIfAbsent(currentState, k -> new HashMap<>());
            Integer nextState = transitions.get(c);
            if (nextState == null) {
                nextState = stateCount++;
                transitions.put(c, nextState);
            }
            currentState = nextState;
        }
        endStates.add(currentState);
    }
    /**
     * 检测文本是否包含敏感词（DFA 匹配）
     * 文本只扫描一遍，时间复杂度 O(m)，m 为文本长度
     * @param text 待检测文本
     * @return 是否包含敏感词
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty() || stateTable.isEmpty()) {
            return false;
        }
        // 文本扫描，逐个字符作为匹配起点
        for (int i = 0; i < text.length(); i++) {
            int state = 0;
            // 从当前位置开始，逐字符匹配
            for (int j = i; j < text.length(); j++) {
                Map<Character, Integer> transitions = stateTable.get(state);
                if (transitions == null) break;
                Integer nextState = transitions.get(text.charAt(j));
                if (nextState == null) break;
                state = nextState;
                if (endStates.contains(state)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取敏感词总数量
     * @return 敏感词数量
     */
    public static int getSensitiveWordCount() {
        return allSensitiveWords.size();
    }

}
