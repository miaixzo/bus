/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.mutable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.Optional;

/**
 * 提供可变值类型接口
 *
 * <p>
 * 相较于{@link Optional}或{@link java.util.Optional}，该所有实现类中的方法都<b>不区分值是否为{@code null}</b>， 因此在使用前需要自行判断值是否为{@code null}，
 * 确保不会因为{@code null}值而抛出{@link NullPointerException}的情况。
 * </p>
 *
 * @param <T> 值得类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Mutable<T> {

    /**
     * 创建一个{@link MutableBool}对象
     *
     * @param value 值
     * @return {@link MutableBool}
     */
    static MutableBool of(final boolean value) {
        return new MutableBool(value);
    }

    /**
     * 创建一个{@link MutableByte}对象
     *
     * @param value 值
     * @return {@link MutableByte}
     */
    static MutableByte of(final byte value) {
        return new MutableByte(value);
    }

    /**
     * 创建一个{@link MutableFloat}对象
     *
     * @param value 值
     * @return {@link MutableFloat}
     */
    static MutableFloat of(final float value) {
        return new MutableFloat(value);
    }

    /**
     * 创建一个{@link MutableInt}对象
     *
     * @param value 值
     * @return {@link MutableInt}
     */
    static MutableInt of(final int value) {
        return new MutableInt(value);
    }

    /**
     * 创建一个{@link MutableLong}对象
     *
     * @param value 值
     * @return {@link MutableLong}
     */
    static MutableLong of(final long value) {
        return new MutableLong(value);
    }

    /**
     * 创建一个{@link MutableDouble}对象
     *
     * @param value 值
     * @return {@link MutableDouble}
     */
    static MutableDouble of(final double value) {
        return new MutableDouble(value);
    }

    /**
     * 创建一个{@link MutableShort}对象
     *
     * @param value 值
     * @return {@link MutableShort}
     */
    static MutableShort of(final short value) {
        return new MutableShort(value);
    }

    /**
     * 创建一个{@link MutableObject}对象
     *
     * @param <T>   值类型
     * @param value 值
     * @return {@link MutableObject}
     */
    static <T> MutableObject<T> of(final T value) {
        return new MutableObject<>(value);
    }

    /**
     * 获得原始值
     *
     * @return 原始值
     */
    T get();

    /**
     * 设置值
     *
     * @param value 值
     */
    void set(T value);

    /**
     * 根据操作修改值
     *
     * @param operator 操作
     * @return 值
     */
    default Mutable<T> map(final UnaryOperator<T> operator) {
        set(operator.apply(get()));
        return this;
    }

    /**
     * 检查并操作值
     *
     * @param consumer 操作
     * @return 当前对象
     */
    default Mutable<T> peek(final Consumer<T> consumer) {
        consumer.accept(get());
        return this;
    }

    /**
     * 检查值是否满足条件
     *
     * @param predicate 条件
     * @return 是否满足条件
     */
    default boolean test(final Predicate<T> predicate) {
        return predicate.test(get());
    }

    /**
     * 获取值，并将值转换为{@link Optional}
     *
     * @return {@link Optional}
     */
    default Optional<T> toOpt() {
        return to(Optional::ofNullable);
    }

    /**
     * 获取值，并将值转换为指定类型。 注意，值为null时，转换函数依然会被调用。
     *
     * @param function 转换函数
     * @param <R>      转换后的类型
     * @return 转换后的值
     */
    default <R> R to(final Function<T, R> function) {
        Objects.requireNonNull(function);
        return function.apply(get());
    }

}
