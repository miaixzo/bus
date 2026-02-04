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
package org.miaixz.bus.proxy;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * A simple factory for obtaining a proxy provider instance. This factory automatically detects the best available proxy
 * implementation (e.g., CGLIB, JDK proxy) on the classpath using Java's Service Provider Interface (SPI).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Factory {

    /**
     * Gets the singleton instance of the proxy provider. It uses SPI to find the best available provider and caches it
     * for subsequent calls.
     *
     * @return The singleton {@link Provider} instance.
     */
    public static Provider getEngine() {
        final Provider engine = Instances.get(Provider.class.getName(), Factory::createEngine);
        Logger.debug(
                "Use [{}] Engine As Default.",
                StringKit.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
        return engine;
    }

    /**
     * Creates a new proxy provider instance by loading the first available implementation found via SPI.
     *
     * @return A new {@link Provider} instance.
     */
    public static Provider createEngine() {
        return NormalSpiLoader.loadFirstAvailable(Provider.class);
    }

}
