/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.text.placeholder.template;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.placeholder.StringTemplate;
import org.miaixz.bus.core.text.placeholder.segment.LiteralSegment;
import org.miaixz.bus.core.text.placeholder.segment.SingleSegment;
import org.miaixz.bus.core.text.placeholder.segment.StringSegment;
import org.miaixz.bus.core.xyz.ArrayKit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * 单占位符字符串模板
 * <p>
 * 例如，"?", "{}", "$$$"
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SingleStringTemplate extends StringTemplate {

    /**
     * 默认的占位符
     */
    public static final String DEFAULT_PLACEHOLDER = Symbol.DELIM;
    /**
     * 占位符，默认为: {@link Symbol#DELIM}
     */
    protected String placeholder;

    protected SingleStringTemplate(final String template, final int features, final String placeholder,
            final char escape, final String defaultValue, final UnaryOperator<String> defaultValueHandler) {
        super(template, escape, defaultValue, defaultValueHandler, features);

        Assert.notEmpty(placeholder);
        this.placeholder = placeholder;

        // 初始化Segment列表
        afterInit();
    }

    /**
     * 创建 builder
     *
     * @param template 字符串模板，不能为 {@code null}
     * @return builder实例
     */
    public static Builder builder(final String template) {
        return new Builder(template);
    }

    @Override
    protected List<StringSegment> parseSegments(final String template) {
        final int placeholderLength = placeholder.length();
        final int strPatternLength = template.length();
        // 记录已经处理到的位置
        int handledPosition = 0;
        // 占位符所在位置
        int delimIndex;
        // 上一个解析的segment是否是固定文本，如果是，则需要和当前新的文本部分合并
        boolean lastIsLiteralSegment = false;
        // 复用的占位符变量
        final SingleSegment singlePlaceholderSegment = SingleSegment.of(placeholder);
        List<StringSegment> segments = null;
        while (true) {
            delimIndex = template.indexOf(placeholder, handledPosition);
            if (delimIndex == -1) {
                // 整个模板都不带占位符
                if (handledPosition == 0) {
                    return Collections.singletonList(new LiteralSegment(template));
                }
                // 字符串模板剩余部分不再包含占位符
                if (handledPosition < strPatternLength) {
                    addLiteralSegment(lastIsLiteralSegment, segments, template.substring(handledPosition));
                }
                return segments;
            } else if (segments == null) {
                segments = new ArrayList<>();
            }

            // 存在 转义符
            if (delimIndex > 0 && template.charAt(delimIndex - 1) == escape) {
                // 存在 双转义符
                if (delimIndex > 1 && template.charAt(delimIndex - 2) == escape) {
                    // 转义符之前还有一个转义符，形如："//{"，占位符依旧有效
                    addLiteralSegment(lastIsLiteralSegment, segments,
                            template.substring(handledPosition, delimIndex - 1));
                    segments.add(singlePlaceholderSegment);
                    lastIsLiteralSegment = false;
                    handledPosition = delimIndex + placeholderLength;
                } else {
                    // 占位符被转义，形如："/{"，当前字符并不是一个真正的占位符，而是普通字符串的一部分
                    addLiteralSegment(lastIsLiteralSegment, segments,
                            template.substring(handledPosition, delimIndex - 1) + placeholder.charAt(0));
                    lastIsLiteralSegment = true;
                    handledPosition = delimIndex + 1;
                }
            } else {
                // 正常占位符
                addLiteralSegment(lastIsLiteralSegment, segments, template.substring(handledPosition, delimIndex));
                segments.add(singlePlaceholderSegment);
                lastIsLiteralSegment = false;
                handledPosition = delimIndex + placeholderLength;
            }
        }
    }

    /**
     * 按顺序使用 数组元素 替换 占位符
     *
     * @param args 可变参数
     * @return 格式化字符串
     */
    public String format(final Object... args) {
        return formatArray(args);
    }

    /**
     * 按顺序使用 原始数组元素 替换 占位符
     *
     * @param array 原始类型数组，例如: {@code int[]}
     * @return 格式化字符串
     */
    public String formatArray(final Object array) {
        return formatArray(ArrayKit.wrap(array));
    }

    /**
     * 按顺序使用 数组元素 替换 占位符
     *
     * @param array 数组
     * @return 格式化字符串
     */
    public String formatArray(final Object[] array) {
        if (array == null) {
            return getTemplate();
        }
        return format(Arrays.asList(array));
    }

    /**
     * 按顺序使用 迭代器元素 替换 占位符
     *
     * @param iterable iterable
     * @return 格式化字符串
     */
    public String format(final Iterable<?> iterable) {
        return super.formatSequence(iterable);
    }

    /**
     * 将 占位符位置的值 按顺序解析为 字符串数组
     *
     * @param text 待解析的字符串，一般是格式化方法的返回值
     * @return 参数值数组
     */
    public String[] matchesToArray(final String text) {
        return matches(text).toArray(new String[0]);
    }

    /**
     * 将 占位符位置的值 按顺序解析为 字符串列表
     *
     * @param text 待解析的字符串，一般是格式化方法的返回值
     * @return 参数值列表
     */
    public List<String> matches(final String text) {
        return super.matchesSequence(text);
    }

    /**
     * 构造器
     */
    public static class Builder extends AbstractBuilder<Builder, SingleStringTemplate> {
        /**
         * 单占位符
         * <p>
         * 例如："?"、"{}"
         * </p>
         * <p>
         * 默认为 {@link SingleStringTemplate#DEFAULT_PLACEHOLDER}
         * </p>
         */
        protected String placeholder;

        protected Builder(final String template) {
            super(template);
        }

        /**
         * 设置 占位符
         *
         * @param placeholder 占位符，不能为 {@code null} 和 {@code ""}
         * @return builder
         */
        public Builder placeholder(final String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        @Override
        protected SingleStringTemplate buildInstance() {
            if (this.placeholder == null) {
                this.placeholder = DEFAULT_PLACEHOLDER;
            }
            return new SingleStringTemplate(this.template, this.features, this.placeholder, this.escape,
                    this.defaultValue, this.defaultValueHandler);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
