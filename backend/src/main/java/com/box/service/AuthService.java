package com.box.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.box.dto.LoginDTO;
import com.box.dto.RegisterDTO;
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

    /**
     * 获取验证码：生成并发送邮箱验证码
     */
    void askCode(String email, String type);

    /**
     * 用户注册：校验验证码并创建账号
     */
    void register(RegisterDTO dto);

    /**
     * 重置密码校验：校验邮箱与验证码是否有效
     */
    void resetConfirm(RegisterDTO dto);

    /**
     * 重置密码：校验通过后写入新密码
     */
    void resetPassword(RegisterDTO dto);
}
