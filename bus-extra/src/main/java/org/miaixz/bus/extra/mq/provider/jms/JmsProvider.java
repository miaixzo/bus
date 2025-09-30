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
package org.miaixz.bus.extra.mq.provider.jms;

import java.io.Closeable;
import java.io.IOException;

import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQProvider;
import org.miaixz.bus.extra.mq.Producer;

import jakarta.jms.*;

/**
 * JMS(Java Message Service)引擎抽象基类 提供JMS消息队列服务的基本实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class JmsProvider implements MQProvider, Closeable {

    /**
     * JMS连接对象
     */
    private Connection connection;

    /**
     * JMS会话对象
     */
    private Session session;

    /**
     * 是否使用Topic模式，false表示使用Queue模式
     */
    private boolean isTopic;

    /**
     * 生产者组名称，默认为"bus.queue"
     */
    private String producerGroup = "bus.queue";

    /**
     * 消费者组名称，默认为"bus.queue"
     */
    private String consumerGroup = "bus.queue";

    /**
     * 使用MQConfig初始化JMS连接和会话
     *
     * @param config JMS配置对象，包含连接所需信息
     * @return 当前JmsProvider实例，支持链式调用
     */
    @Override
    public MQProvider init(final MQConfig config) {
        try {
            this.connection = createConnectionFactory(config).createConnection();
            this.session = this.connection.createSession();
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
        return this;
    }

    /**
     * 创建JMS连接工厂 子类需要实现此方法以提供具体的连接工厂实现
     *
     * @param config JMS配置对象，包含连接所需信息
     * @return JMS连接工厂
     */
    protected abstract ConnectionFactory createConnectionFactory(final MQConfig config);

    /**
     * 设置是否使用Topic模式
     *
     * @param isTopic true表示使用Topic模式，false表示使用Queue模式
     * @return 当前JmsProvider实例，支持链式调用
     */
    public JmsProvider setTopic(final boolean isTopic) {
        this.isTopic = isTopic;
        return this;
    }

    /**
     * 设置生产者组名称
     *
     * @param producerGroup 生产者组名称
     * @return 当前JmsProvider实例，支持链式调用
     */
    public JmsProvider setProducerGroup(final String producerGroup) {
        this.producerGroup = producerGroup;
        return this;
    }

    /**
     * 设置消费者组名称
     *
     * @param consumerGroup 消费者组名称
     * @return 当前JmsProvider实例，支持链式调用
     */
    public JmsProvider setConsumerGroup(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * 获取JMS生产者实例
     *
     * @return JMS生产者实例
     */
    @Override
    public Producer getProducer() {
        final MessageProducer messageProducer;
        try {
            messageProducer = this.session.createProducer(createDestination(producerGroup));
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
        return new JmsProducer(this.session, messageProducer);
    }

    /**
     * 获取JMS消费者实例
     *
     * @return JMS消费者实例
     */
    @Override
    public Consumer getConsumer() {
        final MessageConsumer messageConsumer;
        try {
            messageConsumer = this.session.createConsumer(createDestination(consumerGroup));
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
        return new JmsConsumer(this.consumerGroup, messageConsumer);
    }

    /**
     * 关闭JMS连接和会话，释放资源
     *
     * @throws IOException 关闭过程中发生IO异常时抛出
     */
    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.session);
        IoKit.closeQuietly(this.connection);
    }

    /**
     * 创建目标对象（Queue或Topic）
     *
     * @param group 组名称，用于创建Queue或Topic的名称
     * @return 目标对象（Queue或Topic）
     */
    private Destination createDestination(final String group) {
        try {
            return isTopic ? this.session.createTopic(group) : this.session.createQueue(group);
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
    }

}
