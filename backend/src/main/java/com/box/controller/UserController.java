package com.box.controller;

import com.box.common.Result;
import com.box.dto.ChangePasswordDTO;
import com.box.dto.RegisterDTO;
import com.box.dto.UserDetailDTO;
import com.box.entity.UserPrivacy;
import com.box.service.UserService;
import com.box.vo.TopicUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.box.utils.JwtUtils;

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
    public Result<TopicUserVO> info(@RequestHeader("Authorization") String authHeader) {
        return Result.success(userService.getInfo(JwtUtils.getUid(authHeader)));
    }

    /**
     * 获取当前登录用户详细信息
     */
    @GetMapping("/details")
    public Result<TopicUserVO> details(@RequestHeader("Authorization") String authHeader) {
        return Result.success(userService.getDetails(JwtUtils.getUid(authHeader)));
    }

    /**
     * 获取当前登录用户隐私设置
     */
    @GetMapping("/privacy")
    public Result<UserPrivacy> privacy(@RequestHeader("Authorization") String authHeader) {
        return Result.success(userService.getPrivacy(JwtUtils.getUid(authHeader)));
    }

    /**
     * 保存用户详细信息
     */
    @PostMapping("/save-details")
    public Result<Void> saveDetails(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody UserDetailDTO userDetailDTO) {
        userService.saveDetails(JwtUtils.getUid(authHeader), userDetailDTO);
        return Result.success(null);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(JwtUtils.getUid(authHeader), changePasswordDTO);
        return Result.success(null);
    }

    /**
     * 修改邮箱
     */
    @PostMapping("/modify-email")
    public Result<Void> modifyEmail(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody RegisterDTO dto) {
        userService.modifyEmail(JwtUtils.getUid(authHeader), dto);
        return Result.success(null);
    }
}
