package com.box.exception;

import com.box.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：统一捕获异常并转换为标准响应，避免异常直接抛给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，将异常信息作为错误提示返回
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException businessException){
        return Result.error(businessException.getMessage());
    }
}
