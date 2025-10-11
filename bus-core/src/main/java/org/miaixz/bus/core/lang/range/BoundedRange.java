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

import java.io.Serial;
import java.util.Objects;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.lang.Assert;

/**
 * <p>
 * Inspired by Guava's {@code Range} implementation, this class describes a range defined by two {@link Bound} instances
 * acting as lower and upper bounds. When used as a {@link Predicate}, it can check if a specified value is within the
 * range, meaning the value satisfies both the lower and upper bound's {@link Bound#test} methods.
 *
 * <p>
 * Types of ranges, supported by factory methods:
 *
 * <table>
 * <caption>Range Types</caption>
 * <tr>
 * <th>Range</th>
 * <th>Mathematical Definition</th>
 * <th>Factory Method</th>
 * </tr>
 * <tr>
 * <td>{@code (a, b)}</td>
 * <td>{@code {x | a < x < b}}</td>
 * <td>{@link #open}</td>
 * </tr>
 * <tr>
 * <td>{@code [a, b]}</td>
 * <td>{@code {x | a <= x <= b}}</td>
 * <td>{@link #close}</td>
 * </tr>
 * <tr>
 * <td>{@code (a, b]}</td>
 * <td>{@code {x | a < x <= b}}</td>
 * <td>{@link #openClose}</td>
 * </tr>
 * <tr>
 * <td>{@code [a, b)}</td>
 * <td>{@code {x | a <= x < b}}</td>
 * <td>{@link #closeOpen}</td>
 * </tr>
 * <tr>
 * <td>{@code (a, +∞)}</td>
 * <td>{@code {x | x > a}}</td>
 * <td>{@link #greaterThan}</td>
 * </tr>
 * <tr>
 * <td>{@code [a, +∞)}</td>
 * <td>{@code {x | x >= a}}</td>
 * <td>{@link #atLeast}</td>
 * </tr>
 * <tr>
 * <td>{@code (-∞, b)}</td>
 * <td>{@code {x | x < b}}</td>
 * <td>{@link #lessThan}</td>
 * </tr>
 * <tr>
 * <td>{@code (-∞, b]}</td>
 * <td>{@code {x | x <= b}}</td>
 * <td>{@link #atMost}</td>
 * </tr>
 * <tr>
 * <td>{@code (-∞, +∞)}</td>
 * <td>{@code {x}}</td>
 * <td>{@link #all}</td>
 * </tr>
 * </table>
 *
 * <p>
 * Empty Ranges:
 *
 * <p>
 * According to mathematical definitions, a range represents an empty set if it contains no real numbers. Users can
 * check if the current instance is an empty range using {@link #isEmpty()}. If an instance has a lower bound <em>a</em>
 * and an upper bound <em>b</em>, it is considered an empty range if it satisfies any of the following conditions:
 * <ul>
 * <li>{@code a > b};</li>
 * <li>{@code [a, b)}, and {@code a == b};</li>
 * <li>{@code (a, b)}, and {@code a == b};</li>
 * <li>{@code (a, b]}, and {@code a == b};</li>
 * </ul>
 * When creating ranges via factory methods, an {@link IllegalArgumentException} will be thrown if the range is empty.
 * However, intersection and union operations might still produce empty ranges that satisfy the above description. If an
 * empty range participates in operations, it might lead to unexpected results. Therefore, for ranges obtained through
 * non-factory methods, it is necessary to check with {@link #isEmpty()} before performing operations.
 *
 * @param <T> the type of the boundary values, which must be comparable
 * @author Kimi Liu
 * @see Bound
 * @since Java 17+
 */
public class BoundedRange<T extends Comparable<? super T>> implements PredicateX<T> {

    @Serial
    private static final long serialVersionUID = 2852273351363L;

    /**
     * A static instance representing a doubly unbounded range, i.e., {@code {x | -∞ < x < +∞}}.
     */
    private static final BoundedRange ALL = new BoundedRange(Bound.noneLowerBound(), Bound.noneUpperBound());
    /**
     * The lower bound of this range.
     */
    private final Bound<T> lowerBound;
    /**
     * The upper bound of this range.
     */
    private final Bound<T> upperBound;

