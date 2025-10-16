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
package org.miaixz.bus.extra.mq.provider.kafka;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;

/**
 * Kafka producer implementation class. This class acts as an adapter for sending messages to Apache Kafka, integrating
 * with the internal {@link Producer} interface. It handles the conversion of internal {@link Message} objects into
 * Kafka {@link ProducerRecord}s.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KafkaProducer implements Producer {

    /**
     * The native Apache Kafka producer instance, responsible for sending records to Kafka topics. It sends messages
     * with String keys and byte array values.
     */
    private final org.apache.kafka.clients.producer.Producer<String, byte[]> producer;

    /**
     * Constructs a {@code KafkaProducer} with the specified Kafka configuration properties. This constructor
     * initializes the underlying {@link org.apache.kafka.clients.producer.KafkaProducer}.
     *
     * @param properties The {@link Properties} object containing Kafka producer configuration, e.g., bootstrap servers,
     *                   acks, retries.
     */
    public KafkaProducer(final Properties properties) {
        this(new org.apache.kafka.clients.producer.KafkaProducer<>(properties));
    }

    /**
     * Constructs a {@code KafkaProducer} with an already initialized native Kafka producer instance. This allows for
     * more flexible instantiation where the Kafka producer is managed externally.
     *
     * @param producer The pre-configured native Kafka producer instance.
     */
    public KafkaProducer(final org.apache.kafka.clients.producer.Producer<String, byte[]> producer) {
        this.producer = producer;
    }

    /**
     * Sends a {@link Message} to the specified Kafka topic. The message's topic and content are used to create a
     * {@link ProducerRecord} which is then sent by the underlying Kafka producer.
     *
     * @param message The {@link Message} object to send, containing the topic and content.
     */
    @Override
    public void send(final Message message) {
        this.producer.send(new ProducerRecord<>(message.topic(), message.content()));
    }

    /**
     * Closes the underlying Kafka producer and releases all associated resources. This method ensures that all buffered
     * records are sent and the producer is properly shut down.
     *
     * @throws IOException if an I/O error occurs during closing (though Kafka producer typically handles this
     *                     internally).
     */
    @Override
    public void close() throws IOException {
        IoKit.nullSafeClose(this.producer);
    }

}
