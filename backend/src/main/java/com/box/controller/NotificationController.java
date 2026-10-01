package com.box.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.box.common.Result;
import com.box.entity.Notification;
import com.box.mapper.NotificationMapper;
import com.box.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
