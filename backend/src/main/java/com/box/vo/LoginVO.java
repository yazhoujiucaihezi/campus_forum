package com.box.vo;

import lombok.Data;

import java.util.Date;

/**
 * 登录成功返回结果
 */
@Data
public class LoginVO {
    /** JWT 令牌 */
    private String token;
    /** 过期时间 */
    private Date expire;   // 过期时间
    /** 用户角色 */
    private String role;
    /** 用户名 */
    private String username;
}