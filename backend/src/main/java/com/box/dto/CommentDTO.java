package com.box.dto;

import lombok.Data;

/** 评论请求参数 */
@Data
public class CommentDTO {
    /** 帖子 ID */
    private Integer tid;
    /** 评论内容 */
    private String content;
    /** 回复的评论 ID */
    private Integer quote;
}
