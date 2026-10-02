package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.common.Result;
import com.box.dto.AdminChangePasswordDTO;
import com.box.dto.AdminStatusDTO;
import com.box.dto.AdminUserSaveDTO;
import com.box.dto.TopicTypeDTO;
import com.box.entity.*;
import com.box.mapper.*;
import com.box.utils.JwtUtils;
import com.box.vo.AdminUserDetailVO;
import com.box.vo.AdminUserPrivacyVO;
import com.box.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Transactional
public class adminController {

    private final TopicMapper topicMapper;
    private final TopicTypeMapper topicTypeMapper;
    private final UserMapper userMapper;
    private final UserDetailMapper userDetailMapper;
    private final UserPrivacyMapper userPrivacyMapper;




    /**
     * 获取论坛列表
     */
    @GetMapping("/forum/list")
    public Result<Page<Topic>> getForumList(@RequestHeader("Authorization") String authHeader,
                                            @RequestParam Integer page,
                                            @RequestParam Integer size,
                                            @RequestParam(required = false) String keyword){
        checkRole(authHeader);
        LambdaQueryWrapper<Topic> queryWrapper = new LambdaQueryWrapper<>();
        if(keyword != null) {
            queryWrapper.like(Topic::getTitle, keyword);
        }
        queryWrapper.orderByDesc(Topic::getTime);
        return Result.success(topicMapper.selectPage(new Page<>(page, size), queryWrapper));
    }

    /**
     * 置顶/取消置顶帖子
     */
    @PostMapping("/forum/top")
    public Result<?> topForum(@RequestHeader("Authorization") String authHeader,
                              @RequestBody AdminStatusDTO adminStatusDTO) {
        checkRole(authHeader);
        Integer tid = adminStatusDTO.getTid();
        Boolean status = adminStatusDTO.getStatus();
        Topic topic = topicMapper.selectById(tid);
        topic.setTop(status ? 1 : 0);
        topicMapper.updateById(topic);
        return Result.success("操作成功");
    }



    /**
     * 删除帖子
     */
    @GetMapping("/forum/delete")
    public Result<?> deleteForum(@RequestHeader("Authorization") String authHeader,
                                 @RequestParam Integer tid) {
        checkRole(authHeader);
        Topic topic = topicMapper.selectById(tid);
        if (topic == null) {
            throw new RuntimeException("帖子不存在");
        }

        topicMapper.deleteCommentByTid(tid);
        topicMapper.deleteLikeByTid(tid);
        topicMapper.deleteCollectByTid(tid);
        topicMapper.deleteById(tid);

        return Result.success("操作成功");
    }

    /**
     * 锁定/解锁帖子
     */
    @PostMapping("/forum/locked")
    public Result<Void> lockedForum(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody AdminStatusDTO adminStatusDTO) {
        checkRole(authHeader);
        Integer tid = adminStatusDTO.getTid();
        Boolean locked = adminStatusDTO.getLocked();
        Topic topic = topicMapper.selectById(tid);
        topic.setLocked(locked ? 1 : 0);
        topicMapper.updateById(topic);
        return Result.success(null);
    }

    /**
     * 隐藏/显示帖子
     */
    @PostMapping("/forum/invisible")
    public Result<Void> invisibleForum(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody AdminStatusDTO adminStatusDTO) {
        checkRole(authHeader);
        Integer tid = adminStatusDTO.getTid();
        Boolean invisible = adminStatusDTO.getStatus();
        Topic topic = topicMapper.selectById(tid);
        topic.setInvisible(invisible ? 1 : 0);
        topic.setTop(invisible ? 0 : 1);
        topicMapper.updateById(topic);
        return Result.success(null);
    }

    /**
     * 修改帖子类型
     */
    @GetMapping("/forum/change-topic-type")
    public Result<?> changeTopicType(@RequestHeader("Authorization") String authHeader,
                                     @RequestParam Integer tid,
                                     @RequestParam Integer type) {
        checkRole(authHeader);
        Topic topic = topicMapper.selectById(tid);
        topic.setType(type);
        topicMapper.updateById(topic);
        return Result.success("操作成功");
    }

    @PostMapping("/forum/create-type")
    public Result<Void> createType(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody TopicTypeDTO topicTypeDTO) {
        checkRole(authHeader);
        TopicType topicType = new TopicType();
        BeanUtils.copyProperties(topicTypeDTO, topicType);
        topicTypeMapper.insert(topicType);
        return Result.success(null);
    }

    @PostMapping("/forum/update-type")
    public Result<Void> updateType(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody TopicTypeDTO topicTypeDTO) {
        checkRole(authHeader);
        TopicType topicType = new TopicType();
        TopicType type = topicTypeMapper.selectById(topicTypeDTO.getId());
        if (type == null) {
            throw new RuntimeException("分类不存在");
        }
        BeanUtils.copyProperties(topicTypeDTO, topicType);
        topicTypeMapper.updateById(topicType);
        return Result.success(null);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/user/list")
    public Result<Page<User>> getUserList(@RequestHeader("Authorization") String authHeader,
                                          @RequestParam Integer page,
                                          @RequestParam Integer size,
                                          @RequestParam(required = false) String keyword){
        checkRole(authHeader);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if(keyword != null) {
            queryWrapper.like(User::getUsername, keyword);
        }
        queryWrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), queryWrapper);

        for (User u : userPage.getRecords()) {
            u.setPassword(null);
        }
        return Result.success(userPage);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/user/detail")
    public Result<AdminUserVO> getUserDetail(@RequestHeader("Authorization") String authHeader,
                                             @RequestParam Integer id){
        checkRole(authHeader);
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

        return Result.success(adminUserVO);
    }

    @PostMapping("/user/save")
    public Result<Void> saveUser(@RequestHeader("Authorization") String authHeader,
                                 @RequestBody AdminUserSaveDTO dto) {
        checkRole(authHeader);
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
        return Result.success(null);
    }

    @PostMapping("/user/change-password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody AdminChangePasswordDTO dto) {
        checkRole(authHeader);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        return Result.success(null);
    }

    public void checkRole(String authHeader) {
        String role = JwtUtils.getRole(authHeader);
        if (!"admin".equals(role)) {
            throw new RuntimeException("无权限");
        }
    }


}
