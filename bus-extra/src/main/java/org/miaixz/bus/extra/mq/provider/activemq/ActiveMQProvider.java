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
package org.miaixz.bus.extra.mq.provider.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.provider.jms.JmsProvider;

import jakarta.jms.ConnectionFactory;

/**
 * ActiveMQ message queue engine implementation class. This class extends {@link JmsProvider} to offer specific
 * functionalities for connecting to and interacting with an ActiveMQ broker. It provides the necessary
 * {@link ConnectionFactory} for ActiveMQ.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ActiveMQProvider extends JmsProvider {

    /**
     * Default constructor for {@code ActiveMQProvider}. This constructor is primarily used when the provider is loaded
     * via Java's Service Provider Interface (SPI). It includes an assertion to ensure that the ActiveMQ client library
     * (specifically {@link ActiveMQConnectionFactory}) is present on the classpath, preventing runtime errors if the
     * dependency is missing.
     */
    public ActiveMQProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(org.apache.activemq.ActiveMQConnectionFactory.class);
    }

    /**
     * Creates an ActiveMQ-specific {@link ConnectionFactory} based on the provided {@link MQConfig}. This method uses
     * the broker URL from the {@link MQConfig} to instantiate an {@link ActiveMQConnectionFactory}.
     *
     * @param config The {@link MQConfig} object, containing necessary connection information, including the broker URL.
     * @return An initialized {@link ActiveMQConnectionFactory} instance.
     */
    @Override
    protected ConnectionFactory createConnectionFactory(final MQConfig config) {
        return new ActiveMQConnectionFactory(config.getBrokerUrl());
    }

}
