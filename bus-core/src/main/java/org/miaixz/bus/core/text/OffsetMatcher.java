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
package org.miaixz.bus.core.text;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.BiPredicate;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * String region matcher, used to match whether a substring matches at the head, tail, or a specific position. The
 * offset is used to anchor the start or end position. A positive number indicates an offset from the beginning, and a
 * negative number indicates an offset from the end.
 *
 * <pre>
 *     a  b  c  d  e  f
 *     |  |        |  |
 *     0  1  c  d -2 -1
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OffsetMatcher implements BiPredicate<CharSequence, CharSequence>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852233335568L;

    /**
     * Whether to ignore case during comparison.
     */
    private final boolean ignoreCase;
    /**
     * Whether to ignore the case where the two strings are exactly equal.
     */
    private final boolean ignoreEquals;
    /**
     * The matching position. A positive number indicates an offset from the beginning, and a negative number indicates
     * an offset from the end.
     */
    private final int offset;

    /**
     * Constructs an {@code OffsetMatcher} for prefix or suffix matching.
     *
     * @param ignoreCase   Whether to ignore case during comparison.
     * @param ignoreEquals Whether to ignore the case where the two strings are exactly equal.
     * @param isPrefix     {@code true} for prefix matching, {@code false} for suffix matching.
     */
    public OffsetMatcher(final boolean ignoreCase, final boolean ignoreEquals, final boolean isPrefix) {
        this(ignoreCase, ignoreEquals, isPrefix ? 0 : -1);
    }

    /**
     * Constructs an {@code OffsetMatcher} with a specific offset.
     *
     * @param ignoreCase   Whether to ignore case during comparison.
     * @param ignoreEquals Whether to ignore the case where the two strings are exactly equal.
     * @param offset       The matching position. A positive number indicates an offset from the beginning, and a
     *                     negative number indicates an offset from the end.
     */
    public OffsetMatcher(final boolean ignoreCase, final boolean ignoreEquals, final int offset) {
        this.ignoreCase = ignoreCase;
        this.ignoreEquals = ignoreEquals;
        this.offset = offset;
    }

    @Override
    public boolean test(final CharSequence text, final CharSequence check) {
        if (null == text || null == check) {
            if (ignoreEquals) {
                return false;
            }
            return null == text && null == check;
        }

        final int strToCheckLength = check.length();
        final int toffset = this.offset >= 0 ? this.offset : text.length() - strToCheckLength + this.offset + 1;
        final boolean matches = text.toString()
                .regionMatches(ignoreCase, toffset, check.toString(), 0, strToCheckLength);

        if (matches) {
            return (!ignoreEquals) || (!StringKit.equals(text, check, ignoreCase));
        }
        return false;
    }

}
