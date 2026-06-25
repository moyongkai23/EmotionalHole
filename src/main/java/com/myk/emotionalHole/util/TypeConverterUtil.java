package com.myk.emotionalHole.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeConverterUtil {

    private static final Logger logger = LoggerFactory.getLogger(TypeConverterUtil.class);

    public static Long toLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                logger.warn("无法将字符串转换为 Long: {}", value);
                return defaultValue;
            }
        }

        logger.warn("不支持的类型转换为 Long: {}", value.getClass());
        return defaultValue;
    }

    public static String toString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return value.toString();
    }
}