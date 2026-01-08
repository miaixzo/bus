/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.iterator;

import java.util.*;

import org.miaixz.bus.core.lang.Chain;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Combines multiple {@link Iterator} instances into a single, sequential {@link Iterator}. This allows for iterating
 * over elements from several sources as if they were from one continuous source.
 *
 * @param <T> the type of elements returned by this iterator
 * @author Kimi Liu
 * @since Java 17+
 */
public class IteratorChain<T> implements Iterator<T>, Chain<Iterator<T>, IteratorChain<T>> {

    /**
     * A list containing all the iterators in this chain.
     */
    protected final List<Iterator<T>> allIterators = new ArrayList<>();
    /**
     * The index of the current iterator being processed in the {@link #allIterators} list. Initialized to -1,
     * indicating no iterator has been started yet.
     */
    protected int currentIter = -1;

    /**
     * Constructs an empty {@code IteratorChain}. Additional iterators can be added using the
     * {@link #addChain(Iterator)} method.
     */
    public IteratorChain() {

    }

    /**
     * Constructs an {@code IteratorChain} with the given array of iterators.
     *
     * @param iterators an array of {@link Iterator} instances to be chained. Can be empty or {@code null}.
     * @throws IllegalArgumentException if any of the provided iterators are {@code null} or if duplicate iterators are
     *                                  added.
     */
    @SafeVarargs
    public IteratorChain(final Iterator<T>... iterators) {
        if (ArrayKit.isNotEmpty(iterators)) {
            for (final Iterator<T> iterator : iterators) {
                addChain(iterator);
            }
        }
    }

    /**
     * Adds an iterator to the end of this chain.
     *
     * @param iterator the {@link Iterator} to add, must not be {@code null}.
     * @return this {@code IteratorChain} instance, for method chaining.
     * @throws IllegalArgumentException if the iterator is {@code null} or if it has already been added to this chain.
     */
    @Override
    public IteratorChain<T> addChain(final Iterator<T> iterator) {
        Objects.requireNonNull(iterator, "Iterator must not be null");
        if (allIterators.contains(iterator)) {
            throw new IllegalArgumentException("Duplicate iterator");
        }
        allIterators.add(iterator);
        return this;
    }

    /**
     * Returns {@code true} if the iteration has more elements. This method checks if the current iterator has more
     * elements, or if there are subsequent iterators in the chain with elements.
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        if (currentIter == -1) {
            currentIter = 0;
        }

        final int size = allIterators.size();
        for (int i = currentIter; i < size; i++) {
            final Iterator<T> iterator = allIterators.get(i);
            if (iterator.hasNext()) {
                currentIter = i;
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return allIterators.get(currentIter).next();
    }

    /**
     * Removes from the underlying collection the last element returned by this iterator. This method can be called only
     * once per call to {@link #next()}.
     *
     * @throws IllegalStateException if the {@code next()} method has not yet been called, or the {@code remove()}
     *                               method has already been called after the last call to the {@code next()} method.
     */
    @Override
    public void remove() {
        if (-1 == currentIter) {
            throw new IllegalStateException("next() has not yet been called");
        }

        allIterators.get(currentIter).remove();
    }

    /**
     * Returns an iterator over the iterators contained in this chain.
     *
     * @return an {@link Iterator} that iterates over the {@link Iterator} instances in this chain.
     */
    @Override
    public Iterator<Iterator<T>> iterator() {
        return this.allIterators.iterator();
    }

}
