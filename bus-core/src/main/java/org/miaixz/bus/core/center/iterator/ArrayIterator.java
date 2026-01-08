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

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Array Iterator object. This iterator allows iterating over an array of elements.
 *
 * @param <E> the type of elements in the array
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayIterator<E> implements IterableIterator<E>, ResettableIterator<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852259500387L;

    /**
     * The array to iterate over.
     */
    private final Object array;
    /**
     * The starting index for iteration.
     */
    private int startIndex;
    /**
     * The ending index for iteration (exclusive).
     */
    private int endIndex;
    /**
     * The current index of the iterator.
     */
    private int index;

    /**
     * Constructs an {@code ArrayIterator} for the given array.
     *
     * @param array the array to iterate over
     * @throws IllegalArgumentException if the provided object is not an array
     * @throws NullPointerException     if the provided array is null
     */
    public ArrayIterator(final E[] array) {
        this((Object) array);
    }

    /**
     * Constructs an {@code ArrayIterator} for the given array object.
     *
     * @param array the array object to iterate over
     * @throws IllegalArgumentException if the provided object is not an array
     * @throws NullPointerException     if the provided array is null
     */
    public ArrayIterator(final Object array) {
        this(array, 0);
    }

    /**
     * Constructs an {@code ArrayIterator} for the given array object, starting from a specified index.
     *
     * @param array      the array object to iterate over
     * @param startIndex the starting index for iteration. If less than 0 or greater than the array length, it defaults
     *                   to 0.
     * @throws IllegalArgumentException if the provided object is not an array
     * @throws NullPointerException     if the provided array is null
     */
    public ArrayIterator(final Object array, final int startIndex) {
        this(array, startIndex, -1);
    }

    /**
     * Constructs an {@code ArrayIterator} for the given array object, with specified start and end indices.
     *
     * @param array      the array object to iterate over
     * @param startIndex the starting index for iteration. If less than 0 or greater than the array length, it defaults
     *                   to 0.
     * @param endIndex   the ending index for iteration (exclusive). If less than 0 or greater than the array length, it
     *                   defaults to the array length.
     * @throws IllegalArgumentException if the provided object is not an array
     * @throws NullPointerException     if the provided array is null
     */
    public ArrayIterator(final Object array, final int startIndex, final int endIndex) {
        this.endIndex = Array.getLength(Objects.requireNonNull(array));
        if (endIndex > 0 && endIndex < this.endIndex) {
            this.endIndex = endIndex;
        }

        if (startIndex >= 0 && startIndex < this.endIndex) {
            this.startIndex = startIndex;
        }
        this.array = array;
        this.index = this.startIndex;
    }

    /**
     * Checks if there are more elements in the array.
     *
     * @return {@code true} if there are more elements, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return (index < endIndex);
    }

    /**
     * Returns the next element in the array.
     *
     * @return the next element in the array.
     * @throws NoSuchElementException if there are no more elements to iterate.
     */
    @Override
    public E next() {
        if (hasNext() == false) {
            throw new NoSuchElementException();
        }
        return (E) Array.get(array, index++);
    }

    /**
     * Removes the current element from the array (unsupported operation).
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }

    /**
     * Returns the original array object.
     *
     * @return the original array object.
     */
    public Object getArray() {
        return array;
    }

    /**
     * Resets the iterator to its initial starting position.
     */
    @Override
    public void reset() {
        this.index = this.startIndex;
    }

}
