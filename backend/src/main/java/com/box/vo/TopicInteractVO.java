package com.box.vo;

import lombok.Data;

@Data
public class TopicInteractVO {
    private Boolean like;
    private Boolean collect;
    private Integer likeCount;
    private Integer collectCount;
}