package org.example.notifications;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationListener {
    private final StringRedisTemplate redisTemplate;

    public KafkaNotificationListener(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "orders.events", groupId = "notifications")
    public void onOrderEvent(String payload) {
        redisTemplate.convertAndSend(RedisConfig.NOTIFICATIONS_CHANNEL, payload);
    }
}
