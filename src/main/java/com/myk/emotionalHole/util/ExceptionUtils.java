package com.myk.emotionalHole.util;

import com.myk.emotionalHole.common.Result;

/**
 * 异常工具类
 * 封装通用业务异常和异常处理方法
 */
public class ExceptionUtils {

    /**
     * 通用业务异常类
     */
    public static class BusinessException extends RuntimeException {
        private int code; // 错误码

        /**
         * 构造方法
         * @param code 错误码
         * @param message 错误消息
         */
        public BusinessException(int code, String message) {
            super(message);
            this.code = code;
        }

        /**
         * 构造方法，默认错误码 400
         * @param message 错误消息
         */
        public BusinessException(String message) {
            this(400, message);
        }

        /**
         * 获取错误码
         * @return 错误码
         */
        public int getCode() {
            return code;
        }
    }

    /**
     * 创建参数错误异常
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException createParamException(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 创建禁止访问异常
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException createForbiddenException(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 创建资源不存在异常
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException createNotFoundException(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 创建服务器内部错误异常
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException createServerException(String message) {
        return new BusinessException(500, message);
    }



    /**
     * 将业务异常转换为 Result 对象
     * @param e 业务异常
     * @return Result 对象
     */
    public static <T> Result<T> toResult(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 将普通异常转换为 Result 对象
     * @param e 异常
     * @return Result 对象
     */
    public static <T> Result<T> toResult(Exception e) {
        if (e instanceof BusinessException) {
            return toResult((BusinessException) e);
        }
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }

    /**
     * 检查条件，不满足则抛出异常
     * @param condition 条件
     * @param code 错误码
     * @param message 错误消息
     */
    public static void checkCondition(boolean condition, int code, String message) {
        if (!condition) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 检查条件，不满足则抛出异常，默认错误码 400
     * @param condition 条件
     * @param message 错误消息
     */
    public static void checkCondition(boolean condition, String message) {
        checkCondition(condition, 400, message);
    }

    /**
     * 检查参数非空
     * @param obj 参数对象
     * @param message 错误消息
     */
    public static void checkNotNull(Object obj, String message) {
        checkCondition(obj != null, 400, message);
    }


}
