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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.extra.mq.Producer;
import org.miaixz.bus.extra.mq.MQProvider;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ引擎实现类，用于提供RabbitMQ的消息队列服务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabbitMQProvider implements MQProvider, Closeable {

    private Connection connection;

    /**
     * 默认构造方法，用于SPI方式加载时检查库是否引入
     */
    public RabbitMQProvider() {
        // SPI方式加载时检查库是否引入
        Assert.notNull(com.rabbitmq.client.Connection.class);
    }

    /**
     * 构造方法，使用MQConfig初始化RabbitMQ连接
     *
     * @param config RabbitMQ配置对象，包含连接所需信息
     */
    public RabbitMQProvider(final MQConfig config) {
        init(config);
    }

    /**
     * 构造方法，使用ConnectionFactory初始化RabbitMQ连接
     *
     * @param factory RabbitMQ连接工厂对象
     */
    @SuppressWarnings("resource")
    public RabbitMQProvider(final ConnectionFactory factory) {
        init(factory);
    }

    /**
     * 使用MQConfig初始化RabbitMQ连接
     *
     * @param config RabbitMQ配置对象，包含连接所需信息
     * @return 当前RabbitMQProvider实例，支持链式调用
     */
    @Override
    public RabbitMQProvider init(final MQConfig config) {
        return init(createFactory(config));
    }

    /**
     * 使用ConnectionFactory初始化RabbitMQ连接
     *
     * @param factory RabbitMQ连接工厂对象
     * @return 当前RabbitMQProvider实例，支持链式调用
     */
    public RabbitMQProvider init(final ConnectionFactory factory) {
        try {
            this.connection = factory.newConnection();
        } catch (final IOException | TimeoutException e) {
            throw new MQueueException(e);
        }
        return this;
    }

    /**
     * 获取RabbitMQ生产者实例
     *
     * @return RabbitMQ生产者实例
     */
    @Override
    public Producer getProducer() {
        return new RabbitMQProducer(createChannel());
    }

    /**
     * 获取RabbitMQ消费者实例
     *
     * @return RabbitMQ消费者实例
     */
    @Override
    public Consumer getConsumer() {
        return new RabbitMQConsumer(createChannel());
    }

    /**
     * 关闭RabbitMQ连接，释放资源
     *
     * @throws IOException 关闭连接时发生IO异常
     */
    @Override
    public void close() throws IOException {
        IoKit.nullSafeClose(this.connection);
    }

    /**
     * 创建RabbitMQ通信通道
     *
     * @return RabbitMQ通信通道
     */
    private Channel createChannel() {
        try {
            return this.connection.createChannel();
        } catch (final IOException e) {
            throw new MQueueException(e);
        }
    }

    /**
     * 根据配置创建RabbitMQ连接工厂
     *
     * @param config RabbitMQ配置对象，包含连接所需信息
     * @return 配置好的RabbitMQ连接工厂
     */
    private static ConnectionFactory createFactory(final MQConfig config) {
        final ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(config.getBrokerUrl());
        } catch (final Exception e) {
            throw new MQueueException(e);
        }
        return factory;
    }

}
