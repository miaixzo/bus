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
 * Registers Bus-managed HTTP message converters with Spring MVC.
 * <p>
 * Implementations describe their diagnostic name and ordering, then add one or more concrete Spring
 * {@link org.springframework.http.converter.HttpMessageConverter} instances to the supplied converter list.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface MessageConverterRegistrar {

    /**
     * Returns the registrar name used for diagnostics.
     *
     * @return registrar name
     */
    String name();

    /**
     * Returns the registrar precedence; lower values run first.
     *
     * @return ordering value
     */
    int order();

    /**
     * Adds the concrete Spring message converters managed by this registrar.
     *
     * @param converters mutable Spring message converter list
     */
    void register(List<org.springframework.http.converter.HttpMessageConverter<?>> converters);

    /**
     * Supplies optional safe deserialization type rules to registrars that support them.
     *
     * @param autoType comma-separated safe deserialization type rules
     */
    default void autoType(String autoType) {
        // Registrars without deserialization type handling need no additional configuration.
    }

}
