package com.box.dto;

import lombok.Data;

// 编辑帖子参数
@Data
public class TopicUpdateDTO {
    private Integer id;       // 帖子 ID
    private Integer type;     // 分类 ID
    private String title;     // 标题
    private String content;   // 正文
}