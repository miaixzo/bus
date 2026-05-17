/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
import org.miaixz.bus.logger.Logger;

/**
 * RocketMQ consumer implementation class. This class provides an adapter for consuming messages from Apache RocketMQ,
 * integrating with the internal {@link Consumer} interface. It handles the subscription to topics and the processing of
 * RocketMQ {@link MessageExt} objects.
 *
 * @author Kimi Liu
 * @since Java 21+
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
        final long startedAt = System.nanoTime();
        Logger.info(true, "Extra", "RocketMQ topic subscription started: topic={}", topic);
        try {
            this.consumer.subscribe(topic, "*");
            Logger.info(
                    false,
                    "Extra",
                    "RocketMQ topic subscription registered: topic={}, elapsedMs={}",
                    topic,
                    (System.nanoTime() - startedAt) / 1_000_000L);
        } catch (final MQClientException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "RocketMQ topic subscription failed: topic={}, exception={}, elapsedMs={}",
                    topic,
                    e.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
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
        Logger.info(true, "Extra", "RocketMQ message listener registration started");
        this.consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            final long startedAt = System.nanoTime();
            try {
                for (final MessageExt msg : msgs) {
                    messageHandler.handle(new RawMessage(msg.getTopic(), msg.getBody()));
                }
                Logger.debug(
                        false,
                        "Extra",
                        "RocketMQ batch handled: batchSize={}, elapsedMs={}",
                        msgs == null ? 0 : msgs.size(),
                        (System.nanoTime() - startedAt) / 1_000_000L);
            } catch (RuntimeException e) {
                Logger.warn(
                        false,
                        "Extra",
                        e,
                        "RocketMQ batch handling failed: batchSize={}, exception={}, elapsedMs={}",
                        msgs == null ? 0 : msgs.size(),
                        e.getClass().getSimpleName(),
                        (System.nanoTime() - startedAt) / 1_000_000L);
                throw e;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        Logger.info(false, "Extra", "RocketMQ message listener registered");
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
        final long startedAt = System.nanoTime();
        Logger.debug(true, "Extra", "RocketMQ consumer close requested");
        if (null != this.consumer) {
            this.consumer.shutdown();
        }
        Logger.debug(
                false,
                "Extra",
                "RocketMQ consumer closed: elapsedMs={}",
                (System.nanoTime() - startedAt) / 1_000_000L);
    }

}
