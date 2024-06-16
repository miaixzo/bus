/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.annotation.resolve;

import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * {@link AnnotationMapping}的基本实现，仅仅是简单包装了注解对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenericAnnotationMapping implements AnnotationMapping<Annotation> {

    private final Annotation annotation;
    private final boolean isRoot;
    private final Method[] attributes;

    /**
     * 创建一个通用注解包装类
     *
     * @param annotation 注解对象
     * @param isRoot     是否根注解
     */
    GenericAnnotationMapping(final Annotation annotation, final boolean isRoot) {
        this.annotation = Objects.requireNonNull(annotation);
        this.isRoot = isRoot;
        this.attributes = AnnoKit.getAnnotationAttributes(annotation.annotationType());
    }

    /**
     * 创建一个通用注解包装类
     *
     * @param annotation 注解对象
     * @param isRoot     是否根注解
     * @return {@code GenericAnnotationMapping}实例
     */
    public static GenericAnnotationMapping create(final Annotation annotation, final boolean isRoot) {
        return new GenericAnnotationMapping(annotation, isRoot);
    }

    /**
     * 当前注解是否为根注解
     *
     * @return 是否
     */
    @Override
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * 获取注解对象
     *
     * @return 注解对象
     */
    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * 同{@link #getAnnotation()}
     *
     * @return 注解对象
     */
    @Override
    public Annotation getResolvedAnnotation() {
        return getAnnotation();
    }

    /**
     * 总是返回{@code false}
     *
     * @return {@code false}
     */
    @Override
    public boolean isResolved() {
        return false;
    }

    /**
     * 获取注解原始属性
     *
     * @return 注解属性
     */
    @Override
    public Method[] getAttributes() {
        return attributes;
    }

    /**
     * 获取属性值
     *
     * @param attributeName 属性名称
     * @param attributeType 属性类型
     * @param <R>           返回值类型
     * @return 属性值
     */
    @Override
    public <R> R getAttributeValue(final String attributeName, final Class<R> attributeType) {
        return Stream.of(attributes)
                .filter(attribute -> CharsBacker.equals(attribute.getName(), attributeName))
                .filter(attribute -> ClassKit.isAssignable(attributeType, attribute.getReturnType()))
                .findFirst()
                .map(method -> MethodKit.invoke(annotation, method))
                .map(attributeType::cast)
                .orElse(null);
    }

    /**
     * 获取解析后的属性值
     *
     * @param attributeName 属性名称
     * @param attributeType 属性类型
     * @param <R>           返回值类型
     * @return 属性值
     */
    @Override
    public <R> R getResolvedAttributeValue(final String attributeName, final Class<R> attributeType) {
        return getAttributeValue(attributeName, attributeType);
    }

    /**
     * 比较两个实例是否相等
     *
     * @param o 对象
     * @return 是否
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GenericAnnotationMapping that = (GenericAnnotationMapping) o;
        return isRoot == that.isRoot && annotation.equals(that.annotation);
    }

    /**
     * 获取实例哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(annotation, isRoot);
    }

}
