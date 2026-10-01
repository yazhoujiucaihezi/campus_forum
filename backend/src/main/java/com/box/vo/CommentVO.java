package com.box.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 评论展示数据 */
@Data
public class CommentVO {
    /** 评论 ID */
    private Integer id;
    /** 评论内容 */
    private String content;
    /** 评论时间 */
    private LocalDateTime time;
    /** 被回复评论的内容 */
    private String quote;
    /** 评论者信息 */
    private TopicUserVO user;
}