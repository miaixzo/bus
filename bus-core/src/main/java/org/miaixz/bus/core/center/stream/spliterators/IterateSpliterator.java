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
package org.miaixz.bus.core.center.stream.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * 无限有序流的Spliterator
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class IterateSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

    private final T seed;
    private final Predicate<? super T> hasNext;
    private final UnaryOperator<T> next;
    private T prev;
    private boolean started;
    private boolean finished;

    /**
     * 构造
     *
     * @param seed    初始值
     * @param hasNext 是否有下一个断言
     * @param next    下一个值生产者
     * @return this
     */
    IterateSpliterator(final T seed, final Predicate<? super T> hasNext, final UnaryOperator<T> next) {
        super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        this.seed = seed;
        this.hasNext = hasNext;
        this.next = next;
    }

    /**
     * 创建
     *
     * @param seed    初始值
     * @param hasNext 是否有下一个断言
     * @param next    下一个值生产者
     * @param <T>     流元素类型
     * @return this
     */
    public static <T> IterateSpliterator<T> of(final T seed, final Predicate<? super T> hasNext,
            final UnaryOperator<T> next) {
        return new IterateSpliterator<>(seed, hasNext, next);
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (finished) {
            return false;
        }
        final T t;
        if (started) {
            t = next.apply(prev);
        } else {
            t = seed;
            started = true;
        }
        if (!hasNext.test(t)) {
            prev = null;
            finished = true;
            return false;
        }
        prev = t;
        action.accept(prev);
        return true;
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (finished) {
            return;
        }
        finished = true;
        T t = started ? next.apply(prev) : seed;
        prev = null;
        while (hasNext.test(t)) {
            action.accept(t);
            t = next.apply(t);
        }
    }

}
