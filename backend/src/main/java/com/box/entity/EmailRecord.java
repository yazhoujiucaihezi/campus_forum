package com.box.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 邮件记录，对应 db_email_record
@Data
@TableName("db_email_record")
public class EmailRecord {

    // 记录 ID
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 收件人邮箱
    private String email;

    // 邮件标题
    private String title;

    // 邮件内容
    private String content;

    // 发送时间
    private LocalDateTime time;

    // 状态：1 成功，2 失败
    private Integer status;
}