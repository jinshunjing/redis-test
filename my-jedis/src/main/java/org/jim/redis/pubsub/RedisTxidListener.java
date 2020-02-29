package org.jim.redis.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * 订阅者
 *
 * @author JSJ
 */
@Slf4j
public class RedisTxidListener implements MessageListener {

    public static final String CHANNEL_TXID = "unit.test.ch.txid";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String msg = message.toString();
        log.info("onMessage: {}", msg);
    }

}
