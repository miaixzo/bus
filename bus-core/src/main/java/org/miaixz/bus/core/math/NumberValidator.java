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
 * Number validator.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumberValidator {

    /**
     * The tolerance value for double comparison. Two double values are considered equal if their absolute difference is
     * less than this value.
     */
    public static final double DOUBLE_EPSILON = 1e-6;
    /**
     * The tolerance value for float comparison. Two float values are considered equal if their absolute difference is
     * less than this value.
     */
    public static final double FLOAT_EPSILON = 1e-5;

    /**
     * Checks if a float value is equal to zero within a small tolerance.
     * 
     * @param val The float value.
     * @return {@code true} if the value is close to zero.
     */
    public static boolean isEqualToZero(float val) {
        return Math.copySign(val, 1.0) < FLOAT_EPSILON;
    }

    /**
     * Checks if a float value is not equal to zero within a small tolerance.
     * 
     * @param val The float value.
     * @return {@code true} if the value is not close to zero.
     */
    public static boolean isDifferentFromZero(float val) {
        return Math.copySign(val, 1.0) > FLOAT_EPSILON;
    }

    /**
     * Checks if two float values are equal within a small tolerance.
     * 
     * @param a The first float value.
     * @param b The second float value.
     * @return {@code true} if the values are close to each other.
     */
    public static boolean isEqual(float a, float b) {
        return Math.copySign(a - b, 1.0) <= FLOAT_EPSILON || (a == b) || (Float.isNaN(a) && Float.isNaN(b));
    }

    /**
     * Checks if two float values are not equal within a small tolerance.
     * 
     * @param a The first float value.
     * @param b The second float value.
     * @return {@code true} if the values are not close to each other.
     */
    public static boolean isDifferent(float a, float b) {
        return Math.copySign(a - b, 1.0) >= FLOAT_EPSILON;
    }

    /**
     * Checks if a double value is equal to zero within a small tolerance.
     * 
     * @param val The double value.
     * @return {@code true} if the value is close to zero.
     */
    public static boolean isEqualToZero(double val) {
        return Math.copySign(val, 1.0) < DOUBLE_EPSILON;
    }

    /**
     * Checks if a double value is not equal to zero within a small tolerance.
     * 
     * @param val The double value.
     * @return {@code true} if the value is not close to zero.
     */
    public static boolean isDifferentFromZero(double val) {
        return Math.copySign(val, 1.0) > DOUBLE_EPSILON;
    }

    /**
     * Checks if two double values are equal within a small tolerance.
     * 
     * @param a The first double value.
     * @param b The second double value.
     * @return {@code true} if the values are close to each other.
     */
    public static boolean isEqual(double a, double b) {
        return Math.copySign(a - b, 1.0) <= DOUBLE_EPSILON || (a == b) || (Double.isNaN(a) && Double.isNaN(b));
    }

    /**
     * Checks if two double values are not equal within a small tolerance.
     * 
     * @param a The first double value.
     * @param b The second double value.
     * @return {@code true} if the values are not close to each other.
     */
    public static boolean isDifferent(double a, double b) {
        return Math.copySign(a - b, 1.0) >= DOUBLE_EPSILON;
    }

    /**
     * Checks if the character sequence is a number. Supported formats include:
     *
     * <pre>
     * 1. Decimal
     * 2. Hexadecimal (starting with 0x)
     * 3. Scientific notation (e.g., 1234E3)
     * 4. Type-qualified format (e.g., 123D)
     * 5. Signed format (e.g., +123, -234)
     * 6. Octal (starting with 0)
     * </pre>
     *
     * @param text The character sequence to check. It should not contain any whitespace.
     * @return {@code true} if the character sequence is a number, {@code false} otherwise.
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
     * Checks if the string is an integer.
     *
     * <p>
     * Supported formats:
     * <ol>
     * <li>Decimal, cannot contain leading zeros.</li>
     * <li>Octal (starting with 0).</li>
     * <li>Hexadecimal (starting with 0x or 0X).</li>
     * </ol>
     *
     * @param s The string to validate. It can only contain signs, digits, and {@literal X/x}.
     * @return {@code true} if the string is an {@link Integer}, {@code false} otherwise.
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
     * Checks if the string is a Long.
     * <p>
     * Supported formats:
     * <ol>
     * <li>Decimal, cannot contain leading zeros.</li>
     * <li>Octal (starting with 0).</li>
     * <li>Hexadecimal (starting with 0x or 0X).</li>
     * </ol>
     *
     * @param s The string to validate. It can only contain signs, digits, {@literal X/x}, and the suffix
     *          {@literal L/l}.
     * @return {@code true} if the string is a {@link Long}, {@code false} otherwise.
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
     * Checks if the string is a floating-point number.
     *
     * @param s The string to check.
     * @return {@code true} if the string is a {@link Double}, {@code false} otherwise.
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
     * Checks if a number is a prime number. A prime number is a natural number greater than 1 that has no positive
     * divisors other than 1 and itself.
     *
     * @param n The number to check.
     * @return {@code true} if the number is prime, {@code false} otherwise.
     */
    public static boolean isPrime(final int n) {
        Assert.isTrue(n > 1, "The number must be > 1");
        if (n <= 3) {
            return true;
        } else if ((n & 1) == 0) {
            // Quickly exclude even numbers.
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
