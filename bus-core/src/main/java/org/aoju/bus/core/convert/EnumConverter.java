/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.convert;

import org.aoju.bus.core.toolkit.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 无泛型检查的枚举转换器
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public class EnumConverter extends AbstractConverter<Object> {

    private static final Map<Class<?>, Map<Class<?>, Method>> VALUE_OF_METHOD_CACHE = new ConcurrentHashMap<>();

    private final Class enumClass;

    /**
     * 构造
     *
     * @param enumClass 转换成的目标Enum类
     */
    public EnumConverter(Class enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * 尝试找到类似转换的静态方法调用实现转换
     *
     * @param value     被转换的值
     * @param enumClass enum类
     * @return 对应的枚举值
     */
    protected static Enum tryConvertEnum(Object value, Class enumClass) {
        Enum enumResult = null;
        if (value instanceof Integer) {
            enumResult = EnumKit.getEnumAt(enumClass, (Integer) value);
        } else if (value instanceof String) {
            try {
                enumResult = Enum.valueOf(enumClass, (String) value);
            } catch (IllegalArgumentException e) {
                //ignore
            }
        }

        // 尝试查找其它用户自定义方法
        if (null == enumResult) {
            final Map<Class<?>, Method> valueOfMethods = getValueOfMethods(enumClass);
            if (MapKit.isNotEmpty(valueOfMethods)) {
                final Class<?> valueClass = value.getClass();
                for (Map.Entry<Class<?>, Method> entry : valueOfMethods.entrySet()) {
                    if (ClassKit.isAssignable(entry.getKey(), valueClass)) {
                        enumResult = ReflectKit.invokeStatic(entry.getValue(), value);
                    }
                }
            }
        }

        return enumResult;
    }

    /**
     * 获取用于转换为enum的所有static方法
     *
     * @param enumClass 枚举类
     * @return 转换方法map
     */
    private static Map<Class<?>, Method> getValueOfMethods(Class<?> enumClass) {
        Map<Class<?>, Method> valueOfMethods = VALUE_OF_METHOD_CACHE.get(enumClass);
        if (null == valueOfMethods) {
            valueOfMethods = Arrays.stream(enumClass.getMethods())
                    .filter(BeanKit::isStatic)
                    .filter(m -> m.getReturnType() == enumClass)
                    .filter(m -> m.getParameterCount() == 1)
                    .filter(m -> false == "valueOf".equals(m.getName()))
                    .collect(Collectors.toMap(m -> m.getParameterTypes()[0], m -> m, (existing, replacement) -> existing));
            VALUE_OF_METHOD_CACHE.put(enumClass, valueOfMethods);
        }
        return valueOfMethods;
    }

    @Override
    protected Object convertInternal(Object value) {
        Enum enumValue = tryConvertEnum(value, this.enumClass);
        if (null == enumValue && false == value instanceof String) {
            // 最后尝试valueOf转换
            enumValue = Enum.valueOf(this.enumClass, convertString(value));
        }
        return enumValue;
    }

    @Override
    public Class getTargetType() {
        return this.enumClass;
    }

}
