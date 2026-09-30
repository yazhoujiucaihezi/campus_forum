package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.Topic;
import com.box.entity.TopicComment;
import com.box.vo.TopicUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 帖子表数据访问层：支持帖子列表分页查询
 */
@Mapper
public interface TopicCommentMapper extends BaseMapper<TopicComment> {

}
