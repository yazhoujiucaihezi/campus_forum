package com.box.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.common.Result;
import com.box.dto.AdminChangePasswordDTO;
import com.box.dto.AdminStatusDTO;
import com.box.dto.AdminUserSaveDTO;
import com.box.dto.TopicTypeDTO;
import com.box.entity.EmailRecord;
import com.box.entity.Topic;
import com.box.entity.User;
import com.box.service.AdminService;
import com.box.utils.JwtUtils;
import com.box.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class adminController {

    private final AdminService adminService;

    /**
     * 获取论坛列表
     */
    @GetMapping("/forum/list")
    public Result<Page<Topic>> getForumList(@RequestHeader("Authorization") String authHeader,
                                            @RequestParam Integer page,
                                            @RequestParam Integer size,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getForumList(JwtUtils.getRole(authHeader), page, size, keyword));
    }

    /**
     * 置顶/取消置顶帖子
     */
    @PostMapping("/forum/top")
    public Result<?> topForum(@RequestHeader("Authorization") String authHeader,
                              @RequestBody AdminStatusDTO adminStatusDTO) {
        adminService.topForum(JwtUtils.getRole(authHeader), adminStatusDTO);
        return Result.success("操作成功");
    }

    /**
     * 删除帖子
     */
    @GetMapping("/forum/delete")
    public Result<?> deleteForum(@RequestHeader("Authorization") String authHeader,
                                 @RequestParam Integer tid) {
        adminService.deleteForum(JwtUtils.getRole(authHeader), tid);
        return Result.success("操作成功");
    }

    /**
     * 锁定/解锁帖子
     */
    @PostMapping("/forum/locked")
    public Result<Void> lockedForum(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody AdminStatusDTO adminStatusDTO) {
        adminService.lockedForum(JwtUtils.getRole(authHeader), adminStatusDTO);
        return Result.success(null);
    }

    /**
     * 隐藏/显示帖子
     */
    @PostMapping("/forum/invisible")
    public Result<Void> invisibleForum(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody AdminStatusDTO adminStatusDTO) {
        adminService.invisibleForum(JwtUtils.getRole(authHeader), adminStatusDTO);
        return Result.success(null);
    }

    /**
     * 修改帖子类型
     */
    @GetMapping("/forum/change-topic-type")
    public Result<?> changeTopicType(@RequestHeader("Authorization") String authHeader,
                                     @RequestParam Integer tid,
                                     @RequestParam Integer type) {
        adminService.changeTopicType(JwtUtils.getRole(authHeader), tid, type);
        return Result.success("操作成功");
    }

    /**
     * 新增帖子分类
     */
    @PostMapping("/forum/create-type")
    public Result<Void> createType(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody TopicTypeDTO topicTypeDTO) {
        adminService.createType(JwtUtils.getRole(authHeader), topicTypeDTO);
        return Result.success(null);
    }

    /**
     * 修改帖子分类
     */
    @PostMapping("/forum/update-type")
    public Result<Void> updateType(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody TopicTypeDTO topicTypeDTO) {
        adminService.updateType(JwtUtils.getRole(authHeader), topicTypeDTO);
        return Result.success(null);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/user/list")
    public Result<Page<User>> getUserList(@RequestHeader("Authorization") String authHeader,
                                          @RequestParam Integer page,
                                          @RequestParam Integer size,
                                          @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getUserList(JwtUtils.getRole(authHeader), page, size, keyword));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/user/detail")
    public Result<AdminUserVO> getUserDetail(@RequestHeader("Authorization") String authHeader,
                                             @RequestParam Integer id) {
        return Result.success(adminService.getUserDetail(JwtUtils.getRole(authHeader), id));
    }

    /**
     * 保存用户信息
     */
    @PostMapping("/user/save")
    public Result<Void> saveUser(@RequestHeader("Authorization") String authHeader,
                                 @RequestBody AdminUserSaveDTO dto) {
        adminService.saveUser(JwtUtils.getRole(authHeader), dto);
        return Result.success(null);
    }

    /**
     * 管理员修改用户密码
     */
    @PostMapping("/user/change-password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody AdminChangePasswordDTO dto) {
        adminService.changePassword(JwtUtils.getRole(authHeader), dto);
        return Result.success(null);
    }

    @GetMapping("/email/list")
    public Result<Page<EmailRecord>> getEmailList(@RequestHeader("Authorization") String authHeader,
                                                  @RequestParam Integer page,
                                                  @RequestParam Integer size) {
        return Result.success(adminService.getEmailList(JwtUtils.getRole(authHeader), page, size));
    }

}
