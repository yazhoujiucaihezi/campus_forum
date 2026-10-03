package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.box.common.Result;
import com.box.dto.ChangePasswordDTO;
import com.box.dto.RegisterDTO;
import com.box.dto.UserDetailDTO;
import com.box.entity.User;
import com.box.entity.UserDetail;
import com.box.entity.UserPrivacy;
import com.box.exception.BusinessException;
import com.box.mapper.UserDetailMapper;
import com.box.mapper.UserMapper;
import com.box.mapper.UserPrivacyMapper;
import com.box.service.UserService;
import com.box.vo.TopicUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final UserPrivacyMapper userPrivacyMapper;
    private final UserDetailMapper userDetailMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;


    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<TopicUserVO> info(@RequestHeader("Authorization") String authHeader) {
        Integer uid = JwtUtils.getUid(authHeader);

        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        TopicUserVO vo = new TopicUserVO();
        BeanUtils.copyProperties(user, vo);

        return Result.success(vo);
    }

    /**
     * 获取当前登录用户详细信息
     */
    @GetMapping("/details")
    public Result<TopicUserVO> details(@RequestHeader("Authorization") String authHeader){
        Integer uid = JwtUtils.getUid(authHeader);
        TopicUserVO vo = new TopicUserVO();
        User user = userService.getById(uid);
        if (user != null) {
            BeanUtils.copyProperties(user, vo);
        }

        UserDetail detail = userDetailMapper.selectById(uid);
        if (detail != null) {
            BeanUtils.copyProperties(detail, vo);
        }
        return Result.success(vo);
    }

    /**
     * 获取当前登录用户隐私设置
     */
    @GetMapping("/privacy")
    public Result<UserPrivacy> privacy(@RequestHeader("Authorization") String authHeader){
        Integer uid = JwtUtils.getUid(authHeader);

        UserPrivacy userPrivacy = userPrivacyMapper.selectById(uid);

        return Result.success(userPrivacy);
    }

    @PostMapping("/save-details")
    public Result<Void> saveDetails(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody UserDetailDTO userDetailDTO){

        Integer uid = JwtUtils.getUid(authHeader);
        User user = new User();
        BeanUtils.copyProperties(userDetailDTO, user);
        user.setId(uid);
        userMapper.updateById(user);
        UserDetail userDetail = userDetailMapper.selectById(uid);
        if (userDetail == null) {
            userDetail = new UserDetail();
            BeanUtils.copyProperties(userDetailDTO, userDetail);
            userDetail.setId(uid);
            userDetailMapper.insert(userDetail);
        } else {
            BeanUtils.copyProperties(userDetailDTO, userDetail);
            userDetail.setId(uid);
            userDetailMapper.updateById(userDetail);
        }
        return Result.success(null);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody ChangePasswordDTO changePasswordDTO){
        Integer uid = JwtUtils.getUid(authHeader);
        User user = userMapper.selectById(uid);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(changePasswordDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if(changePasswordDTO.getPassword().equals(changePasswordDTO.getNew_password())){
         throw new BusinessException("新旧密码不能相同");
        }
        if (!changePasswordDTO.getNew_password().equals(changePasswordDTO.getNew_password_repeat())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        user.setPassword(bCryptPasswordEncoder.encode(changePasswordDTO.getNew_password()));
        userMapper.updateById(user);
        return Result.success(null);
    }

    @PostMapping("/modify-email")
    public Result<Void> modifyEmail(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody RegisterDTO dto) {
        Integer uid = JwtUtils.getUid(authHeader);
        User user = userMapper.selectById(uid);
        String oldEmail = user.getEmail();
        String newEmail = dto.getEmail();
        if (oldEmail.equals(newEmail)){
            throw new BusinessException("新旧邮箱不能重复");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, newEmail)) != null) {
            throw new BusinessException("邮箱已存在");
        }
        String code = stringRedisTemplate.opsForValue().get(newEmail + ":modify");
        if(code == null){
            throw new BusinessException("验证码已过期");
        }
        if(!code.equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }
        user.setEmail(newEmail);
        userMapper.updateById(user);
        return Result.success(null);
    }
}
