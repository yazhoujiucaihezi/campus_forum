package com.box.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.dto.AdminChangePasswordDTO;
import com.box.dto.AdminStatusDTO;
import com.box.dto.AdminUserSaveDTO;
import com.box.dto.TopicTypeDTO;
import com.box.entity.EmailRecord;
import com.box.entity.Topic;
import com.box.entity.User;
import com.box.vo.AdminUserVO;

/**
 * 后台管理服务接口
 */
public interface AdminService {

    /** 获取论坛帖子列表 */
    Page<Topic> getForumList(String role, Integer page, Integer size, String keyword);

    /** 置顶/取消置顶帖子 */
    void topForum(String role, AdminStatusDTO adminStatusDTO);

    /** 删除帖子 */
    void deleteForum(String role, Integer tid);

    /** 锁定/解锁帖子 */
    void lockedForum(String role, AdminStatusDTO adminStatusDTO);

    /** 隐藏/显示帖子 */
    void invisibleForum(String role, AdminStatusDTO adminStatusDTO);

    /** 修改帖子类型 */
    void changeTopicType(String role, Integer tid, Integer type);

    /** 新增帖子分类 */
    void createType(String role, TopicTypeDTO topicTypeDTO);

    /** 修改帖子分类 */
    void updateType(String role, TopicTypeDTO topicTypeDTO);

    /** 获取用户列表 */
    Page<User> getUserList(String role, Integer page, Integer size, String keyword);

    /** 获取用户详情 */
    AdminUserVO getUserDetail(String role, Integer id);

    /** 保存用户信息 */
    void saveUser(String role, AdminUserSaveDTO dto);

    /** 管理员修改用户密码 */
    void changePassword(String role, AdminChangePasswordDTO dto);

    Page<EmailRecord> getEmailList(String role, Integer page, Integer size);

    void resendEmail(String role, Integer id);
}
