package org.jim.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁
 *
 * @author JSJ
 */
@Slf4j
public class RedisLock {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 加锁
     * Jedis版本2.9.3
     *
     * @param key
     * @param value
     * @param expireSec
     * @return
     */
    public Boolean lock(String key, String value, Long expireSec) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expireSec, TimeUnit.SECONDS);
        if (result) {
            log.info("Locked: {}, {}, {}", key, value, expireSec);
        } else {
            log.warn("Failed to lock: {}, {}", key, value);
        }
        return result;
    }
    public Boolean lock(String key, String value) {
        return lock(key, value, 30L);
    }

    /**
     * Jedis 版本 2.9.0
     * 通过multi执行事务
     */
    public Boolean lock290(String key, String value, Long expireSec) {
        SessionCallback<Boolean> sessionCallback = new SessionCallback<Boolean>() {
            List<Object> exec = null;
            @Override
            @SuppressWarnings("unchecked")
            public Boolean execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                redisTemplate.opsForValue().setIfAbsent(key, value);
                redisTemplate.expire(key, expireSec, TimeUnit.SECONDS);
                exec = operations.exec();
                if(exec.size() > 0) {
                    return (Boolean) exec.get(0);
                }
                return false;
            }
        };
        return redisTemplate.execute(sessionCallback);
    }

    /**
     * 解锁
     * 执行Lua脚本
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean unlock(String key, String value) {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(UNLOCK_LUA);
        redisScript.setResultType(Boolean.class);
        Boolean result = redisTemplate.execute(redisScript, Arrays.asList(key), value);
        if (result) {
            log.info("Unlocked: {}, {}", key,  value);
        } else {
            log.warn("Failed to unlock: {}, {}", key, value);
        }
        return result;
    }

    private static final String UNLOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    /**
     * 检查是否已经上锁
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean checkLock(String key, String value) {
        String val = redisTemplate.opsForValue().get(key);
        return value.equals(val);
    }

    /**
     * 强制加锁，有效期30s
     * @param key
     * @param value
     */
    public void forceLock(String key, String value) {
        forceLock(key, value, 30);
    }
    public void forceLock(String key, String value, int expire) {
        redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        log.info("Force lock: {}, {}, {}", key, value, expire);
    }

}
