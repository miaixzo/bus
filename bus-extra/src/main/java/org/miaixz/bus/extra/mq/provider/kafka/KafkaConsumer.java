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
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.MessageHandler;

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
            messageHandler.handle(new ConsumerRecordMessage(record));
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

    /**
     * An internal static nested class that wraps a Kafka {@link ConsumerRecord} to conform to the {@link Message}
     * interface. This allows Kafka records to be processed by generic message handlers.
     */
    private static class ConsumerRecordMessage implements Message {

        /**
         * The original Kafka {@link ConsumerRecord} that this object wraps.
         */
        private final ConsumerRecord<String, byte[]> record;

        /**
         * Constructs a {@code ConsumerRecordMessage} with the specified Kafka consumer record.
         *
         * @param record The Kafka {@link ConsumerRecord} to be wrapped.
         */
        private ConsumerRecordMessage(final ConsumerRecord<String, byte[]> record) {
            this.record = record;
        }

        /**
         * Retrieves the topic name from the wrapped Kafka {@link ConsumerRecord}.
         *
         * @return The name of the Kafka topic from which the message was consumed.
         */
        @Override
        public String topic() {
            return record.topic();
        }

        /**
         * Retrieves the message content (value) as a byte array from the wrapped Kafka {@link ConsumerRecord}.
         *
         * @return The message content as a {@code byte[]}.
         */
        @Override
        public byte[] content() {
            return record.value();
        }
    }

}
