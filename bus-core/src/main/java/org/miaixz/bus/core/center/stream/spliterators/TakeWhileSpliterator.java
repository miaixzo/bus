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
 * takeWhile 的 Spliterator
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class TakeWhileSpliterator<T> implements Spliterator<T> {

    private final Spliterator<T> source;
    private final Predicate<? super T> predicate;
    private boolean isContinue = true;

    TakeWhileSpliterator(final Spliterator<T> source, final Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    public static <T> TakeWhileSpliterator<T> create(final Spliterator<T> source,
            final Predicate<? super T> predicate) {
        return new TakeWhileSpliterator<>(source, predicate);
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        boolean hasNext = true;
        // 如果 还可以继续 并且 流中还有元素 则继续遍历
        while (isContinue && hasNext) {
            hasNext = source.tryAdvance(e -> {
                if (predicate.test(e)) {
                    action.accept(e);
                } else {
                    // 终止遍历剩下的元素
                    isContinue = false;
                }
            });
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
        return isContinue ? source.estimateSize() : 0;
    }

    @Override
    public int characteristics() {
        return source.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
    }

    @Override
    public Comparator<? super T> getComparator() {
        return source.getComparator();
    }

}
