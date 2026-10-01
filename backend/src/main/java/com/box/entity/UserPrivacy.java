package com.box.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 用户隐私设置，对应 db_account_privacy
@Data
@TableName("db_account_privacy")
public class UserPrivacy {

    // 用户 ID
    @TableId
    private Integer id;

    // 手机号是否公开
    private Integer phone;

    // 邮箱是否公开
    private Integer email;

    // 微信是否公开
    private Integer wx;

    // QQ 是否公开
    private Integer qq;

    // 性别是否公开
    private Integer gender;
}