package com.box.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

// 分类新增/修改参数
@Data
public class TopicTypeDTO {

    @TableId(type = IdType.AUTO)
    private Integer id;      // 分类 ID（修改时用，新增时不传）
    private String name;     // 分类名
    private String desc;     // 分类描述
    private String color;    // 颜色值
}