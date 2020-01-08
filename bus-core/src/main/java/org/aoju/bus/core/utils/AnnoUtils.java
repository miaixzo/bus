/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.annotation.Element;

import java.lang.annotation.*;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 注解工具类
 * 快速获取注解对象、注解值等工具封装
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class AnnoUtils {

    /**
     * 将指定的被注解的元素转换为组合注解元素
     *
     * @param annotationEle 注解元素
     * @return 组合注解元素
     */
    public static Element toCombination(AnnotatedElement annotationEle) {
        if (annotationEle instanceof Element) {
            return (Element) annotationEle;
        }
        return new Element(annotationEle);
    }

    /**
     * 获取指定注解
     *
     * @param annotationEle {@link AnnotatedElement},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param isCombination boolean
     * @return 注解对象
     */
    public static Annotation[] getAnnotations(AnnotatedElement annotationEle, boolean isCombination) {
        return (null == annotationEle) ? null : (isCombination ? toCombination(annotationEle) : annotationEle).getAnnotations();
    }

    /**
     * 获取指定注解
     *
     * @param <A>            注解类型
     * @param annotationEle  {@link AnnotatedElement},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     */
    public static <A extends Annotation> A getAnnotation(AnnotatedElement annotationEle, Class<A> annotationType) {
        return (null == annotationEle) ? null : toCombination(annotationEle).getAnnotation(annotationType);
    }

    /**
     * 获取指定注解默认值
     * 如果无指定的属性方法返回null
     *
     * @param <T>            注解值类型
     * @param annotationEle  {@link AccessibleObject},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     */
    public static <T> T getAnnotationValue(AnnotatedElement annotationEle, Class<? extends Annotation> annotationType) {
        return getAnnotationValue(annotationEle, annotationType, "value");
    }

    /**
     * 获取指定注解属性的值<br>
     * 如果无指定的属性方法返回null
     *
     * @param <T>            注解值类型
     * @param annotationEle  {@link AccessibleObject},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @param propertyName   属性名,例如注解中定义了name()方法,则 此处传入name
     * @return 注解对象
     */
    public static <T> T getAnnotationValue(AnnotatedElement annotationEle, Class<? extends Annotation> annotationType, String propertyName) {
        final Annotation annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }

        final Method method = ReflectUtils.getMethodOfObj(annotation, propertyName);
        if (null == method) {
            return null;
        }
        return ReflectUtils.invoke(annotation, method);
    }

    /**
     * 获取指定注解中所有属性值
     * 如果无指定的属性方法返回null
     *
     * @param annotationEle  {@link AnnotatedElement},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     */
    public static Map<String, Object> getAnnotationValueMap(AnnotatedElement annotationEle, Class<? extends Annotation> annotationType) {
        final Annotation annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }

        final Method[] methods = ReflectUtils.getMethods(annotationType, t -> {
            if (ArrayUtils.isEmpty(t.getParameterTypes())) {
                final String name = t.getName();
                return (false == "hashCode".equals(name))
                        && (false == "toString".equals(name))
                        && (false == "annotationType".equals(name));
            }
            return false;
        });

        final HashMap<String, Object> result = new HashMap<>(methods.length, 1);
        for (Method method : methods) {
            result.put(method.getName(), ReflectUtils.invoke(annotation, method));
        }
        return result;
    }

    /**
     * 获取注解类的保留时间,可选值 SOURCE（源码时）,CLASS（编译时）,RUNTIME（运行时）,默认为 CLASS
     *
     * @param annotationType 注解类
     * @return 保留时间枚举
     */
    public static RetentionPolicy getRetentionPolicy(Class<? extends Annotation> annotationType) {
        final Retention retention = annotationType.getAnnotation(Retention.class);
        if (null == retention) {
            return RetentionPolicy.CLASS;
        }
        return retention.value();
    }

    /**
     * 获取注解类可以用来修饰哪些程序元素,如 TYPE, METHOD, CONSTRUCTOR, FIELD, PARAMETER 等
     *
     * @param annotationType 注解类
     * @return 注解修饰的程序元素数组
     */
    public static ElementType[] getTargetType(Class<? extends Annotation> annotationType) {
        final Target target = annotationType.getAnnotation(Target.class);
        if (null == target) {
            return new ElementType[]{ElementType.TYPE,
                    ElementType.FIELD,
                    ElementType.METHOD,
                    ElementType.PARAMETER,
                    ElementType.CONSTRUCTOR,
                    ElementType.LOCAL_VARIABLE,
                    ElementType.ANNOTATION_TYPE,
                    ElementType.PACKAGE
            };
        }
        return target.value();
    }

    /**
     * 是否会保存到 Javadoc 文档中
     *
     * @param annotationType 注解类
     * @return 是否会保存到 Javadoc 文档中
     */
    public static boolean isDocumented(Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Documented.class);
    }

    /**
     * 是否可以被继承,默认为 false
     *
     * @param annotationType 注解类
     * @return 是否会保存到 Javadoc 文档中
     */
    public static boolean isInherited(Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Inherited.class);
    }

}
