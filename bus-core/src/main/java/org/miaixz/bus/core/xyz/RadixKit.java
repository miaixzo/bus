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
