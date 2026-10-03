package com.box.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.box.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /** 获取通知列表 */
    List<Notification> list(Integer uid);

    /** 删除单条通知 */
    void delete(Integer uid, Integer id);

    /** 删除全部通知 */
    void deleteAll(Integer uid);
}
