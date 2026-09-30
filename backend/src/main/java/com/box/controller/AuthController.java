package com.box.controller;

import com.box.common.Result;
import com.box.dto.LoginDTO;
import com.box.service.AuthService;
import com.box.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证模块接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto){
        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }
}
