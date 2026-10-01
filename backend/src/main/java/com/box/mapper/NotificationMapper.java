package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认证模块数据访问层：登录时按用户名查询账号
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

}
