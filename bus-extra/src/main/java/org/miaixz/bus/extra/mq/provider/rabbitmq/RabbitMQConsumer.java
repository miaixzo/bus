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
package org.miaixz.bus.extra.mq.provider.rabbitmq;

import java.io.IOException;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.MessageHandler;
import org.miaixz.bus.extra.mq.RawMessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * RabbitMQ consumer implementation class. This class provides an adapter for consuming messages from RabbitMQ,
 * integrating with the internal {@link Consumer} interface. It handles the subscription to queues and the processing of
 * delivered messages.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabbitMQConsumer implements Consumer {

    /**
     * The RabbitMQ communication channel, used for declaring queues, consuming messages, etc.
     */
    private final Channel channel;

    /**
     * The name of the queue (topic) from which messages will be consumed.
     */
    private String topic;

    /**
     * Constructs a {@code RabbitMQConsumer} with the specified RabbitMQ channel.
     *
     * @param channel The RabbitMQ {@link Channel} object to be used for consuming messages.
     */
    public RabbitMQConsumer(final Channel channel) {
        this.channel = channel;
    }

    /**
     * Sets the name of the queue (topic) from which this consumer will receive messages.
     *
     * @param topic The name of the queue.
     * @return This {@code RabbitMQConsumer} instance, allowing for method chaining.
     */
    public RabbitMQConsumer setTopic(final String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Subscribes to messages from the configured RabbitMQ queue and registers a {@link MessageHandler} to process
     * incoming messages. This method declares a queue (if it doesn't exist) and sets up a {@link DeliverCallback} to
     * handle message delivery.
     *
     * @param messageHandler The {@link MessageHandler} to be used for processing received messages.
     * @throws MQueueException if an I/O error occurs during queue declaration or message consumption.
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        // Declare a non-durable, non-exclusive, non-auto-delete queue by default
        queueDeclare(false, false, false, null);

        try {
            this.channel.basicConsume(
                    this.topic,
                    true,
                    (consumerTag, delivery) -> messageHandler.handle(new RawMessage(consumerTag, delivery.getBody())),
                    consumerTag -> {
                    });
        } catch (final IOException e) {
            throw new MQueueException(e);
        }
    }

    /**
     * Closes the underlying RabbitMQ {@link Channel} and releases any associated resources. This method ensures that
     * the channel is properly shut down.
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.channel);
    }

    /**
     * Declares a queue on the RabbitMQ broker with specified properties. This method is used internally to ensure the
     * queue exists before consuming or producing messages.
     *
     * @param durable    {@code true} if the queue should be durable (survive broker restarts); {@code false} otherwise.
     * @param exclusive  {@code true} if the queue should be exclusive (used by only one connection and deleted when
     *                   that connection closes); {@code false} otherwise.
     * @param autoDelete {@code true} if the queue should be auto-deleted when no longer in use (e.g., when the last
     *                   consumer unsubscribes); {@code false} otherwise.
     * @param arguments  A {@link Map} of other properties for the queue, such as message TTL, queue length limit, etc.
     * @throws MQueueException if an I/O error occurs during queue declaration.
     */
    private void queueDeclare(
            final boolean durable,
            final boolean exclusive,
            final boolean autoDelete,
            final Map<String, Object> arguments) {
        try {
            this.channel.queueDeclare(this.topic, durable, exclusive, autoDelete, arguments);
        } catch (final IOException e) {
            throw new MQueueException(e);
        }
    }

}
