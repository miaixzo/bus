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
package org.miaixz.bus.spring.http;

import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

/**
 * An interface for configuring JSON {@link HttpMessageConverter}s in Spring MVC. Implementations provide a name, an
 * order of precedence, and the logic to configure the list of converters. It also supports an {@code autoType} property
 * for serialization/deserialization features like Fastjson's type recognition.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface JsonConverterConfigurer {

    /**
     * Returns the name of the converter, used for logging and debugging.
     *
     * @return The converter's name.
     */
    String name();

    /**
     * Returns the order of precedence for the converter (lower values have higher priority).
     *
     * @return The order value.
     */
    int order();

    /**
     * Configures the list of {@link HttpMessageConverter}s. Implementations should add their custom converter to this
     * list.
     *
     * @param converters The list of message converters to configure.
     */
    void configure(List<HttpMessageConverter<?>> converters);

    /**
     * Sets the {@code autoType} property for serialization/deserialization configuration. The default implementation is
     * empty, allowing subclasses to override it if they support this feature.
     *
     * @param autoType The configuration string for automatic type handling.
     */
    default void autoType(String autoType) {
        // Default implementation is a no-op to support implementations that do not use autoType.
    }

}
