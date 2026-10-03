package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.box.common.Result;
import com.box.dto.LoginDTO;
import com.box.dto.RegisterDTO;
import com.box.entity.User;
import com.box.exception.BusinessException;
import com.box.mapper.UserMapper;
import com.box.service.AuthService;
import com.box.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    /**
     * 用户登出
     */
    @GetMapping("/logout")
    public Result<Void> logout(){
        return Result.success(null);
    }
    /**
     * 获取验证码
     */
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
        SimpleMailMessage message = getSimpleMailMessage(email, type, code);
        if ("reset".equals(type)) {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, email)
            );
            if (user == null) {
                throw new BusinessException("该邮箱未注册");
            }
        }
        javaMailSender.send(message);
        return Result.success(null);
    }

    @NotNull
    private static SimpleMailMessage getSimpleMailMessage(String email, String type, int code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2464859727@qq.com");     // 发件人
        message.setTo(email);                     // 收件人
        if ("register".equals(type)) {
            message.setSubject("注册验证邮件");
            message.setText("您正在注册校园论坛账号，验证码：  " + code + "，3分钟内有效。");
        } else if ("modify".equals(type)) {
            message.setSubject("邮箱修改验证邮件");
            message.setText("您正在绑定新的电子邮箱，验证码：  " + code + "，3分钟内有效。");
        }else {
            message.setSubject("验证邮件");
            message.setText("您的验证码：" + code + "，3分钟内有效。");
        }
        return message;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String code = stringRedisTemplate.opsForValue().get(dto.getEmail() + ":register");
        if (code == null) {
            throw new BusinessException("验证码已过期");
        }
        if (!code.equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail())) != null) {
            throw new BusinessException("邮箱已存在");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 加密密码
        String encoded = passwordEncoder.encode(dto.getPassword());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoded);
        user.setEmail(dto.getEmail());
        user.setRole("user");
        user.setMute(0);
        user.setBanned(0);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        stringRedisTemplate.delete(dto.getEmail() + ":register");

        return Result.success(null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-confirm")
    public Result<Void> resetConfirm(@RequestBody RegisterDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            throw new BusinessException("邮箱不存在");
        }
        String code = stringRedisTemplate.opsForValue().get(dto.getEmail() + ":reset");
        if (code == null) {
            throw new BusinessException("验证码已过期");
        }
        if (!code.equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }
        return Result.success(null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody RegisterDTO dto) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String encoded = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encoded);
        userMapper.updateById(user);
        stringRedisTemplate.delete(dto.getEmail() + ":reset");
        return Result.success(null);
    }

}