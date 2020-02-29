package org.jim.redis.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jim.redis.lock.RedisLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Redis配置文件
 */
@Slf4j
@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout:0}")
    private int timeout;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.pool.max-total:8}")
    private int poolMaxTotal;

    @Value("${spring.redis.pool.max-wait:-1}")
    private int poolMaxWait;

    @Value("${spring.redis.pool.max-idle:8}")
    private int poolMaxIdle;

    @Value("${spring.redis.pool.min-idle:0}")
    private int poolMinIdle;

    /**
     * Jedis链接
     *
     * @return
     */
    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        standaloneConfiguration.setDatabase(database);
        standaloneConfiguration.setPassword(RedisPassword.of(password));

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(poolMaxIdle);
        poolConfig.setMinIdle(poolMinIdle);
        poolConfig.setMaxWaitMillis(poolMaxWait);
        poolConfig.setMaxTotal(poolMaxTotal);

        JedisClientConfiguration clientConfiguration = JedisClientConfiguration.builder()
                .connectTimeout(Duration.ofMillis(timeout))
                .readTimeout(Duration.ofMillis(timeout))
                .usePooling()
                .poolConfig(poolConfig)
                .build();

        JedisConnectionFactory factory = new JedisConnectionFactory(standaloneConfiguration, clientConfiguration);
        return factory;
    }

    /**
     * Redis Template
     *
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisCacheTemplate() {
        log.info("=== To create RedisTemplate");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        RedisSerializer fastJsonSerializer = new RedisSerializer() {
            @Override public byte[] serialize(Object obj) throws SerializationException {
                return JSON.toJSONBytes(obj);
            }

            @Override public Object deserialize(byte[] bytes) throws SerializationException {
                return JSON.parse(new String(bytes));
            }
        };
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(fastJsonSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(fastJsonSerializer);

        log.info("=== Created RedisTemplate");
        return redisTemplate;
    }

    /**
     * 分布式锁
     *
     * @return
     */
    @Bean
    public RedisLock redisLock() {
        return new RedisLock();
    }

}
