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
package org.miaixz.bus.mapper;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.miaixz.bus.logger.Logger;

/**
 * 多语言支持工具类，支持通过 JVM 参数设置 Locale，例如：{@code -Duser.country=US -Duser.language=en}。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Locale {

    /**
     * 获取指定语言的格式化文本，当资源文件或键不存在时，返回 {@code MessageFormat.format(key, args)}。
     *
     * @param locale     语言环境
     * @param bundleName 资源文件名
     * @param key        字符串键
     * @param args       格式化参数
     * @return 格式化后的文本
     */
    public static String message(java.util.Locale locale, String bundleName, String key, Object... args) {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(bundleName, locale);
        } catch (Exception e) {
            bundle = null;
            Logger.warn("Failed to load resource bundle: " + bundleName + " for locale: " + locale);
        }
        try {
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException e) {
            Logger.warn("Resource key not found: " + key + " in bundle: " + bundleName);
            return MessageFormat.format(key, args);
        }
    }

    /**
     * 获取默认语言环境的格式化文本，当资源文件或键不存在时，返回 {@code MessageFormat.format(key, args)}。
     *
     * @param bundleName 资源文件名
     * @param key        字符串键
     * @param args       格式化参数
     * @return 格式化后的文本
     */
    public static String message(String bundleName, String key, Object... args) {
        return message(java.util.Locale.getDefault(), bundleName, key, args);
    }

    /**
     * 获取指定语言环境的语言包。
     *
     * @param locale     语言环境
     * @param bundleName 语言包名称
     * @return 语言包实例
     */
    public static Language language(java.util.Locale locale, String bundleName) {
        return (key, args) -> message(locale, bundleName, key, args);
    }

    /**
     * 获取默认语言环境的语言包。
     *
     * @param bundleName 语言包名称
     * @return 语言包实例
     */
    public static Language language(String bundleName) {
        return language(java.util.Locale.getDefault(), bundleName);
    }

    /**
     * 语言包接口，定义获取格式化文本的方法。
     */
    public interface Language {
        /**
         * 获取对应语言的格式化文本。
         *
         * @param key  字符串键
         * @param args 格式化参数
         * @return 格式化后的文本
         */
        String message(String key, Object... args);
    }

}