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
package org.miaixz.bus.extra.pinyin;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.lang.loader.spi.ServiceLoader;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Simple Pinyin engine factory that automatically creates the corresponding Pinyin engine object based on the Pinyin
 * library JARs introduced by the user. It uses the Simple Factory pattern.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinyinFactory {

    /**
     * Retrieves a singleton instance of {@link PinyinProvider}. The first available Pinyin provider found via SPI will
     * be used.
     *
     * @return A singleton instance of {@link PinyinProvider}.
     */
    public static PinyinProvider get() {
        final PinyinProvider engine = Instances.get(PinyinProvider.class.getName(), PinyinFactory::of);
        Logger.debug(
                "Use [{}] Pinyin Provider As Default.",
                StringKit.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
        return engine;
    }

    /**
     * Creates a new {@link PinyinProvider} instance based on the available Pinyin engine JARs. This method returns a
     * new engine instance each time it is called.
     *
     * @return A new {@link PinyinProvider} instance.
     * @throws InternalException if no Pinyin library is found or available.
     */
    private static PinyinProvider of() {
        final PinyinProvider engine = NormalSpiLoader.loadFirstAvailable(PinyinProvider.class);
        if (null != engine) {
            return engine;
        }

        throw new InternalException("No pinyin jar found !Please add one of it to your project !");
    }

    /**
     * Creates a custom Pinyin engine by name.
     *
     * @param name The name of the engine (case-insensitive), e.g., `Bopomofo4j`, `Houbb`, `JPinyin`, `Pinyin4j`,
     *             `TinyPinyin`.
     * @return The {@link PinyinProvider} instance corresponding to the given name.
     * @throws InternalException if no engine with the specified name is found.
     */
    public static PinyinProvider of(String name) throws InternalException {
        if (!StringKit.endWithIgnoreCase(name, "Provider")) {
            name = name + "Provider";
        }
        final ServiceLoader<PinyinProvider> list = NormalSpiLoader.loadList(PinyinProvider.class);
        for (final String serviceName : list.getServiceNames()) {
            if (StringKit.endWithIgnoreCase(serviceName, name)) {
                return list.getService(serviceName);
            }
        }
        throw new InternalException("No such provider named: " + name);
    }

}
