package com.box.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TopicDetailVO {
    private Integer id;
    private String title;
    private String intro;
    private String content;
    private Integer uid;
    private Integer type;
    private LocalDateTime time;
    private Integer top;
    private Integer locked;
    private Integer invisible;
    private Integer comments;        // 评论总数
    private TopicUserVO user;        // 发帖人
    private TopicInteractVO interact; // 当前用户的点赞收藏状态
}