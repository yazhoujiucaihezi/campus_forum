package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
