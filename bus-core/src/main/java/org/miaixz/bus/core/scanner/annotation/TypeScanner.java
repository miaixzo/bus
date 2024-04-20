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
package org.miaixz.bus.core.scanner.annotation;

import org.miaixz.bus.core.scanner.AnnotationScanner;
import org.miaixz.bus.core.toolkit.CollKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * 扫描{@link Class}上的注解
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TypeScanner extends AbstractTypeScanner<TypeScanner> implements AnnotationScanner {

    /**
     * 构造一个类注解扫描器
     *
     * @param includeSupperClass 是否允许扫描父类
     * @param includeInterfaces  是否允许扫描父接口
     * @param filter             过滤器
     * @param excludeTypes       不包含的类型
     */
    public TypeScanner(boolean includeSupperClass, boolean includeInterfaces, Predicate<Class<?>> filter, Set<Class<?>> excludeTypes) {
        super(includeSupperClass, includeInterfaces, filter, excludeTypes);
    }

    /**
     * 构建一个类注解扫描器，默认允许扫描指定元素的父类以及父接口
     */
    public TypeScanner() {
        this(true, true, t -> true, CollKit.newLinkedHashSet());
    }

    /**
     * 判断是否支持扫描该注解元素，仅当注解元素是{@link Class}接时返回{@code true}
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return 是否支持扫描该注解元素
     */
    @Override
    public boolean support(AnnotatedElement annotatedEle) {
        return annotatedEle instanceof Class;
    }

    /**
     * 将注解元素转为{@link Class}
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return 要递归的类型
     */
    @Override
    protected Class<?> getClassFormAnnotatedElement(AnnotatedElement annotatedEle) {
        return (Class<?>) annotatedEle;
    }

    /**
     * 获取{@link Class#getAnnotations()}
     *
     * @param source      最初的注解元素
     * @param index       类的层级索引
     * @param targetClass 类
     * @return 类上直接声明的注解
     */
    @Override
    protected Annotation[] getAnnotationsFromTargetClass(AnnotatedElement source, int index, Class<?> targetClass) {
        return targetClass.getAnnotations();
    }

    /**
     * 是否允许扫描父类
     *
     * @param includeSupperClass 是否允许扫描父类
     * @return 当前实例
     */
    @Override
    public TypeScanner setIncludeSupperClass(boolean includeSupperClass) {
        return super.setIncludeSupperClass(includeSupperClass);
    }

    /**
     * 是否允许扫描父接口
     *
     * @param includeInterfaces 是否允许扫描父类
     * @return 当前实例
     */
    @Override
    public TypeScanner setIncludeInterfaces(boolean includeInterfaces) {
        return super.setIncludeInterfaces(includeInterfaces);
    }

    /**
     * 若类型为jdk代理类，则尝试转换为原始被代理类
     */
    public static class JdkProxyClassConverter implements UnaryOperator<Class<?>> {
        @Override
        public Class<?> apply(Class<?> sourceClass) {
            return Proxy.isProxyClass(sourceClass) ? apply(sourceClass.getSuperclass()) : sourceClass;
        }
    }

}
