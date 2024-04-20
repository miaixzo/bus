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
import org.miaixz.bus.core.toolkit.AnnoKit;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 扫描注解类上存在的注解，支持处理枚举实例或枚举类型
 * 需要注意，当待解析是枚举类时，有可能与{@link TypeScanner}冲突
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MetaScanner implements AnnotationScanner {

    /**
     * 获取当前注解的元注解后，是否继续递归扫描的元注解的元注解
     */
    private final boolean includeSupperMetaAnnotation;

    /**
     * 构造一个元注解扫描器
     *
     * @param includeSupperMetaAnnotation 获取当前注解的元注解后，是否继续递归扫描的元注解的元注解
     */
    public MetaScanner(boolean includeSupperMetaAnnotation) {
        this.includeSupperMetaAnnotation = includeSupperMetaAnnotation;
    }

    /**
     * 构造一个元注解扫描器，默认在扫描当前注解上的元注解后，并继续递归扫描元注解
     */
    public MetaScanner() {
        this(true);
    }

    /**
     * 判断是否支持扫描该注解元素，仅当注解元素是{@link Annotation}接口的子类{@link Class}时返回{@code true}
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return 是否支持扫描该注解元素
     */
    @Override
    public boolean support(AnnotatedElement annotatedEle) {
        return (annotatedEle instanceof Class && ClassKit.isAssignable(Annotation.class, (Class<?>) annotatedEle));
    }

    /**
     * 获取注解元素上的全部注解。调用该方法前，需要确保调用{@link #support(AnnotatedElement)}返回为true
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return 注解
     */
    @Override
    public List<Annotation> getAnnotations(AnnotatedElement annotatedEle) {
        final List<Annotation> annotations = new ArrayList<>();
        scan(
                (index, annotation) -> annotations.add(annotation), annotatedEle,
                annotation -> ObjectKit.notEquals(annotation, annotatedEle)
        );
        return annotations;
    }

    /**
     * 按广度优先扫描指定注解上的元注解，对扫描到的注解与层级索引进行操作
     *
     * @param consumer     当前层级索引与操作
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param filter       过滤器
     */
    @Override
    public void scan(BiConsumer<Integer, Annotation> consumer, AnnotatedElement annotatedEle, Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, t -> true);
        final Deque<List<Class<? extends Annotation>>> deque = CollKit.newLinkedList(CollKit.newArrayList((Class<? extends Annotation>) annotatedEle));
        int distance = 0;
        do {
            final List<Class<? extends Annotation>> annotationTypes = deque.removeFirst();
            for (final Class<? extends Annotation> type : annotationTypes) {
                final List<Annotation> metaAnnotations = Stream.of(type.getAnnotations())
                        .filter(a -> !AnnoKit.isJdkMetaAnnotation(a.annotationType()))
                        .filter(filter)
                        .collect(Collectors.toList());
                for (final Annotation metaAnnotation : metaAnnotations) {
                    consumer.accept(distance, metaAnnotation);
                }
                deque.addLast(CollKit.toList(metaAnnotations, Annotation::annotationType));
            }
            distance++;
        } while (includeSupperMetaAnnotation && !deque.isEmpty());
    }

}
