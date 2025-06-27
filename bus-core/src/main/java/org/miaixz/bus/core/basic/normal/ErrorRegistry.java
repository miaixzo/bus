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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 基础错误码类，支持国际化，可被继承以定义具体错误码
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class ErrorRegistry implements Errors {

    /**
     * 错误码
     */
    private final String key;

    /**
     * 默认错误信息
     */
    private final String value;

    /**
     * 构造方法，创建并注册错误码
     *
     * @param key   错误码
     * @param value 默认错误信息
     */
    protected ErrorRegistry(String key, String value) {
        this.key = key;
        this.value = value;
        this.register();
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * 获取错误信息，使用默认语言环境
     *
     * @return 错误信息
     */
    @Override
    public String getValue() {
        return getValue(I18n.AUTO_DETECT);
    }

    /**
     * 获取错误信息，根据指定语言环境
     *
     * @param i18n 语言环境（来自I18n枚举）
     * @return 错误信息
     */
    public String getValue(I18n i18n) {
        try {
            Locale locale = i18n == I18n.AUTO_DETECT ? Locale.getDefault() : new Locale(i18n.lang());
            ResourceBundle bundle = ResourceBundle.getBundle(Keys.BUNDLE_NAME, locale);
            return bundle.getString(this.key);
        } catch (Exception e) {
            // 回退到 ERRORS_CACHE 中注册的错误信息
            Errors.Entry entry = Errors.require(this.key);
            return entry != null ? entry.getValue() : this.value;
        }
    }

}