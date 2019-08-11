/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.iterator;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapter to make an {@link Iterator Iterator} instance appear to be an
 * {@link Enumeration Enumeration} instance.
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class IteratorEnumeration<E> implements Enumeration<E> {

    /**
     * The iterator being decorated.
     */
    private Iterator<? extends E> iterator;

    /**
     * Constructs a new <code>IteratorEnumeration</code> that will not function
     * until {@link #setIterator(Iterator) setIterator} is invoked.
     */
    public IteratorEnumeration() {
    }

    /**
     * Constructs a new <code>IteratorEnumeration</code> that will use the given
     * iterator.
     *
     * @param iterator the iterator to use
     */
    public IteratorEnumeration(final Iterator<? extends E> iterator) {
        this.iterator = iterator;
    }

    /**
     * Returns true if the underlying iterator has more elements.
     *
     * @return true if the underlying iterator has more elements
     */
    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element from the underlying iterator.
     *
     * @return the next element from the underlying iterator.
     * @throws java.util.NoSuchElementException if the underlying iterator has
     *                                          no more elements
     */
    @Override
    public E nextElement() {
        return iterator.next();
    }

    /**
     * Returns the underlying iterator.
     *
     * @return the underlying iterator
     */
    public Iterator<? extends E> getIterator() {
        return iterator;
    }

    /**
     * Sets the underlying iterator.
     *
     * @param iterator the new underlying iterator
     */
    public void setIterator(final Iterator<? extends E> iterator) {
        this.iterator = iterator;
    }

}
