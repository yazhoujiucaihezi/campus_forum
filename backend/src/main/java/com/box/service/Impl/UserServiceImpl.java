package com.box.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.box.dto.ChangePasswordDTO;
import com.box.dto.RegisterDTO;
import com.box.dto.UserDetailDTO;
import com.box.dto.UserPrivacyDTO;
import com.box.entity.EmailRecord;
import com.box.entity.User;
import com.box.entity.UserDetail;
import com.box.entity.UserPrivacy;
import com.box.exception.BusinessException;
import com.box.mapper.EmailMapper;
import com.box.mapper.UserDetailMapper;
import com.box.mapper.UserMapper;
import com.box.mapper.UserPrivacyMapper;
import com.box.service.UserService;
import com.box.vo.TopicUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserPrivacyMapper userPrivacyMapper;
    private final UserDetailMapper userDetailMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailMapper emailMapper;

    /**
     * 获取当前登录用户信息
     */
    @Override
    public TopicUserVO getInfo(Integer uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        TopicUserVO vo = new TopicUserVO();
        BeanUtils.copyProperties(user, vo);

        return vo;
    }

    /**
     * 获取当前登录用户详细信息
     */
    @Override
    public TopicUserVO getDetails(Integer uid) {
        TopicUserVO vo = new TopicUserVO();
        User user = getById(uid);
        if (user != null) {
            BeanUtils.copyProperties(user, vo);
        }

        UserDetail detail = userDetailMapper.selectById(uid);
        if (detail != null) {
            BeanUtils.copyProperties(detail, vo);
        }
        return vo;
    }

    /**
     * 获取当前登录用户隐私设置
     */
    @Override
    public UserPrivacy getPrivacy(Integer uid) {
        return userPrivacyMapper.selectById(uid);
    }

    /**
     * 保存用户详细信息
     */
    @Override
    public void saveDetails(Integer uid, UserDetailDTO userDetailDTO) {
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
    }

    /**
     * 修改密码
     */
    @Override
    public void changePassword(Integer uid, ChangePasswordDTO dto) {
        User user = userMapper.selectById(uid);
        if (!bCryptPasswordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (dto.getPassword().equals(dto.getNew_password())) {
            throw new BusinessException("新旧密码不能相同");
        }
        if (!dto.getNew_password().equals(dto.getNew_password_repeat())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        user.setPassword(bCryptPasswordEncoder.encode(dto.getNew_password()));
        userMapper.updateById(user);
    }

    /**
     * 修改邮箱
     */
    @Override
    public void modifyEmail(Integer uid, RegisterDTO dto) {
        User user = userMapper.selectById(uid);
        String oldEmail = user.getEmail();
        String newEmail = dto.getEmail();
        if (oldEmail.equals(newEmail)) {
            throw new BusinessException("新旧邮箱不能重复");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, newEmail)) != null) {
            throw new BusinessException("邮箱已存在");
        }
        String code = stringRedisTemplate.opsForValue().get(newEmail + ":modify");
        if (code == null) {
            throw new BusinessException("验证码已过期");
        }
        if (!code.equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }
        user.setEmail(newEmail);
        userMapper.updateById(user);
        LambdaUpdateWrapper<EmailRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EmailRecord::getEmail, dto.getEmail());
        wrapper.set(EmailRecord::getStatus, 2);
        emailMapper.update(null, wrapper);
        stringRedisTemplate.delete(newEmail + ":modify");
    }

    /**
     * 保存用户隐私设置
     */
    @Override
    public void savePrivacy(Integer uid, UserPrivacyDTO dto) {
        UserPrivacy privacy = userPrivacyMapper.selectById(uid);
        boolean isNew = (privacy == null);
        if (isNew) {
            privacy = new UserPrivacy();
            privacy.setId(uid);
        }

        int value = Boolean.TRUE.equals(dto.getStatus()) ? 1 : 0;
        switch (dto.getType()) {
            case "phone":  privacy.setPhone(value);  break;
            case "email":  privacy.setEmail(value);  break;
            case "wx":     privacy.setWx(value);     break;
            case "qq":     privacy.setQq(value);     break;
            case "gender": privacy.setGender(value); break;
        }

        if (isNew) {
            userPrivacyMapper.insert(privacy);
        } else {
            userPrivacyMapper.updateById(privacy);
        }
    }
}
