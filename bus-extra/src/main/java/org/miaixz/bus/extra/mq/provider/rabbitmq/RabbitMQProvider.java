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
 * RabbitMQ message queue engine implementation class. This class provides an adapter for interacting with a RabbitMQ
 * broker, serving as a concrete {@link MQProvider} for RabbitMQ message queue services. It handles the establishment of
 * connections and provides access to RabbitMQ producers and consumers.
 *
 * @author Kimi Liu
 * @since Java 17+
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
        try {
            this.connection = factory.newConnection();
        } catch (final IOException | TimeoutException e) {
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
        return new RabbitMQProducer(createChannel());
    }

    /**
     * Retrieves a {@link Consumer} instance configured for RabbitMQ. A new {@link Channel} is created for each consumer
     * to ensure thread safety and proper resource management.
     *
     * @return A {@link RabbitMQConsumer} instance for receiving messages from RabbitMQ.
     */
    @Override
    public Consumer getConsumer() {
        return new RabbitMQConsumer(createChannel());
    }

    /**
     * Closes the RabbitMQ connection, releasing all associated resources. This method ensures that the connection is
     * properly shut down.
     *
     * @throws IOException if an I/O error occurs during the closing process.
     */
    @Override
    public void close() throws IOException {
        IoKit.nullSafeClose(this.connection);
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
            factory.setUri(config.getBrokerUrl());
        } catch (final Exception e) {
            throw new MQueueException(e);
        }
        return factory;
    }

}
