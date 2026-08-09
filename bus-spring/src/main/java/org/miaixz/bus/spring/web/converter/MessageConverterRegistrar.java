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
package org.miaixz.bus.spring.web.converter;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * Stateless contract for appending Bus-managed HTTP message converters.
 *
 * @author Kimi Liu
 */
public interface MessageConverterRegistrar extends Ordered {

    /**
     * Returns the stable registrar name used for conflict detection.
     *
     * @return registrar name
     */

    String name();

    @Override
    int getOrder();

    /**
     * Appends this registrar's converters to the MVC converter list.
     *
     * @param converters mutable MVC converter list
     */

    void register(List<HttpMessageConverter<?>> converters);

    /**
     * Applies the configured automatic JSON type rule.
     *
     * @param autoType auto type
     */

    default void autoType(String autoType) {
        // Registrars without deserialization type handling ignore this optional input.
    }

}
