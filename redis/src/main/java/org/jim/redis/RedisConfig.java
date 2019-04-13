package org.jim.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
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
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
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

    @Bean
    public RedisTemplate<String, Object> redisCacheTemplate() {
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

        return redisTemplate;
    }


//    public static final String REDIS_CACHE_MANAGER_PREFIX = "C1_";

//    @Bean
//    public CacheManager cacheManager() {
//        List<String> cacheNames = new ArrayList<>();
//        cacheNames.add("user");
//        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
//        RedisCacheManager cacheManager = new RedisCacheManager(redisCacheTemplate(), configuration, cacheNames);
//        //cacheManager.setDefaultExpiration(23 * 3600);
//        return cacheManager;
//    }

//    @Bean
//    public KeyGenerator keyGenerator() {
//        return (o, method, params) -> {
//            StringBuilder sb = new StringBuilder();
//            sb.append(getPrefix(o));
//            sb.append("_");
//            sb.append(method.getName());
//            for (Object param : params) {
//                sb.append("_");
//                sb.append(param.toString());
//            }
//            return sb.toString();
//        };
//    }
//
//    private static String getPrefix(Object obj) {
//        String name = obj.getClass().getSimpleName();
//        int index = name.indexOf("$$");
//        if(index > 0) {
//            name = name.substring(0, index);
//        }
//        return REDIS_CACHE_MANAGER_PREFIX + name;
//    }
//
//
//    private static final String DELETE_SCRIPT_IN_LUA =
//            "local keys = redis.call('keys', '%s')\n" +
//            " local delete_count=0\n" +
//            "  for i,k in ipairs(keys) do\n" +
//            "    redis.call('del', k)\n" +
//            "    delete_count = delete_count + 1\n" +
//            "  end\n" +
//            "  return delete_count";
//
//    public int clearCache(Object obj) {
//        return clearCache(getPrefix(obj) + "_");
//    }
//
//    public void clearAll() {
//        clearCache(REDIS_CACHE_MANAGER_PREFIX);
//    }
//
//    public int clearCache(String prefix) {
//        String pattern =  prefix + "*";
//        final String script = String.format(DELETE_SCRIPT_IN_LUA, pattern);
//        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//        redisScript.setScriptText(script);
//        redisScript.setResultType(Long.class);
//
//        Long count = redisCacheTemplate().execute(redisScript, null);
//        return count.intValue();
//    }
}
