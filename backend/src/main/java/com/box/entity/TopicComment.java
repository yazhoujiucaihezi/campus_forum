package com.box.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 帖子评论，对应 db_topic_comment
@Data
@TableName("db_topic_comment")
public class TopicComment {

    // 评论 ID
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 评论者 ID
    private Integer uid;

    // 帖子 ID
    private Integer tid;

    // 评论内容
    private String content;

    // 评论时间
    private LocalDateTime time;

    // 回复的评论 ID
    private Integer quote;
}