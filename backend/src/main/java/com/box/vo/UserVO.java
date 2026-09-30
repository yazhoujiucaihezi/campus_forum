package com.box.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息返回结果（不包含密码等敏感字段）
 */
@Data
public class UserVO {
    /** 用户 ID */
    private Integer id;
    /** 用户名 */
    private String username;
    /** 邮箱 */
    private String email;
    /** 角色 */
    private String role;
    /** 头像地址 */
    private String avatar;
    /** 注册时间 */
    private LocalDateTime registerTime;  // 或 createTime，看你数据库字段名
}