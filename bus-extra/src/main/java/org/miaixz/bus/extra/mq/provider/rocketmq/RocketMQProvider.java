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

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.MixAll;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.extra.mq.Producer;
import org.miaixz.bus.extra.mq.MQProvider;

/**
 * RocketMQ引擎
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RocketMQProvider implements MQProvider {

    private MQConfig config;
    private String producerGroup;
    private String consumerGroup;

    /**
     * 默认构造方法 初始化 RocketMQProvider 实例，检查必要库是否引入，并设置默认的生产者和消费者组
     */
    public RocketMQProvider() {
        // SPI方式加载时检查库是否引入
        Assert.notNull(org.apache.rocketmq.common.message.Message.class);
        this.producerGroup = MixAll.DEFAULT_PRODUCER_GROUP;
        this.consumerGroup = MixAll.DEFAULT_CONSUMER_GROUP;
    }

    /**
     * 设置生产者组
     *
     * @param producerGroup 生产者组名称
     * @return 当前 RocketMQProvider 实例
     */
    public RocketMQProvider setProducerGroup(final String producerGroup) {
        this.producerGroup = producerGroup;
        return this;
    }

    /**
     * 设置消费者组
     *
     * @param consumerGroup 消费者组名称
     * @return 当前 RocketMQProvider 实例
     */
    public RocketMQProvider setConsumerGroup(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * 初始化 RocketMQProvider
     *
     * @param config 消息队列配置对象
     * @return 当前 RocketMQProvider 实例
     */
    @Override
    public RocketMQProvider init(final MQConfig config) {
        this.config = config;
        return this;
    }

    /**
     * 获取消息生产者 创建并启动一个 DefaultMQProducer 实例，配置其 Broker 地址
     *
     * @return 消息生产者实例
     * @throws MQueueException 如果启动生产者失败
     */
    @Override
    public Producer getProducer() {
        final DefaultMQProducer defaultMQProducer = new DefaultMQProducer(producerGroup);
        defaultMQProducer.setNamesrvAddr(config.getBrokerUrl());
        try {
            defaultMQProducer.start();
        } catch (final MQClientException e) {
            throw new MQueueException(e);
        }
        return new RocketMQProducer(defaultMQProducer);
    }

    /**
     * 获取消息消费者 创建一个 DefaultMQPushConsumer 实例，配置其 Broker 地址
     *
     * @return 消息消费者实例
     */
    @Override
    public Consumer getConsumer() {
        final DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
        defaultMQPushConsumer.setNamesrvAddr(config.getBrokerUrl());
        return new RocketMQConsumer(defaultMQPushConsumer);
    }

}