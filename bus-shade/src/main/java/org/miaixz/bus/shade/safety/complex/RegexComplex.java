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
package org.miaixz.bus.shade.safety.complex;

import java.util.regex.Pattern;

import org.miaixz.bus.shade.safety.Complex;

/**
 * An abstract {@link Complex} implementation that filters entries based on regular expression matching.
 *
 * @param <E> The type of entry to be filtered.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class RegexComplex<E> implements Complex<E> {

    /**
     * The compiled regular expression pattern used for matching.
     */
    protected final Pattern pattern;

    /**
     * Constructs a new {@code RegexComplex} with the specified regular expression string. The string is compiled into a
     * {@link Pattern}.
     *
     * @param regex The regular expression string to use for filtering.
     */
    protected RegexComplex(String regex) {
        this(Pattern.compile(regex));
    }

    /**
     * Constructs a new {@code RegexComplex} with the specified compiled {@link Pattern}.
     *
     * @param pattern The compiled {@link Pattern} to use for filtering.
     */
    protected RegexComplex(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Evaluates the given entry against the configured regular expression pattern. The entry is first converted to a
     * string representation using {@link #toText(Object)}.
     *
     * @param entry The entry to be evaluated.
     * @return {@code true} if the string representation of the entry matches the regular expression; {@code false}
     *         otherwise.
     */
    @Override
    public boolean on(E entry) {
        String text = toText(entry);
        return pattern.matcher(text).matches();
    }

    /**
     * Converts an entry into its string representation for pattern matching. Subclasses must implement this method to
     * define how their specific entry type is represented as a string.
     *
     * @param entry The entry to convert.
     * @return The string representation of the entry.
     */
    protected abstract String toText(E entry);

}
