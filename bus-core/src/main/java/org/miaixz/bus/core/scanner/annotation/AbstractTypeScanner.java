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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.scanner.AnnotationScanner;
import org.miaixz.bus.core.toolkit.AnnoKit;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * 为需要从类的层级结构中获取注解的{@link AnnotationScanner}提供基本实现
 *
 * @param <T> 对象泛型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractTypeScanner<T extends AbstractTypeScanner<T>> implements AnnotationScanner {

    /**
     * 排除的类型，以上类型及其树结构将直接不被查找
     */
    private final Set<Class<?>> excludeTypes;
    /**
     * 转换器
     */
    private final List<UnaryOperator<Class<?>>> converters;
    /**
     * 当前实例
     */
    private final T typedThis;
    /**
     * 是否允许扫描父接口
     */
    private boolean includeInterfaces;
    /**
     * 过滤器，若类型无法通过该过滤器，则该类型及其树结构将直接不被查找
     */
    private Predicate<Class<?>> filter;
    /**
     * 是否有转换器
     */
    private boolean hasConverters;
    /**
     * 是否允许扫描父类
     */
    // FIXME rename includeSuperClass
    private boolean includeSupperClass;

    /**
     * 构造一个类注解扫描器
     *
     * @param includeSupperClass 是否允许扫描父类
     * @param includeInterfaces  是否允许扫描父接口
     * @param filter             过滤器
     * @param excludeTypes       不包含的类型
     */
    protected AbstractTypeScanner(boolean includeSupperClass, boolean includeInterfaces, Predicate<Class<?>> filter, Set<Class<?>> excludeTypes) {
        Assert.notNull(filter, "filter must not null");
        Assert.notNull(excludeTypes, "excludeTypes must not null");
        this.includeSupperClass = includeSupperClass;
        this.includeInterfaces = includeInterfaces;
        this.filter = filter;
        this.excludeTypes = excludeTypes;
        this.converters = new ArrayList<>();
        this.typedThis = (T) this;
    }

    /**
     * 是否允许扫描父类
     *
     * @return 是否允许扫描父类
     */
    public boolean isIncludeSupperClass() {
        return includeSupperClass;
    }

    /**
     * 是否允许扫描父类
     *
     * @param includeSupperClass 是否
     * @return 当前实例
     */
    protected T setIncludeSupperClass(boolean includeSupperClass) {
        this.includeSupperClass = includeSupperClass;
        return typedThis;
    }

    /**
     * 是否允许扫描父接口
     *
     * @return 是否允许扫描父接口
     */
    public boolean isIncludeInterfaces() {
        return includeInterfaces;
    }

    /**
     * 是否允许扫描父接口
     *
     * @param includeInterfaces 是否
     * @return 当前实例
     */
    protected T setIncludeInterfaces(boolean includeInterfaces) {
        this.includeInterfaces = includeInterfaces;
        return typedThis;
    }

    /**
     * 设置过滤器，若类型无法通过该过滤器，则该类型及其树结构将直接不被查找
     *
     * @param filter 过滤器
     * @return 当前实例
     */
    public T setFilter(Predicate<Class<?>> filter) {
        Assert.notNull(filter, "filter must not null");
        this.filter = filter;
        return typedThis;
    }

    /**
     * 添加不扫描的类型，该类型及其树结构将直接不被查找
     *
     * @param excludeTypes 不扫描的类型
     * @return 当前实例
     */
    public T addExcludeTypes(Class<?>... excludeTypes) {
        CollKit.addAll(this.excludeTypes, excludeTypes);
        return typedThis;
    }

    /**
     * 添加转换器
     *
     * @param converter 转换器
     * @return 当前实例
     * @see JdkProxyClassConverter
     */
    public T addConverters(UnaryOperator<Class<?>> converter) {
        Assert.notNull(converter, "converter must not null");
        this.converters.add(converter);
        if (!this.hasConverters) {
            this.hasConverters = CollKit.isNotEmpty(this.converters);
        }
        return typedThis;
    }

    /**
     * 则根据广度优先递归扫描类的层级结构，并对层级结构中类/接口声明的层级索引和它们声明的注解对象进行处理
     *
     * @param consumer     对获取到的注解和注解对应的层级索引的处理
     * @param annotatedEle 注解元素
     * @param filter       注解过滤器，无法通过过滤器的注解不会被处理。该参数允许为空。
     */
    @Override
    public void scan(BiConsumer<Integer, Annotation> consumer, AnnotatedElement annotatedEle, Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, annotation -> true);
        final Class<?> sourceClass = getClassFormAnnotatedElement(annotatedEle);
        final Deque<List<Class<?>>> classDeque = CollKit.newLinkedList(CollKit.newArrayList(sourceClass));
        final Set<Class<?>> accessedTypes = new LinkedHashSet<>();
        int index = 0;
        while (!classDeque.isEmpty()) {
            final List<Class<?>> currClassQueue = classDeque.removeFirst();
            final List<Class<?>> nextClassQueue = new ArrayList<>();
            for (Class<?> targetClass : currClassQueue) {
                targetClass = convert(targetClass);
                // 过滤不需要处理的类
                if (isNotNeedProcess(accessedTypes, targetClass)) {
                    continue;
                }
                accessedTypes.add(targetClass);
                // 扫描父类
                scanSuperClassIfNecessary(nextClassQueue, targetClass);
                // 扫描接口
                scanInterfaceIfNecessary(nextClassQueue, targetClass);
                // 处理层级索引和注解
                final Annotation[] targetAnnotations = getAnnotationsFromTargetClass(annotatedEle, index, targetClass);
                for (final Annotation annotation : targetAnnotations) {
                    if (AnnoKit.isNotJdkMateAnnotation(annotation.annotationType()) || filter.test(annotation)) {
                        consumer.accept(index, annotation);
                    }
                }
                index++;
            }
            if (CollKit.isNotEmpty(nextClassQueue)) {
                classDeque.addLast(nextClassQueue);
            }
        }
    }

    /**
     * 从要搜索的注解元素上获得要递归的类型
     *
     * @param annotatedElement 注解元素
     * @return 要递归的类型
     */
    protected abstract Class<?> getClassFormAnnotatedElement(AnnotatedElement annotatedElement);

    /**
     * 从类上获取最终所需的目标注解
     *
     * @param source      最初的注解元素
     * @param index       类的层级索引
     * @param targetClass 类
     * @return 最终所需的目标注解
     */
    protected abstract Annotation[] getAnnotationsFromTargetClass(AnnotatedElement source, int index, Class<?> targetClass);

    /**
     * 当前类是否不需要处理
     *
     * @param accessedTypes 访问类型
     * @param targetClass   目标类型
     * @return the boolean
     */
    protected boolean isNotNeedProcess(Set<Class<?>> accessedTypes, Class<?> targetClass) {
        return ObjectKit.isNull(targetClass)
                || accessedTypes.contains(targetClass)
                || excludeTypes.contains(targetClass)
                || filter.negate().test(targetClass);
    }

    /**
     * 若{@link #includeInterfaces}为{@code true}，则将目标类的父接口也添加到nextClasses
     *
     * @param nextClasses 下一个类集合
     * @param targetClass 目标类型
     */
    protected void scanInterfaceIfNecessary(List<Class<?>> nextClasses, Class<?> targetClass) {
        if (includeInterfaces) {
            final Class<?>[] interfaces = targetClass.getInterfaces();
            if (ArrayKit.isNotEmpty(interfaces)) {
                CollKit.addAll(nextClasses, interfaces);
            }
        }
    }

    /**
     * 若{@link #includeSupperClass}为{@code true}，则将目标类的父类也添加到nextClasses
     *
     * @param nextClassQueue 下一个类队列
     * @param targetClass    目标类型
     */
    protected void scanSuperClassIfNecessary(List<Class<?>> nextClassQueue, Class<?> targetClass) {
        if (includeSupperClass) {
            final Class<?> superClass = targetClass.getSuperclass();
            if (!ObjectKit.equals(superClass, Object.class) && ObjectKit.isNotNull(superClass)) {
                nextClassQueue.add(superClass);
            }
        }
    }

    /**
     * 若存在转换器，则使用转换器对目标类进行转换
     *
     * @param target 目标类
     * @return 转换后的类
     */
    protected Class<?> convert(Class<?> target) {
        if (hasConverters) {
            for (final UnaryOperator<Class<?>> converter : converters) {
                target = converter.apply(target);
            }
        }
        return target;
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
