package com.box.dto;

import lombok.Data;

/** 帖子分类筛选参数 */
@Data
public class TopicDTO {
    /** 分类 ID */
    private Integer type;
    /** 标题 */
    private String title;
    /** 正文 */
    private String content;
}
