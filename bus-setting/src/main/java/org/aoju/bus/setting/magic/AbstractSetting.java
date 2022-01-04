/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.setting.magic;

import org.aoju.bus.core.beans.copier.CopyOptions;
import org.aoju.bus.core.beans.copier.ValueProvider;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.getter.OptNullString;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.BeanKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Setting抽象类
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public abstract class AbstractSetting implements OptNullString<String>, Serializable {

    @Override
    public String getStr(String key, String defaultValue) {
        return getStr(key, Normal.EMPTY, defaultValue);
    }

    /**
     * 获得字符串类型值
     *
     * @param key          KEY
     * @param group        分组
     * @param defaultValue 默认值
     * @return 值，如果字符串为{@code null}返回默认值
     */
    public String getStr(String key, String group, String defaultValue) {
        return ObjectKit.defaultIfNull(getByGroup(key, group), defaultValue);
    }

    /**
     * 获得字符串类型值，如果字符串为{@code null}或者""返回默认值
     *
     * @param key          KEY
     * @param group        分组
     * @param defaultValue 默认值
     * @return 值，如果字符串为{@code null}或者""返回默认值
     */
    public String getStrNotEmpty(String key, String group, String defaultValue) {
        return ObjectKit.defaultIfEmpty(getByGroup(key, group), defaultValue);
    }

    /**
     * 获得指定分组的键对应值
     *
     * @param key   键
     * @param group 分组
     * @return 值
     */
    public abstract String getByGroup(String key, String group);

    /**
     * 带有日志提示的get,如果没有定义指定的KEY,则打印debug日志
     *
     * @param key 键
     * @return 值
     */
    public String getWithLog(String key) {
        return getStr(key);
    }

    /**
     * 带有日志提示的get,如果没有定义指定的KEY,则打印debug日志
     *
     * @param key   键
     * @param group 分组
     * @return 值
     */
    public String getByGroupWithLog(String key, String group) {
        return getByGroup(key, group);
    }

    /**
     * 获得数组型
     *
     * @param key 属性名
     * @return 属性值
     */
    public String[] getStrings(String key) {
        return getStrings(key, null);
    }

    /**
     * 获得数组型
     *
     * @param key          属性名
     * @param defaultValue 默认的值
     * @return 属性值
     */
    public String[] getStringsWithDefault(String key, String[] defaultValue) {
        String[] value = getStrings(key, null);
        if (null == value) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * 获得数组型
     *
     * @param key   属性名
     * @param group 分组名
     * @return 属性值
     */
    public String[] getStrings(String key, String group) {
        return getStrings(key, group, Symbol.COMMA);
    }

    /**
     * 获得数组型
     *
     * @param key       属性名
     * @param group     分组名
     * @param delimiter 分隔符
     * @return 属性值
     */
    public String[] getStrings(String key, String group, String delimiter) {
        final String value = getByGroup(key, group);
        if (StringKit.isBlank(value)) {
            return null;
        }
        return StringKit.splitToArray(value, delimiter);
    }

    /**
     * 获取数字型型属性值
     *
     * @param key   属性名
     * @param group 分组名
     * @return 属性值
     */
    public Integer getInt(String key, String group) {
        return getInt(key, group, null);
    }

    /**
     * 获取数字型型属性值
     *
     * @param key          属性名
     * @param group        分组名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public Integer getInt(String key, String group, Integer defaultValue) {
        return Convert.toInt(getByGroup(key, group), defaultValue);
    }

    /**
     * 获取布尔型属性值
     *
     * @param key   属性名
     * @param group 分组名
     * @return 属性值
     */
    public Boolean getBool(String key, String group) {
        return getBool(key, group, null);
    }

    /**
     * 获取布尔型型属性值
     *
     * @param key          属性名
     * @param group        分组名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public Boolean getBool(String key, String group, Boolean defaultValue) {
        return Convert.toBool(getByGroup(key, group), defaultValue);
    }

    /**
     * 获取long类型属性值
     *
     * @param key   属性名
     * @param group 分组名
     * @return 属性值
     */
    public Long getLong(String key, String group) {
        return getLong(key, group, null);
    }

    /**
     * 获取long类型属性值
     *
     * @param key          属性名
     * @param group        分组名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public Long getLong(String key, String group, Long defaultValue) {
        return Convert.toLong(getByGroup(key, group), defaultValue);
    }

    /**
     * 获取char类型属性值
     *
     * @param key   属性名
     * @param group 分组名
     * @return 属性值
     */
    public Character getChar(String key, String group) {
        final String value = getByGroup(key, group);
        if (StringKit.isBlank(value)) {
            return null;
        }
        return value.charAt(0);
    }

    /**
     * 获取double类型属性值
     *
     * @param key   属性名
     * @param group 分组名
     * @return 属性值
     */
    public Double getDouble(String key, String group) {
        return getDouble(key, group, null);
    }

    /**
     * 获取double类型属性值
     *
     * @param key          属性名
     * @param group        分组名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public Double getDouble(String key, String group, Double defaultValue) {
        return Convert.toDouble(getByGroup(key, group), defaultValue);
    }

    /**
     * 将setting中的键值关系映射到对象中，原理是调用对象对应的set方法
     * 只支持基本类型的转换
     *
     * @param <T>   对象
     * @param group 分组
     * @param bean  Bean对象
     * @return Bean
     */
    public <T> T toBean(final String group, T bean) {
        return BeanKit.fillBean(bean, new ValueProvider<String>() {
            @Override
            public Object value(String key, Type valueType) {
                return getByGroup(key, group);
            }

            @Override
            public boolean containsKey(String key) {
                return null != getByGroup(key, group);
            }
        }, CopyOptions.create());
    }

    /**
     * 将setting中的键值关系映射到对象中，原理是调用对象对应的set方法
     * 只支持基本类型的转换
     *
     * @param <T>  对象
     * @param bean Bean
     * @return Bean
     */
    public <T> T toBean(T bean) {
        return toBean(null, bean);
    }

}
