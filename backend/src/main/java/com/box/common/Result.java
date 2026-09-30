package com.box.common;

import lombok.Data;

/**
 * 统一响应结果包装类：所有接口均返回 {code, message, data} 结构
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {
    /** 状态码：200 成功，500 失败 */
    private Integer code;
    /** 提示信息 */
    private String message;
    /** 业务数据 */
    private T data;

    /**
     * 构建成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("成功");
        r.setData(data);
        return r;
    }

    /**
     * 构建失败响应
     */
    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMessage(message);
        return r;
    }
}