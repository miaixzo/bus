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
package org.miaixz.bus.extra.mq.provider.kafka;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.MessageHandler;
import org.miaixz.bus.extra.mq.RawMessage;

/**
 * Kafka consumer implementation class. This class provides an adapter for consuming messages from Apache Kafka,
 * integrating with the internal {@link Consumer} interface. It handles the subscription to topics and the processing of
 * Kafka {@link ConsumerRecord}s.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KafkaConsumer implements Consumer {

    /**
     * The native Apache Kafka consumer instance, responsible for fetching records from Kafka topics. It consumes
     * messages with String keys and byte array values.
     */
    private final org.apache.kafka.clients.consumer.Consumer<String, byte[]> consumer;

    /**
     * Constructs a {@code KafkaConsumer} with the specified Kafka configuration properties. This constructor
     * initializes the underlying {@link org.apache.kafka.clients.consumer.KafkaConsumer}.
     *
     * @param properties The {@link Properties} object containing Kafka consumer configuration, e.g., bootstrap servers,
     *                   group ID.
     */
    public KafkaConsumer(final Properties properties) {
        this.consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(properties);
    }

    /**
     * Constructs a {@code KafkaConsumer} with an already initialized native Kafka consumer instance. This allows for
     * more flexible instantiation where the Kafka consumer is managed externally.
     *
     * @param consumer The pre-configured native Kafka consumer instance.
     */
    public KafkaConsumer(final org.apache.kafka.clients.consumer.Consumer<String, byte[]> consumer) {
        this.consumer = consumer;
    }

    /**
     * Subscribes this consumer to a list of specified Kafka topics.
     *
     * @param topics An array of topic names to subscribe to.
     * @return This {@code KafkaConsumer} instance, allowing for method chaining.
     */
    public KafkaConsumer setTopics(final String... topics) {
        this.consumer.subscribe(ListKit.of(topics));
        return this;
    }

    /**
     * Subscribes this consumer to topics that match the given regular expression pattern. This allows for dynamic
     * subscription to multiple topics based on a pattern.
     *
     * @param topicPattern The {@link Pattern} used to match topic names for subscription.
     * @return This {@code KafkaConsumer} instance, allowing for method chaining.
     */
    public KafkaConsumer setTopicPattern(final Pattern topicPattern) {
        this.consumer.subscribe(topicPattern);
        return this;
    }

    /**
     * Subscribes to messages from Kafka and processes them using the provided {@link MessageHandler}. This method polls
     * Kafka for records and converts each {@link ConsumerRecord} into an internal {@link Message} object before passing
     * it to the handler. The polling duration is fixed at 3000 milliseconds.
     *
     * @param messageHandler The {@link MessageHandler} to be used for processing received messages.
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        for (final ConsumerRecord<String, byte[]> record : this.consumer.poll(Duration.ofMillis(3000))) {
            messageHandler.handle(new RawMessage(record.topic(), record.value()));
        }
    }

    /**
     * Closes the underlying Kafka consumer and releases all associated resources. This method ensures that the consumer
     * is properly shut down.
     *
     * @throws IOException if an I/O error occurs during the closing process (though Kafka consumer typically handles
     *                     this internally).
     */
    @Override
    public void close() throws IOException {
        IoKit.nullSafeClose(this.consumer);
    }

}
