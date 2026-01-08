/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A parser for converting strings to their corresponding numeric types. It supports:
 * <ul>
 * <li>Ignoring leading zeros for numbers starting with 0.</li>
 * <li>Returning 0 for empty strings.</li>
 * <li>Returning 0 for "NaN" (by default).</li>
 * <li>Converting other cases as base-10 numbers.</li>
 * <li>Parsing formats like ".123" as 0.123.</li>
 * </ul>
 *
 * <p>
 * The constructor allows specifying whether to convert "NaN" to 0. The default is true. See:
 * https://stackoverflow.com/questions/5876369/why-does-casting-double-nan-to-int-not-throw-an-exception-in-java
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumberParser {

    /**
     * Singleton instance.
     */
    public static final NumberParser INSTANCE = of(null);

    /**
     * The string representation of "Not a Number".
     */
    private static final String NaN = "NaN";

    /**
     * The locale to use for number formatting.
     */
    private final Locale locale;

    /**
     * Whether to convert "NaN" to 0 instead of throwing an exception.
     */
    private final boolean zeroIfNaN;

    /**
     * Constructor.
     *
     * @param locale    The locale to use, or {@code null} for the default locale.
     * @param zeroIfNaN If {@code true}, converts "NaN" to 0; otherwise, throws a {@link NumberFormatException}.
     */
    public NumberParser(final Locale locale, final boolean zeroIfNaN) {
        this.locale = locale;
        this.zeroIfNaN = zeroIfNaN;
    }

    /**
     * Creates a NumberParser.
     *
     * @param locale The locale to use, or {@code null} for the default locale.
     * @return A new NumberParser instance.
     */
    public static NumberParser of(final Locale locale) {
        return of(locale, true);
    }

    /**
     * Creates a NumberParser.
     *
     * @param locale    The locale to use, or {@code null} for the default locale.
     * @param zeroIfNaN If {@code true}, converts "NaN" to 0; otherwise, throws a {@link NumberFormatException}.
     * @return A new NumberParser instance.
     */
    public static NumberParser of(final Locale locale, final boolean zeroIfNaN) {
        return new NumberParser(locale, zeroIfNaN);
    }

    /**
     * Parses a string into an int based on the following rules:
     *
     * <pre>
     * 1. Strings starting with "0x" are treated as hexadecimal numbers.
     * 2. Leading zeros are ignored.
     * 3. Other cases are converted as base-10 numbers.
     * 4. Empty strings return 0.
     * 5. Formats like ".123" return 0 (treated as a decimal less than 1).
     * 6. For numbers like "123.56", the fractional part is truncated.
     * 7. Scientific notation throws a NumberFormatException.
     * </pre>
     *
     * @param numberStr The string to parse.
     * @return The parsed int.
     * @throws NumberFormatException If the string has an invalid number format.
     */
    public int parseInt(final String numberStr) throws NumberFormatException {
        if (isBlankOrNaN(numberStr)) {
            return 0;
        }

        if (StringKit.startWithIgnoreCase(numberStr, "0x")) {
            // "0x04" represents a hexadecimal number.
            return Integer.parseInt(numberStr.substring(2), 16);
        }

        if (StringKit.containsIgnoreCase(numberStr, "E")) {
            // Scientific notation is not supported because converting very large or small numbers to int results in
            // precision loss.
            throw new NumberFormatException(StringKit.format("Unsupported int format: [{}]", numberStr));
        }

        try {
            return Integer.parseInt(numberStr);
        } catch (final NumberFormatException e) {
            return doParse(numberStr).intValue();
        }
    }

    /**
     * Parses a char array into an int. This method is copied from {@link Integer#parseInt(String, int)} to avoid
     * creating a String object, thus reducing unnecessary copying. It automatically trims leading and trailing
     * whitespace.
     *
     * @param chars The char array to parse.
     * @param radix The radix to be used while parsing.
     * @return The parsed int.
     * @see Integer#parseInt(String, int)
     */
    public int parseInt(final char[] chars, final int radix) {
        if (ArrayKit.isEmpty(chars)) {
            throw new IllegalArgumentException("Empty chars!");
        }

        int result = 0;
        boolean negative = false;
        int i = 0;
        int limit = -Integer.MAX_VALUE;
        int digit;

        // Skip whitespace.
        while (CharKit.isBlankChar(chars[i])) {
            i++;
        }

        final char firstChar = chars[i];
        if (firstChar < Symbol.C_ZERO) { // Possible leading "+" or "-"
            if (firstChar == Symbol.C_MINUS) {
                negative = true;
                limit = Integer.MIN_VALUE;
            } else if (firstChar != Symbol.C_PLUS) {
                throw new NumberFormatException("Invalid first char: " + firstChar);
            }

            if (chars.length == 1) {
                // Cannot have a lone "+" or "-".
                throw new NumberFormatException("Invalid chars has lone: " + firstChar);
            }
            i++;
        }

        final int multmin = limit / radix;
        while (i < chars.length) {
            // Skip whitespace.
            if (CharKit.isBlankChar(chars[i])) {
                i++;
                continue;
            }

            // Accumulating negatively avoids surprises near MAX_VALUE.
            digit = Character.digit(chars[i++], radix);
            if (digit < 0) {
                throw new NumberFormatException(StringKit.format("Invalid chars: {} at {}", chars, i - 1));
            }
            if (result < multmin) {
                throw new NumberFormatException(StringKit.format("Invalid chars: {}", new Object[] { chars }));
            }
            result *= radix;
            if (result < limit + digit) {
                throw new NumberFormatException(StringKit.format("Invalid chars: {}", new Object[] { chars }));
            }
            result -= digit;
        }
        return negative ? result : -result;
    }

    /**
     * Parses a string into a long based on the following rules:
     *
     * <pre>
     * 1. Strings starting with "0x" are treated as hexadecimal numbers.
     * 2. Leading zeros are ignored.
     * 3. Empty strings return 0.
     * 4. Other cases are converted as base-10 numbers.
     * 5. Formats like ".123" return 0 (treated as a decimal less than 1).
     * 6. For numbers like "123.56", the fractional part is truncated.
     * </pre>
     *
     * @param numberStr The string to parse.
     * @return The parsed long.
     */
    public long parseLong(final String numberStr) {
        if (isBlankOrNaN(numberStr)) {
            return 0;
        }

        if (StringKit.startWithIgnoreCase(numberStr, "0x")) {
            // "0x04" represents a hexadecimal number.
            return Long.parseLong(numberStr.substring(2), 16);
        }

        try {
            return Long.parseLong(numberStr);
        } catch (final NumberFormatException e) {
            return doParse(numberStr).longValue();
        }
    }

    /**
     * Parses a string into a float based on the following rules:
     *
     * <pre>
     * 1. Leading zeros are ignored.
     * 2. Empty strings return 0.
     * 3. Other cases are converted as base-10 numbers.
     * 4. Formats like ".123" return 0.123.
     * </pre>
     *
     * @param numberStr The string to parse.
     * @return The parsed float.
     */
    public float parseFloat(final String numberStr) {
        if (isBlankOrNaN(numberStr)) {
            return 0;
        }

        try {
            return Float.parseFloat(numberStr);
        } catch (final NumberFormatException e) {
            return doParse(numberStr).floatValue();
        }
    }

    /**
     * Parses a string into a double based on the following rules:
     *
     * <pre>
     * 1. Leading zeros are ignored.
     * 2. Empty strings return 0.
     * 3. Other cases are converted as base-10 numbers.
     * 4. Formats like ".123" return 0.123.
     * 5. "NaN" returns 0.
     * </pre>
     *
     * @param numberStr The string to parse.
     * @return The parsed double.
     */
    public double parseDouble(final String numberStr) {
        if (isBlankOrNaN(numberStr)) {
            return 0;
        }

        try {
            return Double.parseDouble(numberStr);
        } catch (final NumberFormatException e) {
            return doParse(numberStr).doubleValue();
        }
    }

    /**
     * Parses a string into a {@link BigInteger}, supporting hexadecimal, decimal, and octal formats. Returns
     * {@code null} for blank strings.
     *
     * @param text The string to parse.
     * @return A {@link BigInteger} or {@code null}.
     */
    public BigInteger parseBigInteger(String text) {
        text = StringKit.trimToNull(text);
        if (null == text) {
            return null;
        }

        int pos = 0; // Position in the string.
        int radix = 10;
        boolean negate = false; // Whether the number is negative.
        if (text.startsWith(Symbol.MINUS)) {
            negate = true;
            pos = 1;
        }
        if (text.startsWith("0x", pos) || text.startsWith("0X", pos)) {
            // Hexadecimal
            radix = 16;
            pos += 2;
        } else if (text.startsWith(Symbol.HASH, pos)) {
            // Alternative hex (allowed by Long/Integer).
            radix = 16;
            pos++;
        } else if (text.startsWith("0", pos) && text.length() > pos + 1) {
            // Octal, so long as there are additional digits.
            radix = 8;
            pos++;
        } // Default is to treat as decimal.

        if (pos > 0) {
            text = text.substring(pos);
        }
        final BigInteger value = new BigInteger(text, radix);
        return negate ? value.negate() : value;
    }

    /**
     * Converts the specified string to a {@link Number} object. This method does not support scientific notation.
     *
     * <ul>
     * <li>Blank strings and "NaN" are converted to 0.</li>
     * <li>Strings starting with "0x" are parsed as hexadecimal Longs.</li>
     * </ul>
     *
     * <p>
     * Note that number formats can vary by locale. For example, in Germany, the Netherlands, Belgium, Denmark, Italy,
     * Romania, and most of Europe, a comma (`,`) is used as the decimal separator. In these regions, "1.20" represents
     * 120, not 1.2.
     *
     * @param numberStr The string to parse.
     * @return A Number object.
     * @throws NumberFormatException Wraps a {@link ParseException} and is thrown when the string cannot be parsed.
     */
    public Number parseNumber(final String numberStr) throws NumberFormatException {
        if (isBlankOrNaN(numberStr)) {
            // In JDK 9+, NaN is handled by returning 0. This maintains consistency.
            return 0;
        }

        // Hexadecimal
        if (StringKit.startWithIgnoreCase(numberStr, "0x")) {
            // "0x04" represents a hexadecimal number.
            return Long.parseLong(numberStr.substring(2), 16);
        }

        return doParse(numberStr);
    }

    /**
     * Performs number parsing using {@link NumberFormat}. If it is a {@link DecimalFormat}, it is configured to parse
     * into a BigDecimal to avoid precision loss.
     *
     * @param number The string to parse.
     * @return The parsed Number.
     */
    private Number doParse(String number) {
        Locale locale = this.locale;
        if (null == locale) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }
        if (StringKit.startWith(number, Symbol.C_PLUS)) {
            number = StringKit.subSuf(number, 1);
        }

        try {
            final NumberFormat format = NumberFormat.getInstance(locale);
            if (format instanceof DecimalFormat) {
                // When the string number exceeds the range of a double, it can be truncated. Use BigDecimal to receive
                // it.
                ((DecimalFormat) format).setParseBigDecimal(true);
            }
            return format.parse(Convert.toDBC(number));
        } catch (final ParseException e) {
            final NumberFormatException nfe = new NumberFormatException(e.getMessage());
            nfe.initCause(e);
            throw nfe;
        }
    }

    /**
     * Checks if the string is blank or "NaN". If {@link #zeroIfNaN} is {@code false}, throws a
     * {@link NumberFormatException} for "NaN".
     *
     * @param numberStr The string to check.
     * @return {@code true} if the string is blank or "NaN", {@code false} otherwise.
     * @throws NumberFormatException If the string is "NaN" and {@code zeroIfNaN} is false.
     */
    private boolean isBlankOrNaN(final String numberStr) throws NumberFormatException {
        if (StringKit.isBlank(numberStr)) {
            return true;
        }

        if (NaN.equals(numberStr)) {
            if (zeroIfNaN) {
                return true;
            } else {
                throw new NumberFormatException("Cannot parse NaN when 'zeroIfNaN' is false!");
            }
        }

        return false;
    }

}
