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
package org.miaixz.bus.core.compare;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * A {@code null}-friendly comparator that wraps another comparator. If {@code nullGreater} is true, {@code null} is
 * considered greater than non-null values; otherwise, it's the reverse. If both objects are {@code null}, they are
 * considered equal (returns 0). If both objects are non-null, the wrapped comparator is used for sorting. If the
 * wrapped comparator is {@code null}, this comparator checks if both objects implement {@link Comparable}. If they do,
 * their {@link Comparable#compareTo(Object)} method is invoked. If at least one of them does not implement
 * {@link Comparable}, they are considered equal.
 *
 * @param <T> the type of objects to be compared.
 * @author Kimi Liu
 * @since Java 17+
 */
public class NullCompare<T> implements Comparator<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852262152885L;

    /**
     * Indicates whether {@code null} is considered greater than non-null values.
     */
    protected final boolean nullGreater;
    /**
     * The actual comparator to use for non-null objects.
     */
    protected final Comparator<T> comparator;

    /**
     * Constructs a new {@code NullCompare}.
     *
     * @param nullGreater {@code true} if {@code null} should be treated as the greatest value, {@code false} otherwise.
     * @param comparator  the comparator to use for non-null objects. Can be {@code null}.
     */
    public NullCompare(final boolean nullGreater, final Comparator<? super T> comparator) {
        this.nullGreater = nullGreater;
        this.comparator = (Comparator<T>) comparator;
    }

    /**
     * Compare method.
     *
     * @return the int value
     */
    @Override
    public int compare(final T a, final T b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return nullGreater ? 1 : -1;
        } else if (b == null) {
            return nullGreater ? -1 : 1;
        } else {
            return doCompare(a, b);
        }
    }

    /**
     * Thencomparing method.
     *
     * @return the Comparator&lt;T&gt; value
     */
    @Override
    public Comparator<T> thenComparing(final Comparator<? super T> other) {
        Objects.requireNonNull(other);
        return new NullCompare<>(nullGreater, comparator == null ? other : comparator.thenComparing(other));
    }

    /**
     * A comparison method that does not check for {@code null} values. Users can override this method to customize the
     * comparison logic.
     *
     * @param a the first object to be compared.
     * @param b the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     */
    protected int doCompare(final T a, final T b) {
        if (null == comparator) {
            if (a instanceof Comparable && b instanceof Comparable) {
                return ((Comparable) a).compareTo(b);
            }
            return 0;
        }

        return comparator.compare(a, b);
    }

}
