package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.box.common.Result;
import com.box.entity.Notification;
import com.box.exception.BusinessException;
import com.box.mapper.NotificationMapper;
import com.box.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;

    @GetMapping("list")
    public Result<List<Notification>> list(@RequestHeader("Authorization") String authHeader){
        Integer uid = JwtUtils.getUid(authHeader);

        List<Notification> notifications;
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUid, uid);
        wrapper.orderByDesc(Notification::getTime);
        notifications = notificationMapper.selectList(wrapper);

        return Result.success(notifications);
    }

    @GetMapping("/delete")
    public Result<Void> delete(@RequestHeader("Authorization") String authHeader,
                               @RequestParam Integer id){
        Integer uid = JwtUtils.getUid(authHeader);

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUid().equals(uid)) {
            throw new BusinessException("无权删除");
        }
        notificationMapper.deleteById(id);
        return Result.success(null);
    }

    @GetMapping("/delete-all")
    public Result<Void> deleteAll(@RequestHeader("Authorization") String authHeader){
        Integer uid = JwtUtils.getUid(authHeader);
        notificationMapper.delete(new LambdaQueryWrapper<Notification>().eq(Notification::getUid, uid));
        return Result.success(null);
    }
}