    /**
     * Constructs a new {@code BoundedRange} with the specified lower and upper bounds.
     *
     * @param lowerBound the lower bound of the range
     * @param upperBound the upper bound of the range
     */
    BoundedRange(final Bound<T> lowerBound, final Bound<T> upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Creates a doubly unbounded range, representing all possible values: {@code {x | -∞ < x < +∞}}.
     *
     * @param <T> the type of the comparable objects in the range
     * @return a {@code BoundedRange} instance representing all values
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> all() {
        return ALL;
    }

    /**
     * Creates a closed range, including both lower and upper bounds: {@code {x | lowerBound <= x <= upperBound}}.
     *
     * @param lowerBound the lower bound (inclusive), must not be {@code null}
     * @param upperBound the upper bound (inclusive), must not be {@code null}
     * @param <T>        the type of the boundary values
     * @return a {@code BoundedRange} instance representing the closed range
     * @throws IllegalArgumentException if the created range represents an empty set
     * @throws NullPointerException     if {@code lowerBound} or {@code upperBound} is {@code null}
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> close(final T lowerBound, final T upperBound) {
        Objects.requireNonNull(lowerBound);
        Objects.requireNonNull(upperBound);
        return checkEmpty(new BoundedRange<>(Bound.atLeast(lowerBound), Bound.atMost(upperBound)));
    }

    /**
     * Creates an open range, excluding both lower and upper bounds: {@code {x | lowerBound < x < upperBound}}.
     *
     * @param lowerBound the lower bound (exclusive), must not be {@code null}
     * @param upperBound the upper bound (exclusive), must not be {@code null}
     * @param <T>        the type of the boundary values
     * @return a {@code BoundedRange} instance representing the open range
     * @throws IllegalArgumentException if the created range represents an empty set
     * @throws NullPointerException     if {@code lowerBound} or {@code upperBound} is {@code null}
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> open(final T lowerBound, final T upperBound) {
        Objects.requireNonNull(lowerBound);
        Objects.requireNonNull(upperBound);
        return checkEmpty(new BoundedRange<>(Bound.greaterThan(lowerBound), Bound.lessThan(upperBound)));
    }

    /**
     * Creates a left-closed, right-open range: {@code {x | lowerBound <= x < upperBound}}.
     *
     * @param lowerBound the lower bound (inclusive), must not be {@code null}
     * @param upperBound the upper bound (exclusive), must not be {@code null}
     * @param <T>        the type of the boundary values
     * @return a {@code BoundedRange} instance representing the left-closed, right-open range
     * @throws IllegalArgumentException if the created range represents an empty set
     * @throws NullPointerException     if {@code lowerBound} or {@code upperBound} is {@code null}
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> closeOpen(final T lowerBound, final T upperBound) {
        Objects.requireNonNull(lowerBound);
        Objects.requireNonNull(upperBound);
        return checkEmpty(new BoundedRange<>(Bound.atLeast(lowerBound), Bound.lessThan(upperBound)));
    }

    /**
     * Creates a left-open, right-closed range: {@code {x | lowerBound < x <= upperBound}}.
     *
     * @param lowerBound the lower bound (exclusive), must not be {@code null}
     * @param upperBound the upper bound (inclusive), must not be {@code null}
     * @param <T>        the type of the boundary values
     * @return a {@code BoundedRange} instance representing the left-open, right-closed range
     * @throws IllegalArgumentException if the created range represents an empty set
     * @throws NullPointerException     if {@code lowerBound} or {@code upperBound} is {@code null}
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> openClose(final T lowerBound, final T upperBound) {
        Objects.requireNonNull(lowerBound);
        Objects.requireNonNull(upperBound);
        return checkEmpty(new BoundedRange<>(Bound.greaterThan(lowerBound), Bound.atMost(upperBound)));
    }

    /**
     * Creates a range with an open lower bound and an unbounded upper bound: {@code {x | lowerBound < x < +∞}}.
     *
     * @param lowerBound the lower bound (exclusive), must not be {@code null}
     * @param <T>        the type of the boundary values
     * @return a {@code BoundedRange} instance representing the range
     * @throws NullPointerException if {@code lowerBound} is {@code null}
     * @see Bound#toRange()
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> greaterThan(final T lowerBound) {
        return Bound.greaterThan(lowerBound).toRange();
    }

    /**
     * Creates a range with a closed lower bound and an unbounded upper bound: {@code {x | lowerBound <= x < +∞}}.
     *
     * @param lowerBound the lower bound (inclusive), must not be {@code null}
     * @param <T>        the type of the boundary values
     * @return a {@code BoundedRange} instance representing the range
     * @throws NullPointerException if {@code lowerBound} is {@code null}
     * @see Bound#toRange()
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> atLeast(final T lowerBound) {
        return Bound.atLeast(lowerBound).toRange();
    }

    /**
     * Creates a range with an unbounded lower bound and an open upper bound: {@code {x | -∞ < x < upperBound}}.
     *
     * @param upperBound the upper bound (exclusive), must not be {@code null}
     * @param <T>        the type of the boundary values
     * @return a {@code BoundedRange} instance representing the range
     * @throws NullPointerException if {@code upperBound} is {@code null}
     * @see Bound#toRange()
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> lessThan(final T upperBound) {
        return Bound.lessThan(upperBound).toRange();
    }

    /**
     * Creates a range with an unbounded lower bound and a closed upper bound: {@code {x | -∞ < x <= upperBound}}.
     *
     * @param upperBound the upper bound (inclusive), must not be {@code null}
     * @param <T>        the type of the boundary values
     * @return a {@code BoundedRange} instance representing the range
     * @throws NullPointerException if {@code upperBound} is {@code null}
     * @see Bound#toRange()
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> atMost(final T upperBound) {
        return Bound.atMost(upperBound).toRange();
    }

    /**
     * Checks if the given range is empty and throws an {@link IllegalArgumentException} if it is.
     *
     * @param range the range to check
     * @param <T>   the type of the boundary values
     * @return the checked range (if not empty)
     * @throws IllegalArgumentException if the range is empty
     */
    private static <T extends Comparable<? super T>> BoundedRange<T> checkEmpty(final BoundedRange<T> range) {
        Assert.isFalse(range.isEmpty(), "{} is a empty range", range);
        return range;
    }

    /**
     * Retrieves the lower bound of this range.
     *
     * @return the {@link Bound} representing the lower limit of the range
     */
    public Bound<T> getLowerBound() {
        return lowerBound;
    }

    /**
     * Retrieves the value of the lower bound of this range.
     *
     * @return the value of the lower bound, or {@code null} if the lower bound is unbounded
     */
    public T getLowerBoundValue() {
        return getLowerBound().getValue();
    }

    /**
     * Checks if this range has a finite lower bound.
     *
     * @return {@code true} if the lower bound is not unbounded (i.e., has a specific value), {@code false} otherwise
     */
    public boolean hasLowerBound() {
        return Objects.nonNull(getLowerBound().getValue());
    }

    /**
     * Retrieves the upper bound of this range.
     *
     * @return the {@link Bound} representing the upper limit of the range
     */
    public Bound<T> getUpperBound() {
        return upperBound;
    }

    /**
     * Retrieves the value of the upper bound of this range.
     *
     * @return the value of the upper bound, or {@code null} if the upper bound is unbounded
     */
    public T getUpperBoundValue() {
        return getUpperBound().getValue();
    }

    /**
     * Checks if this range has a finite upper bound.
     *
     * @return {@code true} if the upper bound is not unbounded (i.e., has a specific value), {@code false} otherwise
     */
    public boolean hasUpperBound() {
        return Objects.nonNull(getUpperBound().getValue());
    }

    /**
     * <p>
     * Checks if the current range is empty. A range defined by a lower bound <em>left</em> and an upper bound
     * <em>right</em> is considered empty if it satisfies any of the following conditions:
     * <ul>
     * <li>For any range, if {@code left > right};</li>
     * <li>For a half-open range {@code [left, right)}, if {@code left == right};</li>
     * <li>For an open range {@code (left, right)}, if {@code left == right};</li>
     * <li>For a half-open range {@code (left, right]}, if {@code left == right};</li>
     * </ul>
     *
     * @return {@code true} if the range is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        final Bound<T> low = getLowerBound();
        final Bound<T> up = getUpperBound();
        if (low instanceof NoneLowerBound || up instanceof NoneUpperBound) {
            return false;
        }
        final int compareValue = low.getValue().compareTo(up.getValue());
        if (compareValue < 0) {
            return false;
        }
        // If upper bound is less than lower bound, it's empty
        return compareValue > 0
                // If boundary values are equal, and it's not a degenerate closed interval, it's empty
                || !(low.getType().isClose() && up.getType().isClose());
    }

    /**
     * Returns a string representation of this range, typically in the format {@code "[a, b]"}, where 'a' is the lower
     * bound and 'b' is the upper bound, with appropriate bracket types.
     *
     * @return a string representation of the range
     */
    @Override
    public String toString() {
        return getLowerBound().descBound() + ", " + getUpperBound().descBound();
    }

    /**
     * Indicates whether some other object is "equal to" this one. Two {@code BoundedRange} objects are considered equal
     * if they have the same lower and upper bounds.
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
        final BoundedRange<?> that = (BoundedRange<?>) o;
        return lowerBound.equals(that.lowerBound) && upperBound.equals(that.upperBound);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }

    /**
     * Checks if the {@code other} range is a superset of the current range. A range A is a superset of range B if B is
     * contained within A.
     *
     * @param other another {@code BoundedRange} to compare with
     * @return {@code true} if {@code other} is a superset of this range, {@code false} otherwise
     */
    public boolean isSuperset(final BoundedRange<T> other) {
        return getLowerBound().compareTo(other.getLowerBound()) <= 0
                && getUpperBound().compareTo(other.getUpperBound()) >= 0;
    }

    /**
     * Checks if the {@code other} range is a proper superset of the current range. A range A is a proper superset of
     * range B if B is contained within A and A is not equal to B.
     *
     * @param other another {@code BoundedRange} to compare with
     * @return {@code true} if {@code other} is a proper superset of this range, {@code false} otherwise
     */
    public boolean isProperSuperset(final BoundedRange<T> other) {
        return getLowerBound().compareTo(other.getLowerBound()) < 0
                && getUpperBound().compareTo(other.getUpperBound()) > 0;
    }

    /**
     * Checks if the current range is a subset of the {@code other} range. A range A is a subset of range B if A is
     * contained within B.
     *
     * @param other another {@code BoundedRange} to compare with
     * @return {@code true} if this range is a subset of {@code other}, {@code false} otherwise
     */
    public boolean isSubset(final BoundedRange<T> other) {
        return getLowerBound().compareTo(other.getLowerBound()) >= 0
                && getUpperBound().compareTo(other.getUpperBound()) <= 0;
    }

    /**
     * Checks if the current range is a proper subset of the {@code other} range. A range A is a proper subset of range
     * B if A is contained within B and A is not equal to B.
     *
     * @param other another {@code BoundedRange} to compare with
     * @return {@code true} if this range is a proper subset of {@code other}, {@code false} otherwise
     */
    public boolean isProperSubset(final BoundedRange<T> other) {
        return getLowerBound().compareTo(other.getLowerBound()) > 0
                && getUpperBound().compareTo(other.getUpperBound()) < 0;
    }

    /**
     * Checks if the {@code other} range is disjoint from the current range. Two ranges are disjoint if they have no
     * elements in common.
     *
     * @param other another {@code BoundedRange} to compare with
     * @return {@code true} if {@code other} is disjoint from this range, {@code false} otherwise
     */
    public boolean isDisjoint(final BoundedRange<T> other) {
        return BoundedOperation.isDisjoint(this, other);
    }

    /**
     * Checks if the {@code other} range intersects with the current range. Two ranges intersect if they have at least
     * one element in common.
     *
     * @param other another {@code BoundedRange} to compare with
     * @return {@code true} if {@code other} intersects with this range, {@code false} otherwise
     */
    public boolean isIntersected(final BoundedRange<T> other) {
        return BoundedOperation.isIntersected(this, other);
    }

    /**
     * Tests if the specified value is within the current range.
     *
     * @param value the value to test
     * @return {@code true} if the value is within the range, {@code false} otherwise
     */
    @Override
    public boolean testing(final T value) {
        return getLowerBound().and(getUpperBound()).test(value);
    }

    /**
     * If {@code other} intersects with the current range, this method merges them into a new range that spans both. If
     * the two ranges do not intersect, the current range is returned.
     *
     * @param other another {@code BoundedRange} to union with
     * @return a new {@code BoundedRange} representing the union if intersected, otherwise the current range
     */
    public BoundedRange<T> unionIfIntersected(final BoundedRange<T> other) {
        return BoundedOperation.unionIfIntersected(this, other);
    }

    /**
     * Returns the smallest range that encloses both the current range and the specified {@code other} range.
     *
     * @param other another {@code BoundedRange} to span with
     * @return a new {@code BoundedRange} representing the span of both ranges
     */
    public BoundedRange<T> span(final BoundedRange<T> other) {
        return BoundedOperation.span(this, other);
    }

    /**
     * If {@code other} is not connected to the current range, this method returns the gap between the two ranges. If
     * the two ranges intersect, {@code null} is returned.
     *
     * @param other another {@code BoundedRange} to find the gap with
     * @return a new {@code BoundedRange} representing the gap, or {@code null} if the ranges intersect
     */
    public BoundedRange<T> gap(final BoundedRange<T> other) {
        return BoundedOperation.gap(this, other);
    }

    /**
     * If {@code other} intersects with the current range, this method returns the intersection of the two ranges. If
     * there is no intersection, {@code null} is returned.
     *
     * @param other another {@code BoundedRange} to find the intersection with
     * @return a new {@code BoundedRange} representing the intersection, or {@code null} if there is no intersection
     */
    public BoundedRange<T> intersection(final BoundedRange<T> other) {
        return BoundedOperation.intersection(this, other);
    }

    /**
     * Truncates the current range to include only the part greater than {@code min}. If {@code min} is not within the
     * current range, the current range itself is returned.
     *
     * @param min the minimum value for truncation
     * @return a new {@code BoundedRange} representing the truncated portion, or the original range if no truncation
     *         occurs
     */
    public BoundedRange<T> subGreatThan(final T min) {
        return BoundedOperation.subGreatThan(this, min);
    }

    /**
     * Truncates the current range to include only the part greater than or equal to {@code min}. If {@code min} is not
     * within the current range, the current range itself is returned.
     *
     * @param min the minimum value for truncation (inclusive)
     * @return a new {@code BoundedRange} representing the truncated portion, or the original range if no truncation
     *         occurs
     */
    public BoundedRange<T> subAtLeast(final T min) {
        return BoundedOperation.subAtLeast(this, min);
    }

    /**
     * Truncates the current range to include only the part less than {@code max}. If {@code max} is not within the
     * current range, the current range itself is returned.
     *
     * @param max the maximum value for truncation (exclusive)
     * @return a new {@code BoundedRange} representing the truncated portion, or the original range if no truncation
     *         occurs
     */
    public BoundedRange<T> subLessThan(final T max) {
        return BoundedOperation.subLessThan(this, max);
    }

    /**
     * Truncates the current range to include only the part less than or equal to {@code max}. If {@code max} is not
     * within the current range, the current range itself is returned.
     *
     * @param max the maximum value for truncation (inclusive)
     * @return a new {@code BoundedRange} representing the truncated portion, or the original range if no truncation
     *         occurs
     */
    public BoundedRange<T> subAtMost(final T max) {
        return BoundedOperation.subAtMost(this, max);
    }

}
