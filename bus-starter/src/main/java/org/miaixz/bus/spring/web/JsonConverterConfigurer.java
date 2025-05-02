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

import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

/**
 * JSON 转换器配置器接口，用于配置 Spring MVC 的 HttpMessageConverter。 实现类需提供转换器名称、可用性检查、优先级顺序和配置逻辑。 支持设置 autoType 属性，用于序列化/反序列化配置（如
 * Fastjson 的类型自动识别）。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface JsonConverterConfigurer {

    /**
     * 返回转换器的名称，用于日志和调试。
     *
     * @return 转换器名称
     */
    String name();

    /**
     * 返回转换器的优先级顺序（值越小，优先级越高）。
     *
     * @return 优先级顺序
     */
    int order();

    /**
     * 配置 HttpMessageConverter 列表。
     *
     * @param converters 要配置的消息转换器列表
     */
    void configure(List<HttpMessageConverter<?>> converters);

    /**
     * 设置 autoType 属性，用于序列化/反序列化配置。 默认实现为空，子类可覆盖以支持 autoType。
     *
     * @param autoType 自动类型配置字符串
     */
    default void autoType(String autoType) {
        // 默认实现为空，兼容不依赖 autoType 的实现类
    }

}