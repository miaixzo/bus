/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.mq.provider.rocketmq;

import java.io.IOException;

import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.MessageHandler;
import org.miaixz.bus.extra.mq.RawMessage;

/**
 * RocketMQ consumer implementation class. This class provides an adapter for consuming messages from Apache RocketMQ,
 * integrating with the internal {@link Consumer} interface. It handles the subscription to topics and the processing of
 * RocketMQ {@link MessageExt} objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RocketMQConsumer implements Consumer {

    /**
     * The native Apache RocketMQ push consumer object, responsible for receiving messages.
     */
    private final MQPushConsumer consumer;

    /**
     * Constructs a {@code RocketMQConsumer} with the specified native RocketMQ push consumer.
     *
     * @param consumer The native RocketMQ {@link MQPushConsumer} object.
     */
    public RocketMQConsumer(final MQPushConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Sets the topic for consumption and subscribes the consumer to it. By default, it subscribes to all tags ('*')
     * within the specified topic.
     *
     * @param topic The name of the topic to subscribe to.
     * @return This consumer object, supporting chained calls.
     * @throws MQueueException if subscribing to the topic fails due to an underlying RocketMQ client exception.
     */
    public RocketMQConsumer setTopic(final String topic) {
        try {
            this.consumer.subscribe(topic, "*");
        } catch (final MQClientException e) {
            throw new MQueueException(e);
        }
        return this;
    }

    /**
     * Subscribes to messages from RocketMQ and registers a {@link MessageHandler} to process incoming messages. This
     * method sets up a {@link MessageListenerConcurrently} on the underlying {@link MQPushConsumer} to convert RocketMQ
     * messages into the internal {@link Message} format and dispatch them to the handler.
     *
     * @param messageHandler The {@link MessageHandler} to be used for processing received messages.
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        this.consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (final MessageExt msg : msgs) {
                messageHandler.handle(new RawMessage(msg.getTopic(), msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
    }

    /**
     * Closes the underlying RocketMQ push consumer and releases all associated resources. This method ensures that the
     * consumer is properly shut down.
     *
     * @throws IOException if an I/O error occurs during closing (though RocketMQ shutdown typically handles this
     *                     internally).
     */
    @Override
    public void close() throws IOException {
        if (null != this.consumer) {
            this.consumer.shutdown();
        }
    }

}
