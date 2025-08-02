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

/**
 * RocketMQ 消费者实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RocketMQConsumer implements Consumer {
    private final MQPushConsumer consumer;

    /**
     * 构造方法
     *
     * @param consumer RocketMQ 原生推送消费者对象
     */
    public RocketMQConsumer(final MQPushConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * 设置消费的Topic
     *
     * @param topic 要订阅的主题名称
     * @return 当前消费者对象，支持链式调用
     * @throws MQueueException 订阅主题失败时抛出异常
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
     * 订阅消息并注册消息处理器
     *
     * @param messageHandler 消息处理器，用于处理接收到的消息
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        this.consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (final MessageExt msg : msgs) {
                messageHandler.handle(new RocketMQMessage(msg));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
    }

    /**
     * 关闭消费者，释放资源
     *
     * @throws IOException 关闭过程中发生IO异常时抛出
     */
    @Override
    public void close() throws IOException {
        if (null != this.consumer) {
            this.consumer.shutdown();
        }
    }

    /**
     * RocketMQ消息包装类，实现了Message接口
     */
    private static class RocketMQMessage implements Message {
        private final MessageExt messageExt;

        /**
         * 构造方法
         *
         * @param messageExt RocketMQ原生消息对象
         */
        private RocketMQMessage(final MessageExt messageExt) {
            this.messageExt = messageExt;
        }

        /**
         * 获取消息主题
         *
         * @return 消息主题名称
         */
        @Override
        public String topic() {
            return messageExt.getTopic();
        }

        /**
         * 获取消息内容
         *
         * @return 消息内容的字节数组
         */
        @Override
        public byte[] content() {
            return messageExt.getBody();
        }
    }

}