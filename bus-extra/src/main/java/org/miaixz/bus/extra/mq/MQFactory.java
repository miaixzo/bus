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

import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.logger.Logger;

/**
 * Factory class for creating and managing Message Queue (MQ) engine objects. This factory provides a mechanism to
 * automatically detect and instantiate appropriate {@link MQProvider} implementations based on available JARs or
 * explicit configuration.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MQFactory {

    /**
     * Constructs a new MQFactory instance.
     */
    public MQFactory() {
        // No initialization required.
    }

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
        final long startedAt = System.nanoTime();
        Logger.info(
                true,
                "Extra",
                "MQ provider discovery started: configPresent={}, brokerPresent={}, customEnginePresent={}, propertyCount={}",
                config != null,
                config != null && config.getBrokerUrl() != null,
                config != null && config.getCustomEngine() != null,
                config == null || config.getProperties() == null ? 0 : config.getProperties().size());
        final Class<? extends MQProvider> customEngineClass = config.getCustomEngine();
        final MQProvider engine;
        if (null != customEngineClass) {
            // Custom template engine
            Logger.debug(
                    true,
                    "Extra",
                    "MQ custom provider creation started: provider={}",
                    customEngineClass.getName());
            engine = ReflectKit.newInstance(customEngineClass);
        } else {
            // SPI engine lookup
            Logger.debug(true, "Extra", "MQ SPI provider lookup started");
            engine = NormalSpiLoader.loadFirstAvailable(MQProvider.class);
        }
        if (null != engine) {
            Logger.info(
                    false,
                    "Extra",
                    "MQ provider discovered: provider={}, elapsedMs={}",
                    engine.getClass().getName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            final MQProvider provider = engine.init(config);
            Logger.info(
                    false,
                    "Extra",
                    "MQ provider initialized by factory: provider={}, elapsedMs={}",
                    provider == null ? "null" : provider.getClass().getName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            return provider;
        }

        Logger.error(
                false,
                "Extra",
                "MQ provider discovery failed: customEnginePresent={}, elapsedMs={}",
                customEngineClass != null,
                (System.nanoTime() - startedAt) / 1_000_000L);
        throw new MQueueException("No MQ implement found! Please add one of MQ jar to your project !");
    }

}
