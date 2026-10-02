package com.box.vo;

import lombok.Data;

import java.time.LocalDateTime;

// 管理员查看用户详情
@Data
public class AdminUserVO {

    // 来自 db_account
    private Integer id;
    private String username;
    private String email;
    private String avatar;
    private String role;
    private Boolean mute;
    private Boolean banned;
    private LocalDateTime createTime;
    private AdminUserDetailVO detail;
    private AdminUserPrivacyVO privacy;
}