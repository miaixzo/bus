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
package org.miaixz.bus.extra.mq.provider.rabbitmq;

import java.io.IOException;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.MessageHandler;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * RabbitMQ消费者实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabbitMQConsumer implements Consumer {

    /**
     * RabbitMQ通信通道
     */
    private final Channel channel;

    /**
     * 队列名称（主题）
     */
    private String topic;

    /**
     * 构造方法
     *
     * @param channel RabbitMQ通信通道
     */
    public RabbitMQConsumer(final Channel channel) {
        this.channel = channel;
    }

    /**
     * 设置队列（主题）
     *
     * @param topic 队列名称
     * @return 当前RabbitMQConsumer实例，支持链式调用
     */
    public RabbitMQConsumer setTopic(final String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * 订阅消息并注册消息处理器
     *
     * @param messageHandler 消息处理器，用于处理接收到的消息
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        // 默认声明非持久化、非排他、非自动删除的队列
        queueDeclare(false, false, false, null);

        // 创建消息投递回调
        final DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 创建消息对象并交给处理器处理
            messageHandler.handle(new Message() {
                /**
                 * 获取消息主题（消费者标签）
                 *
                 * @return 消费者标签作为主题
                 */
                @Override
                public String topic() {
                    return consumerTag;
                }

                /**
                 * 获取消息内容
                 *
                 * @return 消息内容的字节数组
                 */
                @Override
                public byte[] content() {
                    return delivery.getBody();
                }
            });
        };

        try {
            // 开始消费消息，自动确认消息
            this.channel.basicConsume(this.topic, true, deliverCallback, consumerTag -> {
                // 取消消费时的回调处理（空实现）
            });
        } catch (final IOException e) {
            throw new MQueueException(e);
        }
    }

    /**
     * 关闭消费者，释放资源
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.channel);
    }

    /**
     * 声明队列
     *
     * @param durable    是否持久化队列，true表示服务器重启后队列仍然存在
     * @param exclusive  是否排他队列，true表示仅当前连接可以使用，连接关闭后队列自动删除
     * @param autoDelete 是否自动删除队列，true表示当没有消费者连接时自动删除队列
     * @param arguments  队列的其他参数配置
     */
    private void queueDeclare(final boolean durable, final boolean exclusive, final boolean autoDelete,
            final Map<String, Object> arguments) {
        try {
            this.channel.queueDeclare(this.topic, durable, exclusive, autoDelete, arguments);
        } catch (final IOException e) {
            throw new MQueueException(e);
        }
    }

}