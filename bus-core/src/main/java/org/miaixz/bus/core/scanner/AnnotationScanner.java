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
package org.miaixz.bus.core.scanner;

import org.miaixz.bus.core.toolkit.AnnoKit;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 注解扫描器，用于从支持的可注解元素上获取所需注解
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AnnotationScanner {

    /**
     * 给定一组扫描器，使用第一个支持处理该类型元素的扫描器获取元素上可能存在的注解
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param scanners     注解扫描器
     * @return 注解
     */
    static List<Annotation> scanByAnySupported(AnnotatedElement annotatedEle, AnnotationScanner... scanners) {
        if (ObjectKit.isNull(annotatedEle) && ArrayKit.isNotEmpty(scanners)) {
            return Collections.emptyList();
        }
        return Stream.of(scanners)
                .filter(scanner -> scanner.support(annotatedEle))
                .findFirst()
                .map(scanner -> scanner.getAnnotations(annotatedEle))
                .orElseGet(Collections::emptyList);
    }

    /**
     * 根据指定的扫描器，扫描元素上可能存在的注解
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param scanners     注解扫描器
     * @return 注解
     */
    static List<Annotation> scanByAllScanner(AnnotatedElement annotatedEle, AnnotationScanner... scanners) {
        if (ObjectKit.isNull(annotatedEle) && ArrayKit.isNotEmpty(scanners)) {
            return Collections.emptyList();
        }
        return Stream.of(scanners)
                .map(scanner -> scanner.getIfSupport(annotatedEle))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 判断是否支持扫描该注解元素
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return 是否支持扫描该注解元素
     */
    default boolean support(AnnotatedElement annotatedEle) {
        return false;
    }

    /**
     * 若{@link #support(AnnotatedElement)}返回{@code true}，
     * 则调用并返回{@link #getAnnotations(AnnotatedElement)}结果，
     * 否则返回{@link Collections#emptyList()}
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return 注解
     */
    default List<Annotation> getIfSupport(AnnotatedElement annotatedEle) {
        return support(annotatedEle) ? getAnnotations(annotatedEle) : Collections.emptyList();
    }

    /**
     * 获取注解元素上的全部注解。调用该方法前，需要确保调用{@link #support(AnnotatedElement)}返回为true
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return 注解
     */
    default List<Annotation> getAnnotations(AnnotatedElement annotatedEle) {
        final List<Annotation> annotations = new ArrayList<>();
        scan((index, annotation) -> annotations.add(annotation), annotatedEle, null);
        return annotations;
    }

    /**
     * 扫描注解元素的层级结构（若存在），然后对获取到的注解和注解对应的层级索引进行处理。
     * 调用该方法前，需要确保调用{@link #support(AnnotatedElement)}返回为true
     *
     * @param consumer     对获取到的注解和注解对应的层级索引的处理
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param filter       注解过滤器，无法通过过滤器的注解不会被处理。该参数允许为空。
     */
    default void scan(BiConsumer<Integer, Annotation> consumer, AnnotatedElement annotatedEle, Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, annotation -> true);
        for (final Annotation annotation : annotatedEle.getAnnotations()) {
            if (AnnoKit.isNotJdkMateAnnotation(annotation.annotationType()) && filter.test(annotation)) {
                consumer.accept(0, annotation);
            }
        }
    }

    /**
     * 若{@link #support(AnnotatedElement)}返回{@code true}，则调用{@link #scan(BiConsumer, AnnotatedElement, Predicate)}
     *
     * @param consumer     对获取到的注解和注解对应的层级索引的处理
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param filter       注解过滤器，无法通过过滤器的注解不会被处理。该参数允许为空。
     */
    default void scanIfSupport(BiConsumer<Integer, Annotation> consumer, AnnotatedElement annotatedEle, Predicate<Annotation> filter) {
        if (support(annotatedEle)) {
            scan(consumer, annotatedEle, filter);
        }
    }

}
