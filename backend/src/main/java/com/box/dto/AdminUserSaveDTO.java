package com.box.dto;

import com.box.vo.AdminUserDetailVO;
import com.box.vo.AdminUserPrivacyVO;
import lombok.Data;

// 管理员保存用户信息参数
@Data
public class AdminUserSaveDTO {

    // 用户 ID
    private Integer id;

    // 用户名
    private String username;

    // 邮箱
    private String email;

    // 是否禁言
    private Boolean mute;

    // 是否封禁
    private Boolean banned;

    // 用户详情
    private AdminUserDetailVO detail;

    // 隐私设置
    private AdminUserPrivacyVO privacy;
}