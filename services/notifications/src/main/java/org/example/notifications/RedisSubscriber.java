package org.example.notifications;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisSubscriber implements MessageListener {
    private final NotificationService notificationService;

    public RedisSubscriber(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = message.toString();
        notificationService.broadcast(payload);
    }
}
