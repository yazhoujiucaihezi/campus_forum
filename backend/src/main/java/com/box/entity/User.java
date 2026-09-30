package com.box.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应数据库表 db_account
 */
@Data
@TableName("db_account")
public class User {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户名（登录账号） */
    private String username;

    /** 邮箱 */
    private String email;

    /** 密码（BCrypt 哈希值，非明文） */
    private String password;

    /** 角色（如 user / admin） */
    private String role;

    /** 头像地址 */
    private String avatar;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否禁言：0 否，1 是 */
    private Integer mute;

    /** 是否封禁：0 否，1 是 */
    private Integer banned;
}