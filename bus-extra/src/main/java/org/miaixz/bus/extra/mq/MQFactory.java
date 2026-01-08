/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.mq;

import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * Factory class for creating and managing Message Queue (MQ) engine objects. This factory provides a mechanism to
 * automatically detect and instantiate appropriate {@link MQProvider} implementations based on available JARs or
 * explicit configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MQFactory {

    /**
     * Creates an {@link MQProvider} instance based on the provided {@link MQConfig}. This method attempts to
     * automatically detect the correct MQ engine implementation from the classpath or uses a custom engine if specified
     * in the configuration. It is recommended to manage the lifecycle of the returned engine, potentially as a
     * singleton, as this method creates a new instance on each call.
     *
     * @param config The {@link MQConfig} containing connection details and optional custom engine class.
     * @return A new {@link MQProvider} instance initialized with the given configuration.
     * @throws MQueueException if no suitable MQ implementation is found or cannot be instantiated.
     */
    public static MQProvider createEngine(final MQConfig config) {
        return doCreateEngine(config);
    }

    /**
     * Internal method to perform the creation of an {@link MQProvider} instance. It first checks for a custom engine
     * specified in the {@link MQConfig}. If none is provided, it uses {@link NormalSpiLoader} to find the first
     * available {@link MQProvider} implementation via Java's Service Provider Interface (SPI) mechanism.
     *
     * @param config The {@link MQConfig} containing connection details and optional custom engine class.
     * @return An initialized {@link MQProvider} instance.
     * @throws MQueueException if no MQ implementation is found (either custom or via SPI).
     */
    private static MQProvider doCreateEngine(final MQConfig config) {
        final Class<? extends MQProvider> customEngineClass = config.getCustomEngine();
        final MQProvider engine;
        if (null != customEngineClass) {
            // Custom template engine
            engine = ReflectKit.newInstance(customEngineClass);
        } else {
            // SPI engine lookup
            engine = NormalSpiLoader.loadFirstAvailable(MQProvider.class);
        }
        if (null != engine) {
            return engine.init(config);
        }

        throw new MQueueException("No MQ implement found! Please add one of MQ jar to your project !");
    }

}
