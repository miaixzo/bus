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
package org.miaixz.bus.core.center.stream.spliterators;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 借鉴自StreamEx
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class DropWhileSpliterator<T> implements Spliterator<T> {

    private final Spliterator<T> source;
    private final Predicate<? super T> predicate;
    private boolean isFound = false;

    /**
     * 构造
     *
     * @param source    {@link Spliterator}
     * @param predicate 断言
     * @return this
     */
    private DropWhileSpliterator(final Spliterator<T> source, final Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    /**
     * 创建
     *
     * @param source    {@link Spliterator}
     * @param predicate 断言
     * @return this
     */
    public static <T> DropWhileSpliterator<T> of(final Spliterator<T> source, final Predicate<? super T> predicate) {
        return new DropWhileSpliterator<>(source, predicate);
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        boolean hasNext = true;
        // 如果 还没找到 并且 流中还有元素 继续找
        while (!isFound && hasNext) {
            hasNext = source.tryAdvance(e -> {
                if (!predicate.test(e)) {
                    // 第一次不匹配
                    isFound = true;
                    action.accept(e);
                }
            });
        }

        // 对找到的元素进行后续处理
        if (isFound) {
            source.forEachRemaining(action);
        }

        // 该环节已经处理完成
        return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return source.characteristics() & ~Spliterator.SIZED;
    }

    @Override
    public Comparator<? super T> getComparator() {
        return source.getComparator();
    }

}
