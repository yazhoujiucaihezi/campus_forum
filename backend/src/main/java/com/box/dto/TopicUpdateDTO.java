package com.box.dto;

import lombok.Data;

/** 创建/编辑帖子参数 */
@Data
public class TopicUpdateDTO {
    /** 帖子 ID */
    private Integer id;
    /** 分类 ID */
    private Integer type;
    /** 标题 */
    private String title;
    /** 正文 */
    private String content;
}