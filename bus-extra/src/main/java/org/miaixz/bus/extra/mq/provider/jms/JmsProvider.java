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
 * Abstract base class for JMS (Java Message Service) engine. Provides a basic implementation for JMS message queue
 * services.
 *
 * @author Kimi Liu
 * @since Java 17+
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
        try {
            this.connection = createConnectionFactory(config).createConnection();
            this.session = this.connection.createSession();
        } catch (final JMSException e) {
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
        try {
            messageProducer = this.session.createProducer(createDestination(producerGroup));
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
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
        try {
            messageConsumer = this.session.createConsumer(createDestination(consumerGroup));
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
        return new JmsConsumer(this.consumerGroup, messageConsumer);
    }

    /**
     * Closes the JMS connection and session, releasing resources.
     *
     * @throws IOException if an I/O error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.session);
        IoKit.closeQuietly(this.connection);
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
            return isTopic ? this.session.createTopic(group) : this.session.createQueue(group);
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
    }

}
