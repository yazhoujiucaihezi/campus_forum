package com.box.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Integer tid;
    private String content;
    private Integer quote;
}
