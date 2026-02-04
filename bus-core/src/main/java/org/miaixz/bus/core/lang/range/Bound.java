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
package org.miaixz.bus.core.lang.range;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * <p>
 * Represents a boundary object that describes a single-sided unbounded range with a specific upper or lower limit.
 *
 * <p>
 * Types of Bounds:
 *
 * <p>
 * A bound, based on its type obtained via {@link #getType()}, can be used to describe inequalities based on a boundary
 * value <em>t</em>:
 * <ul>
 * <li>{@link #noneLowerBound()}：{@code {x | x > -∞}} (all values greater than negative infinity);</li>
 * <li>{@link #noneUpperBound()}：{@code {x | x < +∞}} (all values less than positive infinity);</li>
 * <li>{@link #greaterThan}：{@code {x | x > t}} (all values strictly greater than t);</li>
 * <li>{@link #atLeast}：{@code {x | x >= t}} (all values greater than or equal to t);</li>
 * <li>{@link #lessThan}：{@code {x | x < t}} (all values strictly less than t);</li>
 * <li>{@link #atMost}：{@code {x | x <= t}} (all values less than or equal to t);</li>
 * </ul>
 * When used as a {@link Predicate}, it can determine if an input object satisfies the inequality corresponding to this
 * instance.
 *
 * <p>
 * Comparison of Bounds:
 * <p>
 * The {@code Bound} object itself implements the {@link Comparable} interface. When comparing two bound objects using
 * {@link Comparable#compareTo}, the returned comparison value indicates the relative order of the points represented by
 * the two bounds on the real number line from left to right. For example: If the current boundary point is <em>t1</em>
 * and another boundary point is <em>t2</em>, then:
 * <ul>
 * <li>-1: <em>t1</em> is to the left of <em>t2</em>;</li>
 * <li>0: <em>t1</em> coincides with <em>t2</em>;</li>
 * <li>1: <em>t1</em> is to the right of <em>t2</em>;</li>
 * </ul>
 *
 * @param <T> the type of the boundary value, which must be comparable
 * @author Kimi Liu
 * @see BoundType
 * @see BoundedRange
 * @since Java 17+
 */
public interface Bound<T extends Comparable<? super T>> extends Predicate<T>, Comparable<Bound<T>> {

    /**
     * String representation for negative infinity.
     */
    String INFINITE_MIN = "-∞";

    /**
     * String representation for positive infinity.
     */
    String INFINITE_MAX = "+∞";

    /**
     * Returns a lower bound representing the range {@code {x | x > -∞}}. This signifies an unbounded lower limit.
     *
     * @param <T> the type of the boundary value
     * @return a {@code Bound} instance representing no lower bound
     */
    static <T extends Comparable<? super T>> Bound<T> noneLowerBound() {
        return NoneLowerBound.INSTANCE;
    }

    /**
     * Returns an upper bound representing the range {@code {x | x < +∞}}. This signifies an unbounded upper limit.
     *
     * @param <T> the type of the boundary value
     * @return a {@code Bound} instance representing no upper bound
     */
    static <T extends Comparable<? super T>> Bound<T> noneUpperBound() {
        return NoneUpperBound.INSTANCE;
    }

    /**
     * Returns a lower bound representing the range {@code {x | x > min}}. This is an open lower bound, meaning
     * {@code min} itself is not included.
     *
     * @param min the minimum value for the bound, must not be {@code null}
     * @param <T> the type of the boundary value
     * @return a {@code Bound} instance representing {@code x > min}
     * @throws NullPointerException if {@code min} is {@code null}
     */
    static <T extends Comparable<? super T>> Bound<T> greaterThan(final T min) {
        return new FiniteBound<>(Objects.requireNonNull(min), BoundType.OPEN_LOWER_BOUND);
    }

    /**
     * Returns a lower bound representing the range {@code {x | x >= min}}. This is a closed lower bound, meaning
     * {@code min} itself is included.
     *
     * @param min the minimum value for the bound, must not be {@code null}
     * @param <T> the type of the boundary value
     * @return a {@code Bound} instance representing {@code x >= min}
     * @throws NullPointerException if {@code min} is {@code null}
     */
    static <T extends Comparable<? super T>> Bound<T> atLeast(final T min) {
        return new FiniteBound<>(Objects.requireNonNull(min), BoundType.CLOSE_LOWER_BOUND);
    }

    /**
     * Returns an upper bound representing the range {@code {x | x < max}}. This is an open upper bound, meaning
     * {@code max} itself is not included.
     *
     * @param max the maximum value for the bound, must not be {@code null}
     * @param <T> the type of the boundary value
     * @return a {@code Bound} instance representing {@code x < max}
     * @throws NullPointerException if {@code max} is {@code null}
     */
    static <T extends Comparable<? super T>> Bound<T> lessThan(final T max) {
        return new FiniteBound<>(Objects.requireNonNull(max), BoundType.OPEN_UPPER_BOUND);
    }

    /**
     * Returns an upper bound representing the range {@code {x | x <= max}}. This is a closed upper bound, meaning
     * {@code max} itself is included.
     *
     * @param max the maximum value for the bound, must not be {@code null}
     * @param <T> the type of the boundary value
     * @return a {@code Bound} instance representing {@code x <= max}
     * @throws NullPointerException if {@code max} is {@code null}
     */
    static <T extends Comparable<? super T>> Bound<T> atMost(final T max) {
        return new FiniteBound<>(Objects.requireNonNull(max), BoundType.CLOSE_UPPER_BOUND);
    }

    /**
     * Retrieves the value of this boundary. For unbounded bounds (e.g., {@link #noneLowerBound()}), this method may
     * return {@code null}.
     *
     * @return the boundary value, or {@code null} if the bound is unbounded
     */
    T getValue();

    /**
     * Retrieves the type of this boundary, indicating whether it's an open/closed lower/upper bound, or an unbounded
     * type.
     *
     * @return the {@link BoundType} of this boundary
     */
    BoundType getType();

    /**
     * Tests if the given value satisfies the condition defined by this boundary. For example, if this bound represents
     * {@code x > t}, it returns {@code true} if the given value is greater than {@code t}.
     *
     * @param t the value to test, must not be {@code null}
     * @return {@code true} if the value satisfies the boundary condition, {@code false} otherwise
     * @throws NullPointerException if {@code t} is {@code null}
     */
    @Override
    boolean test(T t);

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
    int compareTo(final Bound<T> bound);

    /**
     * Returns a string representation of the bound in the format {@code "[value"} or {@code "(value"} for lower bounds,
     * or {@code "value]"} or {@code "value)"} for upper bounds.
     *
     * @return a string representation of the bound
     */
    String descBound();

    /**
     * Returns a new {@code Bound} instance that represents the negation of this bound. For example, if this bound is
     * {@code x > t}, its negation would be {@code x <= t}.
     *
     * @return a new {@code Bound} instance representing the negation of this bound
     */
    @Override
    Bound<T> negate();

    /**
     * Converts this single-sided bound into a {@link BoundedRange} instance. The resulting range will be unbounded on
     * one side and bounded by this instance on the other.
     *
     * @return a {@link BoundedRange} representing this bound as part of a range
     */
    BoundedRange<T> toRange();

    /**
     * Returns a string representation of the inequality corresponding to this instance, for example,
     * {@code "x >= value"} or {@code "x < value"}.
     *
     * @return a string representation of the inequality
     */
    @Override
    String toString();

}
