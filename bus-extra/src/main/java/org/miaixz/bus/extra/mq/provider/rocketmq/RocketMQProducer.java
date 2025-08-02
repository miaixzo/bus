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

import org.apache.rocketmq.client.producer.MQProducer;
import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;

/**
 * RocketMQ 生产者实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RocketMQProducer implements Producer {
    private final MQProducer producer;

    /**
     * 构造方法
     *
     * @param producer RocketMQ 原生生产者对象
     */
    public RocketMQProducer(final MQProducer producer) {
        this.producer = producer;
    }

    /**
     * 发送消息到指定主题
     *
     * @param message 要发送的消息对象，包含主题和内容
     * @throws MQueueException 消息发送失败时抛出异常
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
     * 关闭生产者，释放资源
     *
     * @throws IOException 关闭过程中发生IO异常时抛出
     */
    @Override
    public void close() throws IOException {
        if (null != this.producer) {
            this.producer.shutdown();
        }
    }

}