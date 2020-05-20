/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.office.magic.family;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.extra.json.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 包含office支持的{@code DocumentFormat}集合，该集合已从JSON源加载.
 *
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
public class JsonFormatRegistry extends SimpleFormatRegistry {

    protected JsonFormatRegistry() {
        super();
    }

    /**
     * 从给定源创建 {@link JsonFormatRegistry}
     *
     * @param source 包含 {@link DocumentFormat} 集合的字符串(JSON格式).
     * @return 创建的 {@link JsonFormatRegistry}
     */
    public static JsonFormatRegistry create(final String source) {
        return create(source, null);
    }

    /**
     * 从给定的InputStream创建 {@link JsonFormatRegistry}
     *
     * @param source 包含 {@link DocumentFormat} 集合的InputStream (JSON格式).
     * @return 创建的 {@link JsonFormatRegistry}
     * @throws IOException 如果发生I/O错误.
     */
    public static JsonFormatRegistry create(final InputStream source) throws IOException {
        return create(source, null);
    }

    /**
     * 从给定的InputStream创建 {@link JsonFormatRegistry}
     *
     * @param source           包含 {@link DocumentFormat} 集合的InputStream (JSON格式).
     * @param customProperties 加载或存储文档时应用的自定义属性.
     * @return 创建的 {@link JsonFormatRegistry}
     * @throws IOException 如果发生I/O错误.
     */
    public static JsonFormatRegistry create(
            final InputStream source,
            final Map<String, FormatProperties> customProperties)
            throws IOException {
        return create(IoUtils.toString(source, Charset.DEFAULT_UTF_8), customProperties);
    }

    /**
     * 从给定源创建 {@link JsonFormatRegistry}
     *
     * @param source           包含 {@link DocumentFormat} 集合的InputStream (JSON格式).
     * @param customProperties 加载或存储文档时应用的自定义属性.
     * @return 创建的 {@link JsonFormatRegistry}
     */
    public static JsonFormatRegistry create(
            final String source,
            final Map<String, FormatProperties> customProperties) {
        final JsonFormatRegistry registry = new JsonFormatRegistry();
        registry.readJsonArray(source, customProperties);
        return registry;
    }

    /**
     * 读取相关配置信息.
     *
     * @param source           包含 {@link DocumentFormat} 集合的InputStream (JSON格式).
     * @param customProperties 加载或存储文档时应用的自定义属性.
     */
    protected void readJsonArray(final String source,
                                 final Map<String, FormatProperties> customProperties) {
        final List<DocumentFormat> list = JsonUtils.toList(source, DocumentFormat.class);
        if (CollUtils.isNotEmpty(list)) {
            list.stream().map(fmt -> {
                if (ObjectUtils.isEmpty(customProperties) || !customProperties.containsKey(fmt.getExtension())) {
                    return DocumentFormat.unmodifiableCopy(fmt);
                }
                final FormatProperties props = customProperties.get(fmt.getExtension());
                final DocumentFormat.Builder builder = DocumentFormat.builder().from(fmt).unmodifiable(true);

                props.getLoad().forEach(builder::loadProperty);
                props.getStore().forEach((family, storeProps) -> storeProps.forEach((name, value) -> builder.storeProperty(family, name, value)));
                return builder.build();
            }).forEach(this::addFormat);
        }
    }

}
