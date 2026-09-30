package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.Topic;
import com.box.vo.TopicUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 帖子表数据访问层：支持帖子列表分页查询
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    @Select("SELECT COUNT(*) FROM db_topic_interact_like WHERE tid = #{tid} AND uid = #{uid}")
    int countLike(@Param("tid") Integer tid, @Param("uid") Integer uid);

    @Select("SELECT COUNT(*) FROM db_topic_interact_collect WHERE tid = #{tid} AND uid = #{uid}")
    int countCollect(Integer tid, Integer uid);

    @Select("SELECT COUNT(*) FROM db_topic_comment WHERE tid = #{tid}")
    int countComments(@Param("tid") Integer tid);

    @Select("SELECT u.id, u.username, u.avatar FROM db_account u WHERE u.id = (SELECT t.uid FROM db_topic t WHERE t.id = #{tid})")
    TopicUserVO getTopicUser(Integer tid);

    @Select("SELECT COUNT(*) FROM db_topic_interact_like WHERE tid = #{tid}")
    Integer countLikeByTid(Integer tid);

    @Select("SELECT COUNT(*) FROM db_topic_interact_collect WHERE tid = #{tid}")
    Integer countCollectByTid(Integer tid);
}
