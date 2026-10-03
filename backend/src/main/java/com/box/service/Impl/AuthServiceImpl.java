package com.box.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.box.dto.LoginDTO;
import com.box.dto.RegisterDTO;
import com.box.entity.EmailRecord;
import com.box.entity.User;
import com.box.exception.BusinessException;
import com.box.mapper.AuthMapper;
import com.box.mapper.EmailMapper;
import com.box.service.AuthService;
import com.box.vo.LoginVO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {

    private final AuthMapper authMapper;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final StringRedisTemplate stringRedisTemplate;

    private final JavaMailSender javaMailSender;

    private final EmailMapper emailMapper;

    /**
     * 用户登录
     */
    @Override
    public LoginVO login(LoginDTO dto){

        Date expire = new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000);

        User user = authMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if(user == null){
            throw new BusinessException("没找到用户");
        }
        if (user.getBanned() == 1){
            throw new BusinessException("账号已封禁");
        }

        boolean password = bCryptPasswordEncoder.matches(dto.getPassword(), user.getPassword());

        if(!password){
            throw new BusinessException("密码错误");
        }

        String token = Jwts.builder()
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("uid", user.getId())
                .expiration(expire)
                .signWith(Keys.hmacShaKeyFor("box-forum-secret-key-1234567890abcdef".getBytes()))
                .compact();
        LoginVO vo = new LoginVO();
        vo.setUsername(user.getUsername());
        vo.setToken(token);
        vo.setRole(user.getRole());
        vo.setExpire(expire);
        return vo;
    }

    /**
     * 获取验证码
     */
    @Override
    public void askCode(String email, String type){
        EmailRecord emailRecord = new EmailRecord();
        //生成六位验证码
        int code = (int)((Math.random() * 9 + 1) * 100000);
        stringRedisTemplate.opsForValue().set(
                email+":"+type,
                String.valueOf(code),
                3,
                TimeUnit.MINUTES);
        SimpleMailMessage message = getSimpleMailMessage(email, type, code);
        if ("reset".equals(type)) {
            User user = authMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, email)
            );
            if (user == null) {
                throw new BusinessException("该邮箱未注册");
            }
        }
        BeanUtils.copyProperties(emailRecord, message);
        emailRecord.setContent(message.getText());
        emailRecord.setTime(LocalDateTime.now());
        emailRecord.setStatus(1);
        emailRecord.setEmail(email);
        emailRecord.setTitle(message.getSubject());
        emailMapper.insert(emailRecord);
        javaMailSender.send(message);
    }


    /**
     * 用户注册
     */
    @Override
    public void register(RegisterDTO dto) {
        String code = stringRedisTemplate.opsForValue().get(dto.getEmail() + ":register");
        if (code == null) {
            throw new BusinessException("验证码已过期");
        }
        if (!code.equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }
        if (authMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail())) != null) {
            throw new BusinessException("邮箱已存在");
        }
        if (authMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 加密密码
        String encoded = bCryptPasswordEncoder.encode(dto.getPassword());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoded);
        user.setEmail(dto.getEmail());
        user.setRole("user");
        user.setMute(0);
        user.setBanned(0);
        user.setCreateTime(LocalDateTime.now());
        authMapper.insert(user);
        stringRedisTemplate.delete(dto.getEmail() + ":register");
        emailStatus(dto);

    }


    /**
     * 重置密码
     */
    @Override
    public void resetPassword(RegisterDTO dto) {
        User user = authMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String encoded = bCryptPasswordEncoder.encode(dto.getPassword());
        user.setPassword(encoded);
        authMapper.updateById(user);
        stringRedisTemplate.delete(dto.getEmail() + ":reset");
        emailStatus(dto);
    }

    private void emailStatus(RegisterDTO dto) {
        LambdaUpdateWrapper<EmailRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EmailRecord::getEmail, dto.getEmail());
        wrapper.set(EmailRecord::getStatus, 2);
        emailMapper.update(null, wrapper);
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
     * 重置密码校验
     */
    @Override
    public void resetConfirm(RegisterDTO dto) {
        User user = authMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
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
    }


}
