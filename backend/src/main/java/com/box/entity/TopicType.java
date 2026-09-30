package com.box.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 帖子分类实体
 */
@Data
@TableName("db_topic_type")
public class TopicType {

    /** 分类 ID */
    private Integer id;
    /** 分类名称 */
    private String name;
    /** 分类描述 */
    @TableField("`desc`")
    private String desc;
    /** 分类展示颜色 */
    private String color;
}
