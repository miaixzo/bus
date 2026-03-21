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
package org.miaixz.bus.metrics;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;

/**
 * Factory for obtaining the active {@link Provider}. Follows the same singleton + SPI pattern as {@code JsonFactory} in
 * bus-extra.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Factory {

    /**
     * Returns the singleton {@link Provider}, resolved via SPI on first call.
     */
    public static Provider get() {
        return Instances.singletion(Factory.class).of();
    }

    /**
     * Creates a new {@link Provider} instance via SPI. Prefer {@link #get()} for performance.
     *
     * @return a new Provider
     * @throws InternalException if no implementation is found on the classpath
     */
    public static Provider of() {
        final Provider provider = NormalSpiLoader.loadFirstAvailable(Provider.class);
        if (null == provider) {
            throw new InternalException(
                    "No metrics Provider found! Add bus-metrics native provider or configure micrometer.");
        }
        return provider;
    }

}
