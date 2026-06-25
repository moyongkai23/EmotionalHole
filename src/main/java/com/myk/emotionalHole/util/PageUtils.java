package com.myk.emotionalHole.util;

import lombok.Data;

/**
 * 分页工具类
 * 替代 PageHelper，提供分页参数处理和转换功能
 */
public class PageUtils {

    /**
     * 分页参数默认值
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 分页参数类
     */
    @Data
    public static class PageParam {
        private int pageNum;     // 页码
        private int pageSize;    // 每页大小

        /**
         * 构造方法，自动验证和设置默认值
         */
        public PageParam(int pageNum, int pageSize) {
            this.pageNum = Math.max(pageNum, DEFAULT_PAGE_NUM);
            // 允许1-100的pageSize，不再强制最小为10
            this.pageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        }

        /**
         * 获取 MyBatis 分页的 offset 值
         */
        public int getOffset() {
            return (pageNum - 1) * pageSize;
        }

        /**
         * 获取 MyBatis 分页的 size 值
         */
        public int getSize() {
            return pageSize;
        }
    }

    /**
     * 创建分页参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页参数对象
     */
    public static PageParam createPageParam(int pageNum, int pageSize) {
        return new PageParam(pageNum, pageSize);
    }

}
