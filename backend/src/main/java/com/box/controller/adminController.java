package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.common.Result;
import com.box.dto.AdminStatusDTO;
import com.box.dto.TopicTypeDTO;
import com.box.entity.Topic;
import com.box.entity.TopicType;
import com.box.entity.User;
import com.box.mapper.TopicMapper;
import com.box.mapper.TopicTypeMapper;
import com.box.mapper.UserMapper;
import com.box.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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

    public void checkRole(String authHeader) {
        String role = JwtUtils.getRole(authHeader);
        if (!"admin".equals(role)) {
            throw new RuntimeException("无权限");
        }
    }


}
