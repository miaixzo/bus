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
package org.miaixz.bus.core.io.resource;

import java.util.Properties;

import org.miaixz.bus.core.Binder;

/**
 * 配置文件源
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface PropertySource {

    /**
     * 获取属性集合
     *
     * @return 属性集合
     */
    Properties props();

    /**
     * 获取属性
     *
     * @param key 属性键值
     * @return 属性值
     */
    default String getProperty(String key) {
        String value = props().getProperty(key);
        if (null == value) {
            return null;
        }
        return getPlaceholderProperty(value);
    }

    /**
     * 获取属性，可设置默认值
     *
     * @param key          属性键值
     * @param defaultValue 默认值
     * @return 属性值
     */
    default String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取占位符属性
     *
     * @param placeholder 占位 eg. ${a}
     * @return 属性值
     */
    default String getPlaceholderProperty(String placeholder) {
        return Binder.DEFAULT_HELPER.replacePlaceholders(placeholder, props());
    }

    /**
     * 是否包含该前缀属性
     *
     * @param prefix 前缀
     * @return true包含
     */
    default boolean containPrefix(String prefix) {
        Properties properties = props();
        for (Object key : properties.keySet()) {
            if (key.toString().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
