package com.box.dto;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginDTO {
    /** 用户名 */
    private String username;
    /** 密码（明文，传输后与库中哈希比对） */
    private String password;
}
