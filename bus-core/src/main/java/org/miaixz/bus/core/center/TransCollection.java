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
package org.miaixz.bus.core.center;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.IteratorKit;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 使用给定的转换函数，转换源集合为新类型的集合
 *
 * @param <F> 源元素类型
 * @param <T> 目标元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class TransCollection<F, T> extends AbstractCollection<T> {

    private final Collection<F> fromCollection;
    private final Function<? super F, ? extends T> function;

    /**
     * 构造
     *
     * @param fromCollection 源集合
     * @param function       转换函数
     */
    public TransCollection(final Collection<F> fromCollection, final Function<? super F, ? extends T> function) {
        this.fromCollection = Assert.notNull(fromCollection);
        this.function = Assert.notNull(function);
    }

    /**
     * 使用给定的转换函数，转换源{@link Spliterator}为新类型的{@link Spliterator}
     *
     * @param <F>             源元素类型
     * @param <T>             目标元素类型
     * @param fromSpliterator 源{@link Spliterator}
     * @param function        转换函数
     * @return 新类型的{@link Spliterator}
     */
    public static <F, T> Spliterator<T> trans(final Spliterator<F> fromSpliterator, final Function<? super F, ? extends T> function) {
        return new TransSpliterator<>(fromSpliterator, function);
    }

    @Override
    public Iterator<T> iterator() {
        return IteratorKit.trans(fromCollection.iterator(), function);
    }

    @Override
    public void clear() {
        fromCollection.clear();
    }

    @Override
    public boolean isEmpty() {
        return fromCollection.isEmpty();
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        Assert.notNull(action);
        fromCollection.forEach((f) -> action.accept(function.apply(f)));
    }

    @Override
    public boolean removeIf(final Predicate<? super T> filter) {
        Assert.notNull(filter);
        return fromCollection.removeIf(element -> filter.test(function.apply(element)));
    }

    @Override
    public Spliterator<T> spliterator() {
        return trans(fromCollection.spliterator(), function);
    }

    @Override
    public int size() {
        return fromCollection.size();
    }

}
