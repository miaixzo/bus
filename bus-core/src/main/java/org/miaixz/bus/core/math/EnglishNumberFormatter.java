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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Converts a number of a floating-point type into its English representation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EnglishNumberFormatter {

    /**
     * Constructs a new EnglishNumberFormatter. Utility class constructor for static access.
     */
    private EnglishNumberFormatter() {
    }

    /**
     * Converts an Arabic numeral to its English expression.
     *
     * @param x The Arabic numeral, which can be a {@link Number} object or a plain object. It will ultimately be
     *          processed as a string.
     * @return The English expression.
     */
    public static String format(final Object x) {
        if (x != null) {
            return format(x.toString());
        } else {
            return Normal.EMPTY;
        }
    }

    /**
     * Converts an Arabic numeral to a concise counting unit, e.g., 2100 becomes 2.1k. The default range only goes up to
     * 'w' (wan, for Chinese units).
     *
     * @param value The number to be formatted.
     * @return The formatted number string.
     */
    public static String formatSimple(final long value) {
        return formatSimple(value, true);
    }

    /**
     * Converts a number to a short form. The format units are abbreviated as: k (thousand), m (million), b (billion), t
     * (trillion). If using Chinese units, 'w' (wan) is used.
     * <ul>
     * <li>1000 = 1k</li>
     * <li>10000 = 10k, or 1w if using Chinese units.</li>
     * <li>100000 = 100k, or 10w if using Chinese units.</li>
     * </ul>
     *
     * @param number   The number to convert.
     * @param isCNUnit Whether to use Chinese units (e.g., 'w' for wan).
     * @return The abbreviated string representation.
     */
    public static String formatSimple(final long number, final boolean isCNUnit) {
        if (number < 1_000) {
            return String.valueOf(number);
        }

        double value;
        String suffix;

        // Use international system of units: k (kilo), m (mega), b (giga), t (tera).
        if (number < 1_000_000) {
            value = number / 1_000.0;
            suffix = "k";
        } else if (number < 1_000_000_000) {
            value = number / 1_000_000.0;
            suffix = "m";
        } else if (number < 1_000_000_000_000L) {
            value = number / 1_000_000_000.0;
            suffix = "b";
        } else {
            value = number / 1_000_000_000_000.0;
            suffix = "t";
        }

        // Compatible with Chinese short form, e.g., 10k -> 1w.
        if (isCNUnit) {
            if ("m".equals(suffix)) {
                suffix = "w";
                value *= 100;
            } else if ("k".equals(suffix) && value >= 10) {
                suffix = "w";
                value /= 10;
            }
        }

        // Format the number to at most two decimal places, removing trailing zeros.
        return MathKit.format("#.##", value) + suffix;
    }

    /**
     * Converts an Arabic numeral string to its English expression.
     *
     * @param x The Arabic numeral string.
     * @return The English expression.
     */
    private static String format(final String x) {
        final int z = x.indexOf(Symbol.DOT); // Get the position of the decimal point.
        final String lstr;
        String rstr = Normal.EMPTY;
        if (z > -1) { // Check for a decimal part.
            lstr = x.substring(0, z);
            rstr = x.substring(z + 1);
        } else {
            // No decimal part.
            lstr = x;
        }

        String lstrrev = StringKit.reverse(lstr); // Reverse the integer part string.
        final String[] a = new String[5]; // Array to store three-digit groups.

        switch (lstrrev.length() % 3) {
            case 1:
                lstrrev += "00";
                break;

            case 2:
                lstrrev += "0";
                break;
        }
        StringBuilder lm = new StringBuilder(); // Stores the converted integer part.
        for (int i = 0; i < lstrrev.length() / 3; i++) {
            a[i] = StringKit.reverse(lstrrev.substring(3 * i, 3 * i + 3)); // Get a three-digit group.
            if (!"000".equals(a[i])) {
                if (i != 0) {
                    lm.insert(0, transThree(a[i]) + Symbol.SPACE + parseMore(i) + Symbol.SPACE); // Add thousand,
                                                                                                 // million, billion.
                } else {
                    lm = new StringBuilder(transThree(a[i]));
                }
            } else {
                lm.append(transThree(a[i]));
            }
        }

        String xs = lm.isEmpty() ? "ZERO " : Symbol.SPACE; // Stores the converted decimal part.
        if (z > -1) {
            xs += "AND CENTS " + transTwo(rstr) + Symbol.SPACE; // Convert the decimal part if it exists.
        }

        return lm.toString().trim() + xs + "ONLY";
    }

    /**
     * Parses a number between 10 and 19.
     * 
     * @param x The number string.
     * @return The English word for the number.
     */
    private static String parseTeen(final String x) {
        return Normal.EN_NUMBER_TEEN[Integer.parseInt(x) - 10];
    }

    /**
     * Parses a multiple of ten.
     * 
     * @param x The number string.
     * @return The English word for the number.
     */
    private static String parseTen(final String x) {
        return Normal.EN_NUMBER_TEN[Integer.parseInt(x.substring(0, 1)) - 1];
    }

    /**
     * Parses the scale of the number (thousand, million, etc.).
     * 
     * @param i The index of the scale.
     * @return The English word for the scale.
     */
    private static String parseMore(final int i) {
        return Normal.EN_NUMBER_MORE[i];
    }

    /**
     * Converts a two-digit number string to its English representation.
     *
     * @param x The two-digit string.
     * @return The English representation.
     */
    private static String transTwo(String x) {
        final String value;
        // Ensure the string is two digits long.
        if (x.length() > 2) {
            x = x.substring(0, 2);
        } else if (x.length() < 2) {
            // If a single digit appears in the decimal part, treat it as cents.
            x = x + "0";
        }

        if (x.startsWith("0")) { // e.g., 07 -> seven
            value = parseLast(x);
        } else if (x.startsWith("1")) { // e.g., 17 -> seventeen
            value = parseTeen(x);
        } else if (x.endsWith("0")) { // e.g., 20 -> twenty
            value = parseTen(x);
        } else {
            value = parseTen(x) + Symbol.SPACE + parseLast(x);
        }
        return value;
    }

    /**
     * Converts a three-digit number string to its English representation.
     *
     * @param x The three-digit string.
     * @return The English representation.
     */
    private static String transThree(final String x) {
        final String value;
        if (x.startsWith("0")) { // Less than 100.
            value = transTwo(x.substring(1));
        } else if ("00".equals(x.substring(1))) { // Divisible by 100.
            value = parseLast(x.substring(0, 1)) + " HUNDRED";
        } else {
            value = parseLast(x.substring(0, 1)) + " HUNDRED AND " + transTwo(x.substring(1));
        }
        return value;
    }

    /**
     * Parses the last digit of a number string.
     * 
     * @param s The number string.
     * @return The English word for the last digit.
     */
    private static String parseLast(final String s) {
        return Normal.EN_NUMBER[Integer.parseInt(s.substring(s.length() - 1))];
    }

}
