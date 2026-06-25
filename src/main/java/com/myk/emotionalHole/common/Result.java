package com.myk.emotionalHole.common;

/**
 * 统一API响应封装
 * 使用泛型支持不同类型的响应数据
 * 通过静态工厂方法简化Controller返回值构造
 */
public class Result<T> {
    private int code;       // 状态码：200成功，400参数错误，500系统异常
    private String message; // 响应消息
    private T data;         // 响应数据

    /**
     * 私有构造方法，防止直接实例化
     */
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应，无数据
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应，带自定义消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应，带状态码和消息
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应，默认状态码500
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    // getter和setter方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}