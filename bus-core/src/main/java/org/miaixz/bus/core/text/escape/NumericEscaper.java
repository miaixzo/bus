/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.text.escape;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.translate.CodePointTranslator;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Comparator;

/**
 * Translates codepoints to their XML numeric entity escaped value.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumericEscaper extends CodePointTranslator {

    /**
     * whether to escape between the boundaries or outside them.
     */
    private final boolean between;
    /**
     * range from lowest codepoint to highest codepoint.
     */
    private final Range<Integer> range;

    /**
     * <p>Constructs a <code>NumericEscaper</code> for the specified range. This is
     * the underlying method for the other constructors/builders. The <code>below</code>
     * and <code>above</code> boundaries are inclusive when <code>between</code> is
     * <code>true</code> and exclusive when it is <code>false</code>. </p>
     *
     * @param below   int value representing the lowest codepoint boundary
     * @param above   int value representing the highest codepoint boundary
     * @param between whether to escape between the boundaries or outside them
     */
    private NumericEscaper(final int below, final int above, final boolean between) {
        this.range = Range.between(below, above);
        this.between = between;
    }

    /**
     * <p>Constructs a <code>NumericEscaper</code> for all characters. </p>
     */
    public NumericEscaper() {
        this(0, Integer.MAX_VALUE, true);
    }

    /**
     * <p>Constructs a <code>NumericEscaper</code> below the specified value (exclusive). </p>
     *
     * @param codepoint below which to escape
     * @return the newly created {@code NumericEscaper} instance
     */
    public static NumericEscaper below(final int codepoint) {
        return outsideOf(codepoint, Integer.MAX_VALUE);
    }

    /**
     * <p>Constructs a <code>NumericEscaper</code> above the specified value (exclusive). </p>
     *
     * @param codepoint above which to escape
     * @return the newly created {@code NumericEscaper} instance
     */
    public static NumericEscaper above(final int codepoint) {
        return outsideOf(0, codepoint);
    }

    /**
     * <p>Constructs a <code>NumericEscaper</code> between the specified values (inclusive). </p>
     *
     * @param codepointLow  above which to escape
     * @param codepointHigh below which to escape
     * @return the newly created {@code NumericEscaper} instance
     */
    public static NumericEscaper between(final int codepointLow, final int codepointHigh) {
        return new NumericEscaper(codepointLow, codepointHigh, true);
    }

    /**
     * <p>Constructs a <code>NumericEscaper</code> outside of the specified values (exclusive). </p>
     *
     * @param codepointLow  below which to escape
     * @param codepointHigh above which to escape
     * @return the newly created {@code NumericEscaper} instance
     */
    public static NumericEscaper outsideOf(final int codepointLow, final int codepointHigh) {
        return new NumericEscaper(codepointLow, codepointHigh, false);
    }


    @Override
    public boolean translate(final int codepoint, final Writer out) throws IOException {
        if (this.between != this.range.contains(codepoint)) {
            return false;
        }
        out.write("&#");
        out.write(Integer.toString(codepoint, 10));
        out.write(Symbol.C_SEMICOLON);
        return true;
    }

    static class Range<T> implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Comparator<T> comparator;
        private final T minimum;
        private final T maximum;
        private transient int hashCode;
        private transient String toString;

        private Range(T element1, T element2, Comparator<T> comp) {
            if (null == element1 || null == element2) {
                throw new IllegalArgumentException("Elements in a range must not be null: element1=" + element1 + ", element2=" + element2);
            }
            if (null == comp) {
                this.comparator = Range.ComparableComparator.INSTANCE;
            } else {
                this.comparator = comp;
            }
            if (this.comparator.compare(element1, element2) < 1) {
                this.minimum = element1;
                this.maximum = element2;
            } else {
                this.minimum = element2;
                this.maximum = element1;
            }
        }

        public static <T extends Comparable<T>> Range<T> is(T element) {
            return between(element, element, null);
        }

        public static <T> Range<T> is(T element, Comparator<T> comparator) {
            return between(element, element, comparator);
        }

        public static <T extends Comparable<T>> Range<T> between(T fromInclusive, T toInclusive) {
            return between(fromInclusive, toInclusive, null);
        }

        public static <T> Range<T> between(T fromInclusive, T toInclusive, Comparator<T> comparator) {
            return new Range(fromInclusive, toInclusive, comparator);
        }

        public T getMinimum() {
            return this.minimum;
        }

        public T getMaximum() {
            return this.maximum;
        }

        public Comparator<T> getComparator() {
            return this.comparator;
        }

        public boolean isNaturalOrdering() {
            return this.comparator == Range.ComparableComparator.INSTANCE;
        }

        public boolean contains(T element) {
            if (null == element) {
                return false;
            }
            return (this.comparator.compare(element, this.minimum) > -1) && (this.comparator.compare(element, this.maximum) < 1);
        }

        public boolean isAfter(T element) {
            if (null == element) {
                return false;
            }
            return this.comparator.compare(element, this.minimum) < 0;
        }

        public boolean isStartedBy(T element) {
            if (null == element) {
                return false;
            }
            return this.comparator.compare(element, this.minimum) == 0;
        }

        public boolean isEndedBy(T element) {
            if (null == element) {
                return false;
            }
            return this.comparator.compare(element, this.maximum) == 0;
        }

        public boolean isBefore(T element) {
            if (null == element) {
                return false;
            }
            return this.comparator.compare(element, this.maximum) > 0;
        }

        public int elementCompareTo(T element) {
            if (isAfter(element)) {
                return -1;
            }
            if (isBefore(element)) {
                return 1;
            }
            return 0;
        }

        public boolean containsRange(Range<T> otherRange) {
            if (null == otherRange) {
                return false;
            }
            return (contains(otherRange.minimum)) &&
                    (contains(otherRange.maximum));
        }

        public boolean isAfterRange(Range<T> otherRange) {
            if (null == otherRange) {
                return false;
            }
            return isAfter(otherRange.maximum);
        }

        public boolean isOverlappedBy(Range<T> otherRange) {
            if (null == otherRange) {
                return false;
            }
            return (otherRange.contains(this.minimum)) ||
                    (otherRange.contains(this.maximum)) ||
                    (contains(otherRange.minimum));
        }

        public boolean isBeforeRange(Range<T> otherRange) {
            if (null == otherRange) {
                return false;
            }
            return isBefore(otherRange.minimum);
        }

        public Range<T> intersectionWith(Range<T> other) {
            if (!isOverlappedBy(other)) {
                throw new IllegalArgumentException(String.format("Cannot calculate intersection with non-overlapping range %s", other));
            }
            if (equals(other)) {
                return this;
            }
            T min = getComparator().compare(this.minimum, other.minimum) < 0 ? other.minimum : this.minimum;
            T max = getComparator().compare(this.maximum, other.maximum) < 0 ? this.maximum : other.maximum;
            return between(min, max, getComparator());
        }

        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if ((null == object) || (object.getClass() != getClass())) {
                return false;
            }
            Range<T> range = (Range) object;
            return (this.minimum.equals(range.minimum)) &&
                    (this.maximum.equals(range.maximum));
        }

        public int hashCode() {
            int result = this.hashCode;
            if (this.hashCode == 0) {
                result = 17;
                result = 37 * result + getClass().hashCode();
                result = 37 * result + this.minimum.hashCode();
                result = 37 * result + this.maximum.hashCode();
                this.hashCode = result;
            }
            return result;
        }

        public String toString() {
            if (null == this.toString) {
                this.toString = (Symbol.BRACKET_LEFT + this.minimum + Symbol.DOUBLE_DOT + this.maximum + Symbol.BRACKET_RIGHT);
            }
            return this.toString;
        }

        public String toString(String format) {
            return String.format(format, this.minimum, this.maximum, this.comparator);
        }

        private enum ComparableComparator implements Comparator {
            INSTANCE;

            ComparableComparator() {
            }

            public int compare(Object obj1, Object obj2) {
                return ((Comparable) obj1).compareTo(obj2);
            }
        }
    }

}

