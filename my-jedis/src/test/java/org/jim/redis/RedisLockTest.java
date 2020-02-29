package org.jim.redis;

import org.jim.redis.lock.RedisLock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisLockTest {

    @Autowired
    private RedisLock lock;

    private String key;
    private String val;

    @Before
    public void setUp() {
        key = "test.lock";
        val = "1201";
        System.out.println();
        System.out.println();
    }

    @After
    public void tearDown() {
        System.out.println();
        System.out.println();
    }

    @Test
    public void testLock() throws Exception {
        if (lock.lock(key, val)) {
            Thread.sleep(2000L);
        } else {
            System.out.println("Failed to lock #1");
        }

        if (lock.lock(key, val)) {
            Thread.sleep(2000L);
        } else {
            System.out.println("Failed to lock #2");
        }

        if (lock.checkLock(key, val)) {
            Thread.sleep(2000L);
        } else {
            System.out.println("Failed to check lock #1");
        }

        if (lock.unlock(key, val)) {
            Thread.sleep(2000L);
        } else {
            System.out.println("Failed to unlock #1");
        }

        if (lock.checkLock(key, val)) {
            Thread.sleep(2000L);
        } else {
            System.out.println("Failed to check lock #2");
        }

        lock.forceLock(key, "1202");

        if (lock.checkLock(key, "1202")) {
            Thread.sleep(2000L);
        } else {
            System.out.println("Failed to check lock #3");
        }
    }
}
