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
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * String stripper, used to strip prefixes and suffixes from strings. Emphasizes removing specified strings from one or
 * both ends. If one side does not exist, the other side's removal is unaffected.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringStripper implements UnaryOperator<CharSequence>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852233890053L;

    /**
     * The prefix to strip.
     */
    private final CharSequence prefix;
    /**
     * The suffix to strip.
     */
    private final CharSequence suffix;
    /**
     * Whether to ignore case during stripping.
     */
    private final boolean ignoreCase;
    /**
     * Whether to strip all occurrences of the prefix/suffix.
     */
    private final boolean stripAll;

    /**
     * Constructs a new {@code StringStripper} instance.
     *
     * @param prefix     The prefix to strip. {@code null} is ignored.
     * @param suffix     The suffix to strip. {@code null} is ignored.
     * @param ignoreCase Whether to ignore case during stripping.
     * @param stripAll   Whether to strip all occurrences of the prefix/suffix.
     */
    public StringStripper(final CharSequence prefix, final CharSequence suffix, final boolean ignoreCase,
            final boolean stripAll) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.ignoreCase = ignoreCase;
        this.stripAll = stripAll;
    }

    /**
     * Apply method.
     *
     * @return the String value
     */
    @Override
    public String apply(final CharSequence charSequence) {
        return this.stripAll ? stripAll(charSequence) : stripOnce(charSequence);
    }

    /**
     * Strips the specified prefix and suffix from both ends of a string, removing them only once. If characters exist
     * on both sides, they are removed; otherwise, no action is taken.
     *
     * <pre>{@code
     * "aaa_STRIPPED_bbb", "a", "b"  -> "aa_STRIPPED_bb"
     * "aaa_STRIPPED_bbb", null, null  -> "aaa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "", ""  -> "aaa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "", "b"  -> "aaa_STRIPPED_bb"
     * "aaa_STRIPPED_bbb", null, "b"  -> "aaa_STRIPPED_bb"
     * "aaa_STRIPPED_bbb", "a", ""  -> "aa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "a", null  -> "aa_STRIPPED_bbb"
     *
     * "a", "a", "a"  -> ""
     * }</pre>
     *
     * @param charSequence The string to process.
     * @return The processed string.
     */
    private String stripOnce(final CharSequence charSequence) {
        if (StringKit.isEmpty(charSequence)) {
            return StringKit.toStringOrNull(charSequence);
        }

        final String str = charSequence.toString();
        int from = 0;
        int to = str.length();

        if (StringKit.isNotEmpty(this.prefix) && startWith(str, this.prefix, 0)) {
            from = this.prefix.length();
            if (from == to) {
                // "a", "a", "a" -> ""
                return Normal.EMPTY;
            }
        }
        if (endWithSuffix(str)) {
            to -= this.suffix.length();
            if (from == to) {
                // "a", "a", "a" -> ""
                return Normal.EMPTY;
            } else if (to < from) {
                // If prefix removal overlaps with suffix, e.g., ("aba", "ab", "ba") -> "a"
                to += this.suffix.length();
            }
        }

        return str.substring(from, to);
    }

    /**
     * Strips all occurrences of the specified prefix and suffix from both ends of a string.
     *
     * <pre>{@code
     * "aaa_STRIPPED_bbb", "a", "b"  -> "_STRIPPED_"
     * "aaa_STRIPPED_bbb", null, null  -> "aaa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "", ""  -> "aaa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "", "b"  -> "aaa_STRIPPED_"
     * "aaa_STRIPPED_bbb", null, "b"  -> "aaa_STRIPPED_"
     * "aaa_STRIPPED_bbb", "a", ""  -> "_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "a", null  -> "_STRIPPED_bbb"
     *
     * // special test
     * "aaaaaabbb", "aaa", null  -> "bbb"
     * "aaaaaaabbb", "aa", null  -> "abbb"
     *
     * "aaaaaaaaa", "aaa", "aa"  -> ""
     * "a", "a", "a"  -> ""
     * }</pre>
     *
     * @param charSequence The string to process.
     * @return The processed string.
     */
    private String stripAll(final CharSequence charSequence) {
        if (StringKit.isEmpty(charSequence)) {
            return StringKit.toStringOrNull(charSequence);
        }

        final String str = charSequence.toString();
        int from = 0;
        int to = str.length();

        if (StringKit.isNotEmpty(this.prefix)) {
            while (startWith(str, this.prefix, from)) {
                from += this.prefix.length();
                if (from == to) {
                    // "a", "a", "a" -> ""
                    return Normal.EMPTY;
                }
            }
        }
        if (StringKit.isNotEmpty(suffix)) {
            final int suffixLength = this.suffix.length();
            while (startWith(str, suffix, to - suffixLength)) {
                to -= suffixLength;
                if (from == to) {
                    // "a", "a", "a" -> ""
                    return Normal.EMPTY;
                } else if (to < from) {
                    // If prefix removal overlaps with suffix, e.g., ("aba", "ab", "ba") -> "a"
                    to += suffixLength;
                    break;
                }
            }
        }

        return str.substring(from, to);
    }

    /**
     * Checks if the string starts with the specified prefix from a given position.
     *
     * @param charSequence The string to check.
     * @param strToCheck   The prefix string to check for.
     * @param from         The starting position in the string to check.
     * @return {@code true} if the string starts with the prefix at the specified position, {@code false} otherwise.
     */
    private boolean startWith(final CharSequence charSequence, final CharSequence strToCheck, final int from) {
        return new OffsetMatcher(this.ignoreCase, false, from).test(charSequence, strToCheck);
    }

    /**
     * Checks if the string ends with the specified suffix.
     *
     * @param charSequence The string to check.
     * @return {@code true} if the string ends with the suffix, {@code false} otherwise.
     */
    private boolean endWithSuffix(final CharSequence charSequence) {
        return StringKit.isNotEmpty(suffix) && StringKit.endWith(charSequence, suffix, ignoreCase);
    }

}
