/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core;

import lombok.RequiredArgsConstructor;
import org.miaixz.bus.core.annotation.Ignore;
import org.miaixz.bus.core.annotation.Values;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.io.resource.PropertySource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 属性绑定器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@RequiredArgsConstructor
public class Binder {

    /**
     * 默认占位符前缀: {@value}
     */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = Symbol.DOLLAR + Symbol.BRACE_LEFT;

    /**
     * 默认占位符后缀: {@value}
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = Symbol.BRACE_RIGHT;

    /**
     * 默认占位值分隔符: {@value}
     */
    public static final String DEFAULT_VALUE_SEPARATOR = Symbol.COLON;

    /**
     * 默认实例
     */
    public static final Binder DEFAULT_HELPER;

    private static final Map<String, String> SIMPLE_PREFIXES = new HashMap<>(4);

    static {
        SIMPLE_PREFIXES.put(Symbol.BRACE_RIGHT, Symbol.BRACE_LEFT);
        SIMPLE_PREFIXES.put(Symbol.BRACKET_RIGHT, Symbol.BRACKET_LEFT);
        SIMPLE_PREFIXES.put(Symbol.PARENTHESE_RIGHT, Symbol.PARENTHESE_LEFT);

        DEFAULT_HELPER = new Binder(DEFAULT_PLACEHOLDER_PREFIX, DEFAULT_PLACEHOLDER_SUFFIX,
                DEFAULT_VALUE_SEPARATOR, true);
    }

    private final String placeholderPrefix;
    private final String placeholderSuffix;
    private final String simplePrefix;
    private final String valueSeparator;
    private final boolean ignoreUnresolvablePlaceholders;
    private PropertySource source;

    /**
     * 构造
     *
     * @param placeholderPrefix 占位符前缀
     * @param placeholderSuffix 占位符后缀
     */
    public Binder(String placeholderPrefix, String placeholderSuffix) {
        this(placeholderPrefix, placeholderSuffix, null, true);
    }

