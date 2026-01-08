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
package org.miaixz.bus.core.text;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A flexible string joiner that provides more configuration options than {@link java.util.StringJoiner}. Features
 * include:
 * <ul>
 * <li>Support for any {@link Appendable} implementation.</li>
 * <li>Option to wrap each element individually with a prefix and suffix.</li>
 * <li>Customizable handling for null elements.</li>
 * <li>A default result for when no elements are joined.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringJoiner implements Appendable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852233611385L;

    /**
     * The Appendable to which the strings will be appended.
     */
    private Appendable appendable;
    /**
     * The delimiter to be used between each element.
     */
    private CharSequence delimiter;
    /**
     * The prefix to be added to the result.
     */
    private CharSequence prefix;
    /**
     * The suffix to be added to the result.
     */
    private CharSequence suffix;
    /**
     * Whether to wrap each element with the prefix and suffix, or the entire string.
     */
    private boolean wrapElement;
    /**
     * The strategy for handling null elements.
     */
    private NullMode nullMode = NullMode.NULL_STRING;
    /**
     * The default string to return if no elements are added.
     */
    private String emptyResult = Normal.EMPTY;
    /**
     * A flag to indicate if the appendable already has content, to decide whether to add a delimiter first.
     */
    private boolean hasContent;

    /**
     * Constructs a {@code StringJoiner} with a specified delimiter.
     *
     * @param delimiter The delimiter. If {@code null}, elements are concatenated directly.
     */
    public StringJoiner(final CharSequence delimiter) {
        this(null, delimiter);
    }

    /**
     * Constructs a {@code StringJoiner} with a specified appendable and delimiter.
     *
     * @param appendable The appendable to use. If {@code null}, a new {@link StringBuilder} is created.
     * @param delimiter  The delimiter.
     */
    public StringJoiner(final Appendable appendable, final CharSequence delimiter) {
        this(appendable, delimiter, null, null);
    }

    /**
     * Constructs a {@code StringJoiner} with a delimiter, prefix, and suffix.
     *
     * @param delimiter The delimiter.
     * @param prefix    The prefix.
     * @param suffix    The suffix.
     */
    public StringJoiner(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        this(null, delimiter, prefix, suffix);
    }

    /**
     * Constructs a {@code StringJoiner} with full configuration.
     *
     * @param appendable The appendable to use.
     * @param delimiter  The delimiter.
     * @param prefix     The prefix.
     * @param suffix     The suffix.
     */
    public StringJoiner(final Appendable appendable, final CharSequence delimiter, final CharSequence prefix,
            final CharSequence suffix) {
        if (null != appendable) {
            this.appendable = appendable;
            checkHasContent(appendable);
        }
        this.delimiter = delimiter;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Creates a new {@code StringJoiner} with the same configuration as an existing one.
     *
     * @param joiner The existing {@code StringJoiner}.
     * @return A new {@code StringJoiner} with identical settings.
     */
    public static StringJoiner of(final StringJoiner joiner) {
        final StringJoiner joinerNew = new StringJoiner(joiner.delimiter, joiner.prefix, joiner.suffix);
        joinerNew.wrapElement = joiner.wrapElement;
        joinerNew.nullMode = joiner.nullMode;
        joinerNew.emptyResult = joiner.emptyResult;
        return joinerNew;
    }

    /**
     * Creates a {@code StringJoiner} with a specified delimiter.
     *
     * @param delimiter The delimiter.
     * @return A new {@code StringJoiner}.
     */
    public static StringJoiner of(final CharSequence delimiter) {
        return new StringJoiner(delimiter);
    }

    /**
     * Creates a {@code StringJoiner} with a delimiter, prefix, and suffix.
     *
     * @param delimiter The delimiter.
     * @param prefix    The prefix.
     * @param suffix    The suffix.
     * @return A new {@code StringJoiner}.
     */
    public static StringJoiner of(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return new StringJoiner(delimiter, prefix, suffix);
    }

    /**
     * Sets the delimiter.
     *
     * @param delimiter The delimiter.
     * @return this instance for chaining.
     */
    public StringJoiner setDelimiter(final CharSequence delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Sets the prefix.
     *
     * @param prefix The prefix.
     * @return this instance for chaining.
     */
    public StringJoiner setPrefix(final CharSequence prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Sets the suffix.
     *
     * @param suffix The suffix.
     * @return this instance for chaining.
     */
    public StringJoiner setSuffix(final CharSequence suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * Sets whether the prefix and suffix should wrap each element instead of the entire string.
     *
     * @param wrapElement {@code true} to wrap each element, {@code false} to wrap the whole string.
     * @return this instance for chaining.
     */
    public StringJoiner setWrapElement(final boolean wrapElement) {
        this.wrapElement = wrapElement;
        return this;
    }

    /**
     * Sets the handling strategy for null elements.
     *
     * @param nullMode The null handling mode.
     * @return this instance for chaining.
     */
    public StringJoiner setNullMode(final NullMode nullMode) {
        this.nullMode = nullMode;
        return this;
    }

    /**
     * Sets the default string to return if no elements are added.
     *
     * @param emptyResult The default string for an empty result.
     * @return this instance for chaining.
     */
    public StringJoiner setEmptyResult(final String emptyResult) {
        this.emptyResult = emptyResult;
        return this;
    }

    /**
     * Appends an object to the joiner. This method handles various types, including arrays, iterators, iterables, and
     * map entries.
     *
     * @param object The object to append.
     * @return this instance for chaining.
     */
    public StringJoiner append(final Object object) {
        if (null == object) {
            append((CharSequence) null);
        } else if (ArrayKit.isArray(object)) {
            append(new ArrayIterator<>(object));
        } else if (object instanceof Iterator) {
            append((Iterator<?>) object);
        } else if (object instanceof Iterable) {
            append(((Iterable<?>) object).iterator());
        } else if (object instanceof Map.Entry<?, ?> entry) {
            append(entry.getKey()).append(entry.getValue());
        } else {
            append(Convert.toString(object));
        }
        return this;
    }

    /**
     * Appends the elements of an array to this joiner.
     *
     * @param <T>   The type of the elements.
     * @param array The array of elements.
     * @return this instance for chaining.
     */
    public <T> StringJoiner append(final T[] array) {
        if (null == array) {
            return this;
        }
        return append(new ArrayIterator<>(array));
    }

    /**
     * Appends the elements of an {@link Iterator} to this joiner.
     *
     * @param <T>      The type of the elements.
     * @param iterator The iterator.
     * @return this instance for chaining.
     */
    public <T> StringJoiner append(final Iterator<T> iterator) {
        if (null != iterator) {
            while (iterator.hasNext()) {
                append(iterator.next());
            }
        }
        return this;
    }

    /**
     * Appends the elements of an array, converting each to a string using a provided function.
     *
     * @param <T>       The type of the elements.
     * @param array     The array of elements.
     * @param toStrFunc A function to convert each element to a string.
     * @return this instance for chaining.
     */
    public <T> StringJoiner append(final T[] array, final Function<T, ? extends CharSequence> toStrFunc) {
        return append((Iterator<T>) new ArrayIterator<>(array), toStrFunc);
    }

    /**
     * Appends the elements of an {@link Iterable}, converting each to a string using a provided function.
     *
     * @param <E>       The type of the elements.
     * @param iterable  The iterable.
     * @param toStrFunc A function to convert each element to a string.
     * @return this instance for chaining.
     */
    public <E> StringJoiner append(
            final Iterable<E> iterable,
            final Function<? super E, ? extends CharSequence> toStrFunc) {
        return append(IteratorKit.getIter(iterable), toStrFunc);
    }

    /**
     * Appends the elements of an {@link Iterator}, converting each to a string using a provided function.
     *
     * @param <E>       The type of the elements.
     * @param iterator  The iterator.
     * @param toStrFunc A function to convert each element to a string.
     * @return this instance for chaining.
     */
    public <E> StringJoiner append(
            final Iterator<E> iterator,
            final Function<? super E, ? extends CharSequence> toStrFunc) {
        if (null != iterator) {
            while (iterator.hasNext()) {
                append(toStrFunc.apply(iterator.next()));
            }
        }
        return this;
    }

    /**
     * Append method.
     *
     * @return the StringJoiner value
     */
    @Override
    public StringJoiner append(final CharSequence csq) {
        return append(csq, 0, StringKit.length(csq));
    }

    /**
     * Append method.
     *
     * @return the StringJoiner value
     */
    @Override
    public StringJoiner append(CharSequence csq, final int startInclude, int endExclude) {
        if (null == csq) {
            switch (this.nullMode) {
                case IGNORE:
                    return this;

                case TO_EMPTY:
                    csq = Normal.EMPTY;
                    break;

                case NULL_STRING:
                    csq = Normal.NULL;
                    endExclude = Normal.NULL.length();
                    break;
            }
        }
        try {
            final Appendable appendable = prepare();
            if (wrapElement && StringKit.isNotEmpty(this.prefix)) {
                appendable.append(prefix);
            }
            appendable.append(csq, startInclude, endExclude);
            if (wrapElement && StringKit.isNotEmpty(this.suffix)) {
                appendable.append(suffix);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Append method.
     *
     * @return the StringJoiner value
     */
    @Override
    public StringJoiner append(final char c) {
        return append(String.valueOf(c));
    }

    /**
     * Merges another {@code StringJoiner} into this one.
     *
     * @param stringJoiner The other {@code StringJoiner}.
     * @return this instance for chaining.
     */
    public StringJoiner merge(final StringJoiner stringJoiner) {
        if (null != stringJoiner && null != stringJoiner.appendable) {
            final String otherStr = stringJoiner.toString();
            if (stringJoiner.wrapElement) {
                this.append(otherStr);
            } else {
                this.append(otherStr, StringKit.length(this.prefix), otherStr.length());
            }
        }
        return this;
    }

    /**
     * Returns the current length of the joined string.
     *
     * @return The length, or -1 if the result would be null.
     */
    public int length() {
        return (this.appendable != null ? this.appendable.toString().length() + StringKit.length(suffix)
                : (this.emptyResult == null ? -1 : this.emptyResult.length()));
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        if (null == this.appendable) {
            return emptyResult;
        }
        String result = this.appendable.toString();
        if (!wrapElement && StringKit.isNotEmpty(this.suffix)) {
            result += this.suffix;
        }
        return result;
    }

    /**
     * Prepares the {@link Appendable} for appending a new element, adding the prefix on first use and the delimiter on
     * subsequent uses.
     *
     * @return The {@link Appendable}.
     * @throws IOException if an I/O error occurs.
     */
    private Appendable prepare() throws IOException {
        if (hasContent) {
            if (null != delimiter) {
                this.appendable.append(delimiter);
            }
        } else {
            if (null == this.appendable) {
                this.appendable = new StringBuilder();
            }
            if (!wrapElement && StringKit.isNotEmpty(this.prefix)) {
                this.appendable.append(this.prefix);
            }
            this.hasContent = true;
        }
        return this.appendable;
    }

    /**
     * Checks if the provided {@link Appendable} already contains content.
     *
     * @param appendable The {@link Appendable} to check.
     */
    private void checkHasContent(final Appendable appendable) {
        if (appendable instanceof CharSequence charSequence) {
            if (!charSequence.isEmpty() && StringKit.endWith(charSequence, delimiter)) {
                this.hasContent = true;
            }
        } else {
            final String initStr = appendable.toString();
            if (StringKit.isNotEmpty(initStr) && !StringKit.endWith(initStr, delimiter)) {
                this.hasContent = true;
            }
        }
    }

    /**
     * Defines the strategy for handling null elements.
     */
    public enum NullMode {
        /**
         * Ignores null elements.
         */
        IGNORE,
        /**
         * Converts null elements to an empty string ("").
         */
        TO_EMPTY,
        /**
         * Converts null elements to the string "null".
         */
        NULL_STRING
    }

}
