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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 数字检查器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumberValidator {

    public static final double DOUBLE_EPSILON = 1e-6;
    public static final double FLOAT_EPSILON = 1e-5;

    public static boolean isEqualToZero(float val) {
        return Math.copySign(val, 1.0) < FLOAT_EPSILON;
    }

    public static boolean isDifferentFromZero(float val) {
        return Math.copySign(val, 1.0) > FLOAT_EPSILON;
    }

    public static boolean isEqual(float a, float b) {
        return Math.copySign(a - b, 1.0) <= FLOAT_EPSILON || (a == b) || (Float.isNaN(a) && Float.isNaN(b));
    }

    public static boolean isDifferent(float a, float b) {
        return Math.copySign(a - b, 1.0) >= FLOAT_EPSILON;
    }

    public static boolean isEqualToZero(double val) {
        return Math.copySign(val, 1.0) < DOUBLE_EPSILON;
    }

    public static boolean isDifferentFromZero(double val) {
        return Math.copySign(val, 1.0) > DOUBLE_EPSILON;
    }

    public static boolean isEqual(double a, double b) {
        return Math.copySign(a - b, 1.0) <= DOUBLE_EPSILON || (a == b) || (Double.isNaN(a) && Double.isNaN(b));
    }

    public static boolean isDifferent(double a, double b) {
        return Math.copySign(a - b, 1.0) >= DOUBLE_EPSILON;
    }

    /**
     * 是否为数字，支持包括：
     *
     * <pre>
     * 1、10进制
     * 2、16进制数字（0x开头）
     * 3、科学计数法形式（1234E3）
     * 4、类型标识形式（123D）
     * 5、正负数标识形式（+123、-234）
     * 6、八进制数字(0开头)
     * </pre>
     *
     * @param text 字符串值, 不可以含有任何空白字符
     * @return 是否为数字
     */
    public static boolean isNumber(final CharSequence text) {
        if (StringKit.isBlank(text)) {
            return false;
        }
        final char[] chars = text.toString().toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        final int start = (chars[0] == Symbol.C_MINUS || chars[0] == Symbol.C_PLUS) ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0' && !StringKit.contains(text, Symbol.C_DOT)) { // leading 0, skip if is
                                                                                                // a decimal number
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') { // leading 0x/0X
                int i = start + 2;
                if (i == sz) {
                    return false;
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
            if (Character.isDigit(chars[start + 1])) {
                // leading 0, but not hex, must be octal
                int i = start + 1;
                for (; i < chars.length; i++) {
                    if (chars[i] < '0' || chars[i] > '7') {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == Symbol.C_PLUS || chars[i] == Symbol.C_MINUS) {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l' || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    /**
     * 判断字符串是否是整数
     *
     * <p>
     * 支持格式:
     * <ol>
     * <li>10进制, 不能包含前导零</li>
     * <li>8进制(以0开头)</li>
     * <li>16进制(以0x或者0X开头)</li>
     * </ol>
     *
     * @param s 校验的字符串, 只能含有 正负号、数字字符 和 {@literal X/x}
     * @return 是否为 {@link Integer}类型
     * @see Integer#decode(String)
     */
    public static boolean isInteger(final String s) {
        if (!isNumber(s)) {
            return false;
        }
        try {
            Integer.decode(s);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否是Long类型
     * <p>
     * 支持格式:
     * <ol>
     * <li>10进制, 不能包含前导零</li>
     * <li>8进制(以0开头)</li>
     * <li>16进制(以0x或者0X开头)</li>
     * </ol>
     *
     * @param s 校验的字符串, 只能含有 正负号、数字字符、{@literal X/x} 和 后缀{@literal L/l}
     * @return 是否为 {@link Long}类型
     */
    public static boolean isLong(final String s) {
        if (!isNumber(s)) {
            return false;
        }
        final char lastChar = s.charAt(s.length() - 1);
        if (lastChar == 'l' || lastChar == 'L') {
            return true;
        }
        try {
            Long.decode(s);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否是浮点数
     *
     * @param s String
     * @return 是否为 {@link Double}类型
     */
    public static boolean isDouble(final String s) {
        if (StringKit.isBlank(s)) {
            return false;
        }
        try {
            Double.parseDouble(s);
        } catch (final NumberFormatException ignore) {
            return false;
        }
        return s.contains(".");
    }

    /**
     * 是否是质数（素数） 质数表的质数又称素数。指整数在一个大于1的自然数中,除了1和此整数自身外,没法被其他自然数整除的数。
     *
     * @param n 数字
     * @return 是否是质数
     */
    public static boolean isPrime(final int n) {
        Assert.isTrue(n > 1, "The number must be > 1");
        if (n <= 3) {
            return true;
        } else if ((n & 1) == 0) {
            // 快速排除偶数
            return false;
        }
        final int end = (int) Math.sqrt(n);
        for (int i = 3; i <= end; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

}
