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
package org.miaixz.bus.core.text.replacer;

import java.io.Serial;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * A string replacer that operates on a specified character range. It replaces all characters within the given range
 * with a specified replacement character. The length of the string remains unchanged after replacement. This
 * implementation uses {@link String#codePoints()} for splitting and replacement when {@code isCodePoint} is true.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharRangeReplacer extends StringReplacer {

    @Serial
    private static final long serialVersionUID = 2852238751018L;

    /**
     * The inclusive starting index of the character range to be replaced.
     */
    private final int beginInclude;
    /**
     * The exclusive ending index of the character range to be replaced.
     */
    private final int endExclude;
    /**
     * The character used for replacement within the specified range.
     */
    private final char replacedChar;
    /**
     * Flag indicating whether to treat the input as code points (true) or characters (false).
     */
    private final boolean isCodePoint;

    /**
     * Constructs a new {@code CharRangeReplacer}.
     *
     * @param beginInclude The inclusive starting position of the range.
     * @param endExclude   The exclusive ending position of the range.
     * @param replacedChar The character to replace the characters within the range with.
     * @param isCodePoint  {@code true} to treat the input as code points (e.g., for emoji), {@code false} for
     *                     characters.
     */
    public CharRangeReplacer(final int beginInclude, final int endExclude, final char replacedChar,
            final boolean isCodePoint) {
        this.beginInclude = beginInclude;
        this.endExclude = endExclude;
        this.replacedChar = replacedChar;
        this.isCodePoint = isCodePoint;
    }

    /**
     * Applies the character range replacement logic to the given text. Characters within the specified range
     * ({@code beginInclude} to {@code endExclude}) are replaced with {@code replacedChar}, while characters outside
     * this range remain unchanged.
     *
     * @param text The character sequence to be processed.
     * @return The character sequence after the range replacement has been applied.
     */
    @Override
    public String apply(final CharSequence text) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toStringOrNull(text);
        }

        final String originalStr = text.toString();
        final int[] chars = StringKit.toChars(originalStr, this.isCodePoint);
        final int strLength = chars.length;

        final int beginInclude = this.beginInclude;
        if (beginInclude > strLength) {
            return originalStr;
        }
        int endExclude = this.endExclude;
        if (endExclude > strLength) {
            endExclude = strLength;
        }
        if (beginInclude > endExclude) {
            // If the start position is greater than the end position, no replacement occurs.
            return originalStr;
        }

        // New string length remains unchanged
        final StringBuilder stringBuilder = new StringBuilder(originalStr.length());
        for (int i = 0; i < strLength; i++) {
            if (i >= beginInclude && i < endExclude) {
                // All characters within the range are replaced
                replace(originalStr, i, stringBuilder);
            } else {
                // Other characters are retained
                append(stringBuilder, chars[i]);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Appends the {@code replacedChar} to the output. This method is called for each character within the replacement
     * range.
     *
     * @param text The original text (not directly used in this implementation).
     * @param pos  The current position (not directly used in this implementation).
     * @param out  The {@code StringBuilder} to which the {@code replacedChar} is appended.
     * @return The number of characters consumed, which is 1 for a single character replacement.
     */
    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        out.appendCodePoint(replacedChar);
        return 1; // Consumes one character
    }

    /**
     * Appends a character (or code point) to the {@code StringBuilder}.
     *
     * @param stringBuilder The {@code StringBuilder} to append to.
     * @param c             The character or code point to append.
     */
    private void append(final StringBuilder stringBuilder, final int c) {
        if (isCodePoint) {
            stringBuilder.appendCodePoint(c);
        } else {
            stringBuilder.append((char) c);
        }
    }

}
