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
package org.miaixz.bus.core.lang.range;

import java.util.Objects;

import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Represents a finite boundary, defined by a specific value and a {@link BoundType}. This class implements the
 * {@link Bound} interface for comparable types.
 *
 * @param <T> the type of the boundary value, which must be comparable
 * @author Kimi Liu
 * @since Java 17+
 */
public class FiniteBound<T extends Comparable<? super T>> implements Bound<T> {

    /**
     * The finite value that defines this boundary.
     */
    private final T value;

    /**
     * The type of this boundary, indicating whether it's an open/closed lower/upper bound.
     */
    private final BoundType type;

    /**
     * Constructs a new {@code FiniteBound} with the specified value and bound type.
     *
     * @param value the boundary value
     * @param type  the {@link BoundType} of this boundary
     */
    FiniteBound(final T value, final BoundType type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Retrieves the finite value of this boundary.
     *
     * @return the boundary value
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * Retrieves the type of this boundary.
     *
     * @return the {@link BoundType} of this boundary
     */
    @Override
    public BoundType getType() {
        return type;
    }

    /**
     * Tests if the given value satisfies the condition defined by this boundary. For example, if this bound represents
     * {@code x > t}, it returns {@code true} if the given value is greater than {@code t}.
     *
     * @param t the value to test, must not be {@code null}
     * @return {@code true} if the value satisfies the boundary condition, {@code false} otherwise
     * @throws NullPointerException if {@code t} is {@code null}
     */
    @Override
    public boolean test(final T t) {
        final BoundType bt = this.getType();
        final int compareValue = getValue().compareTo(t);
        // If equal to the boundary value
        if (compareValue == 0) {
            return bt.isClose();
        }
        // Less than or greater than the boundary value
        return compareValue > 0 ? bt.isUpperBound() : bt.isLowerBound();
    }

    /**
     * <p>
     * Compares the position of another boundary relative to this boundary on the coordinate axis. If the current
     * boundary is <em>t1</em> and the other boundary is <em>t2</em>, then:
     * <ul>
     * <li>-1: <em>t1</em> is to the left of <em>t2</em>;</li>
     * <li>0: <em>t1</em> coincides with <em>t2</em>;</li>
     * <li>1: <em>t1</em> is to the right of <em>t2</em>;</li>
     * </ul>
     *
     * @param bound the other boundary to compare with
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    @Override
    public int compareTo(final Bound<T> bound) {
        // If the other bound is an infinite lower bound, this bound is necessarily to its right
        if (bound instanceof NoneLowerBound) {
            return 1;
        }
        // If the other bound is an infinite upper bound, this bound is necessarily to its left
        if (bound instanceof NoneUpperBound) {
            return -1;
        }
        // If values are not equal, compare the boundary values directly
        if (ObjectKit.notEquals(getValue(), bound.getValue())) {
            return getValue().compareTo(bound.getValue());
        }
        // If boundary values are equal, compare based on bound type
        return compareIfSameBoundValue(bound);
    }

    /**
     * Returns a string representation of the bound in the format {@code "[value"} or {@code "(value"} for lower bounds,
     * or {@code "value]"} or {@code "value)"} for upper bounds.
     *
     * @return a string representation of the bound
     */
    @Override
    public String descBound() {
        final BoundType bt = getType();
        return bt.isLowerBound() ? bt.getSymbol() + getValue() : getValue() + bt.getSymbol();
    }

    /**
     * Returns a new {@code Bound} instance that represents the negation of this bound. For example, if this bound is
     * {@code x > t}, its negation would be {@code x <= t}.
     *
     * @return a new {@code Bound} instance representing the negation of this bound
     */
    @Override
    public Bound<T> negate() {
        return new FiniteBound<>(value, getType().negate());
    }

    /**
     * Converts this single-sided finite bound into a {@link BoundedRange} instance. The resulting range will be
     * unbounded on one side and bounded by this instance on the other.
     *
     * @return a {@link BoundedRange} representing this bound as part of a range
     */
    @Override
    public BoundedRange<T> toRange() {
        return getType().isLowerBound() ? new BoundedRange<>(this, Bound.noneUpperBound())
                : new BoundedRange<>(Bound.noneLowerBound(), this);
    }

    /**
     * Indicates whether some other object is "equal to" this one. Two {@code FiniteBound} objects are considered equal
     * if they have the same value and {@link BoundType}.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FiniteBound<?> that = (FiniteBound<?>) o;
        return value.equals(that.value) && type == that.type;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    /**
     * Returns a string representation of the inequality corresponding to this instance, for example, {@code "{x | x >=
     * value}"} or {@code "{x | x < value}"} .
     *
     * @return a string representation of the inequality
     */
    @Override
    public String toString() {
        return CharsBacker.format("{x | x {} {}}", type.getOperator(), value);
    }

    /**
     * Compares two bounds that have the same boundary value to determine their relative order. This method resolves the
     * order based on their {@link BoundType}.
     *
     * @param bound the other bound with the same value to compare
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    private int compareIfSameBoundValue(final Bound<T> bound) {
        final BoundType bt1 = this.getType();
        final BoundType bt2 = bound.getType();
        // If the two boundary types are the same, it means the boundaries coincide.
        if (bt1 == bt2) {
            return 0;
        }
        // One is a lower bound, the other is an upper bound.
        if (bt1.isDislocated(bt2)) {
            // Special case: When a closed upper bound and a closed lower bound are at the same point,
            // they are considered to overlap (used for interval intersection judgment).
            if ((bt1 == BoundType.CLOSE_UPPER_BOUND && bt2 == BoundType.CLOSE_LOWER_BOUND)
                    || (bt1 == BoundType.CLOSE_LOWER_BOUND && bt2 == BoundType.CLOSE_UPPER_BOUND)) {
                return 0;
            }
            // General case: The lower bound is always considered "after" (greater than) the upper bound.
            return bt1.isLowerBound() ? 1 : -1;
        }
        // If both are lower bounds, the closed bound comes first;
        // if both are upper bounds, the closed bound comes later.
        return Integer.compare(bt1.getCode(), bt2.getCode());
    }

}
