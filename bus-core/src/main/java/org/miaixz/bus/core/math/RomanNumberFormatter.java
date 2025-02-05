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
package org.miaixz.bus.core.math;

import org.miaixz.bus.core.lang.Normal;

/**
 * 数字和罗马数字转换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RomanNumberFormatter {

    /**
     * 整数转罗马数字 限制：[1,3999]的正整数
     * <ul>
     * <li>I 1</li>
     * <li>V 5</li>
     * <li>X 10</li>
     * <li>L 50</li>
     * <li>C 100</li>
     * <li>D 500</li>
     * <li>M 1000</li>
     * </ul>
     *
     * @param num [1,3999]的正整数
     * @return 罗马数字
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
     * 罗马数字转整数
     *
     * @param roman 罗马字符
     * @return 整数
     * @throws IllegalArgumentException 如果传入非罗马字符串，抛出异常
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
