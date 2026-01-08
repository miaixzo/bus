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
package org.miaixz.bus.core.xyz;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

import org.miaixz.bus.core.compare.IndexedCompare;
import org.miaixz.bus.core.compare.PinyinCompare;

/**
 * Comparison utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompareKit {

    /**
     * Constructs a new CompareKit. Utility class constructor for static access.
     */
    private CompareKit() {
    }

    /**
     * Compares two `char` values.
     *
     * @param x the first value.
     * @param y the second value.
     * @return 0 if x==y, a value less than 0 if x &lt; y, a value greater than 0 if x &gt; y.
     * @see Character#compare(char, char)
     */
    public static int compare(final char x, final char y) {
        return Character.compare(x, y);
    }

    /**
     * Compares two `double` values.
     *
     * @param x the first value.
     * @param y the second value.
     * @return 0 if x==y, a value less than 0 if x &lt; y, a value greater than 0 if x &gt; y.
     * @see Double#compare(double, double)
     */
    public static int compare(final double x, final double y) {
        return Double.compare(x, y);
    }

    /**
     * Compares two `int` values.
     *
     * @param x the first value.
     * @param y the second value.
     * @return 0 if x==y, a value less than 0 if x &lt; y, a value greater than 0 if x &gt; y.
     * @see Integer#compare(int, int)
     */
    public static int compare(final int x, final int y) {
        return Integer.compare(x, y);
    }

    /**
     * Compares two `long` values.
     *
     * @param x the first value.
     * @param y the second value.
     * @return 0 if x==y, a value less than 0 if x &lt; y, a value greater than 0 if x &gt; y.
     * @see Long#compare(long, long)
     */
    public static int compare(final long x, final long y) {
        return Long.compare(x, y);
    }

    /**
     * Compares two `short` values.
     *
     * @param x the first value.
     * @param y the second value.
     * @return 0 if x==y, a value less than 0 if x &lt; y, a value greater than 0 if x &gt; y.
     * @see Short#compare(short, short)
     */
    public static int compare(final short x, final short y) {
        return Short.compare(x, y);
    }

    /**
     * Compares two `byte` values.
     *
     * @param x the first value.
     * @param y the second value.
     * @return 0 if x==y, -1 if x &lt; y, 1 if x &gt; y.
     * @see Byte#compare(byte, byte)
     */
    public static int compare(final byte x, final byte y) {
        return Byte.compare(x, y);
    }

    /**
     * Returns a comparator that imposes the natural ordering on a {@code Comparable} object.
     * <p>
     * For null-friendly operations, use:
     *
     * <ul>
     * <li>{@code Comparator.nullsLast(CompareKit.natural())}</li>
     * <li>{@code Comparator.nullsFirst(CompareKit.natural())}</li>
     * </ul>
     *
     * @param <E> The type of the comparable object.
     * @return The natural order comparator.
     */
    public static <E extends Comparable<? super E>> Comparator<E> natural() {
        return Comparator.naturalOrder();
    }

    /**
     * Returns a comparator that imposes the reverse of the natural ordering.
     * <p>
     * For null-friendly operations, use:
     *
     * <ul>
     * <li>{@code Comparator.nullsLast(CompareKit.naturalReverse())}</li>
     * <li>{@code Comparator.nullsFirst(CompareKit.naturalReverse())}</li>
     * </ul>
     *
     * @param <E> The type of the comparable object.
     * @return The reverse natural order comparator.
     */
    public static <E extends Comparable<? super E>> Comparator<E> naturalReverse() {
        return Comparator.reverseOrder();
    }

    /**
     * Returns a comparator that imposes the reverse of the specified {@code Comparator}.
     * <p>
     * For null-friendly operations, use:
     *
     * <ul>
     * <li>{@code Comparator.nullsLast(CompareKit.reverse(comparator))}</li>
     * <li>{@code Comparator.nullsFirst(CompareKit.reverse(comparator))}</li>
     * </ul>
     *
     * @param <E>        The type of the comparable object.
     * @param comparator The comparator to be reversed.
     * @return The reversed comparator.
     */
    public static <E extends Comparable<? super E>> Comparator<E> reverse(final Comparator<E> comparator) {
        return null == comparator ? naturalReverse() : comparator.reversed();
    }

    /**
     * Compares two objects using the given {@code Comparator}. If the comparator is null, the default comparison rule
     * is used (the objects must implement {@code Comparable}).
     *
     * @param <T>        The type of the objects being compared.
     * @param c1         The first object.
     * @param c2         The second object.
     * @param comparator The comparator.
     * @return The comparison result.
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T> int compare(final T c1, final T c2, final Comparator<T> comparator) {
        if (null == comparator) {
            return compare((Comparable) c1, (Comparable) c2);
        }
        return comparator.compare(c1, c2);
    }

    /**
     * Null-safe comparison of two {@code Comparable} objects. {@code null} is considered smaller than any non-null
     * value.
     *
     * @param <T> The type of the objects being compared.
     * @param c1  The first object (can be `null`).
     * @param c2  The second object (can be `null`).
     * @return The comparison result.
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(final T c1, final T c2) {
        return compare(c1, c2, false);
    }

    /**
     * Null-safe comparison of two {@code Comparable} objects.
     *
     * @param <T>           The type of the objects being compared.
     * @param c1            The first object (can be `null`).
     * @param c2            The second object (can be `null`).
     * @param isNullGreater If `true`, `null` is considered greater than any non-null value; if `false`, `null` is
     *                      considered smaller.
     * @return The comparison result.
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(final T c1, final T c2, final boolean isNullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return isNullGreater ? 1 : -1;
        } else if (c2 == null) {
            return isNullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    /**
     * Naturally compares two objects. The comparison rules are:
     * 
     * <pre>
     * 1. If both are `Comparable`, use `compareTo`.
     * 2. If `o1.equals(o2)`, return 0.
     * 3. Compare by `hashCode`.
     * 4. Compare by `toString`.
     * </pre>
     *
     * @param <T>           The type of the objects being compared.
     * @param o1            The first object.
     * @param o2            The second object.
     * @param isNullGreater If `true`, `null` is considered greater than any non-null value.
     * @return The comparison result.
     */
    public static <T> int compare(final T o1, final T o2, final boolean isNullGreater) {
        if (o1 == o2) {
            return 0;
        } else if (null == o1) {
            return isNullGreater ? 1 : -1;
        } else if (null == o2) {
            return isNullGreater ? -1 : 1;
        }

        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            return ((Comparable) o1).compareTo(o2);
        }

        if (o1.equals(o2)) {
            return 0;
        }

        int result = Integer.compare(o1.hashCode(), o2.hashCode());
        if (0 == result) {
            result = compare(o1.toString(), o2.toString());
        }

        return result;
    }

    /**
     * Returns a `Comparator` that compares `String`s based on their Pinyin (Chinese phonetic) order.
     *
     * @param keyExtractor A function to extract the string to be compared from an object.
     * @param <T>          The type of the object.
     * @return A Pinyin-based comparator.
     */
    public static <T> Comparator<T> comparingPinyin(final Function<T, String> keyExtractor) {
        return comparingPinyin(keyExtractor, false);
    }

    /**
     * Returns a `Comparator` that compares `String`s based on their Pinyin (Chinese phonetic) order.
     *
     * @param keyExtractor A function to extract the string to be compared from an object.
     * @param reverse      If true, the comparator imposes the reverse ordering.
     * @param <T>          The type of the object.
     * @return A Pinyin-based comparator.
     */
    public static <T> Comparator<T> comparingPinyin(final Function<T, String> keyExtractor, final boolean reverse) {
        Objects.requireNonNull(keyExtractor);
        final PinyinCompare pinyinComparator = new PinyinCompare();
        if (reverse) {
            return (o1, o2) -> pinyinComparator.compare(keyExtractor.apply(o2), keyExtractor.apply(o1));
        }
        return (o1, o2) -> pinyinComparator.compare(keyExtractor.apply(o1), keyExtractor.apply(o2));
    }

    /**
     * Returns a `Comparator` that sorts objects based on their position in a given array.
     *
     * @param keyExtractor A function to extract the key to be compared.
     * @param objs         The array defining the sort order.
     * @param <T>          The type of the object being sorted.
     * @param <U>          The type of the elements in the order array.
     * @return An indexed comparator.
     */
    public static <T, U> Comparator<T> comparingIndexed(
            final Function<? super T, ? extends U> keyExtractor,
            final U[] objs) {
        return comparingIndexed(keyExtractor, false, objs);
    }

    /**
     * Returns a `Comparator` that sorts objects based on their position in a given `Iterable`.
     *
     * @param keyExtractor A function to extract the key to be compared.
     * @param objs         The `Iterable` defining the sort order.
     * @param <T>          The type of the object being sorted.
     * @param <U>          The type of the elements in the order `Iterable`.
     * @return An indexed comparator.
     */
    public static <T, U> Comparator<T> comparingIndexed(
            final Function<? super T, ? extends U> keyExtractor,
            final Iterable<U> objs) {
        return comparingIndexed(
                keyExtractor,
                false,
                ArrayKit.ofArray(objs, (Class<U>) objs.iterator().next().getClass()));
    }

    /**
     * Returns a `Comparator` that sorts objects based on their position in a given array.
     *
     * @param keyExtractor A function to extract the key to be compared.
     * @param atEndIfMiss  If `true`, elements not in the order array are placed at the end; otherwise, at the
     *                     beginning.
     * @param objs         The array defining the sort order.
     * @param <T>          The type of the object being sorted.
     * @param <U>          The type of the elements in the order array.
     * @return An indexed comparator.
     */
    public static <T, U> Comparator<T> comparingIndexed(
            final Function<? super T, ? extends U> keyExtractor,
            final boolean atEndIfMiss,
            final U... objs) {
        Objects.requireNonNull(keyExtractor);
        final IndexedCompare<U> indexedComparator = new IndexedCompare<>(atEndIfMiss, objs);
        return (o1, o2) -> indexedComparator.compare(keyExtractor.apply(o1), keyExtractor.apply(o2));
    }

    /**
     * Returns the smaller of two `Comparable` objects. If they are equal, the first one is returned.
     *
     * @param <T> The type of the objects.
     * @param t1  The first object.
     * @param t2  The second object.
     * @return The minimum value.
     */
    public static <T extends Comparable<? super T>> T min(final T t1, final T t2) {
        return compare(t1, t2) <= 0 ? t1 : t2;
    }

    /**
     * Returns the greater of two `Comparable` objects. If they are equal, the first one is returned.
     *
     * @param <T> The type of the objects.
     * @param t1  The first object.
     * @param t2  The second object.
     * @return The maximum value.
     */
    public static <T extends Comparable<? super T>> T max(final T t1, final T t2) {
        return compare(t1, t2) >= 0 ? t1 : t2;
    }

    /**
     * Null-safe check if two `Comparable` objects are equal.
     *
     * @param <T> The type of the objects.
     * @param c1  The first object.
     * @param c2  The second object.
     * @return `true` if they are equal.
     */
    public static <T extends Comparable<? super T>> boolean equals(final T c1, final T c2) {
        return compare(c1, c2) == 0;
    }

    /**
     * Checks if `c1` is greater than `c2`.
     *
     * @param <T> The type of the objects.
     * @param c1  The first object.
     * @param c2  The second object.
     * @return `true` if `c1` > `c2`.
     */
    public static <T extends Comparable<? super T>> boolean gt(final T c1, final T c2) {
        return compare(c1, c2) > 0;
    }

    /**
     * Checks if `c1` is greater than or equal to `c2`.
     *
     * @param <T> The type of the objects.
     * @param c1  The first object.
     * @param c2  The second object.
     * @return `true` if `c1` >= `c2`.
     */
    public static <T extends Comparable<? super T>> boolean ge(final T c1, final T c2) {
        return compare(c1, c2) >= 0;
    }

    /**
     * Checks if `c1` is less than `c2`.
     *
     * @param <T> The type of the objects.
     * @param c1  The first object.
     * @param c2  The second object.
     * @return `true` if `c1` &lt; `c2`.
     */
    public static <T extends Comparable<? super T>> boolean lt(final T c1, final T c2) {
        return compare(c1, c2) < 0;
    }

    /**
     * Checks if `c1` is less than or equal to `c2`.
     *
     * @param <T> The type of the objects.
     * @param c1  The first object.
     * @param c2  The second object.
     * @return `true` if `c1` &lt;= `c2`.
     */
    public static <T extends Comparable<? super T>> boolean le(final T c1, final T c2) {
        return compare(c1, c2) <= 0;
    }

    /**
     * Checks if a value is within the inclusive range of [c1, c2].
     *
     * @param <T>   The type of the objects.
     * @param value The value to check.
     * @param c1    The first boundary.
     * @param c2    The second boundary.
     * @return `true` if the value is within the inclusive range.
     */
    public static <T extends Comparable<? super T>> boolean isIn(final T value, final T c1, final T c2) {
        return ge(value, min(c1, c2)) && le(value, max(c1, c2));
    }

    /**
     * Checks if a value is within the exclusive range of (c1, c2).
     *
     * @param <T>   The type of the objects.
     * @param value The value to check.
     * @param c1    The first boundary.
     * @param c2    The second boundary.
     * @return `true` if the value is within the exclusive range.
     */
    public static <T extends Comparable<? super T>> boolean isInExclusive(final T value, final T c1, final T c2) {
        return gt(value, min(c1, c2)) && lt(value, max(c1, c2));
    }

}
