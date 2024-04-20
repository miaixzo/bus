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

/**
 * Translates codepoints to their Unicode escaped value suitable for Java source.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JavaUnicodeEscaper extends UnicodeEscaper {

    /**
     * <p>
     * Constructs a <code>JavaUnicodeEscaper</code> for the specified range. This is the underlying method for the
     * other constructors/builders. The <code>below</code> and <code>above</code> boundaries are inclusive when
     * <code>between</code> is <code>true</code> and exclusive when it is <code>false</code>.
     * </p>
     *
     * @param below   int value representing the lowest codepoint boundary
     * @param above   int value representing the highest codepoint boundary
     * @param between whether to escape between the boundaries or outside them
     */
    public JavaUnicodeEscaper(final int below, final int above, final boolean between) {
        super(below, above, between);
    }

    /**
     * <p>
     * Constructs a <code>JavaUnicodeEscaper</code> above the specified value (exclusive).
     * </p>
     *
     * @param codepoint above which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static JavaUnicodeEscaper above(final int codepoint) {
        return outsideOf(0, codepoint);
    }

    /**
     * <p>
     * Constructs a <code>JavaUnicodeEscaper</code> below the specified value (exclusive).
     * </p>
     *
     * @param codepoint below which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static JavaUnicodeEscaper below(final int codepoint) {
        return outsideOf(codepoint, Integer.MAX_VALUE);
    }

    /**
     * <p>
     * Constructs a <code>JavaUnicodeEscaper</code> between the specified values (inclusive).
     * </p>
     *
     * @param codepointLow  above which to escape
     * @param codepointHigh below which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static JavaUnicodeEscaper between(final int codepointLow, final int codepointHigh) {
        return new JavaUnicodeEscaper(codepointLow, codepointHigh, true);
    }

    /**
     * <p>
     * Constructs a <code>JavaUnicodeEscaper</code> outside of the specified values (exclusive).
     * </p>
     *
     * @param codepointLow  below which to escape
     * @param codepointHigh above which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static JavaUnicodeEscaper outsideOf(final int codepointLow, final int codepointHigh) {
        return new JavaUnicodeEscaper(codepointLow, codepointHigh, false);
    }

    /**
     * Converts the given codepoint to a hex string of the form {@code "\\uXXXX\\uXXXX"}.
     *
     * @param codepoint a Unicode code point
     * @return the hex string for the given codepoint
     */
    @Override
    protected String toUtf16Escape(final int codepoint) {
        final char[] surrogatePair = Character.toChars(codepoint);
        return Symbol.UNICODE_START_CHAR + hex(surrogatePair[0]) + Symbol.UNICODE_START_CHAR + hex(surrogatePair[1]);
    }

}
