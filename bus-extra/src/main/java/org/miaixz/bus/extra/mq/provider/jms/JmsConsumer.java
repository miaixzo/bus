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
 * JMS (Java Message Service) message consumer implementation. This class acts as an adapter for consuming messages from
 * a JMS provider, converting JMS messages into the internal {@link Message} format and dispatching them to a
 * {@link MessageHandler}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JmsConsumer implements Consumer {

    /**
     * The name of the consumer group to which this consumer belongs. This is used as the topic for the internal
     * {@link Message} representation.
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
     * messages. This method sets up a listener on the underlying {@link MessageConsumer} to convert JMS messages into
     * the internal {@link Message} format.
     *
     * @param messageHandler The {@link MessageHandler} to be used for processing received messages.
     * @throws MQueueException if a JMS error occurs during the subscription process or message handling.
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        try {
            // Set the message listener to handle incoming JMS messages
            this.consumer.setMessageListener(message -> {
                // Create an anonymous Message implementation to wrap the JMS message
                messageHandler.handle(new Message() {

                    /**
                     * Retrieves the message topic, which is represented by the consumer group name in this JMS consumer
                     * implementation.
                     *
                     * @return The consumer group name as the topic of the message.
                     */
                    @Override
                    public String topic() {
                        return consumerGroup;
                    }

                    /**
                     * Retrieves the content of the JMS message as a byte array. It supports {@link TextMessage} and
                     * {@link BytesMessage} types.
                     *
                     * @return The message content as a {@code byte[]}.
                     * @throws MQueueException if an error occurs while extracting content from the JMS message or if an
                     *                         unsupported message type is encountered.
                     */
                    @Override
                    public byte[] content() {
                        try {
                            // Handle text messages by converting their text content to bytes
                            if (message instanceof TextMessage) {
                                return ByteKit.toBytes(((TextMessage) message).getText());
                            }
                            // Handle byte messages by reading their body into a byte array
                            else if (message instanceof BytesMessage) {
                                final BytesMessage bytesMessage = (BytesMessage) message;
                                // Create a byte array with the same length as the message body
                                final byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                                // Read the byte message content into the array
                                bytesMessage.readBytes(bytes);
                                return bytes;
                            }
                            // Throw an exception for unsupported JMS message types
                            else {
                                throw new IllegalArgumentException(
                                        "Unsupported message type: " + message.getClass().getName());
                            }
                        } catch (final JMSException e) {
                            throw new MQueueException(e);
                        }
                    }
                });
            });
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

}
