package com.box.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.box.dto.CommentDTO;
import com.box.dto.TopicUpdateDTO;
import com.box.entity.Forum;
import com.box.entity.Topic;
import com.box.entity.TopicType;
import com.box.vo.CommentVO;
import com.box.vo.TopicDetailVO;

import java.util.List;

/**
 * 论坛板块服务接口
 */
public interface ForumService extends IService<Forum> {

    /** 获取帖子类型 */
    List<TopicType> getTopicTypes();

    /** 分页获取帖子列表 */
    List<Topic> page(Integer page, Integer type);

    /** 获取置顶帖子列表 */
    List<Topic> getTopTopic();

    /** 获取帖子详情 */
    TopicDetailVO getTopic(Integer uid, Integer tid);

    /** 帖子点赞/收藏 */
    void interact(Integer uid, Integer tid, String type, Boolean state);

    /** 获取帖子评论信息 */
    List<CommentVO> getComments(Integer tid, Integer page);

    /** 添加帖子评论 */
    void addComment(Integer uid, CommentDTO commentDTO);

    /** 删除帖子评论 */
    void deleteComment(Integer uid, String role, Integer id);

    /** 修改帖子 */
    void updateTopic(String role, Integer uid, TopicUpdateDTO dto);

    /** 创建帖子 */
    void createTopic(Integer uid, TopicUpdateDTO dto);

    /** 删除帖子 */
    void deleteTopic(Integer uid, String role, Integer tid);

    /** 获取当前用户的帖子列表 */
    List<Topic> getUserTopic(Integer uid);

    /** 搜索帖子 */
    List<Topic> searchTopic(String keyword);

    /** 获取当前用户收藏的帖子 */
    List<Topic> getCollectsTopic(Integer uid);
}
