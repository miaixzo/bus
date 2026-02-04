/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.tuple;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.miaixz.bus.core.lang.exception.CloneException;

/**
 * An immutable pair consisting of two elements.
 * <p>
 * This class is a basic container for two objects. It is immutable, meaning that once created, the left and right
 * elements cannot be changed.
 *
 * @param <L> the left element type
 * @param <R> the right element type
 * @author Kimi Liu
 * @since Java 17+
 */
public class Pair<L, R> implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 2852280961003L;

    /**
     * The left element of this pair.
     */
    protected L left;
    /**
     * The right element of this pair.
     */
    protected R right;

    /**
     * Constructs a new pair with the specified left and right values.
     *
     * @param left  the left value
     * @param right the right value
     */
    public Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Creates a new pair instance.
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @param left  the left value, may be null
     * @param right the right value, may be null
     * @return a new {@code Pair} instance, never null
     */
    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }

    /**
     * Gets the left element from this pair.
     *
     * @return the left element, may be null
     */
    public L getLeft() {
        return this.left;
    }

    /**
     * Gets the right element from this pair.
     *
     * @return the right element, may be null
     */
    public R getRight() {
        return this.right;
    }

    /**
     * Compares this pair to another object for equality.
     * <p>
     * This pair is equal to the other object if it is also a {@code Pair} and the left and right elements are equal.
     *
     * @param o the object to compare to, may be null
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    /**
     * Returns a hash code value for the pair. The hash code is based on the hash codes of the left and right elements.
     *
     * @return a hash code value for this pair
     */
    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    /**
     * Returns a string representation of this pair. The string representation is in the form {@code "Pair{left=L,
     * right=R}"}.
     *
     * @return a string representation of this pair
     */
    @Override
    public String toString() {
        return "Pair{" + "left=" + left + ", right=" + right + '}';
    }

    /**
     * Creates and returns a clone of this object.
     *
     * @return a clone of this instance
     * @throws CloneException if the object's class does not support the {@code Cloneable} interface
     */
    @Override
    public Pair<L, R> clone() {
        try {
            return (Pair<L, R>) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new CloneException(e);
        }
    }

}
