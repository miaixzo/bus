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
import org.miaixz.bus.logger.Logger;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ message queue engine implementation class. This class provides an adapter for interacting with a RabbitMQ
 * broker, serving as a concrete {@link MQProvider} for RabbitMQ message queue services. It handles the establishment of
 * connections and provides access to RabbitMQ producers and consumers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RabbitMQProvider implements MQProvider, Closeable {

    /**
     * The active RabbitMQ {@link Connection} object, representing the connection to the RabbitMQ broker.
     */
    private Connection connection;

    /**
     * Default constructor for {@code RabbitMQProvider}. This constructor is primarily used when the provider is loaded
     * via Java's Service Provider Interface (SPI). It includes an assertion to ensure that the RabbitMQ client library
     * (specifically {@link Connection} class) is present on the classpath, preventing runtime errors if the dependency
     * is missing.
     */
    public RabbitMQProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(com.rabbitmq.client.Connection.class);
    }

    /**
     * Constructs a {@code RabbitMQProvider} with the specified {@link MQConfig}. This constructor initializes the
     * RabbitMQ connection based on the provided configuration.
     *
     * @param config The {@link MQConfig} object, containing necessary connection information like the broker URL and
     *               additional properties.
     */
    public RabbitMQProvider(final MQConfig config) {
        init(config);
    }

    /**
     * Constructs a {@code RabbitMQProvider} with an already initialized RabbitMQ {@link ConnectionFactory}. This allows
     * for more flexible instantiation where the connection factory is managed externally.
     *
     * @param factory The pre-configured RabbitMQ {@link ConnectionFactory} object.
     */
    public RabbitMQProvider(final ConnectionFactory factory) {
        init(factory);
    }

    /**
     * Initializes the RabbitMQ provider using the provided {@link MQConfig}. This method creates a
     * {@link ConnectionFactory} from the {@link MQConfig} and then establishes a connection to the RabbitMQ broker.
     *
     * @param config The {@link MQConfig} object, containing necessary connection information.
     * @return This {@code RabbitMQProvider} instance, allowing for method chaining.
     */
    @Override
    public RabbitMQProvider init(final MQConfig config) {
        return init(createFactory(config));
    }

    /**
     * Initializes the RabbitMQ provider using the provided {@link ConnectionFactory}. This method establishes a new
     * connection to the RabbitMQ broker using the given factory.
     *
     * @param factory The RabbitMQ {@link ConnectionFactory} object.
     * @return This {@code RabbitMQProvider} instance, allowing for method chaining.
     * @throws MQueueException if an error occurs during connection establishment (e.g., {@link IOException},
     *                         {@link TimeoutException}).
     */
    public RabbitMQProvider init(final ConnectionFactory factory) {
        final long startedAt = System.nanoTime();
        Logger.info(true, "Extra", "RabbitMQ provider initialization started: factoryPresent={}", factory != null);
        try {
            this.connection = factory.newConnection();
            Logger.info(
                    false,
                    "Extra",
                    "RabbitMQ provider initialized: elapsedMs={}",
                    (System.nanoTime() - startedAt) / 1_000_000L);
        } catch (final IOException | TimeoutException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "RabbitMQ provider initialization failed: exception={}, elapsedMs={}",
                    e.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            throw new MQueueException(e);
        }
        return this;
    }

    /**
     * Retrieves a {@link Producer} instance configured for RabbitMQ. A new {@link Channel} is created for each producer
     * to ensure thread safety and proper resource management.
     *
     * @return A {@link RabbitMQProducer} instance for sending messages to RabbitMQ.
     */
    @Override
    public Producer getProducer() {
        final long startedAt = System.nanoTime();
        Logger.debug(true, "Extra", "RabbitMQ producer creation started");
        Producer producer = new RabbitMQProducer(createChannel());
        Logger.debug(
                false,
                "Extra",
                "RabbitMQ producer created: elapsedMs={}",
                (System.nanoTime() - startedAt) / 1_000_000L);
        return producer;
    }

    /**
     * Retrieves a {@link Consumer} instance configured for RabbitMQ. A new {@link Channel} is created for each consumer
     * to ensure thread safety and proper resource management.
     *
     * @return A {@link RabbitMQConsumer} instance for receiving messages from RabbitMQ.
     */
    @Override
    public Consumer getConsumer() {
        final long startedAt = System.nanoTime();
        Logger.debug(true, "Extra", "RabbitMQ consumer creation started");
        Consumer consumer = new RabbitMQConsumer(createChannel());
        Logger.debug(
                false,
                "Extra",
                "RabbitMQ consumer created: elapsedMs={}",
                (System.nanoTime() - startedAt) / 1_000_000L);
        return consumer;
    }

    /**
     * Closes the RabbitMQ connection, releasing all associated resources. This method ensures that the connection is
     * properly shut down.
     *
     * @throws IOException if an I/O error occurs during the closing process.
     */
    @Override
    public void close() throws IOException {
        final long startedAt = System.nanoTime();
        Logger.info(true, "Extra", "RabbitMQ provider close requested");
        try {
            IoKit.nullSafeClose(this.connection);
            Logger.info(
                    false,
                    "Extra",
                    "RabbitMQ provider closed: elapsedMs={}",
                    (System.nanoTime() - startedAt) / 1_000_000L);
        } catch (IOException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "RabbitMQ provider close failed: exception={}, elapsedMs={}",
                    e.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            throw e;
        }
    }

    /**
     * Creates a new RabbitMQ communication {@link Channel} from the established connection. Channels are lightweight
     * and are typically used for most API operations.
     *
     * @return A new RabbitMQ {@link Channel} instance.
     * @throws MQueueException if an I/O error occurs during channel creation.
     */
    private Channel createChannel() {
        try {
            return this.connection.createChannel();
        } catch (final IOException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "RabbitMQ channel creation failed: exception={}",
                    e.getClass().getSimpleName());
            throw new MQueueException(e);
        }
    }

    /**
     * Creates a RabbitMQ {@link ConnectionFactory} based on the provided generic {@link MQConfig}. It extracts the
     * broker URL from the {@link MQConfig} and sets it on the factory.
     *
     * @param config The {@link MQConfig} object, containing necessary connection information.
     * @return The configured RabbitMQ {@link ConnectionFactory}.
     * @throws MQueueException if an error occurs during factory creation or URI setting.
     */
    private static ConnectionFactory createFactory(final MQConfig config) {
        final ConnectionFactory factory = new ConnectionFactory();
        try {
            Logger.info(
                    true,
                    "Extra",
                    "RabbitMQ connection factory configuration started: brokerPresent={}",
                    config != null && config.getBrokerUrl() != null);
            factory.setUri(config.getBrokerUrl());
            Logger.info(
                    false,
                    "Extra",
                    "RabbitMQ connection factory configured: brokerPresent={}",
                    config != null && config.getBrokerUrl() != null);
        } catch (final Exception e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "RabbitMQ connection factory configuration failed: exception={}",
                    e.getClass().getSimpleName());
            throw new MQueueException(e);
        }
        return factory;
    }

}
