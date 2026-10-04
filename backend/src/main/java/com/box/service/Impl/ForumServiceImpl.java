package com.box.service.Impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.box.config.RabbitMQConfig;
import com.box.dto.CommentDTO;
import com.box.dto.NotificationMessage;
import com.box.dto.TopicUpdateDTO;
import com.box.entity.*;
import com.box.exception.BusinessException;
import com.box.mapper.*;
import com.box.service.ForumService;
import com.box.vo.CommentVO;
import com.box.vo.TopicDetailVO;
import com.box.vo.TopicInteractVO;
import com.box.vo.TopicUserVO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 论坛板块服务实现
 */
@Service
@RequiredArgsConstructor
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements ForumService {

    private final TopicMapper topicMapper;
    private final TopicTypeMapper topicTypeMapper;
    private final UserMapper userMapper;
    private final TopicCommentMapper topicCommentMapper;
    private final UserDetailMapper userDetailMapper;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 获取帖子类型
     */
    @Override
    public List<TopicType> getTopicTypes() {
        return topicTypeMapper.selectList(null);
    }

    /**
     * 分页获取帖子列表
     */
    @Override
    public List<Topic> page(Integer page, Integer type) {
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

        return topicPage.getRecords();
    }

    /**
     * 获取置顶帖子列表
     */
    @Override
    public List<Topic> getTopTopic() {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getTop, 1);
        wrapper.orderByDesc(Topic::getTime);
        return topicMapper.selectList(wrapper);
    }

    /**
     * 获取帖子详情
     */
    @Override
    public TopicDetailVO getTopic(Integer uid, Integer tid) {
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

        if (topic != null) {
            interactVO.setLikeCount(topicMapper.countLikeByTid(topic.getId()));
            interactVO.setCollectCount(topicMapper.countCollectByTid(topic.getId()));
            interactVO.setLike(topicMapper.countLike(topic.getId(), uid) > 0);
            interactVO.setCollect(topicMapper.countCollect(topic.getId(), uid) > 0);
        }

        topicDetailVO.setInteract(interactVO);
        topicDetailVO.setComments(topicMapper.countComments(tid));
        topicDetailVO.setUser(userVO);

        return topicDetailVO;
    }

    /**
     * 帖子点赞/收藏
     */
    @Override
    public void interact(Integer uid, Integer tid, String type, Boolean state) {
        // 先查操作者用户名，两种通知都要用
        String username = userMapper.selectById(uid).getUsername();

        if ("like".equals(type)) {
            if (state) {
                topicMapper.addLike(tid, uid);

                Topic topic = topicMapper.selectById(tid);
                if (topic != null && !topic.getUid().equals(uid)) {
                    NotificationMessage message = new NotificationMessage();
                    message.setUid(topic.getUid());
                    message.setTitle("收到新点赞");
                    message.setContent(username + " 点赞了你的帖子《" + topic.getTitle() + "》");
                    message.setType("like");
                    message.setUrl("/index/topic-detail/" + topic.getId());
                    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, message);
                }
            } else {
                topicMapper.removeLike(tid, uid);
            }
        } else if ("collect".equals(type)) {
            if (state) {
                topicMapper.addCollect(tid, uid);

                Topic topic = topicMapper.selectById(tid);
                if (topic != null && !topic.getUid().equals(uid)) {
                    NotificationMessage message = new NotificationMessage();
                    message.setUid(topic.getUid());
                    message.setTitle("收到新收藏");
                    message.setContent(username + " 收藏了你的帖子《" + topic.getTitle() + "》");
                    message.setType("collect");
                    message.setUrl("/index/topic-detail/" + topic.getId());
                    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, message);
                }
            } else {
                topicMapper.removeCollect(tid, uid);
            }
        }
    }
    /**
     * 获取帖子评论信息
     */
    @Override
    public List<CommentVO> getComments(Integer tid, Integer page) {
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
        return commentVOList;
    }

    /**
     * 添加帖子评论
     */
    @Override
    public void addComment(Integer uid, CommentDTO commentDTO) {
        if (userMapper.selectById(uid).getMute() == 1) {
            throw new BusinessException("乱嘿讲被禁言了舒服吗");
        }

        TopicComment topicComment = new TopicComment();
        BeanUtils.copyProperties(commentDTO, topicComment);
        topicComment.setTime(LocalDateTime.now());
        topicComment.setUid(uid);
        topicCommentMapper.insert(topicComment);
        Topic topic = topicMapper.selectById(commentDTO.getTid());
        if (topic != null && !topic.getUid().equals(uid)) {
            NotificationMessage message = new NotificationMessage();
            message.setUid(topic.getUid());
            message.setTitle("您的帖子《" + topic.getTitle() + "》有新评论");
            message.setContent(
                            userMapper.selectById(uid).getUsername()
                            + ":" +parseQuillText(topicComment.getContent()));
            message.setType("comment");
            message.setUrl("/index/topic-detail/" + topic.getId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, message);
        }
        if(topicComment.getQuote()>0){
            Integer replyId = topicCommentMapper.selectById(topicComment.getQuote()).getUid();
        if (!topicComment.getUid().equals(replyId)) {
            NotificationMessage message = new NotificationMessage();
            message.setUid(replyId);
            message.setTitle("您的评论《" + parseQuillText(topicCommentMapper.selectById(topicComment.getQuote()).getContent()) + "》有新回复");
            message.setContent(
                    userMapper.selectById(uid).getUsername()
                            + ":" + parseQuillText(topicComment.getContent()));
            message.setType("reply");
            message.setUrl("/index/topic-detail/" + replyId);
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, message);
        }
        }
    }

    /**
     * 删除帖子评论
     */
    @Override
    public void deleteComment(Integer uid, String role, Integer id) {
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
    }

    /**
     * 修改帖子
     */
    @Override
    public void updateTopic(String role, Integer uid, TopicUpdateDTO dto) {
        Topic topic = topicMapper.selectById(dto.getId());
        if (!"admin".equals(role) || !uid.equals(topic.getUid())) {
            throw new BusinessException("无权修改");
        }
        BeanUtils.copyProperties(dto, topic);
        String text = parseQuillText(dto.getContent());
        String substring = text.substring(0, Math.min(text.length(), 8));
        topic.setIntro(substring);
        topicMapper.updateById(topic);
    }

    /**
     * 创建帖子
     */
    @Override
    public void createTopic(Integer uid, TopicUpdateDTO dto) {
        if (userMapper.selectById(uid).getMute() == 1) {
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
    }

    /**
     * 删除帖子
     */
    @Override
    public void deleteTopic(Integer uid, String role, Integer tid) {
        Topic topic = topicMapper.selectById(tid);
        if (!"admin".equals(role) && !uid.equals(topic.getUid())) {
            throw new BusinessException("无权删除");
        }
        topicMapper.deleteById(tid);
        topicMapper.deleteLikeByTid(tid);
        topicMapper.deleteCommentByTid(tid);
        topicMapper.deleteCollectByTid(tid);
    }

    /**
     * 获取当前用户的帖子列表
     */
    @Override
    public List<Topic> getUserTopic(Integer uid) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getUid, uid);
        return selectWithInteract(wrapper);
    }

    /**
     * 搜索帖子
     */
    @Override
    public List<Topic> searchTopic(String keyword) {
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
        return list;
    }

    /**
     * 获取当前用户收藏的帖子
     */
    @Override
    public List<Topic> getCollectsTopic(Integer uid) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        List<Integer> collectTids = topicMapper.selectCollectTids(uid);
        if (collectTids.isEmpty()) {
            return new ArrayList<>();
        }
        wrapper.in(Topic::getId, collectTids);
        return selectWithInteract(wrapper);
    }

    @NotNull
    private List<Topic> selectWithInteract(LambdaQueryWrapper<Topic> wrapper) {
        wrapper.orderByDesc(Topic::getTime);
        List<Topic> list = topicMapper.selectList(wrapper);
        for (Topic t : list) {
            t.setLike(topicMapper.countLikeByTid(t.getId()));
            t.setCollect(topicMapper.countCollectByTid(t.getId()));
        }
        return list;
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
}
