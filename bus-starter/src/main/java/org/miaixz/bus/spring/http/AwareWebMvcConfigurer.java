/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.SpringBuilder;
import org.miaixz.bus.spring.env.SpringEnvironmentPostProcessor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

/**
 * Configures Spring MVC message converters, supporting serialization/deserialization for strings and JSON.
 * <p>
 * This class extends {@link SpringEnvironmentPostProcessor} and implements
 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer} to provide custom configurations for
 * Spring MVC. It ensures UTF-8 encoding for Chinese characters, handles missing dependencies for JSON frameworks, and
 * supports custom JSON converters. It also configures a unified URL prefix for controllers and registers a Sentinel
 * request interceptor.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AwareWebMvcConfigurer extends SpringEnvironmentPostProcessor
        implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    /**
     * Default media types for StringHttpMessageConverter, supporting JSON and plain text with UTF-8 encoding.
     */
    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List
            .of(MediaType.APPLICATION_JSON, MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"));

    /**
     * Configuration for type support during JSON serialization.
     */
    protected String autoType;
    /**
     * Unified URL prefix for controllers.
     */
    protected String prefix;
    /**
     * Sentinel request interceptor for handling requests.
     */
    protected SentinelRequestHandler handler;

    /**
     * Constructs a new {@code AwareWebMvcConfigurer} with the specified autoType, prefix, and handler.
     *
     * @param autoType JSON serialization type support configuration.
     * @param prefix   Unified URL prefix for controllers.
     * @param handler  Sentinel request interceptor.
     */
    public AwareWebMvcConfigurer(String autoType, String prefix, SentinelRequestHandler handler) {
        super();
        this.autoType = autoType;
        this.prefix = prefix;
        this.handler = handler;
    }

    /**
     * Adds interceptors to the Spring MVC configuration. Registers the {@link SentinelRequestHandler} to intercept all
     * paths, excluding static resources, error pages, and favicon.
     *
     * @param registry The interceptor registry.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Ensure the interceptor is correctly registered to intercept all paths, excluding static resources, error
        // pages, and favicon.
        registry.addInterceptor(this.handler).addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/error", "/favicon.ico");
    }

    /**
     * Adds custom argument resolvers to Spring MVC.
     *
     * @param resolvers The list of argument resolvers.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CompositeArgumentResolver());
    }

    /**
     * Configures the list of Spring MVC message converters. Adds {@link StringHttpMessageConverter} and JSON converters
     * in order. Ensures that at least one JSON framework (Jackson, Fastjson, or custom configurer) is available,
     * otherwise throws an exception.
     *
     * @param converters The list of message converters to configure.
     * @throws IllegalStateException if no JSON configurer is available.
     */
    @Override
    public void configureMessageConverters(
            List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        // Configure StringHttpMessageConverter to support string conversion
        configureConverter(converters, this::configureStringConverter, "StringHttpMessageConverter");

        // Configure JSON converters; users can customize by implementing JsonConverterConfigurer and marking with
        // @Component
        configureJsonConverters(converters, getJsonConfigurers());
    }

    /**
     * Configures a unified URL path prefix for all controllers.
     *
     * @param configurer The URL path matching configurer.
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                this.prefix,
                c -> (c.isAnnotationPresent(Controller.class) || c.isAnnotationPresent(RestController.class)));
    }

    /**
     * Retrieves a sorted list of user-defined JSON configurers. Configurers are obtained via
     * {@link SpringBuilder#getBeansOfType(Class)}, sorted by their {@link HttpMessageConverter#order()} value, and
     * their {@code autoType} property is set.
     *
     * @return A sorted list of {@link HttpMessageConverter} instances.
     */
    protected List<HttpMessageConverter> getJsonConfigurers() {
        List<HttpMessageConverter> configurers = SpringBuilder.getBeansOfType(HttpMessageConverter.class).values()
                .stream().peek(configurer -> {
                    try {
                        configurer.autoType(this.autoType);
                        Logger.debug(
                                "Set autoType '{}' for custom JsonConverterConfigurer: {}",
                                this.autoType,
                                configurer.name());
                    } catch (Exception e) {
                        Logger.warn(
                                "Failed to set autoType for custom JsonConverterConfigurer {}: {}",
                                configurer.name(),
                                e.getMessage(),
                                e);
                    }
                }).sorted(Comparator.comparingInt(HttpMessageConverter::order)).toList();
        Logger.debug(
                "Retrieved {} available custom JsonConverterConfigurer beans: {}",
                configurers.size(),
                configurers.stream().map(HttpMessageConverter::name).toList());
        return configurers;
    }

    /**
     * Configures a single message converter and logs the operation.
     *
     * @param converters The list of message converters to add to.
     * @param configurer The consumer that applies the configuration logic for the converter.
     * @param name       The name of the converter (for logging purposes).
     */
    protected void configureConverter(
            List<org.springframework.http.converter.HttpMessageConverter<?>> converters,
            Consumer<List<org.springframework.http.converter.HttpMessageConverter<?>>> configurer,
            String name) {
        try {
            configurer.accept(converters);
            Logger.info("Successfully configured {} message converter", name);
        } catch (Exception e) {
            Logger.warn("Failed to configure {}: {}", name, e.getMessage(), e);
        }
    }

    /**
     * Configures the list of JSON message converters by applying each custom {@link HttpMessageConverter}.
     *
     * @param converters  The list of message converters to add to.
     * @param configurers The list of {@link HttpMessageConverter} instances to apply.
     */
    protected void configureJsonConverters(
            List<org.springframework.http.converter.HttpMessageConverter<?>> converters,
            List<HttpMessageConverter> configurers) {
        for (HttpMessageConverter configurer : configurers) {
            configureConverter(converters, configurer::configure, configurer.name());
        }
    }

    /**
     * Configures the {@link StringHttpMessageConverter} with UTF-8 encoding and default media types.
     *
     * @param converters The list of message converters to add to.
     */
    protected void configureStringConverter(
            List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        /**
         * String message converter instance, using UTF-8 encoding.
         */
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.UTF_8);
        stringConverter.setSupportedMediaTypes(DEFAULT_MEDIA_TYPES);
        converters.add(stringConverter);
        Logger.debug("StringHttpMessageConverter configured with media types: {}", DEFAULT_MEDIA_TYPES);
    }

}
