package com.box.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 帖子关联用户展示数据 */
@Data
public class TopicUserVO {
    /** 用户 ID */
    private Integer id;
    /** 用户名 */
    private String username;
    /** 头像 */
    private String avatar;
    /** 性别 */
    private Integer gender;
    /** 邮箱 */
    private String email;
    /** 微信号 */
    private String wx;
    /** QQ号 */
    private String qq;
    /** 手机号 */
    private String phone;
    /** 个人简介 */
    private String desc;
    /** 创建时间 */
    private LocalDateTime createTime;
}