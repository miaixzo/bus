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
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;

/**
 * JMS (Java Message Service) message producer implementation. This class acts as an adapter for sending messages to a
 * JMS provider, converting the internal {@link Message} format into a JMS {@link BytesMessage} for transmission.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JmsProducer implements Producer {

    /**
     * The underlying Jakarta Messaging {@link Session} object used to create messages and producers.
     */
    private final Session session;

    /**
     * The underlying Jakarta Messaging {@link MessageProducer} object responsible for sending messages.
     */
    private final MessageProducer producer;

    /**
     * Constructs a {@code JmsProducer} with the specified JMS session and message producer.
     *
     * @param session  The JMS {@link Session} object to be used for message creation.
     * @param producer The JMS {@link MessageProducer} object responsible for sending messages.
     */
    public JmsProducer(final Session session, final MessageProducer producer) {
        this.session = session;
        this.producer = producer;
    }

    /**
     * Sends a {@link Message} to the configured JMS destination (queue or topic). The content of the {@link Message} is
     * converted into a JMS {@link BytesMessage}.
     *
     * @param message The {@link Message} object to send, containing the topic and content.
     * @throws MQueueException if a JMS error occurs during message creation or sending.
     */
    @Override
    public void send(final Message message) {
        final BytesMessage bytesMessage;
        try {
            // Create a new BytesMessage from the JMS session
            bytesMessage = this.session.createBytesMessage();
            // Write the content of the bus.extra.mq.Message into the BytesMessage
            bytesMessage.writeBytes(message.content());
            // Send the BytesMessage using the JMS MessageProducer
            this.producer.send(bytesMessage);
        } catch (final JMSException e) {
            // Wrap any JMSException in an MQueueException for consistent error handling
            throw new MQueueException(e);
        }
    }

    /**
     * Closes the underlying JMS {@link MessageProducer} and releases any associated resources.
     *
     * @throws IOException if an I/O error occurs during the closing process.
     */
    @Override
    public void close() throws IOException {
        // Safely close the JMS producer, suppressing any exceptions
        IoKit.closeQuietly(this.producer);
    }

}
