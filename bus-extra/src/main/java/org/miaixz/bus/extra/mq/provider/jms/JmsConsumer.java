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

import java.io.IOException;

import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.MessageHandler;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.TextMessage;

/**
 * JMS (Java Message Service) message consumer implementation.
 * <p>
 * This class acts as an adapter for consuming messages from a JMS provider, converting standard JMS messages into the
 * internal {@link Message} format and dispatching them to a registered {@link MessageHandler}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JmsConsumer implements Consumer {

    /**
     * The name of the consumer group to which this consumer belongs. This is used as the topic identifier for the
     * internal {@link Message} representation.
     */
    private String consumerGroup;

    /**
     * The underlying Jakarta Messaging {@link MessageConsumer} instance responsible for receiving messages.
     */
    private final MessageConsumer consumer;

    /**
     * Constructs a {@code JmsConsumer} with the specified consumer group name and the underlying Jakarta Messaging
     * {@link MessageConsumer}.
     *
     * @param consumerGroup The name of the consumer group. This will be used as the topic for received messages.
     * @param consumer      The actual {@link MessageConsumer} from the JMS provider.
     */
    public JmsConsumer(final String consumerGroup, final MessageConsumer consumer) {
        this.consumerGroup = consumerGroup;
        this.consumer = consumer;
    }

    /**
     * Sets the name of the consumer group for this {@code JmsConsumer}.
     *
     * @param consumerGroup The new name of the consumer group.
     * @return This {@code JmsConsumer} instance, allowing for method chaining.
     */
    public JmsConsumer setConsumerGroup(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Subscribes to messages from the JMS topic/queue and registers a {@link MessageHandler} to process incoming
     * messages.
     * <p>
     * This method sets up an asynchronous message listener on the underlying {@link MessageConsumer}. When a JMS
     * message acts as a trigger, it is wrapped in a {@link JmsMessage} and passed to the handler.
     * </p>
     *
     * @param messageHandler The {@link MessageHandler} to be used for processing received messages.
     * @throws MQueueException if a JMS error occurs during the subscription process.
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        try {
            this.consumer.setMessageListener(message -> messageHandler.handle(new JmsMessage(consumerGroup, message)));
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
    }

    /**
     * Closes the underlying JMS {@link MessageConsumer} and releases any associated resources.
     *
     * @throws IOException if an I/O error occurs during the closing process.
     */
    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.consumer);
    }

    /**
     * Encapsulation of a JMS Message. Adapts the Jakarta JMS Message interface to the internal {@link Message}
     * interface.
     *
     * @param topic      The message topic, typically representing the consumer group in this context.
     * @param jmsMessage The raw Jakarta JMS message instance.
     */
    private record JmsMessage(String topic, jakarta.jms.Message jmsMessage) implements Message {

        /**
         * Retrieves the content of the message as a byte array. Supports {@link TextMessage} and {@link BytesMessage}.
         *
         * @return The message content as a byte array.
         * @throws MQueueException          If a JMS error occurs while reading the message.
         * @throws IllegalArgumentException If the message type is not supported.
         */
        @Override
        public byte[] content() {
            try {
                if (jmsMessage instanceof TextMessage textMessage) {
                    return ByteKit.toBytes(textMessage.getText());
                } else if (jmsMessage instanceof BytesMessage bytesMessage) {
                    // Reset the stream to ensure we read from the beginning
                    bytesMessage.reset();
                    long length = bytesMessage.getBodyLength();
                    byte[] data = new byte[(int) length];
                    bytesMessage.readBytes(data);
                    return data;
                } else {
                    throw new IllegalArgumentException("Unsupported message type: " + jmsMessage.getClass().getName());
                }
            } catch (final JMSException e) {
                throw new MQueueException(e);
            }
        }
    }

}
