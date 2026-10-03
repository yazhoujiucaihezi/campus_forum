package com.box.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.dto.AdminChangePasswordDTO;
import com.box.dto.AdminStatusDTO;
import com.box.dto.AdminUserSaveDTO;
import com.box.dto.TopicTypeDTO;
import com.box.entity.*;
import com.box.exception.BusinessException;
import com.box.mapper.*;
import com.box.service.AdminService;
import com.box.vo.AdminUserDetailVO;
import com.box.vo.AdminUserPrivacyVO;
import com.box.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 后台管理服务实现
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final TopicMapper topicMapper;
    private final TopicTypeMapper topicTypeMapper;
    private final UserMapper userMapper;
    private final UserDetailMapper userDetailMapper;
    private final UserPrivacyMapper userPrivacyMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailMapper emailMapper;
    private final AuthServiceImpl authServiceImpl;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取论坛帖子列表
     */
    @Override
    public Page<Topic> getForumList(String role, Integer page, Integer size, String keyword) {
        checkRole(role);
        LambdaQueryWrapper<Topic> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null) {
            queryWrapper.like(Topic::getTitle, keyword);
        }
        queryWrapper.orderByDesc(Topic::getTime);
        return topicMapper.selectPage(new Page<>(page, size), queryWrapper);
    }

    /**
     * 置顶/取消置顶帖子
     */
    @Override
    public void topForum(String role, AdminStatusDTO adminStatusDTO) {
        checkRole(role);
        Integer tid = adminStatusDTO.getTid();
        Boolean status = adminStatusDTO.getStatus();
        Topic topic = topicMapper.selectById(tid);
        topic.setTop(status ? 1 : 0);
        topicMapper.updateById(topic);
    }

    /**
     * 删除帖子
     */
    @Override
    public void deleteForum(String role, Integer tid) {
        checkRole(role);
        Topic topic = topicMapper.selectById(tid);
        if (topic == null) {
            throw new RuntimeException("帖子不存在");
        }

        topicMapper.deleteCommentByTid(tid);
        topicMapper.deleteLikeByTid(tid);
        topicMapper.deleteCollectByTid(tid);
        topicMapper.deleteById(tid);
    }

    /**
     * 锁定/解锁帖子
     */
    @Override
    public void lockedForum(String role, AdminStatusDTO adminStatusDTO) {
        checkRole(role);
        Integer tid = adminStatusDTO.getTid();
        Boolean locked = adminStatusDTO.getLocked();
        Topic topic = topicMapper.selectById(tid);
        topic.setLocked(locked ? 1 : 0);
        topicMapper.updateById(topic);
    }

    /**
     * 隐藏/显示帖子
     */
    @Override
    public void invisibleForum(String role, AdminStatusDTO adminStatusDTO) {
        checkRole(role);
        Integer tid = adminStatusDTO.getTid();
        Boolean invisible = adminStatusDTO.getStatus();
        Topic topic = topicMapper.selectById(tid);
        topic.setInvisible(invisible ? 1 : 0);
        topic.setTop(invisible ? 0 : 1);
        topicMapper.updateById(topic);
    }

    /**
     * 修改帖子类型
     */
    @Override
    public void changeTopicType(String role, Integer tid, Integer type) {
        checkRole(role);
        Topic topic = topicMapper.selectById(tid);
        topic.setType(type);
        topicMapper.updateById(topic);
    }

    /**
     * 新增帖子分类
     */
    @Override
    public void createType(String role, TopicTypeDTO topicTypeDTO) {
        checkRole(role);
        TopicType topicType = new TopicType();
        BeanUtils.copyProperties(topicTypeDTO, topicType);
        topicTypeMapper.insert(topicType);
    }

    /**
     * 修改帖子分类
     */
    @Override
    public void updateType(String role, TopicTypeDTO topicTypeDTO) {
        checkRole(role);
        TopicType topicType = new TopicType();
        TopicType type = topicTypeMapper.selectById(topicTypeDTO.getId());
        if (type == null) {
            throw new RuntimeException("分类不存在");
        }
        BeanUtils.copyProperties(topicTypeDTO, topicType);
        topicTypeMapper.updateById(topicType);
    }

    /**
     * 获取用户列表
     */
    @Override
    public Page<User> getUserList(String role, Integer page, Integer size, String keyword) {
        checkRole(role);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null) {
            queryWrapper.like(User::getUsername, keyword);
        }
        queryWrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), queryWrapper);

        for (User u : userPage.getRecords()) {
            u.setPassword(null);
        }
        return userPage;
    }

    /**
     * 获取用户详情
     */
    @Override
    public AdminUserVO getUserDetail(String role, Integer id) {
        checkRole(role);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        AdminUserVO adminUserVO = new AdminUserVO();
        BeanUtils.copyProperties(user, adminUserVO);
        adminUserVO.setMute(user.getMute() != null && user.getMute() == 1);
        adminUserVO.setBanned(user.getBanned() != null && user.getBanned() == 1);
        UserDetail userDetail = userDetailMapper.selectById(id);
        if (userDetail != null) {
            AdminUserDetailVO adminUserDetailVO = new AdminUserDetailVO();
            BeanUtils.copyProperties(userDetail, adminUserDetailVO);
            adminUserVO.setDetail(adminUserDetailVO);
        }
        UserPrivacy userPrivacy = userPrivacyMapper.selectById(id);
        if (userPrivacy != null) {
            AdminUserPrivacyVO adminUserPrivacyVO = new AdminUserPrivacyVO();
            BeanUtils.copyProperties(userPrivacy, adminUserPrivacyVO);
            adminUserVO.setPrivacy(adminUserPrivacyVO);
        }

        return adminUserVO;
    }

    /**
     * 保存用户信息
     */
    @Override
    public void saveUser(String role, AdminUserSaveDTO dto) {
        checkRole(role);
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setMute(Boolean.TRUE.equals(dto.getMute()) ? 1 : 0);
        user.setBanned(Boolean.TRUE.equals(dto.getBanned()) ? 1 : 0);
        userMapper.updateById(user);
        UserDetail userDetail = new UserDetail();
        BeanUtils.copyProperties(dto.getDetail(), userDetail);
        userDetail.setId(dto.getId());
        userDetailMapper.updateById(userDetail);
        UserPrivacy userPrivacy = new UserPrivacy();
        BeanUtils.copyProperties(dto.getPrivacy(), userPrivacy);
        userPrivacy.setId(dto.getId());
        userPrivacyMapper.updateById(userPrivacy);
    }

    /**
     * 管理员修改用户密码
     */
    @Override
    public void changePassword(String role, AdminChangePasswordDTO dto) {
        checkRole(role);
        User user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(bCryptPasswordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * 获取邮件列表
     */
    @Override
    public Page<EmailRecord> getEmailList(String role, Integer page, Integer size) {
        checkRole(role);
        LambdaQueryWrapper<EmailRecord> queryWrapper = new LambdaQueryWrapper<>();
        return emailMapper.selectPage(new Page<>(page, size), queryWrapper);
    }

    @Override
    public void resendEmail(String role, Integer id) {
        checkRole(role);
        EmailRecord emailRecord = emailMapper.selectById(id);
        String type;
        String title = emailRecord.getTitle();
        if (title.contains("注册")) {
            type = "register";
        } else if (title.contains("修改")) {
            type = "modify";
        } else {
            type = "reset";
        }

        try {
            authServiceImpl.askCode(emailRecord.getEmail(),type);
        } catch (Exception e) {
            throw new BusinessException("邮件重发失败");
        }
    }

    /**
     * 校验管理员权限
     */
    private void checkRole(String role) {
        if (!"admin".equals(role)) {
            throw new RuntimeException("无权限");
        }
    }
}
