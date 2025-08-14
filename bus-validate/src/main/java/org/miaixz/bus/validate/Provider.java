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
package org.miaixz.bus.validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.normal.ErrorRegistry;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.magic.Material;
import org.miaixz.bus.validate.magic.annotation.Complex;

/**
 * 服务提供者
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Provider {

    /**
     * 被校验对象 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param <T>    对象
     * @param object 原始对象
     * @return the object
     */
    public static <T> T on(Object object) {
        return (T) new Verified(object).access();
    }

    /**
     * 被校验对象 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param <T>     对象
     * @param object  原始对象
     * @param context 上下文信息
     * @return the object
     */
    public static <T> T on(Object object, Context context) {
        return (T) new Verified(object, context).access();
    }

    /**
     * 被校验对象 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param <T>         对象
     * @param object      原始对象
     * @param annotations 注解信息
     * @return the object
     */
    public static <T> T on(Object object, Annotation[] annotations) {
        return (T) new Verified(object, annotations).access();
    }

    /**
     * 被校验对象 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param <T>         对象
     * @param object      原始对象
     * @param annotations 注解信息
     * @param context     上下文信息
     * @return the object
     */
    public static <T> T on(Object object, Annotation[] annotations, Context context) {
        return (T) new Verified(object, annotations, context).access();
    }

    /**
     * 被校验对象 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param <T>         对象
     * @param field       当前属性
     * @param object      原始对象
     * @param annotations 注解信息
     * @param context     上下文信息
     * @return the object
     */
    public static <T> T on(Object object, Annotation[] annotations, Context context, String field) {
        return (T) new Verified(object, annotations, context, field).access();
    }

    /**
     * 是否为校验器注解
     *
     * @param annotation 注解
     * @return the boolean
     */
    public static boolean isAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return null != annotationType.getAnnotation(Complex.class);
    }

    /**
     * 判断校验是否为数组
     *
     * @param object 当前校验组
     * @return true/false
     */
    public static boolean isArray(Object object) {
        return object.getClass().isArray();
    }

    /**
     * 判断校验是否为集合
     *
     * @param object 当前校验组
     * @return true/false
     */
    public static boolean isCollection(Object object) {
        return Collection.class.isAssignableFrom(object.getClass());
    }

    /**
     * 判断校验是否为Map
     *
     * @param object 当前校验组
     * @return true/false
     */
    public static boolean isMap(Object object) {
        return Map.class.isAssignableFrom(object.getClass());
    }

    /**
     * 判断校验组是否符合当前全局校验组范围
     *
     * @param group 当前校验组
     * @param list  校验环境中校验组属性
     * @return true：当前校验组中为空,或任意一个组环境存在于校验环境中
     */
    public static boolean isGroup(String[] group, List<String> list) {
        if (null == group || group.length == 0) {
            return true;
        } else {
            if (null == list || list.isEmpty()) {
                return false;
            } else {
                return Arrays.stream(group).anyMatch(neededGroup -> list.stream().anyMatch(neededGroup::equals));
            }
        }
    }

    /**
     * 解析校验异常
     *
     * @param material 校验器属性
     * @param context  校验上下文
     * @return 最终确定的错误码
     */
    /**
     * 根据校验规则和上下文解析并创建校验异常
     *
     * @param material 校验规则材料，包含校验器、错误信息等配置
     * @param context  校验上下文，包含异常类、错误码等运行时信息
     * @return 根据规则和上下文创建的校验异常实例
     * @throws NoSuchException 当自定义异常类不符合要求时抛出
     */
    public static ValidateException resolve(Material material, Context context) {
        // 1. 确定要使用的异常类：优先使用上下文中的异常类，如果为null则使用材料中的异常类
        Class<? extends ValidateException> exceptionClass = context.getException();
        if (exceptionClass == null) {
            exceptionClass = material.getException();
        }

        // 2. 确定错误码：如果材料中的错误码是默认错误码，则使用材料中的错误码；否则使用上下文中的错误码
        String errorCode;
        if (Builder.DEFAULT_ERRCODE.equals(material.getErrcode())) {
            errorCode = material.getErrcode();
        } else {
            errorCode = context.getErrcode();
        }

        // 3. 构建错误信息对象：使用错误码和格式化后的错误消息
        Errors errors = ErrorRegistry.builder().key(errorCode).value(material.getFormatted()).build();

        // 4. 创建异常实例：如果没有指定异常类，使用默认异常；否则通过反射创建自定义异常
        if (exceptionClass == null) {
            return new ValidateException(errors);
        } else {
            try {
                Constructor<? extends ValidateException> constructor = exceptionClass.getConstructor(String.class,
                        String.class);
                return constructor.newInstance(errors.getKey(), errors.getKey());
            } catch (NoSuchMethodException e) {
                throw new NoSuchException(
                        "Illegal custom validation exception, no specified constructor: constructor(String, int)");
            } catch (IllegalAccessException e) {
                throw new NoSuchException("Unable to access custom validation exception constructor");
            } catch (InstantiationException e) {
                throw new NoSuchException("Failed to instantiate custom validation exception via reflection");
            } catch (InvocationTargetException e) {
                throw new NoSuchException("Failed to instantiate custom validation exception via reflection");
            }
        }
    }

    /**
     * 获取当前对象的注解信息
     *
     * @param clazz 当前对象
     * @return list
     */
    public static List<Annotation> getAnnotation(Class<?> clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        return Arrays.stream(annotations).filter(Provider::isAnnotation).collect(Collectors.toList());
    }

}
