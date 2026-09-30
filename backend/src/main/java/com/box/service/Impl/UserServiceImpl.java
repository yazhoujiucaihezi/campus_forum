package com.box.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.box.entity.User;
import com.box.mapper.UserMapper;
import com.box.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
