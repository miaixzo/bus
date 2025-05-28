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
package org.miaixz.bus.core;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 配置属性管理类，用于加载和获取配置属性，支持单例模式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Context extends Keys {

    /**
     * 全局唯一实例，静态初始化，确保单例模式
     */
    public static final Context INSTANCE = new Context();

    /**
     * 存储配置属性的 Properties 对象，用于键值对存储
     */
    public final Properties delegate = new Properties();

    /**
     * 私有构造方法，防止外部实例化，确保单例
     */
    public Context() {
        // 空实现
    }

    public Context(Properties properties) {
        this.delegate.putAll(properties);
    }

    public static Context newInstance(Properties properties) {
        return new Context(properties);
    }

    public Set<String> keys() {
        return delegate.stringPropertyNames();
    }

    /**
     * 设置配置属性，将外部 Properties 对象合并到内部 delegate 中
     *
     * @param properties 外部配置属性
     */
    public void putAll(Properties properties) {
        this.delegate.putAll(properties);
    }

    /**
     * 获取指定键的属性值
     *
     * @param key 属性键
     * @return 属性值，若不存在返回 null
     */
    public String getProperty(String key) {
        return delegate.getProperty(key);
    }

    /**
     * 获取指定键的属性值，若不存在返回默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值，若不存在或为空返回默认值
     */
    public String getProperty(String key, String defaultValue) {
        return this.delegate.getProperty(key, defaultValue);
    }

    /**
     * 获取指定键的整型属性值，若不存在或无法转换返回默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 整型属性值，若无法解析返回默认值
     */
    public int getProperty(String key, int defaultValue) {
        return Convert.toInt(getProperty(key), defaultValue);
    }

    /**
     * 获取指定键的布尔型属性值，若不存在返回默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 布尔型属性值，若不存在返回默认值
     */
    public boolean getProperty(String key, boolean defaultValue) {
        final String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return BooleanKit.toBoolean(value);
    }

    /**
     * 按分组前缀获取配置属性，返回分组后的属性映射
     *
     * @param group 分组前缀
     * @return 分组后的属性映射，若无匹配返回空映射
     */
    public Map<String, Properties> group(String group) {
        final Set<String> keys = keys();
        // 过滤以指定前缀开头的键
        Set<String> inner = keys.stream().filter(i -> i.startsWith(group)).collect(Collectors.toSet());
        if (CollKit.isEmpty(inner)) {
            return Collections.emptyMap();
        }
        Map<String, Properties> map = MapKit.newHashMap();
        inner.forEach(i -> {
            Properties p = new Properties();
            // 提取分组键后的子键
            String key = i.substring(group.length()) + Symbol.COLON;
            int keyIndex = key.length();
            // 过滤以子键开头的属性并存储
            keys.stream().filter(j -> j.startsWith(key))
                    .forEach(j -> p.setProperty(j.substring(keyIndex), delegate.getProperty(j)));
            map.put(delegate.getProperty(i), p);
        });

        return map;
    }

    public Context whenNotBlank(String key, Consumer<String> consumer) {
        String value = delegate.getProperty(key);
        if (StringKit.isNotBlank(value)) {
            consumer.accept(value);
        }
        return this;
    }

    public <T> Context whenNotBlank(String key, Function<String, T> function, Consumer<T> consumer) {
        String value = delegate.getProperty(key);
        if (StringKit.isNotBlank(value)) {
            consumer.accept(function.apply(value));
        }
        return this;
    }

}
