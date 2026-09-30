package com.box.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.box.common.Result;
import com.box.entity.User;
import com.box.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.JwtUtils;

/**
 * 用户模块接口
 */
@RequestMapping("/api/user")
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(@RequestHeader("Authorization") String authHeader){

        String token = authHeader.substring(7);

        DecodedJWT decodedJWT = JwtUtils.verifyToken(token);

        String username = decodedJWT.getClaim("username").asString();

        User user = userService.getOne(new QueryWrapper<User>().eq("username", username));

        return Result.success(user);
    }

}
