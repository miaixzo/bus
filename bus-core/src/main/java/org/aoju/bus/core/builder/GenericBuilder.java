/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 通用Builder
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class GenericBuilder<T> implements Builder<T> {

    /**
     * 实例化器
     */
    private final Supplier<T> instant;

    /**
     * 修改器列表
     */
    private final List<Consumer<T>> modifiers = new ArrayList<>();

    /**
     * 构造
     *
     * @param instant 实例化器
     */
    public GenericBuilder(Supplier<T> instant) {
        this.instant = instant;
    }

    /**
     * 通过无参数实例化器创建GenericBuilder
     *
     * @param instant 实例化器
     * @param <T>     目标类型
     * @return GenericBuilder对象
     */
    public static <T> GenericBuilder<T> of(Supplier<T> instant) {
        return new GenericBuilder<>(instant);
    }

    /**
     * 调用无参数方法
     *
     * @param consumer 无参数Consumer
     * @return GenericBuilder对象
     */
    public GenericBuilder<T> with(Consumer<T> consumer) {
        modifiers.add(consumer);
        return this;
    }

    /**
     * 调用1参数方法
     *
     * @param <P1>     参数一类型
     * @param consumer 1参数Consumer，一般为Setter方法引用
     * @param p1       参数一
     * @return GenericBuilder对象
     */
    public <P1> GenericBuilder<T> with(BiConsumer<T, P1> consumer, P1 p1) {
        modifiers.add(instant -> consumer.accept(instant, p1));
        return this;
    }

    /**
     * 构建
     *
     * @return 目标对象
     */
    @Override
    public T build() {
        T value = instant.get();
        modifiers.forEach(modifier -> modifier.accept(value));
        modifiers.clear();
        return value;
    }

}
