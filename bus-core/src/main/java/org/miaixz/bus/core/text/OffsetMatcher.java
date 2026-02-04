/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

    /**
     * Tests if the specified substring matches in the given text at the configured offset.
     *
     * @param text  The text to search in.
     * @param check The substring to check for.
     * @return {@code true} if the substring matches at the configured position, {@code false} otherwise.
     */
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
