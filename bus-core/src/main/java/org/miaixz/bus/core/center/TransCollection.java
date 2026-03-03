/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.IteratorKit;

/**
 * Represents a collection that provides a transformed view of a source collection. The transformation is defined by a
 * function that is applied to each element on-the-fly. This class is useful for creating a lightweight,
 * functional-style view of a collection without creating a new collection in memory.
 *
 * @param <F> The type of elements in the source collection.
 * @param <T> The type of elements in this transformed collection.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TransCollection<F, T> extends AbstractCollection<T> {

    /**
     * The underlying source collection that provides the original elements.
     */
    private final Collection<F> from;
    /**
     * The function used to transform each element from the source type {@code F} to the target type {@code T}.
     */
    private final Function<? super F, ? extends T> function;

    /**
     * Constructs a new transformed collection that wraps the given source collection.
     *
     * @param from     The source collection. Must not be {@code null}.
     * @param function The function to apply to each element for transformation. Must not be {@code null}.
     */
    public TransCollection(final Collection<F> from, final Function<? super F, ? extends T> function) {
        this.from = Assert.notNull(from);
        this.function = Assert.notNull(function);
    }

    /**
     * Creates a new {@link Spliterator} that lazily transforms elements from a source spliterator using the provided
     * transformation function.
     *
     * @param <F>      The type of the source elements.
     * @param <T>      The type of the target elements.
     * @param from     The source {@link Spliterator} to transform. Must not be {@code null}.
     * @param function The transformation function to apply to each element. Must not be {@code null}.
     * @return A new {@link Spliterator} that provides a transformed view of the source.
     */
    public static <F, T> Spliterator<T> trans(
            final Spliterator<F> from,
            final Function<? super F, ? extends T> function) {
        return new TransSpliterator<>(from, function);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * <p>
     * Returns an iterator that applies the transformation function to each element of the source collection as it is
     * being iterated.
     */
    @Override
    public Iterator<T> iterator() {
        return IteratorKit.trans(from.iterator(), function);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * <p>
     * This operation clears the underlying source collection, effectively clearing this collection as well.
     */
    @Override
    public void clear() {
        from.clear();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * <p>
     * This operation checks if the underlying source collection is empty.
     */
    @Override
    public boolean isEmpty() {
        return from.isEmpty();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * <p>
     * The specified action is applied to each transformed element of the collection. The transformation is performed
     * before the action is consumed.
     */
    @Override
    public void forEach(final Consumer<? super T> action) {
        Assert.notNull(action);
        from.forEach((f) -> action.accept(function.apply(f)));
    }

    /**
     * Description inherited from parent class or interface.
     *
     * <p>
     * Removes elements from the underlying source collection if their transformed value matches the given predicate.
     * The transformation is applied before the predicate is tested.
     */
    @Override
    public boolean removeIf(final Predicate<? super T> filter) {
        Assert.notNull(filter);
        return from.removeIf(element -> filter.test(function.apply(element)));
    }

    /**
     * Description inherited from parent class or interface.
     *
     * <p>
     * Returns a {@link Spliterator} that applies the transformation function to each element on-demand.
     */
    @Override
    public Spliterator<T> spliterator() {
        return trans(from.spliterator(), function);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * <p>
     * This operation returns the size of the underlying source collection.
     */
    @Override
    public int size() {
        return from.size();
    }

}
