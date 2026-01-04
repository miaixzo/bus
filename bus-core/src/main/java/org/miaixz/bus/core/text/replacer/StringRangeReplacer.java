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
package org.miaixz.bus.core.text.replacer;

import java.io.Serial;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * A string replacer that operates on a specified character range. It removes all characters within the given range and
 * replaces them with a single specified string. This implementation uses {@link String#codePoints()} for splitting and
 * replacement when {@code isCodePoint} is true.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringRangeReplacer extends StringReplacer {

    @Serial
    private static final long serialVersionUID = 2852239629301L;

    /**
     * The inclusive starting index of the character range to be replaced.
     */
    private final int beginInclude;
    /**
     * The exclusive ending index of the character range to be replaced.
     */
    private final int endExclude;
    /**
     * The string used for replacement within the specified range.
     */
    private final CharSequence replacedStr;
    /**
     * Flag indicating whether to treat the input as code points (true) or characters (false).
     */
    private final boolean isCodePoint;

    /**
     * Constructs a new {@code StringRangeReplacer}.
     *
     * @param beginInclude The inclusive starting position of the range.
     * @param endExclude   The exclusive ending position of the range.
     * @param replacedStr  The string to replace the characters within the range with.
     * @param isCodePoint  {@code true} to treat the input as code points (e.g., for emoji), {@code false} for
     *                     characters.
     */
    public StringRangeReplacer(final int beginInclude, final int endExclude, final CharSequence replacedStr,
            final boolean isCodePoint) {
        this.beginInclude = beginInclude;
        this.endExclude = endExclude;
        this.replacedStr = replacedStr;
        this.isCodePoint = isCodePoint;
    }

    /**
     * Applies the string range replacement logic to the given text. Characters within the specified range
     * ({@code beginInclude} to {@code endExclude}) are removed and replaced with {@code replacedStr}, while characters
     * outside this range remain unchanged.
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

        // The new string length will be approximately: original length - (number of code points in replaced range) +
        // length of replacement string
        final StringBuilder stringBuilder = new StringBuilder(
                originalStr.length() - (endExclude - beginInclude) + replacedStr.length());
        for (int i = 0; i < beginInclude; i++) {
            append(stringBuilder, chars[i]);
        }
        // The actual replacement happens here, appending the replacedStr once.
        replace(originalStr, beginInclude, stringBuilder);
        for (int i = endExclude; i < strLength; i++) {
            append(stringBuilder, chars[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Appends the {@code replacedStr} to the output. This method is called once to insert the replacement string for
     * the entire range.
     *
     * @param text The original text (not directly used in this implementation).
     * @param pos  The current position (not directly used in this implementation, as the range is handled by
     *             {@code apply}).
     * @param out  The {@code StringBuilder} to which the {@code replacedStr} is appended.
     * @return The value of {@code endExclude}, which is not directly used for consumption in this specific
     *         implementation but is returned to satisfy the abstract method's contract.
     */
    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        // Since this is a range replacement, the range is already determined, so just append the replacement string.
        out.append(this.replacedStr);

        // This return value is not meaningful for consumption in this specific implementation
        return endExclude;
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
