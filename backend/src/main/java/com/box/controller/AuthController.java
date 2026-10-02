package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.box.common.Result;
import com.box.dto.LoginDTO;
import com.box.entity.User;
import com.box.mapper.UserMapper;
import com.box.service.AuthService;
import com.box.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证模块接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

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
}
