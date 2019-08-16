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

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

/**
 * Implements an {@link java.util.Iterator Iterator} over any array.
 * <p>
 * The array can be either an array of object or of primitives. If you know
 * class is a better choice, as it will perform better.
 * <p>
 * The iterator implements a {@link #reset} method, allowing the reset of
 * the iterator back to the start if required.
 *
 * @param <E> the type of elements returned by this iterator
 * @author Kimi Liu
 * @version 3.0.6
 * @since JDK 1.8
 */
public class ArrayIterator<E> implements ResettableIterator<E> {

    /**
     * The array to iterate over
     */
    final Object array;
    /**
     * The start index to loop from
     */
    final int startIndex;
    /**
     * The end index to loop to
     */
    final int endIndex;
    /**
     * The current iterator index
     */
    int index = 0;

    /**
     * Constructs an ArrayIterator that will iterate over the values in the
     * specified array.
     *
     * @param array the array to iterate over.
     * @throws IllegalArgumentException if <code>array</code> is not an array.
     * @throws NullPointerException     if <code>array</code> is <code>null</code>
     */
    public ArrayIterator(final Object array) {
        this(array, 0);
    }

    /**
     * Constructs an ArrayIterator that will iterate over the values in the
     * specified array from a specific start index.
     *
     * @param array      the array to iterate over.
     * @param startIndex the index to start iterating at.
     * @throws IllegalArgumentException  if <code>array</code> is not an array.
     * @throws NullPointerException      if <code>array</code> is <code>null</code>
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public ArrayIterator(final Object array, final int startIndex) {
        this(array, startIndex, Array.getLength(array));
    }

    /**
     * Construct an ArrayIterator that will iterate over a range of values
     * in the specified array.
     *
     * @param array      the array to iterate over.
     * @param startIndex the index to start iterating at.
     * @param endIndex   the index to finish iterating at.
     * @throws IllegalArgumentException  if <code>array</code> is not an array.
     * @throws NullPointerException      if <code>array</code> is <code>null</code>
     * @throws IndexOutOfBoundsException if either index is invalid
     */
    public ArrayIterator(final Object array, final int startIndex, final int endIndex) {
        super();

        this.array = array;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.index = startIndex;

        final int len = Array.getLength(array);
        checkBound(startIndex, len, "start");
        checkBound(endIndex, len, "end");
        if (endIndex < startIndex) {
            throw new IllegalArgumentException("End index must not be less than start index.");
        }
    }

    /**
     * Checks whether the index is valid or not.
     *
     * @param bound the index to check
     * @param len   the length of the array
     * @param type  the index type (for error messages)
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    protected void checkBound(final int bound, final int len, final String type) {
        if (bound > len) {
            throw new ArrayIndexOutOfBoundsException(
                    "Attempt to make an ArrayIterator that " + type +
                            "s beyond the end of the array. "
            );
        }
        if (bound < 0) {
            throw new ArrayIndexOutOfBoundsException(
                    "Attempt to make an ArrayIterator that " + type +
                            "s before the start of the array. "
            );
        }
    }

    /**
     * Returns true if there are more elements to return from the array.
     *
     * @return true if there is a next element to return
     */
    @Override
    public boolean hasNext() {
        return index < endIndex;
    }

    /**
     * Returns the next element in the array.
     *
     * @return the next element in the array
     * @throws NoSuchElementException if all the elements in the array
     *                                have already been returned
     */
    @Override
    public E next() {
        if (hasNext() == false) {
            throw new NoSuchElementException();
        }
        return (E) Array.get(array, index++);
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }

    /**
     * Gets the array that this iterator is iterating over.
     *
     * @return the array this iterator iterates over.
     */
    public Object getArray() {
        return array;
    }

    /**
     * Gets the start index to loop from.
     *
     * @return the start index
     * @since 4.0
     */
    public int getStartIndex() {
        return this.startIndex;
    }

    /**
     * Gets the end index to loop to.
     *
     * @return the end index
     * @since 4.0
     */
    public int getEndIndex() {
        return this.endIndex;
    }

    /**
     * Resets the iterator back to the start index.
     */
    @Override
    public void reset() {
        this.index = this.startIndex;
    }

}
