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
package org.miaixz.bus.core.lang.tuple;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.lang.exception.CloneException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * An immutable, array-based tuple that can hold multiple values of different types. This is useful for returning
 * multiple values from a method.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Tuple implements Iterable<Object>, Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 2852281275597L;

    /**
     * The elements of this tuple.
     */
    private final Object[] members;
    /**
     * The cached hash code.
     */
    private int hashCode;
    /**
     * Whether the hash code is cached.
     */
    private boolean cacheHash;

    /**
     * Constructs a new tuple with the specified members.
     *
     * @param members the elements of this tuple.
     */
    public Tuple(final Object... members) {
        this.members = members;
    }

    /**
     * Creates a new tuple instance.
     *
     * @param members the elements of the tuple.
     * @return a new {@code Tuple} instance.
     */
    public static Tuple of(final Object... members) {
        return new Tuple(members);
    }

    /**
     * Gets the element at the specified index.
     *
     * @param <T>   the type of the element.
     * @param index the index of the element to retrieve.
     * @return the element at the specified index.
     * @throws ArrayIndexOutOfBoundsException if the index is out of range.
     */
    public <T> T get(final int index) {
        return (T) members[index];
    }

    /**
     * Gets all elements of this tuple as an array.
     *
     * @return an array containing all elements of this tuple.
     */
    public Object[] getMembers() {
        return this.members;
    }

    /**
     * Converts this tuple to a {@link List}.
     *
     * @return a new {@link List} containing the elements of this tuple.
     */
    public final List<Object> toList() {
        return ListKit.of(this.members);
    }

    /**
     * Enables or disables hash code caching. When enabled, the hash code is computed only once and cached for future
     * use. This is useful when the tuple's members are immutable. Note: If caching is enabled and a member object is
     * mutated, the cached hash code will not be updated.
     *
     * @param cacheHash {@code true} to cache the hash code, {@code false} otherwise.
     * @return this {@code Tuple} instance.
     */
    public Tuple setCacheHash(final boolean cacheHash) {
        this.cacheHash = cacheHash;
        return this;
    }

    /**
     * Returns the number of elements in this tuple.
     *
     * @return the size of this tuple.
     */
    public int size() {
        return this.members.length;
    }

    /**
     * Checks if this tuple contains the specified element.
     *
     * @param value the element to check for.
     * @return {@code true} if this tuple contains the element, {@code false} otherwise.
     */
    public boolean contains(final Object value) {
        return ArrayKit.contains(this.members, value);
    }

    /**
     * Returns a sequential {@link Stream} with this tuple's elements as its source.
     *
     * @return a sequential {@link Stream} over the elements in this tuple.
     */
    public final Stream<Object> stream() {
        return Arrays.stream(this.members);
    }

    /**
     * Returns a parallel {@link Stream} with this tuple's elements as its source.
     *
     * @return a parallel {@link Stream} over the elements in this tuple.
     */
    public final Stream<Object> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * Returns a new tuple containing the specified sub-range of elements from this tuple.
     *
     * @param start the starting index (inclusive).
     * @param end   the ending index (exclusive).
     * @return a new {@code Tuple} containing the specified sub-range.
     */
    public final Tuple sub(final int start, final int end) {
        return new Tuple(ArrayKit.sub(this.members, start, end));
    }

    /**
     * Returns the hash code for this tuple. If hash code caching is enabled, this method returns the cached hash code.
     *
     * @return the hash code for this tuple.
     */
    @Override
    public int hashCode() {
        if (this.cacheHash && 0 != this.hashCode) {
            return this.hashCode;
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(members);
        if (this.cacheHash) {
            this.hashCode = result;
        }
        return result;
    }

    /**
     * Compares this tuple to another object for equality. Two tuples are equal if they have the same class and their
     * corresponding elements are deeply equal.
     *
     * @param object the object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final Tuple other = (Tuple) object;
        return Arrays.deepEquals(members, other.members);
    }

    /**
     * Returns a string representation of this tuple. The string representation is the result of calling
     * {@link Arrays#toString(Object[])} on the elements.
     *
     * @return a string representation of this tuple.
     */
    @Override
    public String toString() {
        return Arrays.toString(members);
    }

    /**
     * Returns an iterator over the elements in this tuple.
     *
     * @return an {@link Iterator} over the elements in this tuple.
     */
    @Override
    public Iterator<Object> iterator() {
        return new ArrayIterator<>(members);
    }

    /**
     * Creates a {@link Spliterator} over the elements in this tuple.
     *
     * @return a {@code Spliterator} over the elements in this tuple.
     */
    @Override
    public final Spliterator<Object> spliterator() {
        return Spliterators.spliterator(this.members, Spliterator.ORDERED);
    }

    /**
     * Creates and returns a clone of this object.
     *
     * @return a clone of this instance.
     * @throws CloneException if the object's class does not support the {@code Cloneable} interface.
     */
    @Override
    public Tuple clone() {
        try {
            final Tuple clone = (Tuple) super.clone();
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new CloneException(e);
        }
    }

}
