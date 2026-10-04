package com.box.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.box.dto.ChangePasswordDTO;
import com.box.dto.RegisterDTO;
import com.box.dto.UserDetailDTO;
import com.box.dto.UserPrivacyDTO;
import com.box.entity.User;
import com.box.entity.UserPrivacy;
import com.box.vo.TopicUserVO;

/**
 * 用户服务接口：复用 MyBatis-Plus 通用 CRUD，用于按条件查询用户信息
 */
public interface UserService extends IService<User> {

    /** 获取当前登录用户信息 */
    TopicUserVO getInfo(Integer uid);

    /** 获取当前登录用户详细信息 */
    TopicUserVO getDetails(Integer uid);

    /** 获取当前登录用户隐私设置 */
    UserPrivacy getPrivacy(Integer uid);

    /** 保存用户详细信息 */
    void saveDetails(Integer uid, UserDetailDTO dto);

    /** 修改密码 */
    void changePassword(Integer uid, ChangePasswordDTO dto);

    /** 修改邮箱 */
    void modifyEmail(Integer uid, RegisterDTO dto);

    /** 保存用户隐私设置 */
    void savePrivacy(Integer uid, UserPrivacyDTO dto);
}
