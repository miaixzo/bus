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
package org.miaixz.bus.extra.json;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;

/**
 * Factory for creating JSON provider instances. This factory automatically detects the JSON library introduced by the
 * user (e.g., Jackson, Gson, Fastjson) and creates a corresponding JSON parser.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JsonFactory {

    /**
     * Retrieves the singleton instance of {@link JsonProvider}. The provider is created based on the first available
     * JSON library found on the classpath via SPI.
     *
     * @return The singleton {@link JsonProvider} instance.
     */
    public static JsonProvider get() {
        return Instances.singletion(JsonFactory.class).of();
    }

    /**
     * Creates a new instance of {@link JsonProvider} based on the JSON engine JARs available on the classpath. It is
     * recommended to use the singleton instance provided by {@link #get()} for better performance, as this method
     * creates a new engine instance on each call.
     *
     * @return A new {@link JsonProvider} instance.
     * @throws InternalException if no JSON library (e.g., Jackson, Gson, Fastjson) is found on the classpath.
     */
    public static JsonProvider of() {
        final JsonProvider engine = NormalSpiLoader.loadFirstAvailable(JsonProvider.class);
        if (null == engine) {
            throw new InternalException("No json jar found ! Please add one of it to your project !");
        }
        return engine;
    }

}
