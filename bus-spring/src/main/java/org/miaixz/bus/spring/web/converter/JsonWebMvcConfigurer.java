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

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.miaixz.bus.logger.Logger;

/**
 * Applies Bus message converter registrars to Spring MVC.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JsonWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * Converter registrars supplied by the application context.
     */
    private final List<MessageConverterRegistrar> registrars;

    /**
     * Creates the JSON Web MVC configurer.
     *
     * @param registrars converter registrars supplied by the application context
     */
    public JsonWebMvcConfigurer(List<MessageConverterRegistrar> registrars) {
        this.registrars = registrars.stream().sorted(AnnotationAwareOrderComparator.INSTANCE).toList();
    }

    /**
     * Invokes all Bus message converter registrars in deterministic order.
     *
     * @param builder Spring MVC message converter builder
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        long builtInCount = registrars.stream().filter(JsonMessageConverter.class::isInstance).count();
        if (builtInCount > 1) {
            throw new IllegalStateException("Multiple Bus JsonMessageConverter beans are registered");
        }
        builder.configureMessageConvertersList(converters -> {
            List<HttpMessageConverter<?>> busConverters = new ArrayList<>();
            registrars.forEach(registrar -> {
                registrar.register(busConverters);
                Logger.info(false, "Starter", "HTTP registered {} message converter", registrar.name());
            });
            // Spring builds its default converters before invoking this callback. Bus converters must therefore be
            // inserted at the front; appending them would allow the default Gson or Jackson converter to claim
            // application/json responses before Bus annotation filtering is reached.
            converters.addAll(0, busConverters);
        });
    }

}
