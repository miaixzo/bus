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
package org.miaixz.bus.core.lang.range;

import java.util.Objects;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.CompareKit;

/**
 * Utility class for performing operations on {@link BoundedRange} instances, such as union, intersection, and gap
 * calculations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BoundedOperation {

    /**
     * Constructs a new BoundedOperation. Utility class constructor for static access.
     */
    private BoundedOperation() {
    }

    /**
     * Merges the {@code other} range with the {@code boundedRange} if they intersect. If the two ranges do not
     * intersect, the original {@code boundedRange} is returned.
     *
     * @param <T>          the type of comparable objects in the ranges
     * @param boundedRange the first range
     * @param other        the second range to union with
     * @return a new {@code BoundedRange} representing the union if intersected, otherwise the original
     *         {@code boundedRange}
     * @throws NullPointerException if {@code boundedRange} or {@code other} is {@code null}
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> unionIfIntersected(
            final BoundedRange<T> boundedRange,
            final BoundedRange<T> other) {
        Objects.requireNonNull(boundedRange);
        Objects.requireNonNull(other);
        if (isDisjoint(boundedRange, other)) {
            return boundedRange;
        }
        return new BoundedRange<>(CompareKit.min(boundedRange.getLowerBound(), other.getLowerBound()),
                CompareKit.max(boundedRange.getUpperBound(), other.getUpperBound()));
    }

    /**
     * Returns the smallest {@code BoundedRange} that encloses both the {@code boundedRange} and the {@code other}
     * range. This is also known as the convex hull of the two ranges.
     *
     * @param <T>          the type of comparable objects in the ranges
     * @param boundedRange the first range
     * @param other        the second range to span with
     * @return a new {@code BoundedRange} representing the span of both ranges
     * @throws NullPointerException if {@code boundedRange} or {@code other} is {@code null}
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> span(
            final BoundedRange<T> boundedRange,
            final BoundedRange<T> other) {
        Objects.requireNonNull(boundedRange);
        Objects.requireNonNull(other);
        return new BoundedRange<>(CompareKit.min(boundedRange.getLowerBound(), other.getLowerBound()),
                CompareKit.max(boundedRange.getUpperBound(), other.getUpperBound()));
    }

    /**
     * If the {@code other} range is not connected to the {@code boundedRange}, this method returns the gap between the
     * two ranges. If the two ranges intersect, {@code null} is returned.
     *
     * @param <T>          the type of comparable objects in the ranges
     * @param boundedRange the first range
     * @param other        the second range to find the gap with
     * @return a new {@code BoundedRange} representing the gap, or {@code null} if the ranges intersect
     * @throws NullPointerException if {@code boundedRange} or {@code other} is {@code null}
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> gap(
            final BoundedRange<T> boundedRange,
            final BoundedRange<T> other) {
        Objects.requireNonNull(boundedRange);
        Objects.requireNonNull(other);
        if (isIntersected(boundedRange, other)) {
            return null;
        }
        return new BoundedRange<>(CompareKit.min(boundedRange.getUpperBound(), other.getUpperBound()).negate(),
                CompareKit.max(boundedRange.getLowerBound(), other.getLowerBound()).negate());
    }

    /**
     * If the {@code other} range intersects with the {@code boundedRange}, this method returns the intersection of the
     * two ranges. If there is no intersection, {@code null} is returned.
     *
     * @param <T>          the type of comparable objects in the ranges
     * @param boundedRange the first range
     * @param other        the second range to find the intersection with
     * @return a new {@code BoundedRange} representing the intersection, or {@code null} if there is no intersection
     * @throws NullPointerException if {@code boundedRange} or {@code other} is {@code null}
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> intersection(
            final BoundedRange<T> boundedRange,
            final BoundedRange<T> other) {
        Objects.requireNonNull(boundedRange);
        Objects.requireNonNull(other);
        if (isDisjoint(boundedRange, other)) {
            return null;
        }
        return new BoundedRange<>(CompareKit.max(boundedRange.getLowerBound(), other.getLowerBound()),
                CompareKit.min(boundedRange.getUpperBound(), other.getUpperBound()));
    }

    /**
     * Truncates the {@code boundedRange} to include only the part strictly greater than {@code min}. If {@code min} is
     * not within the current range, the original {@code boundedRange} itself is returned.
     *
     * @param <T>          the type of comparable objects in the range
     * @param boundedRange the range to truncate
     * @param min          the minimum value for truncation (exclusive)
     * @return a new {@code BoundedRange} representing the truncated portion, or the original range if no truncation
     *         occurs
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> subGreatThan(
            final BoundedRange<T> boundedRange,
            final T min) {
        return Optional.ofNullable(min).filter(boundedRange)
                .map(t -> new BoundedRange<>(Bound.greaterThan(t), boundedRange.getUpperBound())).orElse(boundedRange);
    }

    /**
     * Truncates the {@code boundedRange} to include only the part greater than or equal to {@code min}. If {@code min}
     * is not within the current range, the original {@code boundedRange} itself is returned.
     *
     * @param <T>          the type of comparable objects in the range
     * @param boundedRange the range to truncate
     * @param min          the minimum value for truncation (inclusive)
     * @return a new {@code BoundedRange} representing the truncated portion, or the original range if no truncation
     *         occurs
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> subAtLeast(
            final BoundedRange<T> boundedRange,
            final T min) {
        return Optional.ofNullable(min).filter(boundedRange)
                .map(t -> new BoundedRange<>(Bound.atLeast(t), boundedRange.getUpperBound())).orElse(boundedRange);
    }

    /**
     * Truncates the {@code boundedRange} to include only the part strictly less than {@code max}. If {@code max} is not
     * within the current range, the original {@code boundedRange} itself is returned.
     *
     * @param <T>          the type of comparable objects in the range
     * @param boundedRange the range to truncate
     * @param max          the maximum value for truncation (exclusive)
     * @return a new {@code BoundedRange} representing the truncated portion, or the original range if no truncation
     *         occurs
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> subLessThan(
            final BoundedRange<T> boundedRange,
            final T max) {
        return Optional.ofNullable(max).filter(boundedRange)
                .map(t -> new BoundedRange<>(boundedRange.getLowerBound(), Bound.lessThan(max))).orElse(boundedRange);
    }

    /**
     * Truncates the {@code boundedRange} to include only the part less than or equal to {@code max}. If {@code max} is
     * not within the current range, the original {@code boundedRange} itself is returned.
     *
     * @param <T>          the type of comparable objects in the range
     * @param boundedRange the range to truncate
     * @param max          the maximum value for truncation (inclusive)
     * @return a new {@code BoundedRange} representing the truncated portion, or the original range if no truncation
     *         occurs
     */
    public static <T extends Comparable<? super T>> BoundedRange<T> subAtMost(
            final BoundedRange<T> boundedRange,
            final T max) {
        return Optional.ofNullable(max).filter(boundedRange)
                .map(t -> new BoundedRange<>(boundedRange.getLowerBound(), Bound.atMost(max))).orElse(boundedRange);
    }

    /**
     * Checks if the {@code boundedRange} intersects with the {@code other} range. Two ranges intersect if they have at
     * least one element in common.
     *
     * @param <T>          the type of comparable objects in the ranges
     * @param boundedRange the first range
     * @param other        the second range
     * @return {@code true} if the ranges intersect, {@code false} otherwise
     * @throws NullPointerException if {@code boundedRange} or {@code other} is {@code null}
     */
    public static <T extends Comparable<? super T>> boolean isIntersected(
            final BoundedRange<T> boundedRange,
            final BoundedRange<T> other) {
        return !isDisjoint(boundedRange, other);
    }

    /**
     * Checks if the {@code boundedRange} is disjoint from the {@code other} range. Two ranges are disjoint if they have
     * no elements in common.
     *
     * @param <T>          the type of comparable objects in the ranges
     * @param boundedRange the first range
     * @param other        the second range
     * @return {@code true} if the ranges are disjoint, {@code false} otherwise
     * @throws NullPointerException if {@code boundedRange} or {@code other} is {@code null}
     */
    public static <T extends Comparable<? super T>> boolean isDisjoint(
            final BoundedRange<T> boundedRange,
            final BoundedRange<T> other) {
        Objects.requireNonNull(boundedRange);
        Objects.requireNonNull(other);
        return boundedRange.getLowerBound().compareTo(other.getUpperBound()) > 0
                || boundedRange.getUpperBound().compareTo(other.getLowerBound()) < 0;
    }

}
