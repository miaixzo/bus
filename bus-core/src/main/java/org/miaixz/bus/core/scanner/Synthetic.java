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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * 表示基于特定规则聚合的一组注解对象
 *
 * <p>合成注解一般被用于处理类层级结果中具有直接或间接关联的注解对象，
 * 当实例被创建时，会获取到这些注解对象，并使用{@link SynthesizedSelector}对类型相同的注解进行过滤，
 * 并最终得到类型不重复的有效注解对象这些有效注解将被包装为{@link Synthesized}，
 * 然后最终用于“合成”一个{@link Synthesized}
 *
 * <p>合成注解可以作为一个特殊的{@link Annotation}或者{@link AnnotatedElement}，
 * 当调用{@link Annotation}的方法时，应当返回当前实例本身的有效信息，
 * 而当调用{@link AnnotatedElement}的方法时，应当返回用于合成该对象的相关注解的信息
 *
 * <p>合成注解允许通过{@link #syntheticAnnotation(Class)}合成一个指定的注解对象，
 * 该方法返回的注解对象可能是原始的注解对象，也有可能通过动态代理的方式生成，
 * 该对象实例的属性不一定来自对象本身，而是来自于经过{@link SynthesizedProcessor}
 * 处理后的、用于合成当前实例的全部关联注解的相关属性
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Synthetic extends Annotation, AnnotatedElement {

    /**
     * 基于指定根注解，构建包括其元注解在内的合成注解
     *
     * @param rootAnnotation 根注解
     * @param <T>            注解类型
     * @return 合成注解
     */
    static <T extends Annotation> Synthetic of(T rootAnnotation) {
        return new SyntheticMeta(rootAnnotation);
    }

    /**
     * 获取合成注解选择器
     *
     * @return 合成注解选择器
     */
    SynthesizedSelector getAnnotationSelector();

    /**
     * 获取合成注解属性处理器
     *
     * @return 合成注解属性处理器
     */
    SynthesizedProcessor getAttributeProcessor();

    /**
     * 获取已合成的注解
     *
     * @param annotationType 注解类型
     * @return 已合成的注解
     */
    Synthesized getSynthesizedAnnotation(Class<?> annotationType);

    /**
     * 获取当前的注解类型
     *
     * @return 注解类型
     */
    @Override
    default Class<? extends Annotation> annotationType() {
        return this.getClass();
    }

    /**
     * 获取指定注解对象
     *
     * @param annotationType 注解类型
     * @param <T>            注解类型
     * @return 注解对象
     */
    @Override
    <T extends Annotation> T getAnnotation(Class<T> annotationType);

    /**
     * 是否存在指定注解
     *
     * @param annotationType 注解类型
     * @return 是否
     */
    @Override
    boolean isAnnotationPresent(Class<? extends Annotation> annotationType);

    /**
     * 获取全部注解
     *
     * @return 注解对象
     */
    @Override
    Annotation[] getAnnotations();

    /**
     * 获取合成注解
     *
     * @param annotationType 注解类型
     * @param <T>            注解类型
     * @return 类型
     */
    <T extends Annotation> T syntheticAnnotation(Class<T> annotationType);

    /**
     * 获取属性值
     *
     * @param attributeName 属性名称
     * @param attributeType 属性类型
     * @return 属性值
     */
    Object getAttribute(String attributeName, Class<?> attributeType);

}
