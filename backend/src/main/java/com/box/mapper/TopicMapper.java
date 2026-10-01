package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.Topic;
import com.box.vo.TopicUserVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 帖子数据访问层 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    /** 查询某用户是否给某帖子点赞 */
    @Select("SELECT COUNT(*) FROM db_topic_interact_like WHERE tid = #{tid} AND uid = #{uid}")
    int countLike(@Param("tid") Integer tid, @Param("uid") Integer uid);

    /** 查询某用户是否收藏某帖子 */
    @Select("SELECT COUNT(*) FROM db_topic_interact_collect WHERE tid = #{tid} AND uid = #{uid}")
    int countCollect(@Param("tid") Integer tid, @Param("uid") Integer uid);

    /** 查询帖子评论数 */
    @Select("SELECT COUNT(*) FROM db_topic_comment WHERE tid = #{tid}")
    int countComments(@Param("tid") Integer tid);

    /** 查询帖子作者信息 */
    @Select("SELECT u.id, u.username, u.avatar FROM db_account u WHERE u.id = (SELECT t.uid FROM db_topic t WHERE t.id = #{tid})")
    TopicUserVO getTopicUser(Integer tid);

    /** 查询帖子点赞总数 */
    @Select("SELECT COUNT(*) FROM db_topic_interact_like WHERE tid = #{tid}")
    Integer countLikeByTid(Integer tid);

    /** 查询帖子收藏总数 */
    @Select("SELECT COUNT(*) FROM db_topic_interact_collect WHERE tid = #{tid}")
    Integer countCollectByTid(Integer tid);

    /** 点赞 */
    @Insert("INSERT IGNORE INTO db_topic_interact_like(tid, uid, time) VALUES(#{tid}, #{uid}, NOW())")
    int addLike(@Param("tid") Integer tid, @Param("uid") Integer uid);

    /** 取消点赞 */
    @Delete("DELETE FROM db_topic_interact_like WHERE tid = #{tid} AND uid = #{uid}")
    int removeLike(@Param("tid") Integer tid, @Param("uid") Integer uid);

    /** 收藏 */
    @Insert("INSERT IGNORE INTO db_topic_interact_collect(tid, uid, time) VALUES(#{tid}, #{uid}, NOW())")
    int addCollect(@Param("tid") Integer tid, @Param("uid") Integer uid);

    /** 取消收藏 */
    @Delete("DELETE FROM db_topic_interact_collect WHERE tid = #{tid} AND uid = #{uid}")
    int removeCollect(@Param("tid") Integer tid, @Param("uid") Integer uid);

    /** 删除帖子关联的点赞记录 */
    @Delete("DELETE FROM db_topic_interact_like WHERE tid = #{id}")
    void deleteLikeByTid(Integer id);

    /** 删除帖子关联的评论 */
    @Delete("DELETE FROM db_topic_comment WHERE tid = #{id}")
    void deleteCommentByTid(Integer id);

    /** 删除帖子关联的收藏记录 */
    @Delete("DELETE FROM db_topic_interact_collect WHERE tid = #{id}")
    void deleteCollectByTid(Integer id);

    /** 查询用户收藏的帖子 ID 列表 */
    @Select("SELECT tid FROM db_topic_interact_collect WHERE uid = #{uid} ORDER BY time DESC")
    List<Integer> selectCollectTids(@Param("uid") Integer uid);
}