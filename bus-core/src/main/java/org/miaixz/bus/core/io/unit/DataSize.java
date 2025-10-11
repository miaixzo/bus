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
package org.miaixz.bus.core.io.unit;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Represents a data size, allowing conversion from string representations like '12MB' to a numeric byte length. This
 * class is inspired by Spring Framework's DataSize.
 *
 * <pre>
 *     byte        1B     1
 *     kilobyte    1KB    1,024
 *     megabyte    1MB    1,048,576
 *     gigabyte    1GB    1,073,741,824
 *     terabyte    1TB    1,099,511,627,776
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class DataSize implements Comparable<DataSize> {

    /**
     * The pattern used for parsing data size strings. It captures the numeric value and the optional unit suffix.
     */
    private static final Pattern PATTERN = Pattern.compile("^([+-]?\\d+(\\.\\d+)?)([a-zA-Z]{0,3})$");

    /**
     * The number of bytes in a Kilobyte (KB).
     */
    private static final long BYTES_PER_KB = 1024;

    /**
     * The number of bytes in a Megabyte (MB).
     */
    private static final long BYTES_PER_MB = BYTES_PER_KB * 1024;

    /**
     * The number of bytes in a Gigabyte (GB).
     */
    private static final long BYTES_PER_GB = BYTES_PER_MB * 1024;

    /**
     * The number of bytes in a Terabyte (TB).
     */
    private static final long BYTES_PER_TB = BYTES_PER_GB * 1024;

    /**
     * The size in bytes.
     */
    private final long bytes;

    /**
     * Constructs a new {@code DataSize} instance with the specified number of bytes.
     *
     * @param bytes The size in bytes. Can be positive or negative.
     */
    private DataSize(final long bytes) {
        this.bytes = bytes;
    }

    /**
     * Creates a {@code DataSize} instance representing the given number of bytes.
     *
     * @param bytes The size in bytes. Can be positive or negative.
     * @return A {@code DataSize} instance representing the specified bytes.
     */
    public static DataSize ofBytes(final long bytes) {
        return new DataSize(bytes);
    }

    /**
     * Creates a {@code DataSize} instance representing the given number of kilobytes.
     *
     * @param kilobytes The size in kilobytes. Can be positive or negative.
     * @return A {@code DataSize} instance representing the specified kilobytes.
     */
    public static DataSize ofKilobytes(final long kilobytes) {
        return new DataSize(Math.multiplyExact(kilobytes, BYTES_PER_KB));
    }

    /**
     * Creates a {@code DataSize} instance representing the given number of megabytes.
     *
     * @param megabytes The size in megabytes. Can be positive or negative.
     * @return A {@code DataSize} instance representing the specified megabytes.
     */
    public static DataSize ofMegabytes(final long megabytes) {
        return new DataSize(Math.multiplyExact(megabytes, BYTES_PER_MB));
    }

    /**
     * Creates a {@code DataSize} instance representing the given number of gigabytes.
     *
     * @param gigabytes The size in gigabytes. Can be positive or negative.
     * @return A {@code DataSize} instance representing the specified gigabytes.
     */
    public static DataSize ofGigabytes(final long gigabytes) {
        return new DataSize(Math.multiplyExact(gigabytes, BYTES_PER_GB));
    }

    /**
     * Creates a {@code DataSize} instance representing the given number of terabytes.
     *
     * @param terabytes The size in terabytes. Can be positive or negative.
     * @return A {@code DataSize} instance representing the specified terabytes.
     */
    public static DataSize ofTerabytes(final long terabytes) {
        return new DataSize(Math.multiplyExact(terabytes, BYTES_PER_TB));
    }

    /**
     * Creates a {@code DataSize} instance representing the given amount in the specified {@link DataUnit}.
     *
     * @param amount The numeric amount of the data size.
     * @param unit   The {@link DataUnit} of the amount. If {@code null}, {@link DataUnit#BYTES} is used as default.
     * @return A {@code DataSize} instance representing the specified amount and unit.
     */
    public static DataSize of(final long amount, DataUnit unit) {
        if (null == unit) {
            unit = DataUnit.BYTES;
        }
        return new DataSize(Math.multiplyExact(amount, unit.getSize().toBytes()));
    }

    /**
     * Creates a {@code DataSize} instance representing the given amount in the specified {@link DataUnit}.
     *
     * @param amount The numeric amount of the data size as a {@link BigDecimal}.
     * @param unit   The {@link DataUnit} of the amount. If {@code null}, {@link DataUnit#BYTES} is used as default.
     * @return A {@code DataSize} instance representing the specified amount and unit.
     */
    public static DataSize of(final BigDecimal amount, DataUnit unit) {
        if (null == unit) {
            unit = DataUnit.BYTES;
        }
        return new DataSize(amount.multiply(new BigDecimal(unit.getSize().toBytes())).longValue());
    }

    /**
     * Parses a text string representing a data size and returns its value in bytes. If no unit is specified in the
     * text, {@link DataUnit#BYTES} is used as the default. Examples:
     * 
     * <pre>
     * "12KB" -- parses as "12 kilobytes"
     * "5MB"  -- parses as "5 megabytes"
     * "20"   -- parses as "20 bytes"
     * </pre>
     *
     * @param text The text string to parse.
     * @return The parsed data size in bytes.
     * @see #parse(CharSequence, DataUnit)
     */
    public static long parse(final String text) {
        return parse(text, null).toBytes();
    }

    /**
     * Parses a text string representing a data size and returns a {@code DataSize} object. If no unit is specified in
     * the text, {@link DataUnit#BYTES} is used as the default. Examples:
     * 
     * <pre>
     * "12KB" -- parses as "12 kilobytes"
     * "5MB"  -- parses as "5 megabytes"
     * "20"   -- parses as "20 bytes"
     * </pre>
     *
     * @param text The text string to parse.
     * @return The parsed {@code DataSize} object.
     * @see #parse(CharSequence, DataUnit)
     */
    public static DataSize parse(final CharSequence text) {
        return parse(text, null);
    }

    /**
     * Parses a text string representing a data size and returns a {@code DataSize} object. The string should start with
     * a number followed optionally by a unit matching one of the supported {@linkplain DataUnit suffixes}. If no unit
     * is specified, the {@code defaultUnit} is used. Examples:
     * 
     * <pre>
     * "12KB" -- parses as "12 kilobytes"
     * "5MB"  -- parses as "5 megabytes"
     * "20"   -- parses as "20 kilobytes" (where the {@code
     * defaultUnit
     * } is {@link DataUnit#KILOBYTES})
     * </pre>
     *
     * @param text        The text string to parse.
     * @param defaultUnit The default {@link DataUnit} to use if no unit is specified in the text.
     * @return The parsed {@code DataSize} object.
     * @throws IllegalArgumentException If the text is {@code null} or does not match the data size pattern.
     */
    public static DataSize parse(final CharSequence text, final DataUnit defaultUnit) {
        Assert.notNull(text, "Text must not be null");
        try {
            final Matcher matcher = PATTERN.matcher(StringKit.cleanBlank(text));
            Assert.state(matcher.matches(), "Does not match data size pattern");

            final DataUnit unit = determineDataUnit(matcher.group(3), defaultUnit);
            return DataSize.of(new BigDecimal(matcher.group(1)), unit);
        } catch (final Exception ex) {
            throw new IllegalArgumentException("'" + text + "' is not a valid data size", ex);
        }
    }

    /**
     * Determines the {@link DataUnit} from a given suffix. If the suffix is not recognized, the {@code defaultUnit} is
     * used.
     *
     * @param suffix      The unit suffix string (e.g., "KB", "MB").
     * @param defaultUnit The default {@link DataUnit} to use if the suffix is empty or unrecognized.
     * @return The determined {@link DataUnit}.
     */
    private static DataUnit determineDataUnit(final String suffix, final DataUnit defaultUnit) {
        final DataUnit defaultUnitToUse = (defaultUnit != null ? defaultUnit : DataUnit.BYTES);
        return (StringKit.isNotEmpty(suffix) ? DataUnit.fromSuffix(suffix) : defaultUnitToUse);
    }

    /**
     * Formats a given size in bytes into a human-readable string representation. Uses default formatting (2 decimal
     * places, full unit names). Reference: <a href=
     * "http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc">http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc</a>
     *
     * @param size The size in bytes.
     * @return A human-readable string representation of the size.
     */
    public static String format(final long size) {
        return format(size, false);
    }

    /**
     * Formats a given size in bytes into a human-readable string representation, with an option for simple unit names.
     * Reference: <a href=
     * "http://stackoverflow.com/questions/3263892/format-file-as-mb-gb-etc">http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc</a>
     *
     * @param size          The size in bytes.
     * @param useSimpleName {@code true} to use simple unit names (e.g., "K" for Kilobytes), {@code false} for full
     *                      names (e.g., "KB").
     * @return A human-readable string representation of the size.
     */
    public static String format(final long size, final boolean useSimpleName) {
        return format(size, 2, useSimpleName ? Normal.CAPACITY_SIMPLE_NAMES : Normal.CAPACITY_NAMES, Symbol.SPACE);
    }

    /**
     * Formats a given size in bytes into a human-readable string representation with custom scaling, unit names, and
     * delimiter. Reference: <a href=
     * "http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc">http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc</a>
     *
     * @param size      The size in bytes.
     * @param scale     The number of decimal places to round to.
     * @param unitNames An array of unit names (e.g., {"B", "KB", "MB"}).
     * @param delimiter The string to use as a separator between the numeric value and the unit.
     * @return A human-readable string representation of the size.
     */
    public static String format(final long size, final int scale, final String[] unitNames, final String delimiter) {
        if (size <= 0) {
            return "0";
        }
        final int digitGroups = Math.min(unitNames.length - 1, (int) (Math.log10(size) / Math.log10(1024)));
        return new DecimalFormat("#,##0." + StringKit.repeat('#', scale)).format(size / Math.pow(1024, digitGroups))
                + delimiter + unitNames[digitGroups];
    }

    /**
     * Formats a given size in bytes into a string representation using a specific {@link DataUnit}.
     *
     * @param size         The size in bytes.
     * @param fileDataUnit The {@link DataUnit} to convert the size to for display.
     * @return A string representation of the size in the specified unit.
     */
    public static String format(final Long size, final DataUnit fileDataUnit) {
        if (size <= 0) {
            return Symbol.ZERO;
        }
        final int digitGroups = ArrayKit.indexOf(Normal.CAPACITY_NAMES, fileDataUnit.getSuffix());
        return new DecimalFormat("##0.##").format(size / Math.pow(1024, digitGroups)) + Symbol.SPACE
                + Normal.CAPACITY_NAMES[digitGroups];
    }

    /**
     * Checks if this data size is negative (less than 0 bytes).
     *
     * @return {@code true} if the size is negative, {@code false} otherwise.
     */
    public boolean isNegative() {
        return this.bytes < 0;
    }

    /**
     * Returns the size in bytes.
     *
     * @return The size in bytes.
     */
    public long toBytes() {
        return this.bytes;
    }

    /**
     * Returns the size in kilobytes (KB).
     *
     * @return The size in kilobytes.
     */
    public long toKilobytes() {
        return this.bytes / BYTES_PER_KB;
    }

    /**
     * Returns the size in megabytes (MB).
     *
     * @return The size in megabytes.
     */
    public long toMegabytes() {
        return this.bytes / BYTES_PER_MB;
    }

    /**
     * Returns the size in gigabytes (GB).
     *
     * @return The size in gigabytes.
     */
    public long toGigabytes() {
        return this.bytes / BYTES_PER_GB;
    }

    /**
     * Returns the size in terabytes (TB).
     *
     * @return The size in terabytes.
     */
    public long toTerabytes() {
        return this.bytes / BYTES_PER_TB;
    }

    /**
     * Compares this {@code DataSize} object with the specified {@code DataSize} object for order.
     *
     * @param other The {@code DataSize} object to be compared.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    @Override
    public int compareTo(final DataSize other) {
        return Long.compare(this.bytes, other.bytes);
    }

    /**
     * Returns a string representation of this {@code DataSize} object in bytes. The format is "{@code <bytes>B}".
     *
     * @return A string representation of the data size.
     */
    @Override
    public String toString() {
        return String.format("%dB", this.bytes);
    }

    /**
     * Indicates whether some other object is "equal to" this one. Two {@code DataSize} objects are considered equal if
     * they represent the same number of bytes.
     *
     * @param other The reference object with which to compare.
     * @return {@code true} if this object is the same as the {@code other} argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final DataSize otherSize = (DataSize) other;
        return (this.bytes == otherSize.bytes);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Long.hashCode(this.bytes);
    }

}
