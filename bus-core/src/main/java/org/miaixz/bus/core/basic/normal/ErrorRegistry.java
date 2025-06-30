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
package org.miaixz.bus.core.basic.normal;

import java.util.Locale;
import java.util.ResourceBundle;

import org.miaixz.bus.core.lang.I18n;
import org.miaixz.bus.core.lang.Keys;

/**
 * 基础错误码注册类，实现了 {@link Errors} 接口。
 * <p>
 * 本类提供了错误码的注册、构建和国际化支持功能。每个错误码包含一个唯一键(key)和对应的错误信息(value)， 支持根据不同的语言环境获取本地化的错误信息。
 * </p>
 *
 * <p>
 * 使用示例：
 * </p>
 *
 * <pre>{@code
 * Errors error = ErrorRegistry.builder().key("AUTH_001").value("认证失败").build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ErrorRegistry implements Errors {

    /**
     * 错误码键，用于唯一标识一个错误
     */
    private final String key;

    /**
     * 默认错误信息，当找不到本地化信息时使用
     */
    private final String value;

    /**
     * 私有构造函数，通过 Builder 构建实例。
     *
     * @param builder 包含构建参数的 Builder 对象
     * @throws IllegalArgumentException 如果 key 或 value 为 null
     */
    public ErrorRegistry(Builder builder) {
        if (builder.key == null || builder.value == null) {
            throw new IllegalArgumentException("key和value不能为null");
        }
        this.key = builder.key;
        this.value = builder.value;
        // 构造时立即注册
        this.register();
    }

    /**
     * 获取 Builder 实例，用于构建 ErrorRegistry 对象。
     *
     * @return 新的 Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 获取错误码键。
     *
     * @return 错误码键
     */
    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * 获取默认错误信息（使用自动检测的语言环境）。
     *
     * @return 错误信息字符串
     */
    @Override
    public String getValue() {
        return getValue(I18n.AUTO_DETECT);
    }

    /**
     * 根据指定语言环境获取本地化的错误信息。
     *
     * @param i18n 语言环境枚举，使用 {@link I18n#AUTO_DETECT} 表示自动检测
     * @return 本地化的错误信息
     */
    public String getValue(I18n i18n) {
        try {
            Locale locale = i18n == I18n.AUTO_DETECT ? Locale.getDefault() : new Locale(i18n.lang());
            ResourceBundle bundle = ResourceBundle.getBundle(Keys.BUNDLE_NAME, locale);
            return bundle.getString(this.key);
        } catch (Exception e) {
            // 回退到 ERRORS_CACHE 中注册的错误信息
            Entry entry = Errors.require(this.key);
            return entry != null ? entry.getValue() : this.value;
        }
    }

    /**
     * ErrorRegistry 的构建器，支持链式调用。
     */
    public static class Builder {

        /**
         * 错误码键，用于唯一标识一个错误。
         */
        private String key;

        /**
         * 默认错误信息，当找不到本地化信息时使用。
         */
        private String value;

        /**
         * 设置错误码键
         *
         * @param key 错误码键
         * @return 当前 Builder 实例
         */
        public Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * 设置默认错误信息
         *
         * @param value 错误信息
         * @return 当前 Builder 实例
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /**
         * 构建 ErrorRegistry 实例
         *
         * @return 新的 ErrorRegistry 实例
         */
        public ErrorRegistry build() {
            return new ErrorRegistry(this);
        }
    }

}