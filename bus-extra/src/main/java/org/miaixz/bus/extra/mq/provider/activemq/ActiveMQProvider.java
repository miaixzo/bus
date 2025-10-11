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
