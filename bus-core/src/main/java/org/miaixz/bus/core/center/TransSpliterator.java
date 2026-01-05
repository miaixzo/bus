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
package org.miaixz.bus.core.center;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link Spliterator} that provides a transformed view of a source spliterator. This implementation wraps an existing
 * {@code Spliterator} and applies a transformation function to each element as it is consumed. It is a lightweight,
 * non-interfering way to create a functionally transformed sequence of elements.
 *
 * @param <F> The type of elements in the source spliterator.
 * @param <T> The type of elements in the transformed spliterator.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TransSpliterator<F, T> implements Spliterator<T> {

    /**
     * The underlying source spliterator that provides the original elements.
     */
    private final Spliterator<F> from;
    /**
     * The function used to transform each element from the source type {@code F} to the target type {@code T}.
     */
    private final Function<? super F, ? extends T> function;

    /**
     * Constructs a new transforming spliterator that wraps the given source spliterator.
     *
     * @param from     The source {@link Spliterator} to wrap. Must not be {@code null}.
     * @param function The function to apply to each element for transformation. Must not be {@code null}.
     */
    public TransSpliterator(final Spliterator<F> from, final Function<? super F, ? extends T> function) {
        this.from = from;
        this.function = function;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Advances the spliterator and consumes the next element after applying the transformation function. The provided
     * action is performed on the transformed element.
     */
    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        return from.tryAdvance(fromElement -> action.accept(function.apply(fromElement)));
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Applies the transformation function to each remaining element and then performs the given action on each
     * transformed element.
     */
    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        from.forEachRemaining(fromElement -> action.accept(function.apply(fromElement)));
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * If the underlying spliterator can be partitioned, this method returns a new {@code TransSpliterator} covering a
     * portion of the elements. The new spliterator shares the same transformation function.
     */
    @Override
    public Spliterator<T> trySplit() {
        final Spliterator<F> fromSplit = from.trySplit();
        return (fromSplit != null) ? new TransSpliterator<>(fromSplit, function) : null;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns an estimate of the number of elements that would be encountered by a traversal, which is delegated to the
     * underlying source spliterator.
     */
    @Override
    public long estimateSize() {
        return from.estimateSize();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns the characteristics of this spliterator, which are derived from the source spliterator. This
     * implementation removes characteristics such as {@code DISTINCT}, {@code NONNULL}, and {@code SORTED} because the
     * transformation function is not guaranteed to preserve these properties. For example, a function might map
     * distinct elements to the same value or produce nulls from a non-null source.
     */
    @Override
    public int characteristics() {
        return from.characteristics() & ~(Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
    }

}
