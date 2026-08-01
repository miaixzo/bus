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

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.miaixz.bus.logger.Logger;

/**
 * Applies Bus message converter registrars to Spring MVC.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class JsonWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * Converter registrars supplied by the application context.
     */
    private final ObjectProvider<MessageConverterRegistrar> registrars;

    /**
     * Safe deserialization package rules forwarded to compatible registrars.
     */
    private final String autoType;

    /**
     * Creates the JSON Web MVC configurer.
     *
     * @param registrars converter registrars supplied by the application context
     * @param autoType   comma-separated safe deserialization package rules
     */
    public JsonWebMvcConfigurer(ObjectProvider<MessageConverterRegistrar> registrars, String autoType) {
        this.registrars = registrars;
        this.autoType = autoType;
    }

    /**
     * Invokes all Bus message converter registrars in deterministic order.
     *
     * @param builder Spring MVC message converter builder
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        List<MessageConverterRegistrar> available = registrars.orderedStream().peek(this::configureAutoType)
                .sorted(Comparator.comparingInt(MessageConverterRegistrar::order)).toList();
        long builtInCount = available.stream().filter(JsonMessageConverter.class::isInstance).count();
        if (builtInCount > 1) {
            throw new IllegalStateException("Multiple Bus JsonMessageConverter beans are registered");
        }
        builder.configureMessageConvertersList(converters -> available.forEach(registrar -> {
            registrar.register(converters);
            Logger.info(false, "Starter", "HTTP registered {} message converter", registrar.name());
        }));
    }

    /**
     * Applies the configured safe deserialization rules to one converter registrar.
     *
     * @param registrar converter registrar
     */
    private void configureAutoType(MessageConverterRegistrar registrar) {
        registrar.autoType(autoType);
    }

}
