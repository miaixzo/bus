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
package org.miaixz.bus.image.metric;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

/**
 * 属性类，用于表示名称-值对属性。
 *
 * <p>
 * 该类用于存储和操作属性，支持字符串、布尔值和数值类型的属性值。 它提供了从字符串数组创建属性数组、从属性数组中获取特定属性值， 以及通过反射将属性值设置到对象中的方法。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Property implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852261138619L;

    /**
     * 属性名称
     */
    private final String name;
    /**
     * 属性值
     */
    private final Object value;

    /**
     * 构造方法
     *
     * @param name  属性名称
     * @param value 属性值
     * @throws NullPointerException     如果名称或值为null
     * @throws IllegalArgumentException 如果值不是字符串、布尔值或数值类型
     */
    public Property(String name, Object value) {
        if (name == null)
            throw new NullPointerException("name");
        if (value == null)
            throw new NullPointerException("value");
        if (!(value instanceof String || value instanceof Boolean || value instanceof Number))
            throw new IllegalArgumentException("value: " + value.getClass());
        this.name = name;
        this.value = value;
    }

    /**
     * 从字符串构造属性
     *
     * @param s 格式为"name=value"的字符串
     */
    public Property(String s) {
        int endParamName = s.indexOf('=');
        name = s.substring(0, endParamName);
        value = valueOf(s.substring(endParamName + 1));
    }

    /**
     * 将字符串转换为适当的值类型
     *
     * @param s 输入字符串
     * @return 转换后的值（Double、Boolean或String）
     */
    private static Object valueOf(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE : s.equalsIgnoreCase("false") ? Boolean.FALSE : s;
        }
    }

    /**
     * 从字符串数组创建属性数组
     *
     * @param ss 字符串数组，每个元素格式为"name=value"
     * @return 属性数组
     */
    public static Property[] valueOf(String[] ss) {
        Property[] properties = new Property[ss.length];
        for (int i = 0; i < properties.length; i++) {
            properties[i] = new Property(ss[i]);
        }
        return properties;
    }

    /**
     * 从属性数组中获取指定名称的属性值
     *
     * @param <T>    值的类型
     * @param props  属性数组
     * @param name   属性名称
     * @param defVal 默认值
     * @return 找到的属性值，如果未找到则返回默认值
     */
    public static <T> T getFrom(Property[] props, String name, T defVal) {
        for (Property prop : props)
            if (prop.name.equals(name))
                return (T) prop.value;
        return defVal;
    }

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    public final String getName() {
        return name;
    }

    /**
     * 获取属性值
     *
     * @return 属性值
     */
    public final Object getValue() {
        return value;
    }

    /**
     * 计算哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

    /**
     * 判断对象是否相等
     *
     * @param object 要比较的对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        Property other = (Property) object;
        return name.equals(other.name) && value.equals(other.value);
    }

    /**
     * 返回字符串表示
     *
     * @return 格式为"name=value"的字符串
     */
    @Override
    public String toString() {
        return name + '=' + value;
    }

    /**
     * 通过反射将属性值设置到指定对象中
     *
     * @param o 目标对象
     * @throws IllegalArgumentException 如果找不到合适的setter方法或调用失败
     */
    public void setAt(Object o) {
        String setterName = "set" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
        try {
            Class<?> clazz = o.getClass();
            if (value instanceof String) {
                clazz.getMethod(setterName, String.class).invoke(o, value);
            } else if (value instanceof Boolean) {
                clazz.getMethod(setterName, boolean.class).invoke(o, value);
            } else { // value instanceof Number
                try {
                    clazz.getMethod(setterName, double.class).invoke(o, ((Number) value).doubleValue());
                } catch (NoSuchMethodException e) {
                    try {
                        clazz.getMethod(setterName, float.class).invoke(o, ((Number) value).floatValue());
                    } catch (NoSuchMethodException e2) {
                        try {
                            clazz.getMethod(setterName, int.class).invoke(o, ((Number) value).intValue());
                        } catch (NoSuchMethodException e3) {
                            throw e;
                        }
                    }
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
