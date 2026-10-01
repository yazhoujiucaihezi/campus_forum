package com.box.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 用户详情，对应 db_account_details
@Data
@TableName("db_account_details")
public class UserDetail {

    // 用户 ID
    @TableId
    private Integer id;

    // 性别 0男 1女
    private Integer gender;

    // 手机号
    private String phone;

    // QQ 号
    private String qq;

    // 微信号
    private String wx;

    // 个人简介
    @TableField("`desc`")
    private String desc;
}