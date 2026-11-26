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
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import org.miaixz.bus.logger.Logger;

/**
 * Configures Spring MVC message conversion and explicit HTTP message output.
 *
 * @author Kimi Liu
 */
public class MessageWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * Ordered HTTP message converters for Spring MVC.
     */
    private final List<? extends MessageValueConverter<?>> converters;

    /**
     * Creates the MVC configurer with the application-selectable message converters.
     *
     * @param converters message converters supplied by the application
     */
    public MessageWebMvcConfigurer(List<? extends MessageValueConverter<?>> converters) {
        this.converters = converters.stream().sorted(AnnotationAwareOrderComparator.INSTANCE).toList();
    }

    /**
     * Registers the Bus message converters ahead of the default MVC converters.
     *
     * @param builder Spring MVC message converter builder
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        long builtInCount = converters.stream().filter(JsonMessageValueConverter.class::isInstance).count();
        if (builtInCount > 1) {
            throw new IllegalStateException("Multiple Bus JsonMessageValueConverter instances are configured");
        }
        builder.configureMessageConvertersList(mvcConverters -> {
            // MVC gives priority to the first converter supporting the requested type and media type.
            mvcConverters.addAll(0, converters);
            converters.forEach(
                    converter -> Logger
                            .info(false, "Starter", "HTTP registered {} message converter", converter.name()));
        });
    }

    /**
     * Installs explicit HTTP message output at the native response-body handler's position.
     *
     * @param adapter initialized MVC adapter
     */
    public static void configureMessageHandler(RequestMappingHandlerAdapter adapter) {
        var installed = adapter.getReturnValueHandlers();
        if (installed == null || installed.stream().anyMatch(MessageReturnValueHandler.class::isInstance)) {
            return;
        }
        for (int index = 0; index < installed.size(); index++) {
            if (installed.get(index) instanceof RequestResponseBodyMethodProcessor processor) {
                var handlers = new ArrayList<>(installed);
                handlers.set(index, new MessageReturnValueHandler(processor));
                adapter.setReturnValueHandlers(handlers);
                return;
            }
        }
    }

}
