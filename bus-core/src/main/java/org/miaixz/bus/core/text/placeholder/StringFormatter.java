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
package org.miaixz.bus.core.text.placeholder;

import java.util.Map;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.text.placeholder.template.NamedPlaceholderString;
import org.miaixz.bus.core.text.placeholder.template.SinglePlaceholderString;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 字符串格式化工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringFormatter {

    private static final WeakConcurrentMap<Map.Entry<CharSequence, Object>, StringTemplate> CACHE = new WeakConcurrentMap<>();

    /**
     * 格式化字符串 此方法只是简单将占位符 {} 按照顺序替换为参数 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可 例： 通常使用：format("this is {}
     * for {}", "a", "b") = this is a for b 转义{}： format("this is \\{} for {}", "a", "b") = this is {} for a 转义\：
     * format("this is \\\\{} for {}", "a", "b") = this is \a for b
     *
     * @param strPattern 字符串模板
     * @param argArray   参数列表
     * @return 结果
     */
    public static String format(final String strPattern, final Object... argArray) {
        return formatWith(strPattern, Symbol.DELIM, argArray);
    }

    /**
     * 格式化字符串 此方法只是简单将指定占位符 按照顺序替换为参数 如果想输出占位符使用 \\转义即可，如果想输出占位符之前的 \ 使用双转义符 \\\\ 即可 例： 通常使用：format("this is {} for {}",
     * "{}", "a", "b") = this is a for b 转义{}： format("this is \\{} for {}", "{}", "a", "b") = this is {} for a 转义\：
     * format("this is \\\\{} for {}", "{}", "a", "b") = this is \a for b
     *
     * @param strPattern  字符串模板
     * @param placeHolder 占位符，例如{}
     * @param argArray    参数列表
     * @return 结果
     */
    public static String formatWith(final String strPattern, final String placeHolder, final Object... argArray) {
        if (StringKit.isBlank(strPattern) || StringKit.isBlank(placeHolder) || ArrayKit.isEmpty(argArray)) {
            return strPattern;
        }
        return ((SinglePlaceholderString) CACHE.computeIfAbsent(MutableEntry.of(strPattern, placeHolder),
                k -> StringTemplate.of(strPattern).placeholder(placeHolder).build())).format(argArray);
    }

    /**
     * 格式化文本，使用 {varName} 占位 bean = User:{a: "aValue", b: "bValue"} format("{a} and {b}", bean) --- aValue and bValue
     *
     * @param template   文本模板，被替换的部分用 {key} 表示
     * @param bean       参数Bean
     * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
     * @return 格式化后的文本
     */
    public static String formatByBean(final CharSequence template, final Object bean, final boolean ignoreNull) {
        if (null == template) {
            return null;
        }

        if (bean instanceof Map) {
            if (MapKit.isEmpty((Map<?, ?>) bean)) {
                return template.toString();
            }
        }
        // Bean的空检查需要反射，性能很差，此处不检查
        return ((NamedPlaceholderString) CACHE.computeIfAbsent(MutableEntry.of(template, ignoreNull), k -> {
            final NamedPlaceholderString.Builder builder = StringTemplate.ofNamed(template.toString());
            if (ignoreNull) {
                builder.addFeatures(StringTemplate.Feature.FORMAT_NULL_VALUE_TO_WHOLE_PLACEHOLDER);
            } else {
                builder.addFeatures(StringTemplate.Feature.FORMAT_NULL_VALUE_TO_EMPTY);
            }
            return builder.build();
        })).format(bean);
    }

    /**
     * 清空缓存
     */
    public static void clear() {
        CACHE.clear();
    }

}
