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
package org.miaixz.bus.core.codec;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * The Luhn algorithm, also known as the "mod 10" algorithm, is a simple checksum formula used to validate a variety of
 * identification numbers, such as credit card numbers, IMEI numbers, and National Provider Identifier numbers. The
 * validation steps are as follows:
 * <ol>
 * <li>Starting from the rightmost digit (the check digit), double the value of every second digit. If the result of
 * this doubling is greater than 9 (e.g., 7 * 2 = 14), sum the digits of the result (e.g., 1 + 4 = 5).</li>
 * <li>Sum all the digits obtained in step 1 (the doubled digits, after summing their own digits if necessary) and the
 * undoubled digits from the original number.</li>
 * <li>If the total sum modulo 10 is equal to 0, then the number is valid according to the Luhn algorithm.</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Luhn {

    /**
     * Checks if the given string is valid according to the Luhn algorithm.
     *
     * @param text The string containing the number to be checked.
     * @return {@code true} if the string is valid, {@code false} otherwise.
     * @throws IllegalArgumentException If the string is empty or not an 8-19 digit number.
     */
    public static boolean check(final String text) {
        if (StringKit.isBlank(text)) {
            return false;
        }
        if (!PatternKit.isMatch(Pattern.NUMBERS_PATTERN, text)) {
            // Must be all digits
            return false;
        }
        return sum(text) % 10 == 0;
    }

    /**
     * Calculates the check digit for a given number string. This method ignores any existing check digit and calculates
     * the last check digit based on the preceding N digits.
     *
     * @param text           The number string to calculate the check digit for.
     * @param withCheckDigit A boolean indicating whether the input {@code text} already includes a check digit. If
     *                       {@code true}, the last digit is removed before calculation.
     * @return The calculated check digit.
     */
    public static int getCheckDigit(String text, final boolean withCheckDigit) {
        if (withCheckDigit) {
            text = text.substring(0, text.length() - 1);
        }
        return 10 - (sum(text + "0") % 10);
    }

    /**
     * Calculates the sum of digits according to the Luhn algorithm. This private helper method performs the core
     * summation logic of the Luhn algorithm.
     *
     * @param text The number string for which to calculate the sum.
     * @return The sum of digits as per the Luhn algorithm.
     */
    private static int sum(final String text) {
        final char[] strArray = text.toCharArray();
        final int n = strArray.length;
        int sum = strArray[n - 1] - '0';
        for (int i = 2; i <= n; i++) {
            int a = strArray[n - i] - '0';
            // Double every second digit starting from the right
            if ((i & 1) == 0) {
                a *= 2;
            }
            // Add the digits of the doubled number (if > 9) or the number itself
            sum += a / 10 + a % 10;
        }
        return sum;
    }

}
