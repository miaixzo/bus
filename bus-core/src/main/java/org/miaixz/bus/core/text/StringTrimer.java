/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.text;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * String head and tail specified character trimmer. Removes characters from the head and/or tail of a string based on a
 * predicate. If the string is {@code null}, {@code null} is returned.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringTrimer implements UnaryOperator<CharSequence>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852235152928L;

    /**
     * Trimmer instance to remove leading and trailing blank characters.
     */
    public static final StringTrimer TRIM_BLANK = new StringTrimer(TrimMode.BOTH, CharKit::isBlankChar);
    /**
     * Trimmer instance to remove leading blank characters.
     */
    public static final StringTrimer TRIM_PREFIX_BLANK = new StringTrimer(TrimMode.PREFIX, CharKit::isBlankChar);
    /**
     * Trimmer instance to remove trailing blank characters.
     */
    public static final StringTrimer TRIM_SUFFIX_BLANK = new StringTrimer(TrimMode.SUFFIX, CharKit::isBlankChar);

    /**
     * The trimming mode, specifying whether to trim from the prefix, suffix, or both.
     */
    private final TrimMode mode;
    /**
     * The predicate used to determine if a character should be trimmed. Returns {@code true} if the character should be
     * filtered out, {@code false} otherwise.
     */
    private final Predicate<Character> predicate;

    /**
     * Constructs a new {@code StringTrimer} with the specified trimming mode and predicate.
     *
     * @param mode      The trimming mode, specifying whether to trim from the prefix, suffix, or both.
     * @param predicate The predicate to determine if a character should be trimmed. Returns {@code true} if the
     *                  character should be filtered out, {@code false} otherwise.
     */
    public StringTrimer(final TrimMode mode, final Predicate<Character> predicate) {
        this.mode = mode;
        this.predicate = predicate;
    }

    /**
     * Apply method.
     *
     * @return the String value
     */
    @Override
    public String apply(final CharSequence text) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toStringOrNull(text);
        }

        final int length = text.length();
        int begin = 0;
        int end = length;

        if (mode == TrimMode.PREFIX || mode == TrimMode.BOTH) {
            // Scan the head of the string
            while ((begin < end) && (predicate.test(text.charAt(begin)))) {
                begin++;
            }
        }
        if (mode == TrimMode.SUFFIX || mode == TrimMode.BOTH) {
            // Scan the tail of the string
            while ((begin < end) && (predicate.test(text.charAt(end - 1)))) {
                end--;
            }
        }

        final String result;
        if ((begin > 0) || (end < length)) {
            result = text.toString().substring(begin, end);
        } else {
            result = text.toString();
        }

        return result;
    }

    /**
     * Trimming mode enumeration.
     */
    public enum TrimMode {
        /**
         * Trim from the head (prefix) of the string.
         */
        PREFIX,
        /**
         * Trim from the tail (suffix) of the string.
         */
        SUFFIX,
        /**
         * Trim from both the head and tail of the string.
         */
        BOTH
    }

}
