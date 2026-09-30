package com.box.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.box.common.Result;
import com.box.dto.LoginDTO;
import com.box.entity.User;
import com.box.vo.LoginVO;


/**
 * 认证服务接口
 */
public interface AuthService extends IService<User> {
    /**
     * 用户登录：校验账号密码并返回登录凭证
     */
    LoginVO login(LoginDTO loginDTO);
}
