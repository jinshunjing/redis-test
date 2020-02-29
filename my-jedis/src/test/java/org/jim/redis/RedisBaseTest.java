package org.jim.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisBaseTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void testBase() throws Exception {
        String key = "test:jim:key";
        String value = "Hello";
        //Boolean succeed = redisTemplate.opsForValue().setIfAbsent(key, value, 5, TimeUnit.MINUTES);
        //System.out.println(succeed);
        redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);

        Thread.sleep(2_000L);

        value = "World";
        String cached = redisTemplate.opsForValue().getAndSet(key, value);
        System.out.println(cached);
    }

}
