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

/**
 * Represents an infinitely large upper bound, effectively meaning there is no upper limit. This class implements the
 * {@link Bound} interface for comparable types.
 *
 * @param <T> the type of the boundary value, which must be comparable
 * @author Kimi Liu
 * @since Java 17+
 */
public class NoneUpperBound<T extends Comparable<? super T>> implements Bound<T> {

    /**
     * Singleton instance for an infinitely large upper bound.
     */
    static final NoneUpperBound INSTANCE = new NoneUpperBound();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private NoneUpperBound() {

    }

    /**
     * Retrieves the value of this boundary. For an infinitely large upper bound, this is always {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public T getValue() {
        return null;
    }

    /**
     * Retrieves the type of this boundary, which is always {@link BoundType#OPEN_UPPER_BOUND}.
     *
     * @return {@link BoundType#OPEN_UPPER_BOUND}
     */
    @Override
    public BoundType getType() {
        return BoundType.OPEN_UPPER_BOUND;
    }

    /**
     * Tests if the given value is within the range defined by this boundary. Since this is an infinitely large upper
     * bound, any value is considered to be within its range.
     *
     * @param t the value to test, must not be {@code null}
     * @return {@code true} always
     */
    @Override
    public boolean test(final T t) {
        return true;
    }

    /**
     * <p>
     * Compares the position of another boundary relative to this boundary on the coordinate axis. If the current
     * boundary is <em>t1</em> (infinitely large upper bound) and the other boundary is <em>t2</em>, then:
     * <ul>
     * <li>-1: <em>t1</em> is to the left of <em>t2</em> (this case does not exist for an infinitely large upper
     * bound);</li>
     * <li>0: <em>t1</em> coincides with <em>t2</em> (if <em>t2</em> is also an infinitely large upper bound);</li>
     * <li>1: <em>t1</em> is to the right of <em>t2</em> (unless <em>t2</em> is also an infinitely large upper
     * bound).</li>
     * </ul>
     *
     * @param bound the other boundary to compare with
     * @return 0 if the other bound is also {@code NoneUpperBound}, otherwise 1 (meaning this bound is to the right of
     *         the other)
     */
    @Override
    public int compareTo(final Bound<T> bound) {
        return bound instanceof NoneUpperBound ? 0 : 1;
    }

    /**
     * Returns a string representation of this bound, typically in the format "+∞ )".
     *
     * @return a string representation of the bound
     */
    @Override
    public String descBound() {
        return INFINITE_MAX + getType().getSymbol();
    }

    /**
     * Returns a string representation of the inequality corresponding to this instance, for example, {@code "{x | x <
     * +∞}"}.
     *
     * @return a string representation of the inequality
     */
    @Override
    public String toString() {
        return "{x | x < +∞}";
    }

    /**
     * Returns the negation of this boundary. The negation of an infinitely large upper bound is still an infinitely
     * large upper bound.
     *
     * @return this {@code Bound} instance (as it represents an unbounded upper limit)
     */
    @Override
    public Bound<T> negate() {
        return this;
    }

    /**
     * Converts this infinitely large upper bound into a {@link BoundedRange} instance. Since it's an infinitely large
     * upper bound, it forms an unbounded range from negative infinity to positive infinity.
     *
     * @return a {@link BoundedRange} representing the entire real number line
     */
    @Override
    public BoundedRange<T> toRange() {
        return BoundedRange.all();
    }

}
