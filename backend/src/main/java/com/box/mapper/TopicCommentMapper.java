package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.TopicComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子表数据访问层：支持帖子列表分页查询
 */
@Mapper
public interface TopicCommentMapper extends BaseMapper<TopicComment> {

}
