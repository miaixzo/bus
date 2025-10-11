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
        return Instances.singletion(JsonFactory.class).create();
    }

    /**
     * Creates a new instance of {@link JsonProvider} based on the JSON engine JARs available on the classpath. It is
     * recommended to use the singleton instance provided by {@link #get()} for better performance, as this method
     * creates a new engine instance on each call.
     *
     * @return A new {@link JsonProvider} instance.
     * @throws InternalException if no JSON library (e.g., Jackson, Gson, Fastjson) is found on the classpath.
     */
    public static JsonProvider create() {
        final JsonProvider engine = NormalSpiLoader.loadFirstAvailable(JsonProvider.class);
        if (null == engine) {
            throw new InternalException("No json jar found ! Please add one of it to your project !");
        }
        return engine;
    }

}
