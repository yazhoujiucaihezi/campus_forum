package com.box.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.common.Result;
import com.box.entity.Topic;
import com.box.entity.TopicType;
import com.box.entity.User;
import com.box.mapper.TopicMapper;
import com.box.mapper.TopicTypeMapper;
import com.box.mapper.UserMapper;
import com.box.vo.TopicDetailVO;
import com.box.vo.TopicInteractVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import utils.JwtUtils;

import java.util.List;

/**
 * 论坛板块接口
 */
@RequestMapping("/api/forum")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ForumController {

    private final TopicMapper topicMapper;

    private final TopicTypeMapper topicTypeMapper;

    private final UserMapper userMapper;

    /**
     * 获取帖子类型
     */
    @GetMapping("/types")
    public Result<List<TopicType>> getTopicTypes() {

        List<TopicType> types = topicTypeMapper.selectList(null);

        return Result.success(types);
    }

    /**
     * 分页获取帖子列表
     */
    @GetMapping("/list-topic")
    public Result<List<Topic>> page(
            @RequestParam Integer page,
            @RequestParam(required = false) Integer type) {

        LambdaQueryWrapper<Topic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Topic::getInvisible, 0);

        if (type != null && type != 0){
            queryWrapper.eq(Topic::getType, type);
        }

        queryWrapper.orderByDesc(Topic::getTop).orderByDesc(Topic::getTime);

        Page<Topic> topicPage = topicMapper.selectPage(new Page<>(page, 10), queryWrapper);

        return Result.success(topicPage.getRecords());
    }

    /**
     * 获取置顶帖子列表
     */
    @GetMapping("/top-topic")
    public Result<List<Topic>> getTopTopic() {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getTop, 1);
        wrapper.orderByDesc(Topic::getTime);
        return Result.success(topicMapper.selectList(wrapper));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/topic")
    public Result<TopicDetailVO> getTopic(@RequestHeader("Authorization") String authHeader, @RequestParam Integer tid) {

     String token = authHeader.substring(7);

     DecodedJWT jwt = JwtUtils.verifyToken(token);

     String username = jwt.getClaim("username").asString();

     User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

     Integer uid = user.getId();

     TopicDetailVO topicDetailVO = new TopicDetailVO();
        TopicInteractVO interactVO = new TopicInteractVO();
        Topic topic = topicMapper.selectById(tid);
        BeanUtils.copyProperties(topic, topicDetailVO);

        interactVO.setLike(topicMapper.countLike(uid, topic.getUid())>0);
        interactVO.setCollect(topicMapper.countCollect(uid, topic.getUid())>0);
        topicDetailVO.setInteract(interactVO);
        topicDetailVO.setComments(topicMapper.countComments(tid));
        topicDetailVO.setUser(topicMapper.getTopicUser(tid));

        return Result.success(topicDetailVO);
    }

    /**
     * 获取帖子互动信息
     */
    @GetMapping("/interact")
    public Result<TopicInteractVO> getInteract(@RequestHeader("Authorization") String authHeader) {

        TopicInteractVO interactVO = new TopicInteractVO();

        String token = authHeader.substring(7);
        DecodedJWT jwt = JwtUtils.verifyToken(token);
        Integer uid = jwt.getClaim("uid").asInt();

        Topic topic = topicMapper.selectById(uid);

        interactVO.setLike(topicMapper.countLike(uid, topic.getUid())>0);
        interactVO.setCollect(topicMapper.countCollect(uid, topic.getUid())>0);

        return Result.success(interactVO);

    }
}
