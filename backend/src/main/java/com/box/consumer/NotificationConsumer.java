package com.box.consumer;

import com.box.config.RabbitMQConfig;
import com.box.dto.NotificationMessage;
import com.box.entity.Notification;
import com.box.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationMapper notificationMapper;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationMessage message) {
        Notification notification = new Notification();
        notification.setUid(message.getUid());
        notification.setTitle(message.getTitle());
        notification.setContent(message.getContent());
        notification.setType(message.getType());
        notification.setUrl(message.getUrl());
        notification.setTime(LocalDateTime.now());

        notificationMapper.insert(notification);
    }
}
