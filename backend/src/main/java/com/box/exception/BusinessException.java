package com.box.exception;

/**
 * 业务异常：用于登录失败等可预期的业务错误，由全局异常处理器统一捕获返回
 */
public class BusinessException extends RuntimeException{
    public BusinessException(String message) {
        super(message);
    }
}
