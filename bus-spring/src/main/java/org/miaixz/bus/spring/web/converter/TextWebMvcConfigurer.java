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

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import org.miaixz.bus.core.lang.Charset;

/**
 * Configures Spring MVC plain-text conversion with UTF-8 encoding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TextWebMvcConfigurer implements MessageConverterRegistrar {

    /**
     * Creates the text MVC configurer.
     */
    public TextWebMvcConfigurer() {
        // No initialization required.
    }

    @Override
    public String name() {
        return "BusText";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * Registers a UTF-8 string converter while preserving Spring's established media types.
     *
     * @param converters mutable message converter list
     */
    @Override
    public void register(List<HttpMessageConverter<?>> converters) {
        if (converters.stream().noneMatch(StringHttpMessageConverter.class::isInstance)) {
            converters.add(new StringHttpMessageConverter(Charset.UTF_8));
        }
    }

}
