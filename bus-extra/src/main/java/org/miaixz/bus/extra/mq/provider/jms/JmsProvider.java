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
package org.miaixz.bus.extra.mq.provider.jms;

import java.io.Closeable;
import java.io.IOException;

import jakarta.jms.*;

import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQProvider;
import org.miaixz.bus.extra.mq.Producer;
import org.miaixz.bus.logger.Logger;

/**
 * Abstract base class for JMS (Java Message Service) engine. Provides a basic implementation for JMS message queue
 * services.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class JmsProvider implements MQProvider, Closeable {

    /**
     * The JMS connection object.
     */
    private Connection connection;

    /**
     * The JMS session object.
     */
    private Session session;

    /**
     * Indicates whether to use Topic mode (true) or Queue mode (false).
     */
    private boolean isTopic;

    /**
     * The name of the producer group, defaults to "bus.queue".
     */
    private String producerGroup = "bus.queue";

    /**
     * The name of the consumer group, defaults to "bus.queue".
     */
    private String consumerGroup = "bus.queue";

    /**
     * Initializes the JMS connection and session using the provided {@link MQConfig}.
     *
     * @param config The JMS configuration object, containing necessary connection information.
     * @return This {@code JmsProvider} instance, supporting chained calls.
     * @throws MQueueException if a JMS error occurs during initialization.
     */
    @Override
    public MQProvider init(final MQConfig config) {
        final long startedAt = System.nanoTime();
        Logger.info(
                true,
                "Extra",
                "JMS provider initialization started: provider={}, brokerPresent={}, mode={}",
                getClass().getSimpleName(),
                config != null && config.getBrokerUrl() != null,
                isTopic ? "topic" : "queue");
        try {
            this.connection = createConnectionFactory(config).createConnection();
            this.session = this.connection.createSession();
            Logger.info(
                    false,
                    "Extra",
                    "JMS provider initialized: provider={}, mode={}, elapsedMs={}",
                    getClass().getSimpleName(),
                    isTopic ? "topic" : "queue",
                    (System.nanoTime() - startedAt) / 1_000_000L);
        } catch (final JMSException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JMS provider initialization failed: provider={}, mode={}, exception={}, elapsedMs={}",
                    getClass().getSimpleName(),
                    isTopic ? "topic" : "queue",
                    e.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            throw new MQueueException(e);
        }
        return this;
    }

    /**
     * Creates a JMS connection factory. Subclasses must implement this method to provide a concrete connection factory
     * implementation.
     *
     * @param config The JMS configuration object, containing necessary connection information.
     * @return The JMS connection factory.
     */
    protected abstract ConnectionFactory createConnectionFactory(final MQConfig config);

    /**
     * Sets whether to use Topic mode.
     *
     * @param isTopic {@code true} to use Topic mode, {@code false} to use Queue mode.
     * @return This {@code JmsProvider} instance, supporting chained calls.
     */
    public JmsProvider setTopic(final boolean isTopic) {
        this.isTopic = isTopic;
        return this;
    }

    /**
     * Sets the name of the producer group.
     *
     * @param producerGroup The name of the producer group.
     * @return This {@code JmsProvider} instance, supporting chained calls.
     */
    public JmsProvider setProducerGroup(final String producerGroup) {
        this.producerGroup = producerGroup;
        return this;
    }

    /**
     * Sets the name of the consumer group.
     *
     * @param consumerGroup The name of the consumer group.
     * @return This {@code JmsProvider} instance, supporting chained calls.
     */
    public JmsProvider setConsumerGroup(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Retrieves a JMS producer instance.
     *
     * @return A JMS producer instance.
     * @throws MQueueException if a JMS error occurs while creating the producer.
     */
    @Override
    public Producer getProducer() {
        final MessageProducer messageProducer;
        final long startedAt = System.nanoTime();
        Logger.debug(
                true,
                "Extra",
                "JMS producer creation started: provider={}, group={}, mode={}",
                getClass().getSimpleName(),
                producerGroup,
                isTopic ? "topic" : "queue");
        try {
            messageProducer = this.session.createProducer(createDestination(producerGroup));
        } catch (final JMSException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JMS producer creation failed: provider={}, group={}, mode={}, exception={}, elapsedMs={}",
                    getClass().getSimpleName(),
                    producerGroup,
                    isTopic ? "topic" : "queue",
                    e.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            throw new MQueueException(e);
        }
        Logger.debug(
                false,
                "Extra",
                "JMS producer created: provider={}, group={}, mode={}, elapsedMs={}",
                getClass().getSimpleName(),
                producerGroup,
                isTopic ? "topic" : "queue",
                (System.nanoTime() - startedAt) / 1_000_000L);
        return new JmsProducer(this.session, messageProducer);
    }

    /**
     * Retrieves a JMS consumer instance.
     *
     * @return A JMS consumer instance.
     * @throws MQueueException if a JMS error occurs while creating the consumer.
     */
    @Override
    public Consumer getConsumer() {
        final MessageConsumer messageConsumer;
        final long startedAt = System.nanoTime();
        Logger.debug(
                true,
                "Extra",
                "JMS consumer creation started: provider={}, group={}, mode={}",
                getClass().getSimpleName(),
                consumerGroup,
                isTopic ? "topic" : "queue");
        try {
            messageConsumer = this.session.createConsumer(createDestination(consumerGroup));
        } catch (final JMSException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JMS consumer creation failed: provider={}, group={}, mode={}, exception={}, elapsedMs={}",
                    getClass().getSimpleName(),
                    consumerGroup,
                    isTopic ? "topic" : "queue",
                    e.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            throw new MQueueException(e);
        }
        Logger.debug(
                false,
                "Extra",
                "JMS consumer created: provider={}, group={}, mode={}, elapsedMs={}",
                getClass().getSimpleName(),
                consumerGroup,
                isTopic ? "topic" : "queue",
                (System.nanoTime() - startedAt) / 1_000_000L);
        return new JmsConsumer(this.consumerGroup, messageConsumer);
    }

    /**
     * Closes the JMS connection and session, releasing resources.
     *
     * @throws IOException if an I/O error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        final long startedAt = System.nanoTime();
        Logger.info(true, "Extra", "JMS provider close requested: provider={}", getClass().getSimpleName());
        IoKit.closeQuietly(this.session);
        IoKit.closeQuietly(this.connection);
        Logger.info(
                false,
                "Extra",
                "JMS provider closed: provider={}, elapsedMs={}",
                getClass().getSimpleName(),
                (System.nanoTime() - startedAt) / 1_000_000L);
    }

    /**
     * Creates a destination object (Queue or Topic).
     *
     * @param group The group name, used to create the name of the Queue or Topic.
     * @return The destination object (Queue or Topic).
     * @throws MQueueException if a JMS error occurs while creating the destination.
     */
    private Destination createDestination(final String group) {
        try {
            Logger.debug(
                    true,
                    "Extra",
                    "JMS destination creation started: provider={}, group={}, mode={}",
                    getClass().getSimpleName(),
                    group,
                    isTopic ? "topic" : "queue");
            final Destination destination = isTopic ? this.session.createTopic(group) : this.session.createQueue(group);
            Logger.debug(
                    false,
                    "Extra",
                    "JMS destination created: provider={}, group={}, mode={}, destinationType={}",
                    getClass().getSimpleName(),
                    group,
                    isTopic ? "topic" : "queue",
                    destination == null ? "null" : destination.getClass().getSimpleName());
            return destination;
        } catch (final JMSException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JMS destination creation failed: provider={}, group={}, mode={}, exception={}",
                    getClass().getSimpleName(),
                    group,
                    isTopic ? "topic" : "queue",
                    e.getClass().getSimpleName());
            throw new MQueueException(e);
        }
    }

}
