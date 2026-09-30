package com.box.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.box.entity.User;

/**
 * 用户服务接口：复用 MyBatis-Plus 通用 CRUD，用于按条件查询用户信息
 */
public interface UserService extends IService<User> {

}
