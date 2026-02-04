/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
