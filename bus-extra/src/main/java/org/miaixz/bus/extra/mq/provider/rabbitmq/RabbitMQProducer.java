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
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;
import com.rabbitmq.client.Channel;

/**
 * RabbitMQ消息生产者实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabbitMQProducer implements Producer {

    private final Channel channel;
    private String exchange = Normal.EMPTY;

    /**
     * 构造方法
     *
     * @param channel RabbitMQ通信通道
     */
    public RabbitMQProducer(final Channel channel) {
        this.channel = channel;
    }

    /**
     * 设置交换器，默认为{@link Normal#EMPTY}
     *
     * @param exchange 交换器名称
     * @return 当前RabbitMQProducer实例，支持链式调用
     */
    public RabbitMQProducer setExchange(final String exchange) {
        this.exchange = exchange;
        return this;
    }

    /**
     * 声明队列
     *
     * @param queue      队列名称
     * @param durable    是否持久化队列，true表示服务器重启后队列仍然存在
     * @param exclusive  是否排他队列，true表示仅当前连接可以使用，连接关闭后队列自动删除
     * @param autoDelete 是否自动删除队列，true表示当没有消费者连接时自动删除队列
     * @param arguments  队列的其他参数配置
     * @return 当前RabbitMQProducer实例，支持链式调用
     */
    public RabbitMQProducer queueDeclare(
            final String queue,
            final boolean durable,
            final boolean exclusive,
            final boolean autoDelete,
            final Map<String, Object> arguments) {
        try {
            this.channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
        } catch (final IOException e) {
            throw new MQueueException(e);
        }
        return this;
    }

    /**
     * 发送消息到指定队列
     *
     * @param message 要发送的消息对象，包含主题和内容
     * @throws MQueueException 消息发送失败时抛出异常
     */
    @Override
    public void send(final Message message) {
        try {
            this.channel.basicPublish(exchange, message.topic(), null, message.content());
        } catch (final IOException e) {
            throw new MQueueException(e);
        }
    }

    /**
     * 关闭生产者，释放资源
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.channel);
    }

}
