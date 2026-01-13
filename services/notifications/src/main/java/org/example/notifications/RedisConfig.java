package org.example.notifications;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {
    public static final String NOTIFICATIONS_CHANNEL = "notifications";

    @Bean
    ChannelTopic notificationsTopic() {
        return new ChannelTopic(NOTIFICATIONS_CHANNEL);
    }

    @Bean
    MessageListenerAdapter redisListenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    RedisMessageListenerContainer redisContainer(
        RedisConnectionFactory connectionFactory,
        MessageListenerAdapter redisListenerAdapter,
        ChannelTopic notificationsTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(redisListenerAdapter, notificationsTopic);
        return container;
    }
}
