package com.box.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

// 发送到 MQ 的通知消息
@Data
@Accessors(chain = true)
public class NotificationMessage implements Serializable {

    // 接收者用户 ID
    private Integer uid;

    // 通知标题
    private String title;

    // 通知内容
    private String content;

    // 类型：like / comment / follow / system
    private String type;

    // 跳转链接
    private String url;
}