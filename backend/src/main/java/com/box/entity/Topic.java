package com.box.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子实体，对应数据库表 db_topic
 */
@Data
@TableName("db_topic")
public class Topic {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 帖子标题 */
    private String title;

    /** 帖子简介 */
    private String intro;

    /** 帖子正文内容 */
    private String content;

    /** 发帖人用户 ID */
    private Integer uid;

    /** 帖子分类 ID（对应 db_topic_type.id） */
    private Integer type;

    /** 发布时间 */
    private LocalDateTime time;

    /** 是否置顶：0 否，1 是 */
    private Integer top;

    /** 是否锁定（禁止回复）：0 否，1 是 */
    private Integer locked;

    /** 是否隐藏：0 显示，1 隐藏 */
    private Integer invisible;
}