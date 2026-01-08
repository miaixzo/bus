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
 * RocketMQ message queue engine implementation class. This class serves as a concrete {@link MQProvider} for Apache
 * RocketMQ, handling the initialization of RocketMQ producers and consumers. It abstracts the underlying RocketMQ
 * client details and provides a simplified interface for message operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RocketMQProvider implements MQProvider {

    /**
     * The message queue configuration object, holding connection details like broker URL.
     */
    private MQConfig config;
    /**
     * The name of the producer group for RocketMQ. Defaults to {@link MixAll#DEFAULT_PRODUCER_GROUP}.
     */
    private String producerGroup;
    /**
     * The name of the consumer group for RocketMQ. Defaults to {@link MixAll#DEFAULT_CONSUMER_GROUP}.
     */
    private String consumerGroup;

    /**
     * Default constructor for {@code RocketMQProvider}. This constructor is primarily used when the provider is loaded
     * via Java's Service Provider Interface (SPI). It includes an assertion to ensure that the RocketMQ client library
     * (specifically {@link org.apache.rocketmq.common.message.Message} class) is present on the classpath, preventing
     * runtime errors if the dependency is missing. It also sets default values for producer and consumer groups.
     */
    public RocketMQProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(org.apache.rocketmq.common.message.Message.class);
        this.producerGroup = MixAll.DEFAULT_PRODUCER_GROUP;
        this.consumerGroup = MixAll.DEFAULT_CONSUMER_GROUP;
    }

    /**
     * Sets the producer group name for this RocketMQ provider. This group name is used when creating
     * {@link DefaultMQProducer} instances.
     *
     * @param producerGroup The name of the producer group.
     * @return This {@code RocketMQProvider} instance, allowing for method chaining.
     */
    public RocketMQProvider setProducerGroup(final String producerGroup) {
        this.producerGroup = producerGroup;
        return this;
    }

    /**
     * Sets the consumer group name for this RocketMQ provider. This group name is used when creating
     * {@link DefaultMQPushConsumer} instances.
     *
     * @param consumerGroup The name of the consumer group.
     * @return This {@code RocketMQProvider} instance, allowing for method chaining.
     */
    public RocketMQProvider setConsumerGroup(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Initializes the RocketMQProvider with the provided {@link MQConfig}. This method stores the configuration for
     * later use when creating producers and consumers.
     *
     * @param config The {@link MQConfig} object, containing necessary connection information like the broker URL.
     * @return This {@code RocketMQProvider} instance, allowing for method chaining.
     */
    @Override
    public RocketMQProvider init(final MQConfig config) {
        this.config = config;
        return this;
    }

    /**
     * Retrieves a {@link Producer} instance configured for RocketMQ. This method creates and starts a
     * {@link DefaultMQProducer} using the configured producer group and broker address from the {@link MQConfig}.
     *
     * @return A {@link RocketMQProducer} instance for sending messages to RocketMQ.
     * @throws MQueueException if starting the producer fails due to an underlying RocketMQ client exception.
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
     * Retrieves a {@link Consumer} instance configured for RocketMQ. This method creates a
     * {@link DefaultMQPushConsumer} using the configured consumer group and broker address from the {@link MQConfig}.
     *
     * @return A {@link RocketMQConsumer} instance for receiving messages from RocketMQ.
     */
    @Override
    public Consumer getConsumer() {
        final DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
        defaultMQPushConsumer.setNamesrvAddr(config.getBrokerUrl());
        return new RocketMQConsumer(defaultMQPushConsumer);
    }

}
