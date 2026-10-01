package com.box.dto;

import lombok.Data;

// 保存用户信息参数
@Data
public class UserDetailDTO {

    // 用户名
    private String username;

    // 性别 0男 1女
    private Integer gender;

    // 手机号
    private String phone;

    // QQ 号
    private String qq;

    // 微信号
    private String wx;

    // 个人简介
    private String desc;
}