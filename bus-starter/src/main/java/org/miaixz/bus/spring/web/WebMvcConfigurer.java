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
package org.miaixz.bus.spring.web;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.SpringBuilder;
import org.miaixz.bus.spring.env.SpringEnvironmentPostProcessor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * 配置 Spring MVC 消息转换器，支持字符串和 JSON 序列化/反序列化。 支持默认 JSON 框架（Jackson、Fastjson）和通过 JsonConverterConfigurer 配置的自定义框架。 确保
 * UTF-8 编码支持中文字符，并优雅处理缺失的依赖。 使用 bus 库的 SpringBuilder 获取自定义配置器。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WebMvcConfigurer extends SpringEnvironmentPostProcessor
        implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    // String 转换器的默认媒体类型，支持 JSON 和 UTF-8 纯文本
    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List.of(MediaType.APPLICATION_JSON,
            MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"));

    protected String autoType;

    public WebMvcConfigurer(String autoType) {
        super();
        this.autoType = autoType;
    }

    /**
     * 配置 Spring MVC 的消息转换器列表。 按指定顺序添加 StringHttpMessageConverter 和 JSON 转换器（默认和自定义）。 确保至少一个 JSON 框架（Jackson、Fastjson
     * 或自定义配置器）存在，否则抛出异常。
     *
     * @param converters 要配置的消息转换器列表
     * @throws IllegalStateException 如果没有可用的 JSON 配置器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 配置 StringHttpMessageConverter
        configureConverter(converters, this::configureStringConverter, "StringHttpMessageConverter");

        /**
         * 配置 JSON 转换器 用户可自定义实现/只需实现 {@link JsonConverterConfigurer } @Component 即可自动加载
         */
        configureJsonConverters(converters, getJsonConfigurers());
    }

    /**
     * 使用 SpringBuilder.getBeansOfType 获取用户定义的 JSON 配置器。 为每个配置器设置 autoType 属性，按 order() 值排序，仅返回可用的配置器。
     *
     * @return 自定义 JsonConverterConfigurer 实例列表
     */
    private List<JsonConverterConfigurer> getJsonConfigurers() {
        List<JsonConverterConfigurer> configurers = SpringBuilder.getBeansOfType(JsonConverterConfigurer.class).values()
                .stream().peek(configurer -> {
                    try {
                        configurer.autoType(this.autoType);
                        Logger.debug("Set autoType '{}' for custom JsonConverterConfigurer: {}", this.autoType,
                                configurer.name());
                    } catch (Exception e) {
                        Logger.warn("Failed to set autoType for custom JsonConverterConfigurer {}: {}",
                                configurer.name(), e.getMessage(), e);
                    }
                }).sorted(Comparator.comparingInt(JsonConverterConfigurer::order)).toList();
        Logger.debug("Retrieved {} available custom JsonConverterConfigurer beans: {}", configurers.size(),
                configurers.stream().map(JsonConverterConfigurer::name).toList());
        return configurers;
    }

    /**
     * 配置单个消息转换器，处理日志记录。
     *
     * @param converters 要添加到的消息转换器列表
     * @param configurer 转换器的配置逻辑
     * @param name       转换器名称（用于日志）
     */
    private void configureConverter(List<HttpMessageConverter<?>> converters,
            Consumer<List<HttpMessageConverter<?>>> configurer, String name) {
        try {
            configurer.accept(converters);
            Logger.info("Successfully configured {} message converter", name);
        } catch (Exception e) {
            Logger.warn("Failed to configure {}: {}", name, e.getMessage(), e);
        }
    }

    /**
     * 配置 JSON 转换器列表，按顺序应用每个配置器。
     *
     * @param converters  要添加到的消息转换器列表
     * @param configurers 要应用的 JsonConverterConfigurer 实例列表
     */
    private void configureJsonConverters(List<HttpMessageConverter<?>> converters,
            List<JsonConverterConfigurer> configurers) {
        for (JsonConverterConfigurer configurer : configurers) {
            configureConverter(converters, configurer::configure, configurer.name());
        }
    }

    /**
     * 配置 StringHttpMessageConverter，使用 UTF-8 编码和默认媒体类型。
     *
     * @param converters 要添加到的消息转换器列表
     */
    private void configureStringConverter(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.UTF_8);
        stringConverter.setSupportedMediaTypes(DEFAULT_MEDIA_TYPES);
        converters.add(stringConverter);
        Logger.debug("StringHttpMessageConverter configured with media types: {}", DEFAULT_MEDIA_TYPES);
    }

}