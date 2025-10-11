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

import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQProvider;
import org.miaixz.bus.extra.mq.Producer;

/**
 * Kafka message queue engine implementation class. This class provides an adapter for interacting with Apache Kafka,
 * serving as a concrete {@link MQProvider} for Kafka message queue services. It handles the initialization of Kafka
 * client configurations and provides access to Kafka producers and consumers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KafkaProvider implements MQProvider {

    /**
     * Kafka client configuration properties. These properties are used to configure both Kafka producers and consumers,
     * including bootstrap servers, serializers, etc.
     */
    private Properties properties;

    /**
     * Default constructor. This constructor is primarily used when loading the provider via SPI to check for the
     * presence of the Kafka client library on the classpath. It asserts that {@link CommonClientConfigs} class is
     * available.
     */
    public KafkaProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(org.apache.kafka.clients.CommonClientConfigs.class);
    }

    /**
     * Constructs a {@code KafkaProvider} with the specified {@link MQConfig}. This constructor initializes the Kafka
     * client properties based on the provided configuration.
     *
     * @param config The {@link MQConfig} object, containing necessary connection information like the broker URL and
     *               additional properties.
     */
    public KafkaProvider(final MQConfig config) {
        init(config);
    }

    /**
     * Constructs a {@code KafkaProvider} with the specified raw Kafka {@link Properties}. This allows for direct
     * configuration using Kafka's native property format.
     *
     * @param properties The Kafka configuration {@link Properties}.
     */
    public KafkaProvider(final Properties properties) {
        init(properties);
    }

    /**
     * Initializes the Kafka provider using the provided {@link MQConfig}. This method converts the generic
     * {@link MQConfig} into Kafka-specific {@link Properties} and then calls {@link #init(Properties)} to set up the
     * provider.
     *
     * @param config The {@link MQConfig} object, containing necessary connection information.
     * @return This {@code KafkaProvider} instance, allowing for method chaining.
     */
    @Override
    public KafkaProvider init(final MQConfig config) {
        return init(buidProperties(config));
    }

    /**
     * Initializes the Kafka provider using the provided raw Kafka {@link Properties}. This method sets the internal
     * properties that will be used by producers and consumers.
     *
     * @param properties The Kafka configuration {@link Properties}.
     * @return This {@code KafkaProvider} instance, allowing for method chaining.
     */
    public KafkaProvider init(final Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Adds a single configuration item to the existing Kafka properties. This method can be used to set or override
     * specific Kafka client properties.
     *
     * @param key   The key of the configuration item.
     * @param value The value of the configuration item.
     * @return This {@code KafkaProvider} instance, allowing for method chaining.
     */
    public KafkaProvider addProperty(final String key, final String value) {
        this.properties.put(key, value);
        return this;
    }

    /**
     * Retrieves a {@link Producer} instance configured for Kafka. The returned producer uses the properties set during
     * the provider's initialization.
     *
     * @return A {@link KafkaProducer} instance for sending messages to Kafka.
     */
    @Override
    public Producer getProducer() {
        return new KafkaProducer(this.properties);
    }

    /**
     * Retrieves a {@link Consumer} instance configured for Kafka. The returned consumer uses the properties set during
     * the provider's initialization.
     *
     * @return A {@link KafkaConsumer} instance for receiving messages from Kafka.
     */
    @Override
    public Consumer getConsumer() {
        return new KafkaConsumer(this.properties);
    }

    /**
     * Builds Kafka configuration properties based on the provided generic {@link MQConfig}. It extracts the broker URL
     * and merges any additional properties from {@link MQConfig}.
     *
     * @param config The {@link MQConfig} object, containing necessary connection information.
     * @return The constructed Kafka configuration {@link Properties}.
     */
    private static Properties buidProperties(final MQConfig config) {
        final Properties properties = new Properties();
        // Set Kafka server address using CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
        properties.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, config.getBrokerUrl());
        // Add other configuration properties from MQConfig
        properties.putAll(config.getProperties());
        return properties;
    }

}
