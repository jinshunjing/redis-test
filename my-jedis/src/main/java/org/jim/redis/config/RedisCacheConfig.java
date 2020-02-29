package org.jim.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;

/**
 * Redis缓存配置文件
 * 缓存Redis的查询结果，需要配合Spring Cache使用
 */
@Slf4j
@Configuration
public class RedisCacheConfig extends CachingConfigurerSupport {
    public static final String REDIS_CACHE_MANAGER_PREFIX = "C1_";

    @Autowired
    private JedisConnectionFactory redisConnectionFactory;
    @Autowired
    private RedisTemplate<String, Object> redisCacheTemplate;

    @Override
    @Bean
    public CacheManager cacheManager() {
        // 设置缓存有效期
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(23));

        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }

    @Override
    @Bean
    public KeyGenerator keyGenerator() {
        return (o, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(getPrefix(o));
            sb.append("_");
            sb.append(method.getName());
            for (Object param : params) {
                sb.append("_");
                sb.append(param.toString());
            }
            return sb.toString();
        };
    }

    private static String getPrefix(Object obj) {
        String name = obj.getClass().getSimpleName();
        int index = name.indexOf("$$");
        if(index > 0) {
            name = name.substring(0, index);
        }
        return REDIS_CACHE_MANAGER_PREFIX + name;
    }

    private static final String DELETE_SCRIPT_IN_LUA = "local keys = redis.call('keys', '%s')\n" +
            " local delete_count=0\n" +
            "  for i,k in ipairs(keys) do\n" +
            "    redis.call('del', k)\n" +
            "    delete_count = delete_count + 1\n" +
            "  end\n" +
            "  return delete_count";

    public void clearAll() {
        clearCache(REDIS_CACHE_MANAGER_PREFIX);
    }

    public int clearCache(Object obj) {
        return clearCache(getPrefix(obj) + "_");
    }

    public int clearCache(String prefix) {
        String pattern =  prefix + "*";
        final String script = String.format(DELETE_SCRIPT_IN_LUA, pattern);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        Long count = redisCacheTemplate.execute(redisScript, null);
        return count.intValue();
    }

}