    /**
     * 构造
     *
     * @param placeholderPrefix              占位符前缀
     * @param placeholderSuffix              占位符后缀
     * @param valueSeparator                 值分隔符
     * @param ignoreUnresolvablePlaceholders 忽略不可解析的占位符
     */
    public Binder(String placeholderPrefix,
                  String placeholderSuffix,
                  String valueSeparator,
                  boolean ignoreUnresolvablePlaceholders) {

        Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
        Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        String simplePrefixForSuffix = SIMPLE_PREFIXES.get(this.placeholderSuffix);
        if (null != simplePrefixForSuffix && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.simplePrefix = simplePrefixForSuffix;
        } else {
            this.simplePrefix = this.placeholderPrefix;
        }
        this.valueSeparator = valueSeparator;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    private static boolean substringMatch(CharSequence text, int index, CharSequence substring) {
        if (index + substring.length() > text.length()) {
            return false;
        }
        for (int i = 0; i < substring.length(); i++) {
            if (text.charAt(index + i) != substring.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 绑定属性到类
     *
     * @param clazz 类型
     * @param <T>   泛型
     * @return the object
     */
    public <T> T bind(Class<T> clazz) {
        return bind(clazz, Symbol.DOT);
    }

    /**
     * 绑定指定前缀属性到类
     *
     * @param clazz  类型
     * @param prefix 前缀
     * @param <T>    泛型
     * @return the object
     */
    public <T> T bind(Class<T> clazz, String prefix) {
        T object;
        try {
            object = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InternalException(e);
        }

        Class<?> actualClass = ClassKit.getCglibActualClass(clazz);
        boolean b = (null == prefix || Symbol.DOT.equals(prefix)) && actualClass.isAnnotationPresent(Values.class);
        if (b) {
            prefix = actualClass.getAnnotation(Values.class).value();
        }
        return clazz.cast(bind(object, prefix));
    }

    /**
     * 绑定属性到对象实例
     *
     * @param object 对象实例
     * @param prefix 属性前缀
     * @param <T>    泛型
     * @return the object
     */
    public <T> T bind(T object, String prefix) {
        if (!StringKit.hasText(prefix) || Symbol.DOT.equals(prefix)) {
            prefix = null;
        }
        for (Field field : ClassKit.getDeclaredFields(object.getClass())) {
            bindField(object, field, prefix);
        }
        return object;
    }

    private void bindField(Object object, Field field, String prefix) {
        try {
            if (field.isAnnotationPresent(Ignore.class)) {
                return;
            }
            String key = field.getName();
            boolean wrap = false;
            if (field.isAnnotationPresent(Values.class)) {
                key = field.getAnnotation(Values.class).value();
                wrap = true;
            } else if (null != prefix) {
                key = prefix + Symbol.DOT + key;
            }
            Object value = getProperty(key, field.getType(), wrap);
            if (null != value) {
                ClassKit.writeField(field, object, value);
                return;
            }
            if (!(null == field.getType().getClassLoader()) && source
                    .containPrefix(key + Symbol.DOT)) {
                value = ClassKit.readField(field, object);
                if (null == value) {
                    value = bind(field.getType(), key);
                    ClassKit.writeField(field, object, value);
                } else {
                    bind(value, key);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object getProperty(String key, Class<?> type, boolean wrap) {
        String value;
        if (wrap) {
            value = source.getPlaceholderProperty(key);
        } else {
            value = source.getProperty(key);
        }
        if (null == value || value.getClass() == type) {
            return value;
        }
        return Convert.convert(type, value);
    }

    /**
     * 替换所有占位格式 {@code ${name}}
     *
     * @param value      需要转换的属性
     * @param properties 配置集合
     * @return 替换后的字符串
     */
    public String replacePlaceholders(String value, final Properties properties) {
        Assert.notNull(properties, "'properties' must not be null");
        Assert.notNull(value, "'value' must not be null");
        return parseStringValue(value, properties, new HashSet<>());
    }

    /**
     * 替换字符串
     *
     * @param value               字符串
     * @param properties          属性
     * @param visitedPlaceholders 参数占位符
     * @return 替换后的字符串
     */
    protected String parseStringValue(String value, Properties properties,
                                      Set<String> visitedPlaceholders) {
        StringBuilder result = new StringBuilder(value);
        int startIndex = value.indexOf(this.placeholderPrefix);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if (endIndex != -1) {
                String placeholder = result
                        .substring(startIndex + this.placeholderPrefix.length(), endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(originalPlaceholder)) {
                    throw new IllegalArgumentException(
                            "Circular placeholder reference '" + originalPlaceholder
                                    + "' in property definitions");
                }
                // 递归
                placeholder = parseStringValue(placeholder, properties, visitedPlaceholders);
                // 获取没有占位的属性值
                String propVal = properties.getProperty(placeholder);
                propVal = getCommonVal(properties, placeholder, propVal);
                if (null != propVal) {
                    // 递归 表达式中含有表达式
                    propVal = parseStringValue(propVal, properties, visitedPlaceholders);
                    result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                    startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                } else if (this.ignoreUnresolvablePlaceholders) {
                    // 继续解析
                    startIndex = result
                            .indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
                } else {
                    throw new IllegalArgumentException(String
                            .format("Could not resolve placeholder '%s' in value '%s'", placeholder, value));
                }
                visitedPlaceholders.remove(originalPlaceholder);
            } else {
                startIndex = -1;
            }
        }
        return result.toString();
    }

    private String getCommonVal(Properties properties, String placeholder, String propVal) {
        if (null == propVal && null != this.valueSeparator) {
            int separatorIndex = placeholder.indexOf(this.valueSeparator);
            if (separatorIndex != -1) {
                String actualPlaceholder = placeholder.substring(0, separatorIndex);
                String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                propVal = properties.getProperty(actualPlaceholder);
                if (null == propVal) {
                    propVal = defaultValue;
                }
            }
        }
        return propVal;
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + this.placeholderSuffix.length();
                } else {
                    return index;
                }
            } else if (substringMatch(buf, index, this.simplePrefix)) {
                withinNestedPlaceholder++;
                index = index + this.simplePrefix.length();
            } else {
                index++;
            }
        }
        return -1;
    }

}
