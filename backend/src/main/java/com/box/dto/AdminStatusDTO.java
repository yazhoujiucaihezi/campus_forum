package com.box.dto;

import lombok.Data;

// 管理员操作帖子状态参数（置顶、屏蔽）
@Data
public class AdminStatusDTO {
    private Integer tid;      // 帖子 ID
    private Boolean status;   // true 启用，false 取消
}