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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.math.*;

/**
 * Number utility class. For precise calculations, {@link BigDecimal} should be used. Note that in JDK7, the
 * `BigDecimal(double val)` constructor can have unpredictable results. For example, `new BigDecimal(0.1)` does not
 * represent 0.1, but a much more complex number. This is because 0.1 cannot be represented exactly as a double.
 * Therefore, it is recommended to use `new BigDecimal(String)`.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MathKit extends NumberValidator {

    /**
     * Factorials for numbers 0-20. Factorials above 20 will exceed Long.MAX_VALUE.
     */
    private static final long[] FACTORIALS = new long[] { 1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L,
            3628800L, 39916800L, 479001600L, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L,
            355687428096000L, 6402373705728000L, 121645100408832000L, 2432902008176640000L };

    /**
     * Provides precise addition. Returns 0 if the input array is null or empty.
     *
     * @param values The numbers to be added.
     * @return The sum as a BigDecimal.
     */
    public static BigDecimal add(final Number... values) {
        if (ArrayKit.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.add(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * Provides precise addition for string representations of numbers. Note that number formats can differ by `Locale`.
     * For example, many European countries use a comma as a decimal separator.
     *
     * @param values The string numbers to be added.
     * @return The sum as a BigDecimal.
     */
    public static BigDecimal add(final String... values) {
        if (ArrayKit.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (StringKit.isNotBlank(value)) {
                result = result.add(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * Provides precise subtraction. Returns 0 if the input array is null or empty.
     *
     * @param values The numbers to be subtracted.
     * @return The difference as a BigDecimal.
     */
    public static BigDecimal sub(final Number... values) {
        if (ArrayKit.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.subtract(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * Provides precise subtraction for string representations of numbers.
     *
     * @param values The string numbers to be subtracted.
     * @return The difference as a BigDecimal.
     */
    public static BigDecimal sub(final String... values) {
        if (ArrayKit.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (StringKit.isNotBlank(value)) {
                result = result.subtract(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * Provides precise multiplication. Returns 0 if the input array is null or empty, or contains a null.
     *
     * @param values The numbers to be multiplied.
     * @return The product as a BigDecimal.
     */
    public static BigDecimal mul(final Number... values) {
        if (ArrayKit.isEmpty(values) || ArrayKit.hasNull(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        if (isZero(value)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (isZero(value)) {
                return BigDecimal.ZERO;
            }
            result = result.multiply(toBigDecimal(value));
        }
        return result;
    }

    /**
     * Provides precise multiplication for string representations of numbers.
     *
     * @param values The string numbers to be multiplied.
     * @return The product as a BigDecimal.
     */
    public static BigDecimal mul(final String... values) {
        if (ArrayKit.isEmpty(values) || ArrayKit.hasNull(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = toBigDecimal(values[0]);
        if (isZero(result)) {
            return BigDecimal.ZERO;
        }

        BigDecimal ele;
        for (int i = 1; i < values.length; i++) {
            ele = toBigDecimal(values[i]);
            if (isZero(ele)) {
                return BigDecimal.ZERO;
            }
            result = result.multiply(ele);
        }

        return result;
    }

    /**
     * Provides precise division. If the division results in a non-terminating decimal, it's rounded to 10 decimal
     * places.
     *
     * @param v1 The dividend.
     * @param v2 The divisor.
     * @return The quotient.
     */
    public static BigDecimal div(final Number v1, final Number v2) {
        return div(v1, v2, Normal._10);
    }

    /**
     * Provides precise division for string representations. If the division results in a non-terminating decimal, it's
     * rounded to 10 decimal places.
     *
     * @param v1 The dividend.
     * @param v2 The divisor.
     * @return The quotient.
     */
    public static BigDecimal div(final String v1, final String v2) {
        return div(v1, v2, Normal._10);
    }

    /**
     * Provides precise division with a specified scale and HALF_UP rounding.
     *
     * @param v1    The dividend.
     * @param v2    The divisor.
     * @param scale The number of decimal places to keep.
     * @return The quotient.
     */
    public static BigDecimal div(final Number v1, final Number v2, final int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * Provides precise division for string representations with a specified scale and HALF_UP rounding.
     *
     * @param v1    The dividend.
     * @param v2    The divisor.
     * @param scale The number of decimal places to keep.
     * @return The quotient.
     */
    public static BigDecimal div(final String v1, final String v2, final int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * Provides precise division with a specified scale and rounding mode for string representations.
     *
     * @param v1           The dividend.
     * @param v2           The divisor.
     * @param scale        The number of decimal places to keep.
     * @param roundingMode The rounding mode.
     * @return The quotient.
     */
    public static BigDecimal div(final String v1, final String v2, final int scale, final RoundingMode roundingMode) {
        return div(toBigDecimal(v1), toBigDecimal(v2), scale, roundingMode);
    }

    /**
     * Provides precise division with a specified scale and rounding mode.
     *
     * @param v1           The dividend.
     * @param v2           The divisor.
     * @param scale        The number of decimal places to keep.
     * @param roundingMode The rounding mode.
     * @return The quotient.
     */
    public static BigDecimal div(final Number v1, final Number v2, int scale, final RoundingMode roundingMode) {
        Assert.notNull(v2, "Divisor must be not null !");
        if (null == v1 || isZero(v1)) {
            return BigDecimal.ZERO;
        }

        if (scale < 0) {
            scale = -scale;
        }
        return toBigDecimal(v1).divide(toBigDecimal(v2), scale, roundingMode);
    }

    /**
     * Ceiling division for integers. Complements `Math.floorDiv()`.
     *
     * @param v1 The dividend.
     * @param v2 The divisor.
     * @return The ceiling of the division.
     */
    public static int ceilDiv(final int v1, final int v2) {
        return (int) Math.ceil((double) v1 / v2);
    }

    /**
     * Rounds a number to a specified number of decimal places using `RoundingMode.HALF_UP`.
     *
     * @param number The number.
     * @param scale  The number of decimal places to keep.
     * @return The rounded number.
     */
    public static BigDecimal round(final Number number, final int scale) {
        return round(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * Rounds a number to a specified number of decimal places with a given rounding mode.
     *
     * @param number       The number.
     * @param scale        The number of decimal places to keep.
     * @param roundingMode The rounding mode.
     * @return The rounded number.
     */
    public static BigDecimal round(final Number number, int scale, RoundingMode roundingMode) {
        final BigDecimal bigDecimal = toBigDecimal(number);
        if (scale < 0) {
            scale = 0;
        }
        if (null == roundingMode) {
            roundingMode = RoundingMode.HALF_UP;
        }
        return bigDecimal.setScale(scale, roundingMode);
    }

    /**
     * Rounds a number string to a specified number of decimal places using `RoundingMode.HALF_UP`.
     *
     * @param numberStr The number string.
     * @param scale     The number of decimal places to keep.
     * @return The rounded number as a string.
     */
    public static String roundString(final String numberStr, final int scale) {
        return roundString(numberStr, scale, RoundingMode.HALF_UP);
    }

    /**
     * Rounds a number to a specified number of decimal places using `RoundingMode.HALF_UP`.
     *
     * @param number The number.
     * @param scale  The number of decimal places to keep.
     * @return The rounded number as a string.
     */
    public static String roundString(final Number number, final int scale) {
        return roundString(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * Rounds a number string to a specified number of decimal places with a given rounding mode.
     *
     * @param numberStr    The number string.
     * @param scale        The number of decimal places to keep.
     * @param roundingMode The rounding mode.
     * @return The rounded number as a string.
     */
    public static String roundString(final String numberStr, final int scale, final RoundingMode roundingMode) {
        return roundString(toBigDecimal(numberStr), scale, roundingMode);
    }

    /**
     * Rounds a number to a specified number of decimal places with a given rounding mode.
     *
     * @param number       The number.
     * @param scale        The number of decimal places to keep.
     * @param roundingMode The rounding mode.
     * @return The rounded number as a string.
     */
    public static String roundString(final Number number, final int scale, final RoundingMode roundingMode) {
        return round(number, scale, roundingMode).toPlainString();
    }

    /**
     * Rounds a number using the "round half to even" method (also known as banker's rounding).
     *
     * @param number The number.
     * @param scale  The number of decimal places to keep.
     * @return The rounded number.
     */
    public static BigDecimal roundHalfEven(final Number number, final int scale) {
        return round(toBigDecimal(number), scale, RoundingMode.HALF_EVEN);
    }

    /**
     * Rounds a number down (truncates).
     *
     * @param number The number.
     * @param scale  The number of decimal places to keep.
     * @return The rounded number.
     */
    public static BigDecimal roundDown(final Number number, final int scale) {
        return round(toBigDecimal(number), scale, RoundingMode.DOWN);
    }

    /**
     * Formats a number using a given pattern.
     *
     * @param pattern The format pattern.
     * @param value   The value.
     * @return The formatted string.
     */
    public static String format(final String pattern, final double value) {
        Assert.isTrue(isValid(value), "value is NaN or Infinite!");
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * Formats a number using a given pattern.
     *
     * @param pattern The format pattern.
     * @param value   The value.
     * @return The formatted string.
     */
    public static String format(final String pattern, final long value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * Formats a number using a given pattern.
     *
     * @param pattern The format pattern.
     * @param value   The value (supports BigDecimal, BigInteger, Number, etc.).
     * @return The formatted string.
     */
    public static String format(final String pattern, final Object value) {
        return format(pattern, value, null);
    }

    /**
     * Formats a number using a given pattern and rounding mode.
     *
     * @param pattern      The format pattern.
     * @param value        The value.
     * @param roundingMode The rounding mode.
     * @return The formatted string.
     */
    public static String format(final String pattern, final Object value, final RoundingMode roundingMode) {
        if (value instanceof Number) {
            Assert.isTrue(isValidNumber((Number) value), "value is NaN or Infinite!");
        }
        final DecimalFormat decimalFormat = new DecimalFormat(pattern);
        if (null != roundingMode) {
            decimalFormat.setRoundingMode(roundingMode);
        }
        return decimalFormat.format(value);
    }

    /**
     * Formats a monetary value with thousands separators.
     *
     * @param value The monetary value.
     * @return The formatted string.
     */
    public static String formatMoney(final double value) {
        return format(",##0.00", value);
    }

    /**
     * Formats a number as a percentage.
     *
     * @param number The number.
     * @param scale  The number of decimal places to keep.
     * @return The percentage string.
     */
    public static String formatPercent(final double number, final int scale) {
        final NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    /**
     * Formats a number with thousands separators.
     *
     * @param number The number.
     * @param scale  The number of decimal places to keep.
     * @return The formatted string.
     */
    public static String formatThousands(final double number, final int scale) {
        final NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    /**
     * Generates an array of integers from 0 to `stopIncluded - 1`.
     *
     * @param stopIncluded The exclusive end number.
     * @return The array of integers.
     */
    public static int[] range(final int stopIncluded) {
        return range(0, stopIncluded, 1);
    }

    /**
     * Generates an array of integers from `startInclude` to `stopIncluded`.
     *
     * @param startInclude The inclusive start number.
     * @param stopIncluded The inclusive end number.
     * @return The array of integers.
     */
    public static int[] range(final int startInclude, final int stopIncluded) {
        return range(startInclude, stopIncluded, 1);
    }

    /**
     * Generates an array of integers within a range with a given step.
     *
     * @param startInclude The inclusive start number.
     * @param stopIncluded The exclusive end number.
     * @param step         The step increment.
     * @return The array of integers.
     */
    public static int[] range(int startInclude, int stopIncluded, int step) {
        if (startInclude > stopIncluded) {
            final int tmp = startInclude;
            startInclude = stopIncluded;
            stopIncluded = tmp;
        }

        if (step <= 0) {
            step = 1;
        }

        final int deviation = stopIncluded + 1 - startInclude;
        int length = deviation / step;
        if (deviation % step != 0) {
            length += 1;
        }
        final int[] range = new int[length];
        for (int i = 0; i < length; i++) {
            range[i] = startInclude;
            startInclude += step;
        }
        return range;
    }

    /**
     * Appends a range of integers to an existing collection.
     *
     * @param start  The inclusive start number.
     * @param stop   The inclusive end number.
     * @param values The collection to append to.
     * @return The modified collection.
     */
    public static Collection<Integer> appendRange(final int start, final int stop, final Collection<Integer> values) {
        return appendRange(start, stop, 1, values);
    }

    /**
     * Appends a range of integers to an existing collection with a given step.
     *
     * @param startInclude The inclusive start number.
     * @param stopInclude  The inclusive end number.
     * @param step         The step increment.
     * @param values       The collection to append to.
     * @return The modified collection.
     */
    public static Collection<Integer> appendRange(
            final int startInclude,
            final int stopInclude,
            int step,
            final Collection<Integer> values) {
        if (startInclude < stopInclude) {
            step = Math.abs(step);
        } else if (startInclude > stopInclude) {
            step = -Math.abs(step);
        } else {
            values.add(startInclude);
            return values;
        }

        for (int i = startInclude; (step > 0) ? i <= stopInclude : i >= stopInclude; i += step) {
            values.add(i);
        }
        return values;
    }

    /**
     * 获得数字对应的二进制字符串
     * <ul>
     * <li>Integer/Long：直接使用 JDK 内置方法转换</li>
     * <li>Byte/Short：转换为无符号整数后补充前导零至对应位数（Byte=8位，Short=16位）</li>
     * <li>Float/Double：使用 IEEE 754 标准格式转换，Float=32位，Double=64位</li>
     * </ul>
     *
     * @param number 待转换的Number对象（支持Integer、Long、Byte、Short、Float、Double）
     * @return 二进制字符串
     */
    public static String getBinaryString(final Number number) {
        Assert.notNull(number, "Number must be not null!");

        // 根据Number的实际类型处理
        if (number instanceof Integer) {
            return Integer.toBinaryString(number.intValue());
        } else if (number instanceof Long) {
            return Long.toBinaryString(number.longValue());
        } else if (number instanceof Byte) {
            // Byte是8位，补前导0至8位
            return String.format("%8s", Integer.toBinaryString(number.byteValue() & 0xFF)).replace(' ', '0');
        } else if (number instanceof Short) {
            // Short是16位，补前导0至16位
            return String.format("%16s", Integer.toBinaryString(number.shortValue() & 0xFFFF)).replace(' ', '0');
        } else if (number instanceof Float) {
            // Float转换为IEEE 754 32位二进制
            final int floatBits = Float.floatToIntBits(number.floatValue());
            return String.format("%32s", Integer.toBinaryString(floatBits)).replace(' ', '0');
        } else if (number instanceof Double) {
            // Double转换为IEEE 754 64位二进制
            final long doubleBits = Double.doubleToLongBits(number.doubleValue());
            return String.format("%64s", Long.toBinaryString(doubleBits)).replace(' ', '0');
        } else if (number instanceof BigInteger) {
            // 大数整数类型
            return ((BigInteger) number).toString(2);
        } else {
            // 不支持的类型（如BigInteger、BigDecimal需额外处理）
            throw new IllegalArgumentException("Number not support：" + number.getClass().getName());
        }
    }

    /**
     * Converts a binary string to an integer.
     *
     * @param binaryStr The binary string.
     * @return The integer.
     */
    public static int binaryToInt(final String binaryStr) {
        return Integer.parseInt(binaryStr, 2);
    }

    /**
     * Converts a binary string to a long.
     *
     * @param binaryStr The binary string.
     * @return The long.
     */
    public static long binaryToLong(final String binaryStr) {
        return Long.parseLong(binaryStr, 2);
    }

    /**
     * Checks if two numbers are equal. Special handling for `BigDecimal` to ignore scale.
     *
     * @param number1 The first number.
     * @param number2 The second number.
     * @return `true` if they are equal.
     */
    public static boolean equals(final Number number1, final Number number2) {
        if (number1 instanceof BigDecimal && number2 instanceof BigDecimal) {
            return CompareKit.equals((BigDecimal) number1, (BigDecimal) number2);
        }
        return Objects.equals(number1, number2);
    }

    /**
     * Converts a number to a string, returning a default value if the number is null.
     *
     * @param number       The number.
     * @param defaultValue The default value.
     * @return The string representation.
     */
    public static String toString(final Number number, final String defaultValue) {
        return (null == number) ? defaultValue : toString(number);
    }

    /**
     * Converts a number to a string, stripping trailing zeros from the decimal part.
     *
     * @param number The number.
     * @return The string representation.
     */
    public static String toString(final Number number) {
        return toString(number, true);
    }

    /**
     * Converts a number to a string.
     *
     * @param number               The number.
     * @param isStripTrailingZeros If true, strips trailing zeros from the decimal part.
     * @return The string representation.
     */
    public static String toString(final Number number, final boolean isStripTrailingZeros) {
        Assert.notNull(number, "Number is null !");

        if (number instanceof BigDecimal) {
            return toString((BigDecimal) number, isStripTrailingZeros);
        }

        Assert.isTrue(isValidNumber(number), "Number is non-finite!");
        String string = number.toString();
        if (isStripTrailingZeros) {
            if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
                while (string.endsWith("0")) {
                    string = string.substring(0, string.length() - 1);
                }
                if (string.endsWith(".")) {
                    string = string.substring(0, string.length() - 1);
                }
            }
        }
        return string;
    }

    /**
     * Converts a `BigDecimal` to a string, stripping trailing zeros.
     *
     * @param bigDecimal The `BigDecimal`.
     * @return The string representation.
     */
    public static String toString(final BigDecimal bigDecimal) {
        return toString(bigDecimal, true);
    }

    /**
     * Converts a `BigDecimal` to a string.
     *
     * @param bigDecimal           The `BigDecimal`.
     * @param isStripTrailingZeros If true, strips trailing zeros.
     * @return The string representation using `toPlainString()`.
     */
    public static String toString(BigDecimal bigDecimal, final boolean isStripTrailingZeros) {
        Assert.notNull(bigDecimal, "BigDecimal is null !");
        if (isStripTrailingZeros) {
            bigDecimal = bigDecimal.stripTrailingZeros();
        }
        return bigDecimal.toPlainString();
    }

    /**
     * Converts a `Number` to a `BigDecimal`. `null` is converted to `BigDecimal.ZERO`.
     *
     * @param number The number.
     * @return The `BigDecimal`.
     */
    public static BigDecimal toBigDecimal(final Number number) {
        if (null == number) {
            return BigDecimal.ZERO;
        }

        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof Long) {
            return new BigDecimal((Long) number);
        } else if (number instanceof Integer) {
            return new BigDecimal((Integer) number);
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        }

        return new BigDecimal(number.toString());
    }

    /**
     * Converts a string to a `BigDecimal`.
     *
     * @param numberStr The number string.
     * @return The `BigDecimal`.
     * @throws IllegalArgumentException if the string is blank.
     */
    public static BigDecimal toBigDecimal(final String numberStr) throws IllegalArgumentException {
        Assert.notBlank(numberStr, "Number text must be not blank!");
        try {
            return new BigDecimal(numberStr);
        } catch (final Exception ignore) {
            // ignore
        }
        return toBigDecimal(parseNumber(numberStr));
    }

    /**
     * Converts a `Number` to a `BigInteger`.
     *
     * @param number The number.
     * @return The `BigInteger`.
     */
    public static BigInteger toBigInteger(final Number number) {
        Assert.notNull(number, "Number must be not null!");

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        } else if (number instanceof Long) {
            return BigInteger.valueOf((Long) number);
        }

        return toBigInteger(number.longValue());
    }

    /**
     * Converts a string to a `BigInteger`.
     *
     * @param numberStr The number string.
     * @return The `BigInteger`.
     */
    public static BigInteger toBigInteger(final String numberStr) {
        Assert.notBlank(numberStr, "Number text must be not blank!");
        try {
            return new BigInteger(numberStr);
        } catch (final Exception ignore) {
            // ignore
        }
        return parseBigInteger(numberStr);
    }

    /**
     * Calculates the number of partitions.
     *
     * @param total    The total number of items.
     * @param pageSize The size of each partition.
     * @return The number of partitions.
     */
    public static int count(final int total, final int pageSize) {
        return total == 0 ? 0 : (total - 1) / pageSize + 1;
    }

    /**
     * Returns 1 if the given value is 0, otherwise returns the original value.
     *
     * @param value The value.
     * @return 1 or the non-zero value.
     */
    public static int zeroToOne(final int value) {
        return 0 == value ? 1 : value;
    }

    /**
     * Returns 0 if the given number is `null`, otherwise returns the number's value.
     *
     * @param number The number.
     * @return 0 or the number's value.
     */
    public static int nullToZero(final Integer number) {
        return number == null ? 0 : number;
    }

    /**
     * Returns 0 if the given number is `null`, otherwise returns the number's value.
     *
     * @param number The number.
     * @return 0 or the number's value.
     */
    public static long nullToZero(final Long number) {
        return number == null ? 0L : number;
    }

    /**
     * Returns 0.0 if the given number is `null`, otherwise returns the number's value.
     *
     * @param number The number.
     * @return 0.0 or the number's value.
     */
    public static double nullToZero(final Double number) {
        return number == null ? 0.0 : number;
    }

    /**
     * Returns 0.0f if the given number is `null`, otherwise returns the number's value.
     *
     * @param number The number.
     * @return 0.0f or the number's value.
     */
    public static float nullToZero(final Float number) {
        return number == null ? 0.0f : number;
    }

    /**
     * Returns 0 if the given number is `null`, otherwise returns the number's value.
     *
     * @param number The number.
     * @return 0 or the number's value.
     */
    public static short nullToZero(final Short number) {
        return number == null ? (short) 0 : number;
    }

    /**
     * Returns 0 if the given number is `null`, otherwise returns the number's value.
     *
     * @param number The number.
     * @return 0 or the number's value.
     */
    public static byte nullToZero(final Byte number) {
        return number == null ? (byte) 0 : number;
    }

    /**
     * Returns `BigInteger.ZERO` if the given number is `null`, otherwise returns the number.
     *
     * @param number The number.
     * @return `BigInteger.ZERO` or the number.
     */
    public static BigInteger nullToZero(final BigInteger number) {
        return ObjectKit.defaultIfNull(number, BigInteger.ZERO);
    }

    /**
     * Returns `BigDecimal.ZERO` if the given number is `null`, otherwise returns the number.
     *
     * @param decimal The `BigDecimal`.
     * @return `BigDecimal.ZERO` or the number.
     */
    public static BigDecimal nullToZero(final BigDecimal decimal) {
        return ObjectKit.defaultIfNull(decimal, BigDecimal.ZERO);
    }

    /**
     * Parses a string into a `BigInteger`.
     *
     * @param numberStr The number string.
     * @return The `BigInteger`.
     */
    public static BigInteger parseBigInteger(final String numberStr) {
        return NumberParser.INSTANCE.parseBigInteger(numberStr);
    }

    /**
     * Checks if two numbers are adjacent (their absolute difference is 1).
     *
     * @param number1 The first number.
     * @param number2 The second number.
     * @return `true` if they are adjacent.
     */
    public static boolean isBeside(final long number1, final long number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * Checks if two numbers are adjacent.
     *
     * @param number1 The first number.
     * @param number2 The second number.
     * @return `true` if they are adjacent.
     */
    public static boolean isBeside(final int number1, final int number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * Divides a total into a number of parts, returning the size of each part.
     *
     * @param total     The total number.
     * @param partCount The number of parts.
     * @return The size of each part.
     */
    public static int partValue(final int total, final int partCount) {
        return partValue(total, partCount, true);
    }

    /**
     * Divides a total into a number of parts, returning the size of each part.
     *
     * @param total               The total number.
     * @param partCount           The number of parts.
     * @param isPlusOneWhenHasRem If true, adds 1 to each part if there is a remainder.
     * @return The size of each part.
     */
    public static int partValue(final int total, final int partCount, final boolean isPlusOneWhenHasRem) {
        int partValue = total / partCount;
        if (isPlusOneWhenHasRem && total % partCount > 0) {
            partValue++;
        }
        return partValue;
    }

    /**
     * Provides precise exponentiation.
     *
     * @param number The base.
     * @param n      The exponent.
     * @return The result of the exponentiation.
     */
    public static BigDecimal pow(final Number number, final int n) {
        return pow(toBigDecimal(number), n);
    }

    /**
     * Provides precise exponentiation. If n is negative, returns 1 / (a^-n).
     *
     * @param number The base.
     * @param n      The exponent.
     * @return The result.
     */
    public static BigDecimal pow(final BigDecimal number, final int n) {
        return pow(number, n, 2, RoundingMode.HALF_UP);
    }

    /**
     * Provides precise exponentiation with a specified scale and rounding mode for negative exponents.
     *
     * @param number       The base.
     * @param n            The exponent.
     * @param scale        The scale for division when the exponent is negative.
     * @param roundingMode The rounding mode for division.
     * @return The result.
     */
    public static BigDecimal pow(
            final BigDecimal number,
            final int n,
            final int scale,
            final RoundingMode roundingMode) {
        if (n < 0) {
            return BigDecimal.ONE.divide(pow(number, -n), scale, roundingMode);
        }
        return number.pow(n);
    }

    /**
     * Checks if an integer is a power of two.
     *
     * @param n The integer to check.
     * @return `true` if n is a power of two.
     */
    public static boolean isPowerOfTwo(final long n) {
        return (n > 0) && ((n & (n - 1)) == 0);
    }

    /**
     * Parses a string into an `Integer`, returning a default value on failure.
     *
     * @param numberStr    The number string.
     * @param defaultValue The default value.
     * @return The parsed `Integer` or the default value.
     */
    public static Integer parseInt(final String numberStr, final Integer defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseInt(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * Parses a string into an `int`.
     *
     * @param numberStr The number string.
     * @return The parsed `int`.
     * @throws NumberFormatException if the string is not a valid integer.
     */
    public static int parseInt(final String numberStr) throws NumberFormatException {
        return NumberParser.INSTANCE.parseInt(numberStr);
    }

    /**
     * Parses a char array into an `int`.
     *
     * @param chars The char array.
     * @param radix The radix.
     * @return The `int` value.
     * @throws NumberFormatException if parsing fails.
     */
    public static int parseInt(final char[] chars, final int radix) throws NumberFormatException {
        return NumberParser.INSTANCE.parseInt(chars, radix);
    }

    /**
     * Parses a string into a `Long`, returning a default value on failure.
     *
     * @param numberStr    The number string.
     * @param defaultValue The default value.
     * @return The parsed `Long` or the default value.
     */
    public static Long parseLong(final String numberStr, final Long defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseLong(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * Parses a string into a `long`.
     *
     * @param numberStr The number string.
     * @return The parsed `long`.
     */
    public static long parseLong(final String numberStr) {
        return NumberParser.INSTANCE.parseLong(numberStr);
    }

    /**
     * Parses a string into a `Float`, returning a default value on failure.
     *
     * @param numberStr    The number string.
     * @param defaultValue The default value.
     * @return The parsed `Float` or the default value.
     */
    public static Float parseFloat(final String numberStr, final Float defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseFloat(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * Parses a string into a `float`.
     *
     * @param numberStr The number string.
     * @return The parsed `float`.
     */
    public static float parseFloat(final String numberStr) {
        return NumberParser.INSTANCE.parseFloat(numberStr);
    }

    /**
     * Parses a string into a `Double`, returning a default value on failure.
     *
     * @param numberStr    The number string.
     * @param defaultValue The default value.
     * @return The parsed `Double` or the default value.
     */
    public static Double parseDouble(final String numberStr, final Double defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseDouble(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * Parses a string into a `double`.
     *
     * @param numberStr The number string.
     * @return The parsed `double`.
     */
    public static double parseDouble(final String numberStr) {
        return NumberParser.INSTANCE.parseDouble(numberStr);
    }

    /**
     * Parses a string into a `Number`, returning a default value on failure.
     *
     * @param numberStr    The number string.
     * @param defaultValue The default value.
     * @return The parsed `Number` or the default value.
     */
    public static Number parseNumber(final String numberStr, final Number defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseNumber(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * Parses a string into a `Number`.
     *
     * @param numberStr The number string.
     * @return The `Number` object.
     * @throws NumberFormatException if parsing fails.
     */
    public static Number parseNumber(final String numberStr) throws NumberFormatException {
        return NumberParser.INSTANCE.parseNumber(numberStr);
    }

    /**
     * Parses a string into a `Number` using a specific `Locale`.
     *
     * @param numberStr The number string.
     * @param locale    The locale.
     * @return The `Number` object.
     * @throws NumberFormatException if parsing fails.
     */
    public static Number parseNumber(final String numberStr, final Locale locale) throws NumberFormatException {
        return NumberParser.of(locale).parseNumber(numberStr);
    }

    /**
     * Checks if a `Number` is valid (not infinite or NaN).
     *
     * @param number The number to check.
     * @return `true` if the number is valid.
     */
    public static boolean isValidNumber(final Number number) {
        if (null == number) {
            return false;
        }
        if (number instanceof Double) {
            return (!((Double) number).isInfinite()) && (!((Double) number).isNaN());
        } else if (number instanceof Float) {
            return (!((Float) number).isInfinite()) && (!((Float) number).isNaN());
        }
        return true;
    }

    /**
     * Checks if a `double` is valid (not infinite or NaN).
     *
     * @param number The double to check.
     * @return `true` if valid.
     */
    public static boolean isValid(final double number) {
        return !(Double.isNaN(number) || Double.isInfinite(number));
    }

    /**
     * Checks if a `float` is valid (not infinite or NaN).
     *
     * @param number The float to check.
     * @return `true` if valid.
     */
    public static boolean isValid(final float number) {
        return !(Float.isNaN(number) || Float.isInfinite(number));
    }

    /**
     * Calculates the value of a mathematical expression (supports +, -, *, /, %).
     *
     * @param expression The mathematical expression.
     * @return The result.
     */
    public static double calculate(final String expression) {
        return Calculator.conversion(expression);
    }

    /**
     * Converts a `Number` value to a `double` with better precision for `Float`.
     *
     * @param value The number to convert.
     * @return The double value.
     */
    public static double toDouble(final Number value) {
        if (value instanceof Float) {
            return Double.parseDouble(value.toString());
        } else {
            return value.doubleValue();
        }
    }

    /**
     * Checks if a number is odd.
     *
     * @param num The number.
     * @return `true` if odd.
     */
    public static boolean isOdd(final int num) {
        return (num & 1) == 1;
    }

    /**
     * Checks if a number is even.
     *
     * @param num The number.
     * @return `true` if even.
     */
    public static boolean isEven(final int num) {
        return !isOdd(num);
    }

    /**
     * Checks if a number is zero.
     *
     * @param n The number.
     * @return `true` if the number is zero.
     */
    public static boolean isZero(final Number n) {
        Assert.notNull(n);

        if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long) {
            return 0L == n.longValue();
        } else if (n instanceof BigInteger) {
            return equals(BigInteger.ZERO, n);
        } else if (n instanceof Float) {
            return 0f == n.floatValue();
        } else if (n instanceof Double) {
            return 0d == n.doubleValue();
        }
        return equals(toBigDecimal(n), BigDecimal.ZERO);
    }

    /**
     * Converts an integer to a Roman numeral.
     *
     * @param num An integer between 1 and 3999.
     * @return The Roman numeral string.
     */
    public static String intToRoman(final int num) {
        return RomanNumberFormatter.intToRoman(num);
    }

    /**
     * Converts a Roman numeral to an integer.
     *
     * @param roman The Roman numeral string.
     * @return The integer.
     * @throws IllegalArgumentException if the input is not a valid Roman numeral.
     */
    public static int romanToInt(final String roman) {
        return RomanNumberFormatter.romanToInt(roman);
    }

    /**
     * Calculates the number of arrangements (permutations), i.e., P(n, m) = n! / (n-m)!.
     *
     * @param n The total number of items.
     * @param m The number of items to choose.
     * @return The number of arrangements.
     */
    public static long arrangementCount(final int n, final int m) {
        return Arrangement.count(n, m);
    }

    /**
     * Calculates the number of arrangements of n items, i.e., P(n, n) = n!.
     *
     * @param n The total number of items.
     * @return The number of arrangements.
     */
    public static long arrangementCount(final int n) {
        return Arrangement.count(n);
    }

    /**
     * Selects `m` permutations from a list of items.
     *
     * @param datas The list of items.
     * @param m     The number of items to choose.
     * @return A list of all permutations.
     */
    public static List<String[]> arrangementSelect(final String[] datas, final int m) {
        return new Arrangement(datas).select(m);
    }

    /**
     * Selects all permutations from a list of items.
     *
     * @param datas The list of items.
     * @return A list of all permutations.
     */
    public static List<String[]> arrangementSelect(final String[] datas) {
        return new Arrangement(datas).select();
    }

    /**
     * Calculates the number of combinations, i.e., C(n, m) = n! / ((n-m)! * m!).
     *
     * @param n The total number of items.
     * @param m The number of items to choose.
     * @return The number of combinations.
     */
    public static long combinationCount(final int n, final int m) {
        return Combination.count(n, m);
    }

    /**
     * Selects `m` combinations from a list of items.
     *
     * @param datas The list of items.
     * @param m     The number of items to choose.
     * @return A list of all combinations.
     */
    public static List<String[]> combinationSelect(final String[] datas, final int m) {
        return new Combination(datas).select(m);
    }

    /**
     * Converts an amount in yuan to cents.
     *
     * @param yuan The amount in yuan.
     * @return The amount in cents.
     */
    public static long yuanToCent(final double yuan) {
        return new Money(yuan).getCent();
    }

    /**
     * Converts an amount in cents to yuan.
     *
     * @param cent The amount in cents.
     * @return The amount in yuan.
     */
    public static double centToYuan(final long cent) {
        final long yuan = cent / 100;
        final int centPart = (int) (cent % 100);
        return new Money(yuan, centPart).getAmount().doubleValue();
    }

    /**
     * Calculates the factorial of a `BigInteger`.
     *
     * @param n The number.
     * @return The factorial result.
     */
    public static BigInteger factorial(final BigInteger n) {
        if (n.equals(BigInteger.ZERO)) {
            return BigInteger.ONE;
        }
        return factorial(n, BigInteger.ZERO);
    }

    /**
     * Calculates the factorial over a range.
     *
     * @param start The start of the factorial (inclusive).
     * @param end   The end of the factorial (exclusive).
     * @return The result.
     */
    public static BigInteger factorial(BigInteger start, BigInteger end) {
        Assert.notNull(start, "Factorial start must be not null!");
        Assert.notNull(end, "Factorial end must be not null!");
        if (start.compareTo(BigInteger.ZERO) < 0 || end.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException(
                    StringKit.format("Factorial start and end both must be > 0, but got start={}, end={}", start, end));
        }
        if (start.equals(BigInteger.ZERO)) {
            start = BigInteger.ONE;
        }
        if (end.compareTo(BigInteger.ONE) < 0) {
            end = BigInteger.ONE;
        }
        BigInteger result = start;
        end = end.add(BigInteger.ONE);
        while (start.compareTo(end) > 0) {
            start = start.subtract(BigInteger.ONE);
            result = result.multiply(start);
        }
        return result;
    }

    /**
     * Calculates the factorial over a range for `long` values.
     *
     * @param start The start of the factorial (inclusive).
     * @param end   The end of the factorial (exclusive).
     * @return The result.
     */
    public static long factorial(final long start, final long end) {
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException(StringKit
                    .format("Factorial start and end both must be >= 0, but got start={}, end={}", start, end));
        }
        if (0L == start || start == end) {
            return 1L;
        }
        if (start < end) {
            return 0L;
        }
        return factorialMultiplyAndCheck(start, factorial(start - 1, end));
    }

    /**
     * Calculates factorial, checking for overflow.
     * 
     * @param a multiplier
     * @param b multiplicand
     * @return the result of a * b, or throw an exception if overflow occurs
     */
    private static long factorialMultiplyAndCheck(final long a, final long b) {
        if (a <= Long.MAX_VALUE / b) {
            return a * b;
        }
        throw new IllegalArgumentException(StringKit.format("Overflow in multiplication: {} * {}", a, b));
    }

    /**
     * Calculates the factorial of a number (0-20).
     *
     * @param n The number.
     * @return The factorial result.
     */
    public static long factorial(final long n) {
        if (n < 0 || n > 20) {
            throw new IllegalArgumentException(
                    StringKit.format("Factorial must have n >= 0 and n <= 20 for n!, but got n = {}", n));
        }
        return FACTORIALS[(int) n];
    }

    /**
     * Square root algorithm. `Math.sqrt(double)` is recommended.
     *
     * @param x The value.
     * @return The square root.
     */
    public static long sqrt(long x) {
        long y = 0;
        long b = (~Long.MAX_VALUE) >>> 1;
        while (b > 0) {
            if (x >= y + b) {
                x -= y + b;
                y >>= 1;
                y += b;
            } else {
                y >>= 1;
            }
            b >>= 2;
        }
        return y;
    }

    /**
     * Calculates the number of combinations for lottery-style selections.
     *
     * @param selectNum The number of items selected.
     * @param minNum    The number of items in a combination.
     * @return The number of combinations.
     */
    public static int processMultiple(final int selectNum, final int minNum) {
        final int result;
        result = mathSubNode(selectNum, minNum) / mathNode(selectNum - minNum);
        return result;
    }

    /**
     * Calculates the greatest common divisor (GCD).
     *
     * @param a The first value.
     * @param b The second value.
     * @return The GCD.
     */
    public static int gcd(int a, int b) {
        Assert.isTrue(a >= 0, "a must be >= 0");
        Assert.isTrue(b >= 0, "b must be >= 0");
        if (a == 0) {
            return b;
        } else if (b == 0) {
            return a;
        }
        final int aTwos = Integer.numberOfTrailingZeros(a);
        a >>= aTwos;
        final int bTwos = Integer.numberOfTrailingZeros(b);
        b >>= bTwos;
        while (a != b) {
            final int delta = a - b;
            final int minDeltaOrZero = delta & (delta >> (Integer.SIZE - 1));
            a = delta - minDeltaOrZero - minDeltaOrZero;
            b += minDeltaOrZero;
            a >>= Integer.numberOfTrailingZeros(a);
        }
        return a << Math.min(aTwos, bTwos);
    }

    /**
     * Calculates the least common multiple (LCM).
     *
     * @param m The first value.
     * @param n The second value.
     * @return The LCM.
     */
    public static int multiple(final int m, final int n) {
        // 先计算最大公约数
        final int gcd = gcd(m, n);
        // 使用长整型避免溢出，再转换回整型
        final long result = (long) m / gcd * (long) n;
        // 检查结果是否在int范围内
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new ArithmeticException("Integer overflow: " + m + " * " + n + " / " + gcd);
        }
        return (int) result;
    }

    /**
     * Helper for permutation calculation.
     * 
     * @param selectNum The number of items to select from.
     * @param minNum    The number of items to select.
     * @return The result of the sub-calculation.
     */
    private static int mathSubNode(final int selectNum, final int minNum) {
        if (selectNum == minNum) {
            return 1;
        } else {
            return selectNum * mathSubNode(selectNum - 1, minNum);
        }
    }

    /**
     * Helper for factorial calculation.
     * 
     * @param selectNum The number to calculate factorial for.
     * @return The factorial result.
     */
    private static int mathNode(final int selectNum) {
        if (selectNum == 0) {
            return 1;
        } else {
            return selectNum * mathNode(selectNum - 1);
        }
    }

}
