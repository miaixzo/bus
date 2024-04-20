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
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * 扫描{@link Field}上的注解
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldScanner implements AnnotationScanner {

    /**
     * 判断是否支持扫描该注解元素，仅当注解元素是{@link Field}时返回{@code true}
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return 是否支持扫描该注解元素
     */
    @Override
    public boolean support(AnnotatedElement annotatedEle) {
        return annotatedEle instanceof Field;
    }

    /**
     * 扫描{@link Field}上直接声明的注解，调用前需要确保调用{@link #support(AnnotatedElement)}返回为true
     *
     * @param consumer     对获取到的注解和注解对应的层级索引的处理
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param filter       注解过滤器，无法通过过滤器的注解不会被处理。该参数允许为空。
     */
    @Override
    public void scan(BiConsumer<Integer, Annotation> consumer, AnnotatedElement annotatedEle, Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, annotation -> true);
        for (final Annotation annotation : annotatedEle.getAnnotations()) {
            if (AnnoKit.isNotJdkMateAnnotation(annotation.annotationType()) && filter.test(annotation)) {
                consumer.accept(0, annotation);
            }
        }
    }

}
