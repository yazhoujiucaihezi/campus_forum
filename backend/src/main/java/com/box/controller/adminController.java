package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.common.Result;
import com.box.dto.AdminStatusDTO;
import com.box.entity.Topic;
import com.box.mapper.TopicMapper;
import com.box.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Transactional
public class adminController {

    private final TopicMapper topicMapper;


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


    public void checkRole(String authHeader) {
        String role = JwtUtils.getRole(authHeader);
        if (!"admin".equals(role)) {
            throw new RuntimeException("无权限");
        }
    }


}
