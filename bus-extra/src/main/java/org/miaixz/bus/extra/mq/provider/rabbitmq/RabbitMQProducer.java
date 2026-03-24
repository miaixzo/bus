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

import java.io.IOException;
import java.util.Map;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;
import com.rabbitmq.client.Channel;

/**
 * RabbitMQ message producer implementation class. This class provides an adapter for sending messages to RabbitMQ,
 * integrating with the internal {@link Producer} interface. It handles the publication of messages to exchanges and
 * queues within RabbitMQ.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RabbitMQProducer implements Producer {

    /**
     * The RabbitMQ communication channel, used for declaring queues, publishing messages, etc.
     */
    private final Channel channel;
    /**
     * The name of the exchange to which messages will be published. Defaults to {@link Normal#EMPTY}, which corresponds
     * to the default exchange in RabbitMQ (direct exchange to queue named by routing key).
     */
    private String exchange = Normal.EMPTY;

    /**
     * Constructs a {@code RabbitMQProducer} with the specified RabbitMQ channel.
     *
     * @param channel The RabbitMQ {@link Channel} object to be used for publishing messages.
     */
    public RabbitMQProducer(final Channel channel) {
        this.channel = channel;
    }

    /**
     * Sets the name of the exchange to which messages will be published. If not set, the default exchange (direct
     * exchange) will be used.
     *
     * @param exchange The name of the exchange.
     * @return This {@code RabbitMQProducer} instance, allowing for method chaining.
     */
    public RabbitMQProducer setExchange(final String exchange) {
        this.exchange = exchange;
        return this;
    }

    /**
     * Declares a queue on the RabbitMQ broker with specified properties. This method is used to ensure the queue exists
     * before messages are sent to it.
     *
     * @param queue      The name of the queue to declare.
     * @param durable    {@code true} if the queue should be durable (survive broker restarts); {@code false} otherwise.
     * @param exclusive  {@code true} if the queue should be exclusive (used by only one connection and deleted when
     *                   that connection closes); {@code false} otherwise.
     * @param autoDelete {@code true} if the queue should be auto-deleted when no longer in use (e.g., when the last
     *                   consumer unsubscribes); {@code false} otherwise.
     * @param arguments  A {@link Map} of other properties for the queue, such as message TTL, queue length limit, etc.
     * @return This {@code RabbitMQProducer} instance, allowing for method chaining.
     * @throws MQueueException if an I/O error occurs during queue declaration.
     */
    public RabbitMQProducer queueDeclare(
            final String queue,
            final boolean durable,
            final boolean exclusive,
            final boolean autoDelete,
            final Map<String, Object> arguments) {
        try {
            this.channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
        } catch (final IOException e) {
            throw new MQueueException(e);
        }
        return this;
    }

    /**
     * Sends a {@link Message} to the configured RabbitMQ exchange with the message's topic as the routing key. The
     * message content is published as a byte array.
     *
     * @param message The {@link Message} object to send, containing the topic (routing key) and content.
     * @throws MQueueException if an I/O error occurs during message publishing.
     */
    @Override
    public void send(final Message message) {
        try {
            this.channel.basicPublish(exchange, message.topic(), null, message.content());
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

}
