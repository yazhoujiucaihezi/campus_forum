package com.box.vo;

import lombok.Data;

// 用户详情（嵌套在 AdminUserVO.detail）
@Data
public class AdminUserDetailVO {
    private Integer gender;
    private String phone;
    private String qq;
    private String wx;
    private String desc;
}