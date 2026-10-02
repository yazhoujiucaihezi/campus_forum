package com.box.dto;

import lombok.Data;

// 用户注册参数
@Data
public class RegisterDTO {
    private String username;   // 用户名
    private String password;   // 密码
    private String email;      // 邮箱
    private String code;       // 邮箱验证码
}