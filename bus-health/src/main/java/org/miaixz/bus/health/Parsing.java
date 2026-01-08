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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Regex;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.logger.Logger;

/**
 * String parsing support.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Parsing {

    /**
     * Default log message template for recording parsing failures.
     */
    private static final String DEFAULT_LOG_MSG = "{} didn't parse. Returning default. {}";

    /**
     * Regular expression for matching Hertz values, e.g., "2.00MHz".
     */
    private static final java.util.regex.Pattern HERTZ_PATTERN = java.util.regex.Pattern
            .compile("(\\d+(.\\d+)?) ?([kKMGT]?Hz).*");

    /**
     * Regular expression for matching byte values, e.g., "4096 MB".
     */
    private static final java.util.regex.Pattern BYTES_PATTERN = java.util.regex.Pattern
            .compile("(\\d+) ?([kKMGT]?B?).*");

    /**
     * Regular expression for matching numbers with units, e.g., "53G".
     */
    private static final java.util.regex.Pattern UNITS_PATTERN = java.util.regex.Pattern
            .compile("(\\d+(.\\d+)?)[\\s]?([kKMGT])?");

    /**
     * Regular expression for matching time format [dd-[hh:[mm:[ss[.sss]]]]].
     */
    private static final java.util.regex.Pattern DHMS = java.util.regex.Pattern
            .compile("(?:(\\d+)-)?(?:(\\d+):)??(?:(\\d+):)?(\\d+)(?:\\.(\\d+))?");

    /**
     * Regular expression for matching UUID format.
     */
    private static final java.util.regex.Pattern UUID_PATTERN = java.util.regex.Pattern
            .compile(".*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*");

    /**
     * Regular expression for matching Vendor ID, Product ID, and Serial Number from Windows device IDs.
     */
    private static final java.util.regex.Pattern VENDOR_PRODUCT_ID_SERIAL = java.util.regex.Pattern
            .compile(".*(?:VID|VEN)_(\\p{XDigit}{4})&(?:PID|DEV)_(\\p{XDigit}{4})(.*)\\\\(.*)");

    /**
     * Regular expression for matching Linux lspci machine-readable format.
     */
    private static final java.util.regex.Pattern LSPCI_MACHINE_READABLE = java.util.regex.Pattern
            .compile("(.+)\\s\\[(.*?)\\]");

    /**
     * Regular expression for matching Linux lspci memory size.
     */
    private static final java.util.regex.Pattern LSPCI_MEMORY_SIZE = java.util.regex.Pattern
            .compile(".+\\s\\[size=(\\d+)([kKMGT])\\]");

    /**
     * Difference in milliseconds between PDH timestamp (starting 1601) and local time (starting 1970).
     */
    private static final long EPOCH_DIFF = 11_644_473_600_000L;

    /**
     * Offset of the current timezone in milliseconds.
     */
    private static final int TZ_OFFSET = TimeZone.getDefault().getOffset(System.currentTimeMillis());

    /**
     * Powers of ten table for quick calculation of powers of 10.
     */
    private static final long[] POWERS_OF_TEN = { 1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L,
            100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L,
            100_000_000_000_000L, 1_000_000_000_000_000L, 10_000_000_000_000_000L, 100_000_000_000_000_000L,
            1_000_000_000_000_000_000L };

    /**
     * DateTime formatter for WMI returned DateTime.
     */
    private static final DateTimeFormatter CIM_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss.SSSSSSZZZZZ", Locale.US);

    /**
     * Decodes REG_BINARY to String. Supports UTF-16LE and Windows-1252 C-strings, otherwise returns a hex.
     */
    public static String decodeBinaryToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        int len = bytes.length;

        // Check for UTF-16LE null terminator (00 00)
        if (len >= 2 && bytes[len - 1] == 0x00 && bytes[len - 2] == 0x00) {
            return new String(bytes, Charset.UTF_16_LE).trim();
        }

        // Check for Windows-1252 (single null terminator)
        if (len >= 1 && bytes[len - 1] == 0x00) {
            return new String(bytes, Charset.WINDOWS_1252).trim();
        }

        // fall back to Hex
        return IntStream.range(0, bytes.length).mapToObj(i -> String.format(Locale.ROOT, "%02X", bytes[i]))
                .collect(Collectors.joining(" "));
    }

    /**
     * Parses a speed from a string, e.g., "2.00 MT/s" to 2000000L.
     *
     * @param speed The transfer speed string.
     * @return The {@link java.lang.Long} MT/s value. If parsing fails, it delegates to {@link #parseHertz(String)}.
     */
    public static long parseSpeed(String speed) {
        if (speed.contains("T/s")) {
            return parseHertz(speed.replace("T/s", "Hz"));
        }
        return parseHertz(speed);
    }

    /**
     * Parses a Hertz value from a string, e.g., "2.00MHz" to 2000000L.
     *
     * @param hertz The Hertz size string.
     * @return The {@link java.lang.Long} Hertz value, or -1 if parsing fails.
     */
    public static long parseHertz(String hertz) {
        Matcher matcher = HERTZ_PATTERN.matcher(hertz.trim());
        if (matcher.find()) {
            // Regex forces #(.#) format, no need to check for NumberFormatException
            Map<String, Long> map = new HashMap<>() {

                {
                    put("Hz", 1L);
                    put("kHz", 1_000L);
                    put("MHz", 1_000_000L);
                    put("GHz", 1_000_000_000L);
                    put("THz", 1_000_000_000_000L);
                    put("PHz", 1_000_000_000_000_000L);
                }
            };
            double value = Double.valueOf(matcher.group(1)) * map.getOrDefault(matcher.group(3), -1L);
            if (value >= 0d) {
                return (long) value;
            }
        }
        return -1L;
    }

    /**
     * Parses the last element of a space-delimited string into an integer value.
     *
     * @param s The string to parse.
     * @param i The default integer to return if parsing fails.
     * @return The parsed value or the given default.
     */
    public static int parseLastInt(String s, int i) {
        try {
            String ls = parseLastString(s);
            if (ls.toLowerCase(Locale.ROOT).startsWith("0x")) {
                return Integer.decode(ls);
            } else {
                return Integer.parseInt(ls);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return i;
        }
    }

    /**
     * Parses the last element of a space-delimited string into a long integer value.
     *
     * @param s  The string to parse.
     * @param li The default long integer to return if parsing fails.
     * @return The parsed value or the given default.
     */
    public static long parseLastLong(String s, long li) {
        try {
            String ls = parseLastString(s);
            if (ls.toLowerCase(Locale.ROOT).startsWith("0x")) {
                return Long.decode(ls);
            } else {
                return Long.parseLong(ls);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return li;
        }
    }

    /**
     * Parses the last element of a space-delimited string into a double-precision floating-point value.
     *
     * @param s The string to parse.
     * @param d The default double to return if parsing fails.
     * @return The parsed value or the given default.
     */
    public static double parseLastDouble(String s, double d) {
        try {
            return Double.parseDouble(parseLastString(s));
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return d;
        }
    }

    /**
     * Parses the last element of a space-delimited string into a string.
     *
     * @param s The string to parse.
     * @return The last space-delimited element.
     */
    public static String parseLastString(String s) {
        String[] ss = Pattern.SPACES_PATTERN.split(s);
        // Guaranteed to have at least one element
        return ss[ss.length - 1];
    }

    /**
     * Parses a human-readable ASCII string into a byte array, truncating or zero-padding if necessary to make the array
     * the specified length.
     *
     * @param text   The string to parse.
     * @param length The length of the returned array.
     * @return A byte array of the specified length, containing the first {@code length} characters converted to bytes.
     *         If the length exceeds the provided string length, it will be zero-padded.
     */
    public static byte[] asciiStringToByteArray(String text, int length) {
        return Arrays.copyOf(text.getBytes(Charset.US_ASCII), length);
    }

    /**
     * Converts a long integer value to a byte array using big-endian order, truncating or zero-padding if necessary to
     * make the array the specified length.
     *
     * @param value     The value to convert.
     * @param valueSize The number of bytes representing the value.
     * @param length    The number of bytes to return.
     * @return A byte array of the specified length, representing the first {@code valueSize} bytes of the long integer.
     */
    public static byte[] longToByteArray(long value, int valueSize, int length) {
        long val = value;
        // Convert long to 8-byte big-endian representation
        byte[] b = new byte[8];
        for (int i = 7; i >= 0 && val != 0L; i--) {
            b[i] = (byte) val;
            val >>>= 8;
        }
        // Copy the rightmost valueSize bytes
        // e.g., for an int, we need the rightmost 4 bytes
        return Arrays.copyOfRange(b, 8 - valueSize, 8 + length - valueSize);
    }

    /**
     * Converts a string to its integer representation.
     *
     * @param text The human-readable ASCII string.
     * @param size The number of characters to convert to a long. Must not exceed 8.
     * @return The integer representing the string, with each character treated as a byte.
     */
    public static long strToLong(String text, int size) {
        return byteArrayToLong(text.getBytes(Charset.US_ASCII), size);
    }

    /**
     * Converts a byte array to its (long integer) representation, assuming big-endian order.
     *
     * @param bytes The byte array to convert. Must not be smaller than the size to convert.
     * @param size  The number of bytes to convert to a long. Must not exceed 8.
     * @return The long integer representing the byte array.
     */
    public static long byteArrayToLong(byte[] bytes, int size) {
        return byteArrayToLong(bytes, size, true);
    }

    /**
     * Converts a byte array to its (long integer) representation, using the specified endianness.
     *
     * @param bytes     The byte array to convert. Must not be smaller than the size to convert.
     * @param size      The number of bytes to convert to a long. Must not exceed 8.
     * @param bigEndian If {@code true}, use big-endian order; if {@code false}, use little-endian order.
     * @return The long integer representing the byte array.
     */
    public static long byteArrayToLong(byte[] bytes, int size, boolean bigEndian) {
        if (size > 8) {
            throw new IllegalArgumentException("Cannot convert more than 8 bytes.");
        }
        if (size > bytes.length) {
            throw new IllegalArgumentException("Size cannot exceed array length.");
        }
        long total = 0L;
        for (int i = 0; i < size; i++) {
            if (bigEndian) {
                total = total << 8 | bytes[i] & 0xff;
            } else {
                total = total << 8 | bytes[size - i - 1] & 0xff;
            }
        }
        return total;
    }

    /**
     * Converts a byte array to its float representation.
     *
     * @param bytes  The byte array to convert. Must not be smaller than the size to convert.
     * @param size   The number of bytes to convert to a float. Must not exceed 8.
     * @param fpBits The number of bits representing the fractional part.
     * @return The float, representing the integer part of the byte array, shifted by {@code fpBits} bits, with the
     *         remaining bits used for the fractional part.
     */
    public static float byteArrayToFloat(byte[] bytes, int size, int fpBits) {
        return byteArrayToLong(bytes, size) / (float) (1 << fpBits);
    }

    /**
     * Converts an unsigned integer to a long integer value. Assumes all bits of the specified integer value are data
     * bits, including the most significant bit which Java ordinarily considers the sign bit. Use this method only when
     * you are certain that the integer value represents an unsigned integer, such as when the integer is returned by
     * the JNA library in a structure that holds an unsigned integer.
     *
     * @param unsignedValue The unsigned integer value to convert.
     * @return The unsigned integer value extended to a long integer.
     */
    public static long unsignedIntToLong(int unsignedValue) {
        // Convert to long using standard Java extension, which performs sign extension,
        // then remove any copies of the sign bit to prevent Java from treating it as a negative value.
        long longValue = unsignedValue;
        return longValue & 0xffff_ffffL;
    }

    /**
     * Converts an unsigned long integer to a signed long integer value by stripping the sign bit. This method will
     * "flip" long integer values greater than the maximum long integer value, but ensures the result is never negative.
     *
     * @param unsignedValue The unsigned long integer value to convert.
     * @return The signed long integer value.
     */
    public static long unsignedLongToSignedLong(long unsignedValue) {
        return unsignedValue & 0x7fff_ffff_ffff_ffffL;
    }

    /**
     * Parses a hexadecimal digit string into a string where each pair of hex digits represents an ASCII character.
     *
     * @param hexString The sequence of hexadecimal digits.
     * @return The corresponding string if it's valid hex; otherwise, the original hexString.
     */
    public static String hexStringToString(String hexString) {
        // Odd length strings can't be parsed, return as-is
        if (hexString.length() % 2 > 0) {
            return hexString;
        }
        int charAsInt;
        StringBuilder sb = new StringBuilder();
        try {
            for (int pos = 0; pos < hexString.length(); pos += 2) {
                charAsInt = Integer.parseInt(hexString.substring(pos, pos + 2), 16);
                if (charAsInt < 32 || charAsInt > 127) {
                    return hexString;
                }
                sb.append((char) charAsInt);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, hexString, e);
            // Hex parsing failed, return original string
            return hexString;
        }
        return sb.toString();
    }

    /**
     * Attempts to parse a string to an integer. If it fails, returns the default value.
     *
     * @param s          The string to parse.
     * @param defaultInt The default value to return if parsing fails.
     * @return The parsed integer, or the default value if parsing fails.
     */
    public static int parseIntOrDefault(String s, int defaultInt) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return defaultInt;
        }
    }

    /**
     * Attempts to parse a string to a long integer. If it fails, returns the default value.
     *
     * @param s           The string to parse.
     * @param defaultLong The default long integer to return if parsing fails.
     * @return The parsed long integer, or the default value if parsing fails.
     */
    public static long parseLongOrDefault(String s, long defaultLong) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return defaultLong;
        }
    }

    /**
     * Attempts to parse a string to an "unsigned" long integer. If it fails, returns the default value.
     *
     * @param s           The string to parse.
     * @param defaultLong The default value to return if parsing fails.
     * @return The parsed long integer, which contains the same 64 bits as the unsigned long (may result in a negative
     *         value).
     */
    public static long parseUnsignedLongOrDefault(String s, long defaultLong) {
        try {
            return new BigInteger(s).longValue();
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return defaultLong;
        }
    }

    /**
     * Attempts to parse a string to a double-precision floating-point number. If it fails, returns the default value.
     *
     * @param s             The string to parse.
     * @param defaultDouble The default double to return if parsing fails.
     * @return The parsed double, or the default value if parsing fails.
     */
    public static double parseDoubleOrDefault(String s, double defaultDouble) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_LOG_MSG, s, e);
            return defaultDouble;
        }
    }

    /**
     * Attempts to parse a string of the form [DD-[hh:]]mm:ss[.ddd] to milliseconds. If it fails, returns the default
     * value.
     *
     * @param s           The string to parse.
     * @param defaultLong The default value to return if parsing fails.
     * @return The parsed number of seconds, or the default value if parsing fails.
     */
    public static long parseDHMSOrDefault(String s, long defaultLong) {
        Matcher m = DHMS.matcher(s);
        if (m.matches()) {
            long milliseconds = 0L;
            if (m.group(1) != null) {
                milliseconds += parseLongOrDefault(m.group(1), 0L) * 86_400_000L;
            }
            if (m.group(2) != null) {
                milliseconds += parseLongOrDefault(m.group(2), 0L) * 3_600_000L;
            }
            if (m.group(3) != null) {
                milliseconds += parseLongOrDefault(m.group(3), 0L) * 60_000L;
            }
            milliseconds += parseLongOrDefault(m.group(4), 0L) * 1000L;
            if (m.group(5) != null) {
                milliseconds += (long) (1000 * parseDoubleOrDefault("0." + m.group(5), 0d));
            }
            return milliseconds;
        }
        return defaultLong;
    }

    /**
     * Attempts to parse a UUID. If it fails, returns the default value.
     *
     * @param s          The string to parse.
     * @param defaultStr The default value to return if parsing fails.
     * @return The parsed UUID, or the default value if parsing fails.
     */
    public static String parseUuidOrDefault(String s, String defaultStr) {
        Matcher m = UUID_PATTERN.matcher(s.toLowerCase(Locale.ROOT));
        if (m.matches()) {
            return m.group(1);
        }
        return defaultStr;
    }

    /**
     * Parses a string of the form key = 'value' (string).
     *
     * @param line The entire string.
     * @return The value between the single quotes.
     */
    public static String getSingleQuoteStringValue(String line) {
        return getStringBetween(line, '\'');
    }

    /**
     * Parses a string of the form key = "value" (string).
     *
     * @param line The entire string.
     * @return The value between the double quotes.
     */
    public static String getDoubleQuoteStringValue(String line) {
        return getStringBetween(line, '"');
    }

    /**
     * Gets the value between two identical characters. Examples:
     * <ul>
     * <li>"name = 'James Gosling's Java'" returns "James Gosling's Java"</li>
     * <li>"pci.name = 'Realtek AC'97 Audio Device'" returns "Realtek AC'97 Audio Device"</li>
     * </ul>
     *
     * @param line The "key-value" pair line to parse.
     * @param c    The character delimiting the string within the line.
     * @return The value between the characters.
     */
    public static String getStringBetween(String line, char c) {
        int firstOcc = line.indexOf(c);
        if (firstOcc < 0) {
            return Normal.EMPTY;
        }
        return line.substring(firstOcc + 1, line.lastIndexOf(c)).trim();
    }

    /**
     * Parses a string of the form "10.12.2" or "key = 1 (0x1) (int)" to find the integer value of the first contiguous
     * set of digits.
     *
     * @param line The entire string.
     * @return The first integer value, or 0 if none is found.
     */
    public static int getFirstIntValue(String line) {
        return getNthIntValue(line, 1);
    }

    /**
     * Parses a string of the form "10.12.2" or "key = 1 (0x1) (int)" to find the integer value of the nth contiguous
     * set of digits.
     *
     * @param line The entire string.
     * @param n    The set of integers to return.
     * @return The nth integer value, or 0 if none is found.
     */
    public static int getNthIntValue(String line, int n) {
        // Split the string by non-digits,
        String[] split = Pattern.NOT_NUMBERS_PATTERN
                .split(Pattern.WITH_NOT_NUMBERS_PATTERN.matcher(line).replaceFirst(Normal.EMPTY));
        if (split.length >= n) {
            return parseIntOrDefault(split[n - 1], 0);
        }
        return 0;
    }

    /**
     * Removes all matching substrings from a given string. More efficient than regular expressions.
     *
     * @param original The source string to remove from.
     * @param toRemove The substring to remove.
     * @return The string with all matching substrings removed.
     */
    public static String removeMatchingString(final String original, final String toRemove) {
        if (original == null || original.isEmpty() || toRemove == null || toRemove.isEmpty()) {
            return original;
        }

        int matchIndex = original.indexOf(toRemove);
        if (matchIndex == -1) {
            return original;
        }

        StringBuilder buffer = new StringBuilder(original.length() - toRemove.length());
        int currIndex = 0;
        do {
            buffer.append(original, currIndex, matchIndex);
            currIndex = matchIndex + toRemove.length();
            matchIndex = original.indexOf(toRemove, currIndex);
        } while (matchIndex != -1);

        buffer.append(original.substring(currIndex));
        return buffer.toString();
    }

    /**
     * Parses a delimited string into a long array. Optimized for handling predictably sized arrays (such as reliable
     * formatted output from Linux proc or sys filesystems) with minimal new object creation. Users should perform
     * additional data integrity checks.
     * <p>
     * Special case, non-numeric fields at the end of the list (e.g., UUID in OpenVZ) will be ignored. Values greater
     * than the maximum long integer value will return the maximum long integer value.
     * <p>
     * The index parameter assumes the specified length for reference, with leading characters ignored. For example, if
     * the string is "foo 12 34 5" and the length is 3, then index 0 is 12, index 1 is 34, and index 2 is 5.
     *
     * @param s         The string to parse.
     * @param indices   An array indicating which indices in the final array should be populated; other values will be
     *                  skipped. This index assumes the rightmost delimited fields of the string contain the array,
     *                  referenced from zero.
     * @param length    The total number of elements in the string array. The number of elements in the string can
     *                  exceed this value; leading elements will be ignored. This should be calculated once per text
     *                  format via {@link #countStringToLongArray}.
     * @param delimiter The delimiter to use.
     * @return The parsed long array if successful. If a parsing error occurs, a zero array will be returned.
     */
    public static long[] parseStringToLongArray(String s, int[] indices, int length, char delimiter) {
        // Ensure the last character is a digit
        s = s.trim();

        long[] parsed = new long[indices.length];
        // Iterate from right to left of the string
        // Use the index array to populate the result array from right to left
        int charIndex = s.length();
        int parsedIndex = indices.length - 1;
        int stringIndex = length - 1;

        int power = 0;
        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean numberFound = false; // Ignore non-digits at the end
        boolean dashSeen = false; // Flag UUID as non-numeric
        while (--charIndex >= 0 && parsedIndex >= 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                // First parsable digit?
                if (!numberFound && numeric) {
                    numberFound = true;
                }
                if (!delimCurrent) {
                    if (numberFound && indices[parsedIndex] == stringIndex--) {
                        parsedIndex--;
                    }
                    delimCurrent = true;
                    power = 0;
                    dashSeen = false;
                    numeric = true;
                }
            } else if (indices[parsedIndex] != stringIndex || c == Symbol.C_PLUS || !numeric) {
                // Does not affect parsing, ignore
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                if (power > 18 || power == 17 && c == '9' && parsed[parsedIndex] > 223_372_036_854_775_807L) {
                    parsed[parsedIndex] = Long.MAX_VALUE;
                } else {
                    parsed[parsedIndex] += (c - '0') * Parsing.POWERS_OF_TEN[power++];
                }
                delimCurrent = false;
            } else if (c == Symbol.C_MINUS) {
                parsed[parsedIndex] *= -1L;
                delimCurrent = false;
                dashSeen = true;
            } else {
                // Mark as non-numeric and continue, unless we've already seen a number
                // Otherwise error
                if (numberFound) {
                    if (!noLog(s)) {
                        Logger.error("Illegal character parsing string '{}' to long array: {}", s, s.charAt(charIndex));
                    }
                    return new long[indices.length];
                }
                parsed[parsedIndex] = 0;
                numeric = false;
            }
        }
        if (parsedIndex > 0) {
            if (!noLog(s)) {
                Logger.error(
                        "Not enough fields in string '{}' parsing to long array: {}",
                        s,
                        indices.length - parsedIndex);
            }
            return new long[indices.length];
        }
        return parsed;
    }

    /**
     * Tests whether to log this message.
     *
     * @param s The string to log.
     * @return {@code true} if the string starts with {@code NOLOG: }.
     */
    private static boolean noLog(String s) {
        return s.startsWith("NOLOG: ");
    }

    /**
     * Parses a delimited string to count the number of elements in a long array. Intended to be called once for the
     * {@code length} field of {@link #parseStringToLongArray} for calculation.
     * <p>
     * Special case, non-numeric fields at the end of the list (e.g., UUID in OpenVZ) will be ignored.
     *
     * @param s         The string to parse.
     * @param delimiter The delimiter to use.
     * @return The number of parsable long integer values after the last unparsable value.
     */
    public static int countStringToLongArray(String s, char delimiter) {
        // Ensure the last character is a digit
        s = s.trim();

        // Iterate from right to left of the string
        // Use the index array to populate the result array from right to left
        int charIndex = s.length();
        int numbers = 0;

        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean dashSeen = false; // Flag UUID as non-numeric
        while (--charIndex >= 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                if (!delimCurrent) {
                    if (numeric) {
                        numbers++;
                    }
                    delimCurrent = true;
                    dashSeen = false;
                    numeric = true;
                }
            } else if (c == Symbol.C_PLUS || !numeric) {
                // Does not affect parsing, ignore
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                delimCurrent = false;
            } else if (c == Symbol.C_MINUS) {
                delimCurrent = false;
                dashSeen = true;
            } else {
                // Found a non-digit or delimiter. If not the last field, exit.
                if (numbers > 0) {
                    return numbers;
                }
                // Otherwise mark as non-numeric and continue
                numeric = false;
            }
        }
        // We reached the beginning of the string, only digits, treat the beginning as a delimiter and exit.
        return numbers + 1;
    }

    /**
     * Gets the string between two marker strings in a line of text.
     *
     * @param text   The text to search for matches.
     * @param before Start matching after this text.
     * @param after  End matching before this text.
     * @return The text between {@code before} and {@code after}, or an empty string if either marker is not found.
     */
    public static String getTextBetweenStrings(String text, String before, String after) {

        String result = Normal.EMPTY;

        if (text.contains(before) && text.contains(after)) {
            result = text.substring(text.indexOf(before) + before.length());
            result = result.substring(0, result.indexOf(after));
        }
        return result;
    }

    /**
     * Converts a long integer representing a filetime (100-nanosecond intervals since 1601) to milliseconds since 1970.
     *
     * @param filetime The 64-bit value representing the FILETIME.
     * @param local    {@code true} if converting from a local filetime (PDH counters); {@code false} if already UTC
     *                 (WMI PerfRawData classes).
     * @return The equivalent number of milliseconds since epoch.
     */
    public static long filetimeToUtcMs(long filetime, boolean local) {
        return filetime / 10_000L - EPOCH_DIFF - (local ? TZ_OFFSET : 0L);
    }

    /**
     * Parses a date string in MM-DD-YYYY or MM/DD/YYYY format to YYYY-MM-DD format.
     *
     * @param dateString The date in MM DD YYYY format.
     * @return The date in ISO YYYY-MM-DD format if parsable; otherwise, the original string.
     */
    public static String parseMmDdYyyyToYyyyMmDD(String dateString) {
        try {
            // Date is MM-DD-YYYY, convert to YYYY-MM-DD
            return String.format(
                    Locale.ROOT,
                    "%s-%s-%s",
                    dateString.substring(6, 10),
                    dateString.substring(0, 2),
                    dateString.substring(3, 5));
        } catch (StringIndexOutOfBoundsException e) {
            return dateString;
        }
    }

    /**
     * Converts a CIM date format string (e.g., {@code 20160513072950.782000-420}) returned by WMI to an
     * {@link java.time.OffsetDateTime}.
     *
     * @param cimDateTime A non-null CIM date format datetime string.
     * @return The parsed {@link java.time.OffsetDateTime} if the string is parsable; otherwise,
     *         {@link Builder#UNIX_EPOCH}.
     */
    public static OffsetDateTime parseCimDateTimeToOffset(String cimDateTime) {
        // Keep the first 22 characters: digits, decimal point, and + or - sign
        // But change the last 3 characters from minute offset to hh:mm
        try {
            // Get from WMI, e.g., 20160513072950.782000-420,
            int tzInMinutes = Integer.parseInt(cimDateTime.substring(22));
            // Modify to 20160513072950.782000-07:00, which is parsable
            LocalTime offsetAsLocalTime = LocalTime.MIDNIGHT.plusMinutes(tzInMinutes);
            return OffsetDateTime.parse(
                    cimDateTime.substring(0, 22) + offsetAsLocalTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                    Parsing.CIM_FORMAT);
        } catch (IndexOutOfBoundsException // If cimDate is not 22+ characters
                | NumberFormatException // If timezone minutes are unparsable
                | DateTimeParseException e) {
            Logger.trace("Unable to parse {} to CIM DateTime.", cimDateTime);
            return Builder.UNIX_EPOCH;
        }
    }

    /**
     * Checks if a file path is equal to or starts with a prefix in the given list.
     *
     * @param prefixList A list of path prefixes.
     * @param path       The string path to check.
     * @return {@code true} if the path is exactly equal to or starts with one of the strings in {@code prefixList}.
     */
    public static boolean filePathStartsWith(List<String> prefixList, String path) {
        for (String match : prefixList) {
            if (path.equals(match) || path.startsWith(match + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a string of the form "53G" or "54.904 M" to its long integer value.
     *
     * @param count The count with a multiplier, e.g., "4096 M".
     * @return The parsed count as a long integer.
     */
    public static long parseMultipliedToLongs(String count) {
        Matcher matcher = UNITS_PATTERN.matcher(count.trim());
        String[] mem;
        if (matcher.find() && matcher.groupCount() == 3) {
            mem = new String[2];
            mem[0] = matcher.group(1);
            mem[1] = matcher.group(3);
        } else {
            mem = new String[] { count };
        }

        double number = Parsing.parseDoubleOrDefault(mem[0], 0L);
        if (mem.length == 2 && mem[1] != null && !mem[1].isEmpty()) {
            switch (mem[1].charAt(0)) {
                case 'T':
                    number *= 1_000_000_000_000L;
                    break;

                case 'G':
                    number *= 1_000_000_000L;
                    break;

                case 'M':
                    number *= 1_000_000L;
                    break;

                case 'K':
                case 'k':
                    number *= 1_000L;
                    break;

                default:
            }
        }
        return (long) number;
    }

    /**
     * Parses a string of the form "4096 MB" to its long integer value. Used for parsing macOS and *nix memory chip
     * sizes. Although the units given are decimal, they must be parsed as binary units.
     *
     * @param size The memory size string, e.g., "4096 MB".
     * @return The parsed size as a long integer.
     */
    public static long parseDecimalMemorySizeToBinary(String size) {
        String[] mem = Regex.SPACES.split(size);
        if (mem.length < 2) {
            // If no spaces, use regex
            Matcher matcher = BYTES_PATTERN.matcher(size.trim());
            if (matcher.find() && matcher.groupCount() == 2) {
                mem = new String[2];
                mem[0] = matcher.group(1);
                mem[1] = matcher.group(2);
            }
        }
        long capacity = Parsing.parseLongOrDefault(mem[0], 0L);
        if (mem.length == 2 && mem[1].length() > 1) {
            switch (mem[1].charAt(0)) {
                case 'T':
                    capacity <<= 40;
                    break;

                case 'G':
                    capacity <<= 30;
                    break;

                case 'M':
                    capacity <<= 20;
                    break;

                case 'K':
                case 'k':
                    capacity <<= 10;
                    break;

                default:
                    break;
            }
        }
        return capacity;
    }

    /**
     * Parses a Windows device ID to obtain the Vendor ID, Product ID, and Serial Number.
     *
     * @param deviceId The device ID.
     * @return A {@link Triplet} where the first element is the Vendor ID, the second is the Product ID, and the third
     *         is the Serial Number or an empty string if successfully parsed, otherwise {@code null}.
     */
    public static Triplet<String, String, String> parseDeviceIdToVendorProductSerial(String deviceId) {
        Matcher m = VENDOR_PRODUCT_ID_SERIAL.matcher(deviceId);
        if (m.matches()) {
            String vendorId = "0x" + m.group(1).toLowerCase(Locale.ROOT);
            String productId = "0x" + m.group(2).toLowerCase(Locale.ROOT);
            String serial = m.group(4);
            return Triplet.of(
                    vendorId,
                    productId,
                    !m.group(3).isEmpty() || serial.contains(Symbol.AND) ? Normal.EMPTY : serial);
        }
        return null;
    }

    /**
     * Parses a Linux lshw resource string to calculate memory size.
     *
     * @param resources A string containing one or more elements like {@code memory:b00000000-bffffffff}.
     * @return The number of bytes of memory consumed in the {@code resources} string.
     */
    public static long parseLshwResourceString(String resources) {
        long bytes = 0L;
        // First split by spaces
        String[] resourceArray = Regex.SPACES.split(resources);
        for (String r : resourceArray) {
            // Remove prefix
            if (r.startsWith("memory:")) {
                // Split into low and high addresses
                String[] mem = r.substring(7).split(Symbol.MINUS);
                if (mem.length == 2) {
                    try {
                        // Parse hex strings
                        bytes += Long.parseLong(mem[1], 16) - Long.parseLong(mem[0], 16) + 1;
                    } catch (NumberFormatException e) {
                        Logger.trace(DEFAULT_LOG_MSG, r, e);
                    }
                }
            }
        }
        return bytes;
    }

    /**
     * Parses a Linux lspci machine-readable line to get its name and ID.
     *
     * @param line A string of the form Foo [bar].
     * @return A pair of strings separating the text before and inside the brackets if found, otherwise {@code null}.
     */
    public static Pair<String, String> parseLspciMachineReadable(String line) {
        Matcher matcher = LSPCI_MACHINE_READABLE.matcher(line);
        if (matcher.matches()) {
            return Pair.of(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    /**
     * Parses a Linux lspci line containing memory size.
     *
     * @param line A string of the form Foo [size=256M].
     * @return The memory size in bytes.
     */
    public static long parseLspciMemorySize(String line) {
        Matcher matcher = LSPCI_MEMORY_SIZE.matcher(line);
        if (matcher.matches()) {
            return parseDecimalMemorySizeToBinary(matcher.group(1) + Symbol.SPACE + matcher.group(2) + "B");
        }
        return 0;
    }

    /**
     * Parses a space-delimited list of integers with hyphenated ranges into a list containing only integers. For
     * example, 0 1 4-7 parses to a list containing 0, 1, 4, 5, 6, and 7. Also supports comma-separated entries like 0,
     * 2-5, 7-8, 9 which parses to a list containing 0, 2, 3, 4, 5, 7, 8, 9.
     *
     * @param text The string containing space-delimited integers or hyphenated range integers.
     * @return A list of integers representing the provided ranges.
     */
    public static List<Integer> parseHyphenatedIntList(String text) {
        List<Integer> result = new ArrayList<>();
        String[] csvTokens = text.split(Symbol.COMMA);
        for (String csvToken : csvTokens) {
            csvToken = csvToken.trim();
            for (String s : Regex.SPACES.split(csvToken)) {
                if (s.contains(Symbol.MINUS)) {
                    int first = getFirstIntValue(s);
                    int last = getNthIntValue(s, 2);
                    for (int i = first; i <= last; i++) {
                        result.add(i);
                    }
                } else {
                    int only = Parsing.parseIntOrDefault(s, -1);
                    if (only >= 0) {
                        result.add(only);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Parses a big-endian IP format integer into its component bytes representing an IPv4 address.
     *
     * @param ip The address as an integer.
     * @return The address as a four-byte array.
     */
    public static byte[] parseIntToIP(int ip) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ip).array();
    }

    /**
     * Parses a big-endian IP format integer array into its component bytes representing an IPv6 address.
     *
     * @param ip6 The address as an integer array.
     * @return The address as a sixteen-byte array.
     */
    public static byte[] parseIntArrayToIP(int[] ip6) {
        ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        for (int i : ip6) {
            bb.putInt(i);
        }
        return bb.array();
    }

    /**
     * TCP network addresses and ports are by definition in big-endian format. The two bytes of a 16-bit unsigned short
     * port value must be reversed.
     *
     * @param port The port number in big-endian order.
     * @return The port number.
     * @see <a href= "https://docs.microsoft.com/en-us/windows/win32/api/winsock/nf-winsock-ntohs">ntohs</a>
     */
    public static int bigEndian16ToLittleEndian(int port) {
        // 20480 = 0x5000 should be 0x0050 = 80
        // 47873 = 0xBB01 should be 0x01BB = 443
        return port >> 8 & 0xff | port << 8 & 0xff00;
    }

    /**
     * Parses an integer array into an IPv4 or IPv6 address, as appropriate.
     * <p>
     * Applicable for the {@code ut_addr_v6} element of the Utmp structure.
     *
     * @param utAddrV6 A four-integer array representing an IPv6 address. IPv4 addresses only use {@code utAddrV6[0]}.
     * @return The string representation of the IP address.
     */
    public static String parseUtAddrV6toIP(int[] utAddrV6) {
        if (utAddrV6.length != 4) {
            throw new IllegalArgumentException("ut_addr_v6 must have exactly 4 elements");
        }
        // IPv4 only has the first element
        if (utAddrV6[1] == 0 && utAddrV6[2] == 0 && utAddrV6[3] == 0) {
            // Special case, all zeros
            if (utAddrV6[0] == 0) {
                return "::";
            }
            // Use InetAddress to parse
            byte[] ipv4 = ByteBuffer.allocate(4).putInt(utAddrV6[0]).array();
            try {
                return InetAddress.getByAddress(ipv4).getHostAddress();
            } catch (UnknownHostException e) {
                // Should not happen for length 4 or 16
                return Normal.UNKNOWN;
            }
        }
        // Parse all 16 bytes
        byte[] ipv6 = ByteBuffer.allocate(16).putInt(utAddrV6[0]).putInt(utAddrV6[1]).putInt(utAddrV6[2])
                .putInt(utAddrV6[3]).array();
        try {
            return InetAddress.getByAddress(ipv6).getHostAddress()
                    .replaceAll("((?:(?:^|:)0+\\b){2,8}):?(?!\\S*\\b\\1:0+\\b)(\\S*)", "::$2");
        } catch (UnknownHostException e) {
            // Should not happen for length 4 or 16
            return Normal.UNKNOWN;
        }
    }

    /**
     * Parses a hexadecimal digit string into an integer value.
     *
     * @param hexString    The sequence of hexadecimal digits.
     * @param defaultValue The default value to return if parsing fails.
     * @return The corresponding integer value.
     */
    public static int hexStringToInt(String hexString, int defaultValue) {
        if (hexString != null) {
            try {
                if (hexString.startsWith("0x")) {
                    return new BigInteger(hexString.substring(2), 16).intValue();
                } else {
                    return new BigInteger(hexString, 16).intValue();
                }
            } catch (NumberFormatException e) {
                Logger.trace(DEFAULT_LOG_MSG, hexString, e);
            }
        }
        // Hex parsing failed, return default integer
        return defaultValue;
    }

    /**
     * Parses a hexadecimal digit string into a long integer value.
     *
     * @param hexString    The sequence of hexadecimal digits.
     * @param defaultValue The default value to return if parsing fails.
     * @return The corresponding long integer value.
     */
    public static long hexStringToLong(String hexString, long defaultValue) {
        if (hexString != null) {
            try {
                if (hexString.startsWith("0x")) {
                    return new BigInteger(hexString.substring(2), 16).longValue();
                } else {
                    return new BigInteger(hexString, 16).longValue();
                }
            } catch (NumberFormatException e) {
                Logger.trace(DEFAULT_LOG_MSG, hexString, e);
            }
        }
        // Hex parsing failed, return default long integer
        return defaultValue;
    }

    /**
     * Parses a string of the form "....foo" to "foo".
     *
     * @param dotPrefixedStr The string potentially prefixed with dots.
     * @return The string with leading dots removed.
     */
    public static String removeLeadingDots(String dotPrefixedStr) {
        int pos = 0;
        while (pos < dotPrefixedStr.length() && dotPrefixedStr.charAt(pos) == '.') {
            pos++;
        }
        return pos < dotPrefixedStr.length() ? dotPrefixedStr.substring(pos) : Normal.EMPTY;
    }

    /**
     * Parses a null-delimited byte array into a list of strings.
     *
     * @param bytes The byte array containing null-delimited strings. Two consecutive nulls mark the end of the list.
     * @return A list of strings between the null characters.
     */
    public static List<String> parseByteArrayToStrings(byte[] bytes) {
        List<String> strList = new ArrayList<>();
        int start = 0;
        int end = 0;
        // Iterate characters
        do {
            // If we hit a delimiter or end of array or newline (Linux), add to list
            if (end == bytes.length || bytes[end] == 0 || bytes[end] == '\n') {
                // Zero-length string means two nulls, done
                if (start == end) {
                    break;
                }
                // Otherwise add string and reset start
                // Deliberately using platform default charset
                strList.add(new String(bytes, start, end - start, Charset.UTF_8));
                start = end + 1;
            }
        } while (end++ < bytes.length);
        return strList;
    }

    /**
     * Parses a null-delimited byte array into a map of string key-value pairs.
     *
     * @param bytes The byte array containing string key-value pairs, with keys and values separated by {@code =} and
     *              pairs separated by null characters. Two consecutive nulls mark the end of the map.
     * @return A map of string key-value pairs between the null characters.
     */
    public static Map<String, String> parseByteArrayToStringMap(byte[] bytes) {
        // The API does not specify a particular order for entries, but it is reasonable to preserve the order provided
        // by the operating system to the end user.
        Map<String, String> strMap = new LinkedHashMap<>();
        int start = 0;
        int end = 0;
        String key = null;
        // Iterate characters
        do {
            // If we hit a delimiter or end of array, add to list
            if (end == bytes.length || bytes[end] == 0) {
                // Zero-length string with no key, done
                if (start == end && key == null) {
                    break;
                }
                // Otherwise add string (which may be empty) and reset start
                // Deliberately using platform default charset
                strMap.put(key, new String(bytes, start, end - start, Charset.UTF_8));
                key = null;
                start = end + 1;
            } else if (bytes[end] == Symbol.C_EQUAL && key == null) {
                key = new String(bytes, start, end - start, Charset.UTF_8);
                start = end + 1;
            }
        } while (end++ < bytes.length);
        return strMap;
    }

    /**
     * Parses a null-delimited character array into a map of string key-value pairs.
     *
     * @param chars The character array containing string key-value pairs, with keys and values separated by {@code =}
     *              and pairs separated by null characters. Two consecutive nulls mark the end of the map.
     * @return A map of string key-value pairs between the null characters.
     */
    public static Map<String, String> parseCharArrayToStringMap(char[] chars) {
        // The API does not specify a particular order for entries, but it is reasonable to preserve the order provided
        // by the operating system to the end user.
        Map<String, String> strMap = new LinkedHashMap<>();
        int start = 0;
        int end = 0;
        String key = null;
        // Iterate characters
        do {
            // If we hit a delimiter or end of array, add to list
            if (end == chars.length || chars[end] == 0) {
                // Zero-length string with no key, done
                if (start == end && key == null) {
                    break;
                }
                // Otherwise add string (which may be empty) and reset start
                // Deliberately using platform default charset
                strMap.put(key, new String(chars, start, end - start));
                key = null;
                start = end + 1;
            }
            // If we hit an equals sign and haven't found a key yet, set the key
            else if (chars[end] == Symbol.C_EQUAL && key == null) {
                key = new String(chars, start, end - start);
                start = end + 1;
            }
        } while (end++ < chars.length);
        return strMap;
    }

    /**
     * Parses a delimited string into an enum map. Multiple consecutive delimiters are treated as one.
     *
     * @param <K>    The type extending Enum.
     * @param clazz  The enum class.
     * @param values The delimited string to parse into a map.
     * @param delim  The delimiter to use.
     * @return An EnumMap populated with the delimited string values in order. If there are fewer string values than
     *         enum values, subsequent enum values are not mapped. The last enum value will contain the remainder of the
     *         string, including excess delimiters.
     */
    public static <K extends Enum<K>> Map<K, String> stringToEnumMap(Class<K> clazz, String values, char delim) {
        EnumMap<K, String> map = new EnumMap<>(clazz);
        int start = 0;
        int len = values.length();
        EnumSet<K> keys = EnumSet.allOf(clazz);
        int keySize = keys.size();
        for (K key : keys) {
            // If this is the last enum, place the index at the end of the string, otherwise at the delimiter
            int idx = --keySize == 0 ? len : values.indexOf(delim, start);
            if (idx >= 0) {
                map.put(key, values.substring(start, idx));
                start = idx;
                do {
                    start++;
                } while (start < len && values.charAt(start) == delim);
            } else {
                map.put(key, values.substring(start));
                break;
            }
        }
        return map;
    }

    /**
     * Checks if a value exists in the map for the given key and returns it, or {@code unknown} if not.
     *
     * @param map The map of string key-value pairs.
     * @param key The key for which to get the value.
     * @return The value associated with the key if it exists in the map; otherwise, {@code unknown}.
     */
    public static String getValueOrUnknown(Map<String, String> map, String key) {
        String value = map.getOrDefault(key, Normal.EMPTY);
        return value.isEmpty() ? Normal.UNKNOWN : value;
    }

    /**
     * Checks if a value exists in the map for the given key and returns it, or {@code unknown} if not.
     *
     * @param map The map where keys can be of any type and values are strings.
     * @param key The key to retrieve the value from the map. The key can be of any type compatible with the map's key
     *            type.
     * @return The value associated with the key if it exists in the map and is not empty; otherwise, the predefined
     *         "unknown" string.
     */
    public static String getValueOrUnknown(Map<?, String> map, Object key) {
        return getStringValueOrUnknown(map.get(key));
    }

    /**
     * Returns the given string value if it is not null or empty; otherwise, returns {@code Constants.UNKNOWN}.
     *
     * @param value The input string value.
     * @return The input value if it is not null or empty; otherwise, {@code Constants.UNKNOWN}.
     */
    public static String getStringValueOrUnknown(String value) {
        return (value == null || value.isEmpty()) ? Normal.UNKNOWN : value;
    }

    /**
     * Parses a date string from a given format and converts it to epoch time (milliseconds since epoch). This method is
     * useful for handling date formats across different operating systems, such as:
     * <ul>
     * <li>{@code yyyyMMdd}</li>
     * <li>{@code dd/MM/yy, HH:mm}</li>
     * </ul>
     *
     * @param dateString  The date string to parse.
     * @param datePattern The expected date format pattern (e.g., {@code "yyyyMMdd"}).
     * @return The number of milliseconds since January 1, 1970, UTC. Returns {@code 0} if parsing fails.
     */
    public static long parseDateToEpoch(String dateString, String datePattern) {
        if (dateString == null || dateString.equals(Normal.UNKNOWN) || dateString.isEmpty() || datePattern.isEmpty()) {
            return 0; // Return default if date is unknown or empty
        }

        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(datePattern)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                    .toFormatter(Locale.ROOT);
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            Logger.trace("Unable to parse date string: " + dateString);
            return 0;
        }
    }

}
