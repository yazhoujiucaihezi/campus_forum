package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.TopicComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子评论数据访问层
 */
@Mapper
public interface TopicCommentMapper extends BaseMapper<TopicComment> {

}
