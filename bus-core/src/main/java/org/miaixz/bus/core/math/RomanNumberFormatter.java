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
package org.miaixz.bus.core.math;

import org.miaixz.bus.core.lang.Normal;

/**
 * Converts between integers and Roman numerals.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RomanNumberFormatter {

    /**
     * Converts an integer to a Roman numeral. The valid range for the input integer is [1, 3999].
     * <ul>
     * <li>I: 1</li>
     * <li>V: 5</li>
     * <li>X: 10</li>
     * <li>L: 50</li>
     * <li>C: 100</li>
     * <li>D: 500</li>
     * <li>M: 1000</li>
     * </ul>
     *
     * @param num The integer to convert, must be in the range [1, 3999].
     * @return The Roman numeral representation.
     */
    public static String intToRoman(final int num) {
        if (num > 3999 || num < 1) {
            return Normal.EMPTY;
        }
        final String[] thousands = { "", "M", "MM", "MMM" };
        final String[] hundreds = { "", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM" };
        final String[] tens = { "", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC" };
        final String[] ones = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX" };

        return thousands[num / 1000] + hundreds[(num % 1000) / 100] + tens[(num % 100) / 10] + ones[num % 10];
    }

    /**
     * Converts a Roman numeral to an integer.
     *
     * @param roman The Roman numeral string.
     * @return The integer value.
     * @throws IllegalArgumentException If the input string contains non-Roman characters.
     */
    public static int romanToInt(final String roman) {
        int result = 0;
        int prevValue = 0;
        int currValue;

        final char[] charArray = roman.toCharArray();
        for (int i = charArray.length - 1; i >= 0; i--) {
            final char c = charArray[i];
            switch (c) {
                case 'I':
                    currValue = 1;
                    break;

                case 'V':
                    currValue = 5;
                    break;

                case 'X':
                    currValue = 10;
                    break;

                case 'L':
                    currValue = 50;
                    break;

                case 'C':
                    currValue = 100;
                    break;

                case 'D':
                    currValue = 500;
                    break;

                case 'M':
                    currValue = 1000;
                    break;

                default:
                    throw new IllegalArgumentException("Invalid Roman character: " + c);
            }
            if (currValue < prevValue) {
                result -= currValue;
            } else {
                result += currValue;
            }

            prevValue = currValue;
        }
        return result;
    }

}
