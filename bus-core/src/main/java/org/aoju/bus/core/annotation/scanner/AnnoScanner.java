/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.annotation.scanner;

import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.ObjectKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 注解扫描器，用于从支持的可注解元素上获取所需注解
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AnnoScanner {

    /**
     * 给定一组扫描器，使用第一个支持处理该类型元素的扫描器获取元素上可能存在的注解
     *
     * @param annotatedElement 可注解元素
     * @param scanners         注解扫描器
     * @return 注解
     */
    static List<Annotation> scanByAnySupported(AnnotatedElement annotatedElement, AnnoScanner... scanners) {
        if (ObjectKit.isNull(annotatedElement) && ArrayKit.isNotEmpty(scanners)) {
            return Collections.emptyList();
        }
        return Stream.of(scanners)
                .filter(scanner -> scanner.support(annotatedElement))
                .findFirst()
                .map(scanner -> scanner.getAnnotations(annotatedElement))
                .orElseGet(Collections::emptyList);
    }

    /**
     * 根据指定的扫描器，扫描元素上可能存在的注解
     *
     * @param annotatedElement 可注解元素
     * @param scanners         注解扫描器
     * @return 注解
     */
    static List<Annotation> scanByAllScanner(AnnotatedElement annotatedElement, AnnoScanner... scanners) {
        if (ObjectKit.isNull(annotatedElement) && ArrayKit.isNotEmpty(scanners)) {
            return Collections.emptyList();
        }
        return Stream.of(scanners)
                .map(scanner -> scanner.getIfSupport(annotatedElement))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 是否支持扫描该可注解元素
     *
     * @param annotatedElement 可注解元素
     * @return 是否支持扫描该可注解元素
     */
    default boolean support(AnnotatedElement annotatedElement) {
        return false;
    }

    /**
     * 获取可注解元素上的全部注解。调用该方法前，需要确保调用{@link #support(AnnotatedElement)}返回为true
     *
     * @param annotatedElement 可注解元素
     * @return 元素上的注解
     */
    List<Annotation> getAnnotations(AnnotatedElement annotatedElement);

    /**
     * 若{@link #support(AnnotatedElement)}返回{@code true}，
     * 则调用并返回{@link #getAnnotations(AnnotatedElement)}结果，
     * 否则返回{@link Collections#emptyList()}
     *
     * @param annotatedElement 元素
     * @return 元素上的注解
     */
    default List<Annotation> getIfSupport(AnnotatedElement annotatedElement) {
        return support(annotatedElement) ? getAnnotations(annotatedElement) : Collections.emptyList();
    }

}
