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
package org.miaixz.bus.spring.http;

import java.util.List;

/**
 * An interface for configuring JSON {@link org.springframework.http.converter.HttpMessageConverter}s in Spring MVC.
 * Implementations provide a name, an order of precedence, and the logic to configure the list of converters. It also
 * supports an {@code autoType} property for serialization/deserialization features like Fastjson's type recognition.
 * <p>
 * This interface provides methods to handle field filtering based on annotations like {@code @Transient} and
 * {@code @Include} across different JSON libraries (Fastjson, GSON, and Jackson).
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface HttpMessageConverter {

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
     * Configures the list of {@link org.springframework.http.converter.HttpMessageConverter}s. Implementations should
     * add their custom converter to this list.
     *
     * @param converters The list of message converters to configure.
     */
    void configure(List<org.springframework.http.converter.HttpMessageConverter<?>> converters);

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
