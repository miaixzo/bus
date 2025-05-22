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
package org.miaixz.bus.mapper.parsing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.mapper.Context;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 属性映射基类，提供属性的存储和操作
 *
 * @param <T> 子类类型，用于支持链式调用
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class PropertyMeta<T extends PropertyMeta> {

    /**
     * 附加属性，用于扩展
     */
    protected Map<String, String> props;

    /**
     * 获取属性值
     *
     * @param key 属性名
     * @return 属性值，若不存在则返回 null
     */
    public String getProp(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        String val = props != null ? props.get(key) : null;
        // 如果配置值不存在，从全局获取配置
        if (val == null) {
            val = Context.INSTANCE.getProperty(key);
        }
        return val;
    }

    /**
     * 获取属性值，支持默认值
     *
     * @param key          属性名
     * @param defaultValue 默认值
     * @return 属性值，若不存在则返回默认值
     */
    public String getProp(String key, String defaultValue) {
        return ObjectKit.defaultIfNull(getProp(key), defaultValue);
    }

    /**
     * 获取整型属性值
     *
     * @param prop 属性名
     * @return 整型属性值，若不存在或无法解析则返回 null
     */
    public Integer getInt(String prop) {
        return Convert.toInt(getProp(prop));
    }

    /**
     * 获取整型属性值，支持默认值
     *
     * @param key          属性名
     * @param defaultValue 默认值
     * @return 整型属性值，若不存在或无法解析则返回默认值
     */
    public Integer getInt(String key, Integer defaultValue) {
        return Convert.toInt(getInt(key), defaultValue);
    }

    /**
     * 获取布尔型属性值
     *
     * @param key 属性名
     * @return 布尔型属性值，若不存在则返回 null
     */
    public Boolean getBoolean(String key) {
        return BooleanKit.toBoolean(getProp(key));
    }

    /**
     * 获取布尔型属性值，支持默认值
     *
     * @param key          属性名
     * @param defaultValue 默认值
     * @return 布尔型属性值，若不存在则返回默认值
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        final String value = getProp(key);
        if (value == null) {
            return defaultValue;
        }
        return BooleanKit.toBoolean(value);
    }

    /**
     * 设置属性值
     *
     * @param prop  属性名
     * @param value 属性值
     * @return 当前实例，支持链式调用
     */
    public T put(String prop, String value) {
        if (this.props == null) {
            synchronized (this) {
                if (this.props == null) {
                    this.props = new ConcurrentHashMap<>();
                }
            }
        }
        this.props.put(prop, value);
        return (T) this;
    }

    /**
     * 批量设置属性值，追加到原有属性集合
     *
     * @param props 属性映射
     * @return 当前实例，支持链式调用
     */
    public T put(Map<String, String> props) {
        if (props != null && !props.isEmpty()) {
            for (Map.Entry<String, String> entry : props.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
        return (T) this;
    }

    /**
     * 删除指定属性
     *
     * @param prop 属性名
     * @return 被删除的属性值，若不存在则返回 null
     */
    public String remove(String prop) {
        if (props != null) {
            String value = getProp(prop);
            props.remove(prop);
            return value;
        }
        return null;
    }

}