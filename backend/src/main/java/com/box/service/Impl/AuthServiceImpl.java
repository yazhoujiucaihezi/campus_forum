package com.box.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.box.dto.LoginDTO;
import com.box.entity.User;
import com.box.exception.BusinessException;
import com.box.mapper.AuthMapper;
import com.box.service.AuthService;
import com.box.vo.LoginVO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {

    private final AuthMapper authMapper;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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

}
