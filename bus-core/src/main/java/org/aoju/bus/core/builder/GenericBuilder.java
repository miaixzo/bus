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
package org.aoju.bus.core.builder;

import org.aoju.bus.core.lang.function.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 通用Builder
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenericBuilder<T> implements Builder<T> {

    private static final long serialVersionUID = 1L;

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
     * 通过1参数实例化器创建GenericBuilder
     *
     * @param instant 实例化器
     * @param p1      参数一
     * @param <T>     目标类型
     * @param <P1>    参数一类型
     * @return GenericBuilder对象
     */
    public static <T, P1> GenericBuilder<T> of(Supplier1<T, P1> instant, P1 p1) {
        return of(instant.toSupplier(p1));
    }

    /**
     * 通过2参数实例化器创建GenericBuilder
     *
     * @param instant 实例化器
     * @param p1      参数一
     * @param p2      参数二
     * @param <T>     目标类型
     * @param <P1>    参数一类型
     * @param <P2>    参数二类型
     * @return GenericBuilder对象
     */
    public static <T, P1, P2> GenericBuilder<T> of(Supplier2<T, P1, P2> instant, P1 p1, P2 p2) {
        return of(instant.toSupplier(p1, p2));
    }

    /**
     * 通过3参数实例化器创建GenericBuilder
     *
     * @param instant 实例化器
     * @param p1      参数一
     * @param p2      参数二
     * @param p3      参数三
     * @param <T>     目标类型
     * @param <P1>    参数一类型
     * @param <P2>    参数二类型
     * @param <P3>    参数三类型
     * @return GenericBuilder对象
     */
    public static <T, P1, P2, P3> GenericBuilder<T> of(Supplier3<T, P1, P2, P3> instant, P1 p1, P2 p2, P3 p3) {
        return of(instant.toSupplier(p1, p2, p3));
    }

    /**
     * 通过4参数实例化器创建GenericBuilder
     *
     * @param instant 实例化器
     * @param p1      参数一
     * @param p2      参数二
     * @param p3      参数三
     * @param p4      参数四
     * @param <T>     目标类型
     * @param <P1>    参数一类型
     * @param <P2>    参数二类型
     * @param <P3>    参数三类型
     * @param <P4>    参数四类型
     * @return GenericBuilder对象
     */
    public static <T, P1, P2, P3, P4> GenericBuilder<T> of(Supplier4<T, P1, P2, P3, P4> instant, P1 p1, P2 p2, P3 p3, P4 p4) {
        return of(instant.toSupplier(p1, p2, p3, p4));
    }

    /**
     * 通过5参数实例化器创建GenericBuilder
     *
     * @param instant 实例化器
     * @param p1      参数一
     * @param p2      参数二
     * @param p3      参数三
     * @param p4      参数四
     * @param p5      参数五
     * @param <T>     目标类型
     * @param <P1>    参数一类型
     * @param <P2>    参数二类型
     * @param <P3>    参数三类型
     * @param <P4>    参数四类型
     * @param <P5>    参数五类型
     * @return GenericBuilder对象
     */
    public static <T, P1, P2, P3, P4, P5> GenericBuilder<T> of(Supplier5<T, P1, P2, P3, P4, P5> instant, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) {
        return of(instant.toSupplier(p1, p2, p3, p4, p5));
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
     * @param consumer 1参数Consumer
     * @param p1       参数一
     * @param <P1>     参数一类型
     * @return GenericBuilder对象
     */
    public <P1> GenericBuilder<T> with(BiConsumer<T, P1> consumer, P1 p1) {
        modifiers.add(instant -> consumer.accept(instant, p1));
        return this;
    }

    /**
     * 调用2参数方法
     *
     * @param consumer 2参数Consumer
     * @param p1       参数一
     * @param p2       参数二
     * @param <P1>     参数一类型
     * @param <P2>     参数二类型
     * @return GenericBuilder对象
     */
    public <P1, P2> GenericBuilder<T> with(Consumer3<T, P1, P2> consumer, P1 p1, P2 p2) {
        modifiers.add(instant -> consumer.accept(instant, p1, p2));
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
