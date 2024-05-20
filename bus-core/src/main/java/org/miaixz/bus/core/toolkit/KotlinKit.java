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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.beans.copier.ValueProvider;
import org.miaixz.bus.core.beans.copier.provider.MapValueProvider;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.reflect.kotlin.KotlinCallable;
import org.miaixz.bus.core.lang.reflect.kotlin.KotlinClassImpl;
import org.miaixz.bus.core.lang.reflect.kotlin.KotlinParameter;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * Kotlin反射包装相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KotlinKit {

    private static final Class<? extends Annotation> META_DATA_CLASS =
            (Class<? extends Annotation>) Optional.ofTry(() -> Class.forName("kotlin.Metadata")).get();

    /**
     * 是否提供或处于Kotlin环境中
     */
    public static final boolean IS_KOTLIN_ENABLE = null != META_DATA_CLASS;

    /**
     * 检查给定的类是否为Kotlin类
     * Kotlin类带有@kotlin.Metadata注解
     *
     * @param clazz 类
     * @return 是否Kotlin类
     */
    public static boolean isKotlinClass(final Class<?> clazz) {
        return IS_KOTLIN_ENABLE && clazz.isAnnotationPresent(META_DATA_CLASS);
    }

    /**
     * 获取Kotlin类的所有构造方法
     *
     * @param targetType kotlin类
     * @return 构造列表
     */
    public static List<?> getConstructors(final Class<?> targetType) {
        return KotlinClassImpl.getConstructors(targetType);
    }

    /**
     * 获取参数列表
     *
     * @param kCallable kotlin的类、方法或构造
     * @return 参数列表
     */
    public static List<KotlinParameter> getParameters(final Object kCallable) {
        return KotlinCallable.getParameters(kCallable);
    }

    /**
     * 从{@link ValueProvider}中提取对应name的参数列表
     *
     * @param kCallable     kotlin的类、方法或构造
     * @param valueProvider {@link ValueProvider}
     * @return 参数数组
     */
    public static Object[] getParameterValues(final Object kCallable, final ValueProvider<String> valueProvider) {
        final List<KotlinParameter> parameters = getParameters(kCallable);
        final Object[] args = new Object[parameters.size()];
        KotlinParameter kParameter;
        for (int i = 0; i < parameters.size(); i++) {
            kParameter = parameters.get(i);
            args[i] = valueProvider.value(kParameter.getName(), kParameter.getType());
        }
        return args;
    }

    /**
     * 实例化Kotlin对象
     *
     * @param <T>        对象类型
     * @param targetType 对象类型
     * @param map        参数名和参数值的Map
     * @return 对象
     */
    public static <T> T newInstance(final Class<T> targetType, final Map<String, ?> map) {
        return newInstance(targetType, new MapValueProvider(map));
    }

    /**
     * 实例化Kotlin对象
     *
     * @param <T>           对象类型
     * @param targetType    对象类型
     * @param valueProvider 值提供器，用于提供构造所需参数值
     * @return 对象
     */
    public static <T> T newInstance(final Class<T> targetType, final ValueProvider<String> valueProvider) {
        final List<?> constructors = getConstructors(targetType);
        RuntimeException exception = null;
        for (final Object constructor : constructors) {
            final Object[] parameterValues = getParameterValues(constructor, valueProvider);
            try {
                return (T) KotlinCallable.call(constructor, parameterValues);
            } catch (final RuntimeException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
        return null;
    }

}
