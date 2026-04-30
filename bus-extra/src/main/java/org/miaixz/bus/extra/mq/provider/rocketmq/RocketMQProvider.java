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
import org.miaixz.bus.logger.Logger;

/**
 * RocketMQ message queue engine implementation class. This class serves as a concrete {@link MQProvider} for Apache
 * RocketMQ, handling the initialization of RocketMQ producers and consumers. It abstracts the underlying RocketMQ
 * client details and provides a simplified interface for message operations.
 *
 * @author Kimi Liu
 * @since Java 21+
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
        Logger.info(
                true,
                "Extra",
                "RocketMQ provider initialization started: brokerPresent={}, producerGroup={}, consumerGroup={}",
                config != null && config.getBrokerUrl() != null,
                producerGroup,
                consumerGroup);
        this.config = config;
        Logger.info(
                false,
                "Extra",
                "RocketMQ provider initialized: brokerPresent={}, producerGroup={}, consumerGroup={}",
                config != null && config.getBrokerUrl() != null,
                producerGroup,
                consumerGroup);
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
        final long startedAt = System.nanoTime();
        Logger.debug(
                true,
                "Extra",
                "RocketMQ producer creation started: producerGroup={}, brokerPresent={}",
                producerGroup,
                config != null && config.getBrokerUrl() != null);
        final DefaultMQProducer defaultMQProducer = new DefaultMQProducer(producerGroup);
        defaultMQProducer.setNamesrvAddr(config.getBrokerUrl());
        try {
            defaultMQProducer.start();
        } catch (final MQClientException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "RocketMQ producer creation failed: producerGroup={}, exception={}, elapsedMs={}",
                    producerGroup,
                    e.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            throw new MQueueException(e);
        }
        Logger.debug(
                false,
                "Extra",
                "RocketMQ producer created: producerGroup={}, elapsedMs={}",
                producerGroup,
                (System.nanoTime() - startedAt) / 1_000_000L);
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
        final long startedAt = System.nanoTime();
        Logger.debug(
                true,
                "Extra",
                "RocketMQ consumer creation started: consumerGroup={}, brokerPresent={}",
                consumerGroup,
                config != null && config.getBrokerUrl() != null);
        final DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
        defaultMQPushConsumer.setNamesrvAddr(config.getBrokerUrl());
        Consumer consumer = new RocketMQConsumer(defaultMQPushConsumer);
        Logger.debug(
                false,
                "Extra",
                "RocketMQ consumer created: consumerGroup={}, elapsedMs={}",
                consumerGroup,
                (System.nanoTime() - startedAt) / 1_000_000L);
        return consumer;
    }

}
