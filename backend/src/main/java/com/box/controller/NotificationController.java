package com.box.controller;

import com.box.common.Result;
import com.box.entity.Notification;
import com.box.service.NotificationService;
import com.box.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知模块接口
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 获取通知列表 */
    @GetMapping("list")
    public Result<List<Notification>> list(@RequestHeader("Authorization") String authHeader) {
        return Result.success(notificationService.list(JwtUtils.getUid(authHeader)));
    }

    /** 删除单条通知 */
    @GetMapping("/delete")
    public Result<Void> delete(@RequestHeader("Authorization") String authHeader,
                               @RequestParam Integer id) {
        notificationService.delete(JwtUtils.getUid(authHeader), id);
        return Result.success(null);
    }

    /** 删除全部通知 */
    @GetMapping("/delete-all")
    public Result<Void> deleteAll(@RequestHeader("Authorization") String authHeader) {
        notificationService.deleteAll(JwtUtils.getUid(authHeader));
        return Result.success(null);
    }
}
