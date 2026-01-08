/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.apache.rocketmq.client.producer.MQProducer;
import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;

/**
 * RocketMQ producer implementation class. This class provides an adapter for sending messages to Apache RocketMQ,
 * integrating with the internal {@link Producer} interface. It handles the conversion of internal {@link Message}
 * objects into RocketMQ {@link org.apache.rocketmq.common.message.Message} objects for transmission.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RocketMQProducer implements Producer {

    /**
     * The native Apache RocketMQ producer object, responsible for sending messages to RocketMQ topics.
     */
    private final MQProducer producer;

    /**
     * Constructs a {@code RocketMQProducer} with the specified native RocketMQ producer object.
     *
     * @param producer The native RocketMQ {@link MQProducer} object.
     */
    public RocketMQProducer(final MQProducer producer) {
        this.producer = producer;
    }

    /**
     * Sends a {@link Message} to the specified RocketMQ topic. The message's topic and content are used to create a
     * RocketMQ {@link org.apache.rocketmq.common.message.Message} which is then sent by the underlying RocketMQ
     * producer.
     *
     * @param message The {@link Message} object to send, containing the topic and content.
     * @throws MQueueException if message sending fails due to an underlying RocketMQ client exception.
     */
    @Override
    public void send(final Message message) {
        final org.apache.rocketmq.common.message.Message rocketMessage = new org.apache.rocketmq.common.message.Message(
                message.topic(), message.content());
        try {
            this.producer.send(rocketMessage);
        } catch (final Exception e) {
            throw new MQueueException(e);
        }
    }

    /**
     * Closes the underlying RocketMQ producer and releases all associated resources. This method ensures that the
     * producer is properly shut down.
     *
     * @throws IOException if an I/O error occurs during closing (though RocketMQ shutdown typically handles this
     *                     internally).
     */
    @Override
    public void close() throws IOException {
        if (null != this.producer) {
            this.producer.shutdown();
        }
    }

}
