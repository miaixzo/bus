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
package org.miaixz.bus.extra.mq;

import java.io.Serial;
import java.io.Serializable;
import java.util.Properties;

/**
 * Configuration class for Message Queue (MQ) settings. This class holds parameters required to connect to and interact
 * with an MQ broker, such as the broker URL, additional properties, and an optional custom engine for specific MQ
 * providers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MQConfig implements Serializable {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852266759152L;

    /**
     * Creates a new {@code MQConfig} instance with the specified broker URL. This is a static factory method for
     * convenient object creation.
     *
     * @param brokerUrl The URL or address of the MQ broker.
     * @return A new {@link MQConfig} instance initialized with the given broker URL.
     */
    public static MQConfig of(final String brokerUrl) {
        return new MQConfig(brokerUrl);
    }

    /**
     * The URL or address of the MQ broker.
     */
    private String brokerUrl;
    /**
     * Additional properties for configuring the MQ connection or specific provider settings. These properties can be
     * used to pass vendor-specific configurations.
     */
    private Properties properties;
    /**
     * Specifies a custom {@link MQProvider} implementation to be used. This is useful when multiple MQ provider JARs
     * are present, allowing explicit selection of the desired engine.
     */
    private Class<? extends MQProvider> customEngine;

    /**
     * Constructs an {@code MQConfig} with the specified broker URL.
     *
     * @param brokerUrl The URL or address of the MQ broker.
     */
    public MQConfig(final String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    /**
     * Retrieves the broker URL configured for this MQ connection.
     *
     * @return The broker URL as a {@link String}.
     */
    public String getBrokerUrl() {
        return brokerUrl;
    }

    /**
     * Sets the broker URL for this MQ configuration.
     *
     * @param brokerUrl The new broker URL to set.
     * @return This {@code MQConfig} instance, allowing for method chaining.
     */
    public MQConfig setBrokerUrl(final String brokerUrl) {
        this.brokerUrl = brokerUrl;
        return this;
    }

    /**
     * Retrieves the additional properties configured for this MQ connection.
     *
     * @return The {@link Properties} object containing additional configuration settings.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the additional properties for this MQ configuration.
     *
     * @param properties The {@link Properties} object to set.
     * @return This {@code MQConfig} instance, allowing for method chaining.
     */
    public MQConfig setProperties(final Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Adds a single property to the existing set of additional properties. If no properties object exists, a new one
     * will be created.
     *
     * @param key   The key of the property to add.
     * @param value The value of the property to add.
     * @return The updated {@link Properties} object after adding the new property.
     */
    public Properties addProperty(final String key, final String value) {
        if (null == this.properties) {
            this.properties = new Properties();
        }
        this.properties.setProperty(key, value);
        return this.properties;
    }

    /**
     * Retrieves the custom {@link MQProvider} engine class configured. This allows specifying a particular MQ
     * implementation when multiple are available.
     *
     * @return The {@link Class} object representing the custom MQ provider engine.
     */
    public Class<? extends MQProvider> getCustomEngine() {
        return customEngine;
    }

    /**
     * Sets a custom {@link MQProvider} engine class to be used. This is useful for explicitly selecting an MQ
     * implementation when multiple JAR packages are introduced.
     *
     * @param customEngine The {@link Class} object of the custom MQ provider engine.
     * @return This {@code MQConfig} instance, allowing for method chaining.
     */
    public MQConfig setCustomEngine(final Class<? extends MQProvider> customEngine) {
        this.customEngine = customEngine;
        return this;
    }

}
