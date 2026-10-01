package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.Forum;
import org.apache.ibatis.annotations.Mapper;

/** 论坛板块数据访问层 */
@Mapper
public interface ForumMapper extends BaseMapper<Forum> {
}
