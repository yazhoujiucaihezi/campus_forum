package com.box.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 帖子详情展示数据 */
@Data
public class TopicDetailVO {
    /** 帖子 ID */
    private Integer id;
    /** 标题 */
    private String title;
    /** 简介 */
    private String intro;
    /** 正文 */
    private String content;
    /** 发帖人 ID */
    private Integer uid;
    /** 分类 ID */
    private Integer type;
    /** 发布时间 */
    private LocalDateTime time;
    /** 是否置顶 */
    private Integer top;
    /** 是否锁定 */
    private Integer locked;
    /** 是否隐藏 */
    private Integer invisible;
    /** 评论总数 */
    private Integer comments;
    /** 发帖人信息 */
    private TopicUserVO user;
    /** 当前用户的互动状态 */
    private TopicInteractVO interact;
}