package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.box.common.Result;
import com.box.dto.LoginDTO;
import com.box.entity.User;
import com.box.mapper.UserMapper;
import com.box.service.AuthService;
import com.box.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 认证模块接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto){


        String username = dto.getUsername();
        User user = userMapper.selectOne( new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user.getBanned() == 1){
            return Result.error("账号已封禁");
        }

        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }

    @GetMapping("/logout")
    public Result<Void> logout(){
        return Result.success(null);
    }

    @GetMapping("/ask-code")
    public Result<Void> askCode(@RequestParam String email,
                                @RequestParam String type){
        //生成六位验证码
        int code = (int)((Math.random() * 9 + 1) * 100000);
        stringRedisTemplate.opsForValue().set(
                email+":"+type,
                String.valueOf(code),
                3,
                TimeUnit.MINUTES);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2464859727@qq.com");     // 发件人
        message.setTo(email);                     // 收件人
        if ("register".equals(type)) {
            message.setSubject("注册验证邮件");
            message.setText("您正在注册校园论坛账号，验证码：" + code + "，3分钟内有效。");
        } else if ("modify".equals(type)) {
            message.setSubject("邮箱修改验证邮件");
            message.setText("您正在绑定新的电子邮箱，验证码：" + code + "，3分钟内有效。");
        }javaMailSender.send(message);
        return Result.success(null);
    }
}
