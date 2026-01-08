/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health;

import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * Utility class for formatting units or converting between numeric types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Formats {

    /**
     * Formats bytes as a rounded string representation using IEC standard (matches Mac/Linux). For hard drive capacity,
     * use {@link #formatBytesDecimal(long)}. For Windows KB, MB, and GB display, edit the returned string to remove the
     * 'i' to display (incorrect) JEDEC units.
     *
     * @param bytes The number of bytes.
     * @return A rounded string representation of the byte size.
     */
    public static String formatBytes(long bytes) {
        if (bytes == 1L) { // Single byte
            return String.format(Locale.ROOT, "%d byte", bytes);
        } else if (bytes < Normal.KIBI) { // Less than 1024 bytes
            return String.format(Locale.ROOT, "%d bytes", bytes);
        } else if (bytes < Normal.MEBI) { // KiB
            return formatUnits(bytes, Normal.KIBI, "KiB");
        } else if (bytes < Normal.GIBI) { // MiB
            return formatUnits(bytes, Normal.MEBI, "MiB");
        } else if (bytes < Normal.TEBI) { // GiB
            return formatUnits(bytes, Normal.GIBI, "GiB");
        } else if (bytes < Normal.PEBI) { // TiB
            return formatUnits(bytes, Normal.TEBI, "TiB");
        } else if (bytes < Normal.EXBI) { // PiB
            return formatUnits(bytes, Normal.PEBI, "PiB");
        } else { // EiB
            return formatUnits(bytes, Normal.EXBI, "EiB");
        }
    }

    /**
     * Formats a unit to an exact integer or decimal based on the prefix, appending the appropriate unit.
     *
     * @param value  The value to format.
     * @param prefix The divisor for the unit multiplier.
     * @param unit   The string representing the unit.
     * @return The formatted string value.
     */
    private static String formatUnits(long value, long prefix, String unit) {
        if (value % prefix == 0) {
            return String.format(Locale.ROOT, "%d %s", value / prefix, unit);
        }
        return String.format(Locale.ROOT, "%.1f %s", (double) value / prefix, unit);
    }

    /**
     * Formats bytes as a rounded string representation using decimal SI units. Used by hard drive manufacturers for
     * capacity representation. Most other storage should use {@link #formatBytes(long)}.
     *
     * @param bytes The number of bytes.
     * @return A rounded string representation of the byte size.
     */
    public static String formatBytesDecimal(long bytes) {
        if (bytes == 1L) { // Single byte
            return String.format(Locale.ROOT, "%d byte", bytes);
        } else if (bytes < Normal.KILO) { // Less than 1000 bytes
            return String.format(Locale.ROOT, "%d bytes", bytes);
        } else {
            return formatValue(bytes, "B");
        }
    }

    /**
     * Formats hertz as a rounded string representation.
     *
     * @param hertz The hertz value.
     * @return A rounded string representation of the hertz size.
     */
    public static String formatHertz(long hertz) {
        return formatValue(hertz, "Hz");
    }

    /**
     * Formats any unit as a rounded string representation.
     *
     * @param value The value to format.
     * @param unit  The unit to append with metric prefix.
     * @return A rounded string representation with metric prefix.
     */
    public static String formatValue(long value, String unit) {
        if (value < Normal.KILO) {
            return String.format(Locale.ROOT, "%d %s", value, unit).trim();
        } else if (value < Normal.MEGA) { // K
            return formatUnits(value, Normal.KILO, "K" + unit);
        } else if (value < Normal.GIGA) { // M
            return formatUnits(value, Normal.MEGA, "M" + unit);
        } else if (value < Normal.TERA) { // G
            return formatUnits(value, Normal.GIGA, "G" + unit);
        } else if (value < Normal.PETA) { // T
            return formatUnits(value, Normal.TERA, "T" + unit);
        } else if (value < Normal.EXA) { // P
            return formatUnits(value, Normal.PETA, "P" + unit);
        } else { // E
            return formatUnits(value, Normal.EXA, "E" + unit);
        }
    }

    /**
     * Formats elapsed time in seconds as days, hours:minutes:seconds.
     *
     * @param secs The number of elapsed seconds.
     * @return A string representation of the elapsed time.
     */
    public static String formatElapsedSecs(long secs) {
        long eTime = secs;
        final long days = TimeUnit.SECONDS.toDays(eTime);
        eTime -= TimeUnit.DAYS.toSeconds(days);
        final long hr = TimeUnit.SECONDS.toHours(eTime);
        eTime -= TimeUnit.HOURS.toSeconds(hr);
        final long min = TimeUnit.SECONDS.toMinutes(eTime);
        eTime -= TimeUnit.MINUTES.toSeconds(min);
        final long sec = eTime;
        return String.format(Locale.ROOT, "%d days, %02d:%02d:%02d", days, hr, min, sec);
    }

    /**
     * Converts an unsigned integer to a signed long.
     *
     * @param x A signed integer representing an unsigned integer.
     * @return The unsigned long value of x.
     */
    public static long getUnsignedInt(int x) {
        return x & 0x0000_0000_ffff_ffffL;
    }

    /**
     * Represents a 32-bit value as an unsigned integer.
     * <p>
     * This is a Java 7 implementation of Java 8's Integer.toUnsignedString.
     *
     * @param i The 32-bit value.
     * @return The string representation of the unsigned integer.
     */
    public static String toUnsignedString(int i) {
        if (i >= 0) {
            return Integer.toString(i);
        }
        return Long.toString(getUnsignedInt(i));
    }

    /**
     * Represents a 64-bit value as an unsigned long.
     * <p>
     * This is a Java 7 implementation of Java 8's Long.toUnsignedString.
     *
     * @param l The 64-bit value.
     * @return The string representation of the unsigned long.
     */
    public static String toUnsignedString(long l) {
        if (l >= 0) {
            return Long.toString(l);
        }
        return BigInteger.valueOf(l).add(Normal.TWOS_COMPLEMENT_REF).toString();
    }

    /**
     * Converts an integer error code to its hexadecimal representation.
     *
     * @param errorCode The error code.
     * @return A string representing the error code in 0x.... format.
     */
    public static String formatError(int errorCode) {
        return String.format(Locale.ROOT, Normal.HEX_ERROR, errorCode);
    }

    /**
     * Rounds a floating-point number to the nearest integer.
     *
     * @param x The floating-point number.
     * @return The rounded integer.
     */
    public static int roundToInt(double x) {
        return (int) Math.round(x);
    }

}
