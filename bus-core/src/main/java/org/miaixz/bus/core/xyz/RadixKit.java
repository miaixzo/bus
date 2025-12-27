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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.lang.Assert;

/**
 * Radix (base) conversion utility class. This can be used to convert a decimal integer to a custom-defined base.
 * <p>
 * Applications include:
 * <ul>
 * <li>Generating short, non-guessable invitation codes from IDs.</li>
 * <li>Generating short URLs.</li>
 * <li>Obfuscating numbers through multiple radix conversions.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RadixKit {

    /**
     * Encodes an integer into a custom radix string. The length of the `radixs` string determines the base.
     *
     * @param radixs The custom character set for the radix (e.g., "0123456789abcdef"). Must not contain duplicate
     *               characters.
     * @param num    The number to convert.
     * @return The custom radix string.
     */
    public static String encode(final String radixs, final int num) {
        final long tmpNum = (num >= 0 ? num : (0x100000000L - (~num + 1)));
        return encode(radixs, tmpNum, 32);
    }

    /**
     * Encodes a long into a custom radix string.
     *
     * @param radixs The custom character set for the radix.
     * @param num    The number to convert.
     * @return The custom radix string.
     */
    public static String encode(final String radixs, final long num) {
        if (num < 0) {
            throw new RuntimeException("Negative numbers are not supported yet.");
        }
        return encode(radixs, num, 64);
    }

    /**
     * Decodes a custom radix string back to an integer value.
     *
     * @param radixs The custom character set (must be the same as used for encoding).
     * @param encode The string to decode.
     * @return The decoded integer.
     */
    public static int decodeToInt(final String radixs, final String encode) {
        return (int) decode(radixs, encode);
    }

    /**
     * Decodes a custom radix string back to a long value.
     *
     * @param radixs The custom character set (must be the same as used for encoding).
     * @param encode The string to decode.
     * @return The decoded long.
     */
    public static long decode(final String radixs, final String encode) {
        Assert.notNull(radixs, "radixs must not be null");
        Assert.notEmpty(encode, "encode must not be empty");

        final int rl = radixs.length();
        Assert.isTrue(rl >= 2, "radixs must be at least 2 characters");
        long res = 0L;

        for (final char c : encode.toCharArray()) {
            final int idx = radixs.indexOf(c);
            Assert.isTrue(idx >= 0, "Illegal character '" + c + "' for radixs");
            res = res * rl + idx;
        }
        return res;
    }

    /**
     * Private helper for encoding.
     *
     * @param radixs    The custom radix characters.
     * @param num       The number.
     * @param maxLength The max length of the result.
     * @return The encoded string.
     */
    private static String encode(final String radixs, long num, final int maxLength) {
        if (radixs.length() < 2) {
            throw new RuntimeException("Custom radix must have at least two characters!");
        }
        final int rl = radixs.length();
        final char[] aa = new char[maxLength];
        int i = aa.length;
        do {
            aa[--i] = radixs.charAt((int) (num % rl));
            num /= rl;
        } while (num > 0);
        return new String(aa, i, aa.length - i);
    }

}
