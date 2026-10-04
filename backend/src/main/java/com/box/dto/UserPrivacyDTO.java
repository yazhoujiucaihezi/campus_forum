package com.box.dto;

import lombok.Data;

// 保存隐私设置参数
@Data
public class UserPrivacyDTO {
    private String type;      // phone / email / wx / qq / gender
    private Boolean status;   // true 公开，false 隐藏
}