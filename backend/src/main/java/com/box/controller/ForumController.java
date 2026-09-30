package com.box.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.common.Result;
import com.box.entity.Topic;
import com.box.entity.TopicComment;
import com.box.entity.TopicType;
import com.box.entity.User;
import com.box.mapper.TopicCommentMapper;
import com.box.mapper.TopicMapper;
import com.box.mapper.TopicTypeMapper;
import com.box.mapper.UserMapper;
import com.box.vo.CommentVO;
import com.box.vo.TopicDetailVO;
import com.box.vo.TopicInteractVO;
import com.box.vo.TopicUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import utils.JwtUtils;

import java.util.ArrayList;
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

    private final TopicCommentMapper topicCommentMapper;

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

        for (Topic t : topicPage.getRecords()) {
            t.setLike(topicMapper.countLikeByTid(t.getId()));
            t.setCollect(topicMapper.countCollectByTid(t.getId()));
        }

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
        Integer uid = jwt.getClaim("uid").asInt();

        TopicDetailVO topicDetailVO = new TopicDetailVO();
        TopicInteractVO interactVO = new TopicInteractVO();
        Topic topic = topicMapper.selectById(tid);
        BeanUtils.copyProperties(topic, topicDetailVO);

        if (topic.getId() == 22) {
            interactVO.setLikeCount(999 + topicMapper.countLikeByTid(topic.getId()));
            interactVO.setCollectCount(999 + topicMapper.countCollectByTid(topic.getId()));
        } else {
            interactVO.setLikeCount(topicMapper.countLikeByTid(topic.getId()));
            interactVO.setCollectCount(topicMapper.countCollectByTid(topic.getId()));
        }

        // 当前用户有没有点赞、收藏
        interactVO.setLike(topicMapper.countLike(topic.getId(), uid) > 0);
        interactVO.setCollect(topicMapper.countCollect(topic.getId(), uid) > 0);

        topicDetailVO.setInteract(interactVO);
        topicDetailVO.setComments(topicMapper.countComments(tid));
        topicDetailVO.setUser(topicMapper.getTopicUser(tid));

        return Result.success(topicDetailVO);
    }

    /**
     * 获取帖子互动信息
     */
    @GetMapping("/interact")
    public Result<Void> interact(@RequestHeader("Authorization") String authHeader,
                                 @RequestParam Integer tid,
                                 @RequestParam String type,
                                 @RequestParam Boolean state) {
        String token = authHeader.substring(7);
        DecodedJWT jwt = JwtUtils.verifyToken(token);
        Integer uid = jwt.getClaim("uid").asInt();

        if ("like".equals(type)) {
            if (state) {
                topicMapper.addLike(tid, uid);
            } else {
                topicMapper.removeLike(tid, uid);
            }
        } else if ("collect".equals(type)) {
            if (state) {
                topicMapper.addCollect(tid, uid);
            } else {
                topicMapper.removeCollect(tid, uid);
            }
        }

        return Result.success(null);
    }

    /**
     * 获取帖子评论信息
     */
    @GetMapping("/comments")
    public Result<List<CommentVO>> getComments(@RequestParam Integer tid, @RequestParam Integer page) {
        List<CommentVO> commentVOList = new ArrayList<>();
        LambdaQueryWrapper<TopicComment> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TopicComment::getTid, tid);
        wrapper.orderByDesc(TopicComment::getTime);

        Page<TopicComment> topicCommentPage = topicCommentMapper.selectPage(new Page<>(page + 1, 10), wrapper);

        for (TopicComment topicComment : topicCommentPage.getRecords()) {
            CommentVO commentVO = new CommentVO();

            BeanUtils.copyProperties(topicComment, commentVO);
            User user = userMapper.selectById(topicComment.getUid());
            if (user != null) {
                TopicUserVO topicUserVO = new TopicUserVO();
                BeanUtils.copyProperties(user, topicUserVO);
                commentVO.setUser(topicUserVO);
            }
            commentVOList.add(commentVO);
        }


        return Result.success(commentVOList);
    }
    }