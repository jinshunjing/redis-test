package org.jim.redis;

import org.jim.redis.pubsub.RedisMessagePublisher;
import org.jim.redis.pubsub.RedisTxidListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisPubSubTest {
    @Autowired
    private RedisMessagePublisher messagePublisher;


    @Before
    public void setUp() {
        System.out.println();
        System.out.println();
    }

    @After
    public void tearDown() {
        System.out.println();
        System.out.println();
    }

    @Test
    public void testPubSub() throws Exception {
        String msg = "Hello";
        messagePublisher.publish(RedisTxidListener.CHANNEL_TXID, msg);

        // 等待订阅者接收消息
        Thread.sleep(1000);
    }
}
