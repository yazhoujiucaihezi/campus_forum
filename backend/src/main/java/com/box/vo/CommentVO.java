package com.box.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Integer id;
    private String content;
    private LocalDateTime time;
    private String quote;         // 被回复评论的内容，没有就 null
    private TopicUserVO user;     // 评论者，复用 TopicUserVO
}