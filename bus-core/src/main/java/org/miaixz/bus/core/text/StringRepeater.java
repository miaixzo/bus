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

import java.util.Arrays;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * String or character repeater. Used to repeat a given string or character a specified number of times and then
 * concatenate them.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringRepeater {

    /**
     * The number of repetitions or the fixed length.
     */
    private final int countOrLength;

    /**
     * Constructs a new {@code StringRepeater} with the specified count or length.
     *
     * @param countOrLength The number of repetitions or the fixed length.
     */
    public StringRepeater(final int countOrLength) {
        this.countOrLength = countOrLength;
    }

    /**
     * Creates a {@code StringRepeater} instance.
     *
     * @param countOrLength The number of repetitions or the fixed length.
     * @return A new {@code StringRepeater} instance.
     */
    public static StringRepeater of(final int countOrLength) {
        return new StringRepeater(countOrLength);
    }

    /**
     * Repeats a character a specified number of times.
     *
     * <pre>
     * repeat('e', 0)  = ""
     * repeat('e', 3)  = "eee"
     * repeat('e', -2) = ""
     * </pre>
     *
     * @param c The character to repeat.
     * @return The string consisting of the repeated character.
     */
    public String repeat(final char c) {
        final int count = this.countOrLength;
        if (count <= 0) {
            return Normal.EMPTY;
        }

        final char[] result = new char[count];
        Arrays.fill(result, c);
        return new String(result);
    }

    /**
     * Repeats a string a specified number of times.
     *
     * @param text The string to repeat.
     * @return The string consisting of the repeated string.
     * @throws ArrayIndexOutOfBoundsException if the required string length is too large.
     */
    public String repeat(final CharSequence text) {
        if (null == text) {
            return null;
        }

        final int count = this.countOrLength;
        if (count <= 0 || text.isEmpty()) {
            return Normal.EMPTY;
        }
        if (count == 1) {
            return text.toString();
        }

        // Check for overflow
        final int len = text.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required String length is too large: " + longSize);
        }

        // More efficient than using StringBuilder
        final char[] array = new char[size];
        text.toString().getChars(0, len, array, 0);
        int n;
        for (n = len; n < size - n; n <<= 1) {// n <<= 1 is equivalent to n * 2
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }

    /**
     * Repeats a string to a specified total length.
     * <ul>
     * <li>If the specified length is not an integer multiple of the string's length, it is truncated to the fixed
     * length.</li>
     * <li>If the specified length is less than the string's own length, it is truncated.</li>
     * </ul>
     *
     * @param text The string to repeat.
     * @return The string repeated to the specified length.
     */
    public String repeatByLength(final CharSequence text) {
        if (null == text) {
            return null;
        }

        final int padLen = this.countOrLength;
        if (padLen <= 0) {
            return Normal.EMPTY;
        }
        final int strLen = text.length();
        if (strLen == padLen) {
            return text.toString();
        } else if (strLen > padLen) {
            return StringKit.subPre(text, padLen);
        }

        // Repeat until the specified length is reached
        final char[] padding = new char[padLen];
        for (int i = 0; i < padLen; i++) {
            padding[i] = text.charAt(i % strLen);
        }
        return new String(padding);
    }

    /**
     * Repeats a string and joins the repetitions with a delimiter.
     *
     * <pre>
     * repeatAndJoin("?", 5, ",")   = "?,?,?,?,?"
     * repeatAndJoin("?", 0, ",")   = ""
     * repeatAndJoin("?", 5, null) = "?????"
     * </pre>
     *
     * @param text      The string to repeat.
     * @param delimiter The delimiter to use between repetitions.
     * @return The joined string.
     */
    public String repeatAndJoin(final CharSequence text, final CharSequence delimiter) {
        int count = this.countOrLength;
        if (count <= 0) {
            return Normal.EMPTY;
        }
        if (StringKit.isEmpty(delimiter)) {
            return repeat(text);
        }

        // Initial size = total length of all repeated strings + total length of delimiters
        final StringBuilder builder = new StringBuilder(text.length() * count + delimiter.length() * (count - 1));
        builder.append(text);
        count--;

        while (count-- > 0) {
            builder.append(delimiter).append(text);
        }
        return builder.toString();
    }

}
