package com.box.vo;

import lombok.Data;

// 用户隐私（嵌套在 AdminUserVO.privacy）
@Data
public class AdminUserPrivacyVO {
    private Integer phone;
    private Integer email;
    private Integer wx;
    private Integer qq;
    private Integer gender;
}