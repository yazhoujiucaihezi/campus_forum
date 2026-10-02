package com.box.dto;

import lombok.Data;

// 管理员修改用户密码参数
@Data
public class AdminChangePasswordDTO {
    private Integer id;
    private String newPassword;
}