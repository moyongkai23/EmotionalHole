package com.myk.emotionalHole.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.myk.emotionalHole.util.ExceptionUtils;

import java.util.List;

/**
 * 全局异常处理器
 * 统一拦截Controller层异常，返回标准响应格式
 * 避免将内部错误细节暴露给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(ExceptionUtils.BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handleBusinessException(ExceptionUtils.BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        
        // 提取第一个错误信息返回
        if (!fieldErrors.isEmpty()) {
            FieldError fieldError = fieldErrors.get(0);
            logger.warn("参数校验异常: {}", fieldError.getDefaultMessage());
            return Result.error(400, fieldError.getDefaultMessage());
        }
        
        logger.warn("参数校验异常: 未知错误");
        return Result.error(400, "参数校验失败");
    }

    /**
     * 处理请求体不可读异常（如JSON格式错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.warn("请求数据格式错误: {}", e.getMessage());
        return Result.error(400, "请求数据格式错误");
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("请求方法不支持: {}", e.getMessage());
        return Result.error(405, "请求方法不支持");
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e, WebRequest request) {
        // 排除SpringDoc/Swagger相关路径，让其正常返回
        String path = request.getDescription(false);
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
            throw new RuntimeException(e);
        }
        // 记录详细异常信息
        logger.error("系统异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}
