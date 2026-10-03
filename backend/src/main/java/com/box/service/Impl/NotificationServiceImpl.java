package com.box.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.box.entity.Notification;
import com.box.exception.BusinessException;
import com.box.mapper.NotificationMapper;
import com.box.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务实现
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * 获取通知列表
     */
    @Override
    public List<Notification> list(Integer uid) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUid, uid);
        wrapper.orderByDesc(Notification::getTime);
        return notificationMapper.selectList(wrapper);
    }

    /**
     * 删除单条通知
     */
    @Override
    public void delete(Integer uid, Integer id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUid().equals(uid)) {
            throw new BusinessException("无权删除");
        }
        notificationMapper.deleteById(id);
    }

    /**
     * 删除全部通知
     */
    @Override
    public void deleteAll(Integer uid) {
        notificationMapper.delete(new LambdaQueryWrapper<Notification>().eq(Notification::getUid, uid));
    }
}
