package com.box.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.common.Result;
import com.box.dto.CommentDTO;
import com.box.dto.TopicUpdateDTO;
import com.box.entity.*;
import com.box.exception.BusinessException;
import com.box.mapper.*;
import com.box.vo.CommentVO;
import com.box.vo.TopicDetailVO;
import com.box.vo.TopicInteractVO;
import com.box.vo.TopicUserVO;
import com.box.service.WeatherService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import com.box.utils.JwtUtils;

import java.time.LocalDateTime;
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
    private final UserDetailMapper userDetailMapper;
    private final WeatherService weatherService;


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

        if (type != null && type != 0) {
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

        Integer uid = JwtUtils.getUid(authHeader);

        TopicDetailVO topicDetailVO = new TopicDetailVO();
        TopicInteractVO interactVO = new TopicInteractVO();
        TopicUserVO userVO = topicMapper.getTopicUser(tid);
        UserDetail userDetail = userDetailMapper.selectById(userVO.getId());
        if (userDetail != null) {
            BeanUtils.copyProperties(userDetail, userVO);
        }
        Topic topic = topicMapper.selectById(tid);
        if (topic != null) {
            BeanUtils.copyProperties(topic, topicDetailVO);
        }

        if(topic != null) {
            interactVO.setLikeCount(topicMapper.countLikeByTid(topic.getId()));
            interactVO.setCollectCount(topicMapper.countCollectByTid(topic.getId()));
            interactVO.setLike(topicMapper.countLike(topic.getId(), uid) > 0);
            interactVO.setCollect(topicMapper.countCollect(topic.getId(), uid) > 0);
        }

        topicDetailVO.setInteract(interactVO);
        topicDetailVO.setComments(topicMapper.countComments(tid));
        topicDetailVO.setUser(userVO);

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

        Integer uid = JwtUtils.getUid(authHeader);

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

            if (topicComment.getQuote() != null && topicComment.getQuote() > 0) {
                TopicComment quoted = topicCommentMapper.selectById(topicComment.getQuote());
                if (quoted != null) {
                    commentVO.setQuote(parseQuillText(quoted.getContent()));
                }
            }

            Integer commentUid = topicComment.getUid();
            TopicUserVO topicUserVO = new TopicUserVO();

            User user = userMapper.selectById(commentUid);
            if (user != null) {
                BeanUtils.copyProperties(user, topicUserVO);
            }

            UserDetail userDetail = userDetailMapper.selectById(commentUid);
            if (userDetail != null) {
                BeanUtils.copyProperties(userDetail, topicUserVO);
            }

            commentVO.setUser(topicUserVO);
            commentVOList.add(commentVO);
        }
        return Result.success(commentVOList);
    }

    /**
     * 添加帖子评论
     */
    @PostMapping("/add-comment")
    public Result<Void> addComment(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody CommentDTO commentDTO) {

        Integer uid = JwtUtils.getUid(authHeader);

        if (userMapper.selectById(uid).getMute() == 1){
            throw new BusinessException("乱嘿讲被禁言了舒服吗");
        }

        TopicComment topicComment = new TopicComment();
        BeanUtils.copyProperties(commentDTO, topicComment);
        topicComment.setTime(LocalDateTime.now());
        topicComment.setUid(uid);

        topicCommentMapper.insert(topicComment);

        return Result.success(null);
    }

    /**
     * 删除帖子评论
     */
    @GetMapping("/delete-comment")
    public Result<Void> deleteComment(@RequestHeader("Authorization") String authHeader,
                                      @RequestParam Integer id) {
        Integer uid = JwtUtils.getUid(authHeader);
        String role = JwtUtils.getRole(authHeader);
        TopicComment comment = topicCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        Integer tid = comment.getTid();
        Topic topic = topicMapper.selectById(tid);
        boolean isAuthor = comment.getUid().equals(uid);
        boolean isOwner = topic.getUid().equals(uid);
        boolean isAdmin = "admin".equals(role);
        if (!isAuthor && !isOwner && !isAdmin) {
            throw new BusinessException("无权删除");
        }
        topicCommentMapper.deleteById(id);
        return Result.success(null);
    }

    /**
     * 修改帖子
     */
    @PostMapping("/update-topic")
    public Result<Void> updateTopic(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody TopicUpdateDTO dto) {
        String role = JwtUtils.getRole(authHeader);
        Integer uid = JwtUtils.getUid(authHeader);

        Topic topic = topicMapper.selectById(dto.getId());
        if (!"admin".equals(role) || !uid.equals(topic.getUid())) {
            throw new BusinessException("无权修改");
        }
        BeanUtils.copyProperties(dto, topic);
        String text = parseQuillText(dto.getContent());
        String substring = text.substring(0, Math.min(text.length(), 8));
        topic.setIntro(substring);
        topicMapper.updateById(topic);
        return Result.success(null);
    }

    /**
     * 创建帖子
     */
    @PostMapping("/create-topic")
    public Result<Void> createTopic(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody TopicUpdateDTO dto) {
        Integer uid = JwtUtils.getUid(authHeader);

        if (userMapper.selectById(uid).getMute() == 1){
            throw new BusinessException("您已被禁言，请联系管理员");
        }

        Topic topic = new Topic();
        BeanUtils.copyProperties(dto, topic);
        String text = parseQuillText(dto.getContent());
        String substring = text.substring(0, Math.min(text.length(), 8));
        topic.setIntro(substring);
        topic.setUid(uid);
        topic.setTime(LocalDateTime.now());
        topicMapper.insert(topic);
        return Result.success(null);
    }

    /**
     * 删除帖子
     */
    @GetMapping("/delete-topic")
    public Result<Void> deleteTopic(@RequestHeader("Authorization") String authHeader,
                                    @RequestParam Integer tid) {
        Integer uid = JwtUtils.getUid(authHeader);
        String role = JwtUtils.getRole(authHeader);
        Topic topic = topicMapper.selectById(tid);
        if (!"admin".equals(role) && !uid.equals(topic.getUid())) {
            throw new BusinessException("无权删除");
        }
        topicMapper.deleteById(tid);
        topicMapper.deleteLikeByTid(tid);
        topicMapper.deleteCommentByTid(tid);
        topicMapper.deleteCollectByTid(tid);

        return Result.success(null);
    }

    /** 获取当前用户的帖子列表 */
    @GetMapping("/user-topic")
    public Result<List<Topic>> getUserTopic(@RequestHeader("Authorization") String authHeader){
        Integer uid = JwtUtils.getUid(authHeader);
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getUid, uid);
        return getListResult(wrapper);
    }

    /** 搜索帖子 */
    @GetMapping("/search-topic")
    public Result<List<Topic>> searchTopic(@RequestParam String keyword) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Topic::getTitle, keyword);
        wrapper.eq(Topic::getInvisible, 0);
        wrapper.orderByDesc(Topic::getTime);
        wrapper.last("LIMIT 20");
        List<Topic> list = topicMapper.selectList(wrapper);
        for (Topic t : list) {
            t.setLike(topicMapper.countLikeByTid(t.getId()));
            t.setCollect(topicMapper.countCollectByTid(t.getId()));
        }
        return Result.success(list);
    }

    /** 获取当前用户收藏的帖子 */
    @GetMapping("/collects")
    public Result<List<Topic>> getCollectsTopic(@RequestHeader("Authorization") String authHeader) {
        Integer uid = JwtUtils.getUid(authHeader);
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        List<Integer> collectTids = topicMapper.selectCollectTids(uid);
        if (collectTids.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        wrapper.in(Topic::getId, collectTids);
        return getListResult(wrapper);
    }

    @NotNull
    private Result<List<Topic>> getListResult(LambdaQueryWrapper<Topic> wrapper) {
        wrapper.orderByDesc(Topic::getTime);
        List<Topic> list = topicMapper.selectList(wrapper);
        for (Topic t : list) {
            t.setLike(topicMapper.countLikeByTid(t.getId()));
            t.setCollect(topicMapper.countCollectByTid(t.getId()));
        }
        return Result.success(list);
    }

    /** 解析 Quill 富文本为纯文本 */
    private String parseQuillText(String content) {
        if (content == null || content.isEmpty()) return "";
        try {
            JSONArray ops = JSON.parseObject(content).getJSONArray("ops");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ops.size(); i++) {
                JSONObject op = ops.getJSONObject(i);
                if (op.containsKey("insert")) {
                    Object insert = op.get("insert");
                    if (insert instanceof String) {
                        sb.append((String) insert);
                    }
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return content;
        }
    }

    @GetMapping("/weather")
    public Result<JsonNode> weather(@RequestParam String longitude,
                                    @RequestParam String latitude) {
        return Result.success(weatherService.getWeather(longitude, latitude));
    }

}