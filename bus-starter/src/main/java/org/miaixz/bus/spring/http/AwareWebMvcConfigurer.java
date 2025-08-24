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
package org.miaixz.bus.spring.http;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

/**
 * 配置Spring MVC消息转换器，支持字符串和JSON的序列化/反序列化。 支持默认JSON框架（Jackson、Fastjson）以及通过JsonConverterConfigurer配置的自定义框架。
 * 确保UTF-8编码支持中文字符，并妥善处理缺失的依赖。 使用bus库的SpringBuilder获取自定义配置器。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AwareWebMvcConfigurer extends SpringEnvironmentPostProcessor
        implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    /**
     * String转换器的默认媒体类型，支持JSON和UTF-8编码的纯文本
     */
    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List.of(MediaType.APPLICATION_JSON,
            MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"));

    /**
     * JSON序列化时的类型支持配置
     */
    protected String autoType;
    /**
     * Controller的统一URL前缀
     */
    protected String prefix;
    /**
     * Sentinel请求拦截器
     */
    protected SentinelRequestHandler handler;

    /**
     * 构造函数，初始化autoType、prefix和handler
     *
     * @param autoType JSON序列化时的类型支持配置
     * @param prefix   Controller的统一URL前缀
     * @param handler  Sentinel请求拦截器
     */
    public AwareWebMvcConfigurer(String autoType, String prefix, SentinelRequestHandler handler) {
        super();
        this.autoType = autoType;
        this.prefix = prefix;
        this.handler = handler;
    }

    /**
     * 添加拦截器到Spring MVC配置，注册SentinelRequestHandler并指定拦截路径
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 确保拦截器正确注册，拦截所有路径，排除静态资源、错误页面和favicon
        registry.addInterceptor(this.handler).addPathPatterns("/**").excludePathPatterns("/static/**", "/error",
                "/favicon.ico");
    }

    /**
     * 添加自定义参数解析器到Spring MVC
     *
     * @param resolvers 参数解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CompositeArgumentResolver());
    }

    /**
     * 配置Spring MVC的消息转换器列表，按顺序添加StringHttpMessageConverter和JSON转换器。 确保至少一个JSON框架（Jackson、Fastjson或自定义配置器）可用，否则抛出异常。
     *
     * @param converters 要配置的消息转换器列表
     * @throws IllegalStateException 如果没有可用的JSON配置器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 配置StringHttpMessageConverter以支持字符串转换
        configureConverter(converters, this::configureStringConverter, "StringHttpMessageConverter");

        // 配置JSON转换器，用户可通过实现JsonConverterConfigurer并标记@Component来自定义
        configureJsonConverters(converters, getJsonConfigurers());
    }

    /**
     * 为所有Controller配置统一的URL路径前缀
     *
     * @param configurer URL路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(this.prefix,
                c -> (c.isAnnotationPresent(Controller.class) || c.isAnnotationPresent(RestController.class)));
    }

    /**
     * 获取用户定义的JSON配置器列表。 通过SpringBuilder.getBeansOfType获取，按order()值排序，并为每个配置器设置autoType。
     *
     * @return 排序后的JsonConverterConfigurer实例列表
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
     * 配置单个消息转换器并记录日志
     *
     * @param converters 要添加到的消息转换器列表
     * @param configurer 转换器的配置逻辑
     * @param name       转换器名称（用于日志记录）
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
     * 配置JSON转换器列表，逐个应用自定义配置器
     *
     * @param converters  要添加到的消息转换器列表
     * @param configurers 要应用的JsonConverterConfigurer实例列表
     */
    private void configureJsonConverters(List<HttpMessageConverter<?>> converters,
            List<JsonConverterConfigurer> configurers) {
        for (JsonConverterConfigurer configurer : configurers) {
            configureConverter(converters, configurer::configure, configurer.name());
        }
    }

    /**
     * 配置StringHttpMessageConverter，使用UTF-8编码和默认媒体类型
     *
     * @param converters 要添加到的消息转换器列表
     */
    private void configureStringConverter(List<HttpMessageConverter<?>> converters) {
        /**
         * 字符串消息转换器实例，使用UTF-8编码
         */
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.UTF_8);
        stringConverter.setSupportedMediaTypes(DEFAULT_MEDIA_TYPES);
        converters.add(stringConverter);
        Logger.debug("StringHttpMessageConverter configured with media types: {}", DEFAULT_MEDIA_TYPES);
    }

}