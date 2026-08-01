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

import org.miaixz.bus.core.lang.Charset;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * Configures Spring MVC plain-text conversion with UTF-8 encoding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TextWebMvcConfigurer implements MessageConverterRegistrar {

    /**
     * Creates the text MVC configurer.
     */
    public TextWebMvcConfigurer() {
    }

    @Override
    public String name() {
        return "BusText";
    }

    @Override
    public int order() {
        return 1;
    }

    /** Registers a UTF-8 String converter while preserving Spring's established media types. */
    @Override
    public void register(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.UTF_8);
        converters.removeIf(StringHttpMessageConverter.class::isInstance);
        converters.add(Math.min(order(), converters.size()), converter);
    }

}
