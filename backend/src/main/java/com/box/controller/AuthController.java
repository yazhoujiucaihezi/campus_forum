package com.box.controller;

import com.box.common.Result;
import com.box.dto.LoginDTO;
import com.box.dto.RegisterDTO;
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

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto){
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
        authService.askCode(email, type);
        return Result.success(null);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success(null);
    }

    /**
     * 重置密码校验
     */
    @PostMapping("/reset-confirm")
    public Result<Void> resetConfirm(@RequestBody RegisterDTO dto) {
        authService.resetConfirm(dto);
        return Result.success(null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody RegisterDTO dto) {
        authService.resetPassword(dto);
        return Result.success(null);
    }

}
