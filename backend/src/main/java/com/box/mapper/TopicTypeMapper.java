package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.TopicType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子分类表数据访问层
 */
@Mapper
public interface TopicTypeMapper extends BaseMapper<TopicType> {
}
