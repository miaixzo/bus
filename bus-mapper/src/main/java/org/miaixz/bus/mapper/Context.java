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

import java.util.Properties;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.BooleanKit;

/**
 * 配置属性管理类，用于加载和获取配置属性
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Context {

    /**
     * 全局唯一实例，静态初始化
     */
    public static final Context INSTANCE = new Context();

    /**
     * 存储配置属性的Properties对象
     */
    private final Properties properties = new Properties();

    /**
     * 私有构造方法，防止外部实例化
     */
    private Context() {
    }

    /**
     * 设置配置属性，将外部Properties对象合并到内部
     * 
     * @param properties 外部配置属性
     */
    public void setProperties(Properties properties) {
        this.properties.putAll(properties);
    }

    /**
     * 获取指定键的属性值
     * 
     * @param key 属性键
     * @return 属性值，若不存在返回null
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * 获取指定键的属性值，若不存在返回默认值
     * 
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值或默认值
     */
    public String getProperty(String key, String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    /**
     * 获取指定键的整型属性值，若不存在或无法转换返回默认值
     * 
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 整型属性值或默认值
     */
    public int getInt(String key, int defaultValue) {
        return Convert.toInt(getProperty(key), defaultValue);
    }

    /**
     * 获取指定键的布尔型属性值，若不存在返回默认值
     * 
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 布尔型属性值或默认值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        final String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return BooleanKit.toBoolean(value);
    }

}