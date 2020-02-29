package org.jim.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.jim.redis.pubsub.RedisMessagePublisher;
import org.jim.redis.pubsub.RedisTxidListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Redis发布订阅
 *
 * @author JSJ
 */
@Slf4j
@Configuration
public class RedisPubSubConfig {

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    /**
     * 发布者
     *
     * @return
     */
    @Bean
    public RedisMessagePublisher messagePublisher() {
        return new RedisMessagePublisher();
    }

    /**
     * 订阅者
     *
     * @return
     */
    @Bean
    public RedisMessageListenerContainer messageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory);

        // TODO 添加所有的订阅者
        container.addMessageListener(
                new MessageListenerAdapter(txidListener()),
                new ChannelTopic(RedisTxidListener.CHANNEL_TXID));

        return container;
    }

    /**
     * 订阅消息的监听器
     *
     * @return
     */
    @Bean
    public RedisTxidListener txidListener() {
        return new RedisTxidListener();
    }

}
