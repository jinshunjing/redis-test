package org.jim.redis.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis Publisher
 *
 * @author JSJ
 */
@Slf4j
public class RedisMessagePublisher {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void publish(String topic, String message) {
        redisTemplate.convertAndSend(topic, message);
        log.info("Published: {}, {}", topic, message);
    }

}
