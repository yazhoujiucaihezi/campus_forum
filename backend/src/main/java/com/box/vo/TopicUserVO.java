package com.box.vo;

import lombok.Data;

@Data
public class TopicUserVO {
    private Integer id;
    private String username;
    private String avatar;
    private Integer gender;
    private String email;
    private String wx;
    private String qq;
    private String phone;
    private String desc;
}