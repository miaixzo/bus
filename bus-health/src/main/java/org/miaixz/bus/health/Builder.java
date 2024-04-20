/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.unix.LibCAPI;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triple;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String parsing utility.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Builder {

    /**
     * The official/approved path for sysfs information. Note: /sys/class/dmi/id
     * symlinks here
     */
    public static final String SYSFS_SERIAL_PATH = "/sys/devices/virtual/dmi/id/";
    /**
     * The Unix Epoch, a default value when WMI DateTime queries return no value.
     */
    public static final OffsetDateTime UNIX_EPOCH = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    /**
     * Constant <code>whitespacesColonWhitespace</code>
     */
    public static final Pattern whitespacesColonWhitespace = Pattern.compile("\\s+:\\s");
    /**
     * Constant <code>notDigits</code>
     */
    public static final Pattern notDigits = Pattern.compile("[^0-9]+");
    /**
     * Constant <code>startWithNotDigits</code>
     */
    public static final Pattern startWithNotDigits = Pattern.compile("^[^0-9]*");
    /**
     * Constant <code>forwardSlash</code>
     */
    public static final Pattern slash = Pattern.compile("\\/");
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";
    private static final String READING_LOG = "Reading file {}";
    private static final String READ_LOG = "Read {}";
    private static final String DEFAULT_LOG_MSG = "{} didn't parse. Returning default. {}";
    /**
     * Used for matching
     */
    private static final Pattern HERTZ_PATTERN = Pattern.compile("(\\d+(.\\d+)?) ?([kKMGT]?Hz).*");
    private static final Pattern BYTES_PATTERN = Pattern.compile("(\\d+) ?([kKMGT]?B?).*");
    private static final Pattern UNITS_PATTERN = Pattern.compile("(\\d+(.\\d+)?)[\\s]?([kKMGT])?");
    /**
     * Used to check validity of a hexadecimal string
     */
    private static final Pattern VALID_HEX = Pattern.compile("[0-9a-fA-F]+");
    /**
     * Pattern for [dd-[hh:[mm:[ss[.sss]]]]]
     */
    private static final Pattern DHMS = Pattern.compile("(?:(\\d+)-)?(?:(\\d+):)??(?:(\\d+):)?(\\d+)(?:\\.(\\d+))?");
    /**
     * Pattern for a UUID
     */
    private static final Pattern UUID_PATTERN = Pattern
            .compile(".*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*");
    /**
     * Pattern for Windows DeviceID vendor and product ID and serial
     */
    private static final Pattern VENDOR_PRODUCT_ID_SERIAL = Pattern
            .compile(".*(?:VID|VEN)_(\\p{XDigit}{4})&(?:PID|DEV)_(\\p{XDigit}{4})(.*)\\\\(.*)");
    /**
     * Pattern for Linux lspci machine readable
     */
    private static final Pattern LSPCI_MACHINE_READABLE = Pattern.compile("(.+)\\s\\[(.*?)\\]");
    /**
     * Pattern for Linux lspci memory
     */
    private static final Pattern LSPCI_MEMORY_SIZE = Pattern.compile(".+\\s\\[size=(\\d+)([kKMGT])\\]");
    /**
     * Hertz related variables.
     */
    private static final String HZ = "Hz";
    private static final String KHZ = "kHz";
    private static final String MHZ = "MHz";
    private static final String GHZ = "GHz";
    private static final String THZ = "THz";
    private static final String PHZ = "PHz";
    private static final Map<String, Long> multipliers;
    // PDH timestamps are 1601 epoch, local time
    // Constants to convert to UTC millis
    private static final long EPOCH_DIFF = 11_644_473_600_000L;
    private static final int TZ_OFFSET = TimeZone.getDefault().getOffset(System.currentTimeMillis());
    // Fast decimal exponentiation: pow(10,y) --> POWERS_OF_10[y]
    private static final long[] POWERS_OF_TEN = {1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L,
            100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L,
            100_000_000_000_000L, 1_000_000_000_000_000L, 10_000_000_000_000_000L, 100_000_000_000_000_000L,
            1_000_000_000_000_000_000L};
    // Format returned by WMI for DateTime
    private static final DateTimeFormatter CIM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSSSZZZZZ",
            Locale.US);

    static {
        multipliers = new HashMap<>();
        multipliers.put(HZ, 1L);
        multipliers.put(KHZ, 1_000L);
        multipliers.put(MHZ, 1_000_000L);
        multipliers.put(GHZ, 1_000_000_000L);
        multipliers.put(THZ, 1_000_000_000_000L);
        multipliers.put(PHZ, 1_000_000_000_000_000L);
    }

    /**
     * Parse hertz from a string, eg. "2.00MHz" is 2000000L.
     *
     * @param hertz Hertz size.
     * @return {@link java.lang.Long} Hertz value or -1 if not parseable.
     */
    public static long parseHertz(String hertz) {
        Matcher matcher = HERTZ_PATTERN.matcher(hertz.trim());
        if (matcher.find() && matcher.groupCount() == 3) {
            // Regexp enforces #(.#) format so no test for NFE required
            double value = Double.valueOf(matcher.group(1)) * multipliers.getOrDefault(matcher.group(3), -1L);
            if (value >= 0d) {
                return (long) value;
            }
        }
        return -1L;
    }

    /**
     * Parse the last element of a space-delimited string to a value
     *
     * @param s The string to parse
     * @param i Default integer if not parsable
     * @return value or the given default if not parsable
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
     * Parse the last element of a space-delimited string to a value
     *
     * @param s  The string to parse
     * @param li Default long integer if not parsable
     * @return value or the given default if not parsable
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
     * Parse the last element of a space-delimited string to a value
     *
     * @param s The string to parse
     * @param d Default double if not parsable
     * @return value or the given default if not parsable
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
     * Parse the last element of a space-delimited string to a string
     *
     * @param s The string to parse
     * @return last space-delimited element
     */
    public static String parseLastString(String s) {
        String[] ss = RegEx.SPACES.split(s);
        // guaranteed at least one element
        return ss[ss.length - 1];
    }

    /**
     * Parse a byte array into a string of hexadecimal digits including all array
     * bytes as digits
     *
     * @param bytes The byte array to represent
     * @return A string of hex characters corresponding to the bytes. The string is
     * upper case.
     */
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b & 0xf0) >>> 4, 16));
            sb.append(Character.forDigit(b & 0x0f, 16));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * Parse a string of hexadecimal digits into a byte array
     *
     * @param digits The string to be parsed
     * @return a byte array with each pair of characters converted to a byte, or
     * empty array if the string is not valid hex
     */
    public static byte[] hexStringToByteArray(String digits) {
        int len = digits.length();
        // Check if string is valid hex
        if (!VALID_HEX.matcher(digits).matches() || (len & 0x1) != 0) {
            Logger.warn("Invalid hexadecimal string: {}", digits);
            return new byte[0];
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (Character.digit(digits.charAt(i), 16) << 4
                    | Character.digit(digits.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Parse a human readable ASCII string into a byte array, truncating or padding
     * with zeros (if necessary) so the array has the specified length.
     *
     * @param text   The string to be parsed
     * @param length Length of the returned array.
     * @return A byte array of specified length, with each of the first length
     * characters converted to a byte. If length is longer than the provided
     * string length, will be filled with zeroes.
     */
    public static byte[] asciiStringToByteArray(String text, int length) {
        return Arrays.copyOf(text.getBytes(Charset.US_ASCII), length);
    }

    /**
     * Convert a long value to a byte array using Big Endian, truncating or padding
     * with zeros (if necessary) so the array has the specified length.
     *
     * @param value     The value to be converted
     * @param valueSize Number of bytes representing the value
     * @param length    Number of bytes to return
     * @return A byte array of specified length representing the long in the first
     * valueSize bytes
     */
    public static byte[] longToByteArray(long value, int valueSize, int length) {
        long val = value;
        // Convert the long to 8-byte BE representation
        byte[] b = new byte[8];
        for (int i = 7; i >= 0 && val != 0L; i--) {
            b[i] = (byte) val;
            val >>>= 8;
        }
        // Then copy the rightmost valueSize bytes
        // e.g., for an integer we want rightmost 4 bytes
        return Arrays.copyOfRange(b, 8 - valueSize, 8 + length - valueSize);
    }

    /**
     * Convert a string to an integer representation.
     *
     * @param text A human readable ASCII string
     * @param size Number of characters to convert to the long. May not exceed 8.
     * @return An integer representing the string where each character is treated as
     * a byte
     */
    public static long strToLong(String text, int size) {
        return byteArrayToLong(text.getBytes(Charset.US_ASCII), size);
    }

    /**
     * Convert a byte array to its (long) integer representation assuming big endian
     * ordering.
     *
     * @param bytes An array of bytes no smaller than the size to be converted
     * @param size  Number of bytes to convert to the long. May not exceed 8.
     * @return A long integer representing the byte array
     */
    public static long byteArrayToLong(byte[] bytes, int size) {
        return byteArrayToLong(bytes, size, true);
    }

    /**
     * Convert a byte array to its (long) integer representation in the specified
     * endianness.
     *
     * @param bytes     An array of bytes no smaller than the size to be converted
     * @param size      Number of bytes to convert to the long. May not exceed 8.
     * @param bigEndian True to parse big-endian, false to parse little-endian
     * @return An long integer representing the byte array
     */
    public static long byteArrayToLong(byte[] bytes, int size, boolean bigEndian) {
        if (size > 8) {
            throw new IllegalArgumentException("Can't convert more than 8 bytes.");
        }
        if (size > bytes.length) {
            throw new IllegalArgumentException("Size can't be larger than array length.");
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
     * Convert a byte array to its floating point representation.
     *
     * @param bytes  An array of bytes no smaller than the size to be converted
     * @param size   Number of bytes to convert to the float. May not exceed 8.
     * @param fpBits Number of bits representing the decimal
     * @return A float; the integer portion representing the byte array as an
     * integer shifted by the bits specified in fpBits; with the remaining
     * bits used as a decimal
     */
    public static float byteArrayToFloat(byte[] bytes, int size, int fpBits) {
        return byteArrayToLong(bytes, size) / (float) (1 << fpBits);
    }

    /**
     * Convert an unsigned integer to a long value. The method assumes that all bits
     * in the specified integer value are 'data' bits, including the
     * most-significant bit which Java normally considers a sign bit. The method
     * must be used only when it is certain that the integer value represents an
     * unsigned integer, for example when the integer is returned by JNA library in
     * a structure which holds unsigned integers.
     *
     * @param unsignedValue The unsigned integer value to convert.
     * @return The unsigned integer value widened to a long.
     */
    public static long unsignedIntToLong(int unsignedValue) {
        // use standard Java widening conversion to long which does
        // sign-extension,
        // then drop any copies of the sign bit, to prevent the value being
        // considered a negative one by Java if it is set
        long longValue = unsignedValue;
        return longValue & 0xffff_ffffL;
    }

    /**
     * Convert an unsigned long to a signed long value by stripping the sign bit.
     * This method "rolls over" long values greater than the max value but ensures
     * the result is never negative.
     *
     * @param unsignedValue The unsigned long value to convert.
     * @return The signed long value.
     */
    public static long unsignedLongToSignedLong(long unsignedValue) {
        return unsignedValue & 0x7fff_ffff_ffff_ffffL;
    }

    /**
     * Parses a string of hex digits to a string where each pair of hex digits
     * represents an ASCII character
     *
     * @param hexString A sequence of hex digits
     * @return The corresponding string if valid hex; otherwise the original
     * hexString
     */
    public static String hexStringToString(String hexString) {
        // Odd length strings won't parse, return
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
            // Hex failed to parse, just return the existing string
            return hexString;
        }
        return sb.toString();
    }

    /**
     * Attempts to parse a string to an int. If it fails, returns the default
     *
     * @param s          The string to parse
     * @param defaultInt The value to return if parsing fails
     * @return The parsed int, or the default if parsing failed
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
     * Attempts to parse a string to a long. If it fails, returns the default
     *
     * @param s           The string to parse
     * @param defaultLong The value to return if parsing fails
     * @return The parsed long, or the default if parsing failed
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
     * Attempts to parse a string to an "unsigned" long. If it fails, returns the
     * default
     *
     * @param s           The string to parse
     * @param defaultLong The value to return if parsing fails
     * @return The parsed long containing the same 64 bits that an unsigned long
     * would contain (which may produce a negative value)
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
     * Attempts to parse a string to a double. If it fails, returns the default
     *
     * @param s             The string to parse
     * @param defaultDouble The value to return if parsing fails
     * @return The parsed double, or the default if parsing failed
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
     * Attempts to parse a string of the form [DD-[hh:]]mm:ss[.ddd] to a number of
     * milliseconds. If it fails, returns the default.
     *
     * @param s           The string to parse
     * @param defaultLong The value to return if parsing fails
     * @return The parsed number of seconds, or the default if parsing fails
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
     * Attempts to parse a UUID. If it fails, returns the default.
     *
     * @param s          The string to parse
     * @param defaultStr The value to return if parsing fails
     * @return The parsed UUID, or the default if parsing fails
     */
    public static String parseUuidOrDefault(String s, String defaultStr) {
        Matcher m = UUID_PATTERN.matcher(s.toLowerCase(Locale.ROOT));
        if (m.matches()) {
            return m.group(1);
        }
        return defaultStr;
    }

    /**
     * Parses a string key = 'value' (string)
     *
     * @param line The entire string
     * @return the value contained between single tick marks
     */
    public static String getSingleQuoteStringValue(String line) {
        return getStringBetween(line, '\'');
    }

    /**
     * Parse a string key = "value" (string)
     *
     * @param line the entire string
     * @return the value contained between double tick marks
     */
    public static String getDoubleQuoteStringValue(String line) {
        return getStringBetween(line, '"');
    }

    /**
     * Gets a value between two characters having multiple same characters between
     * them. <b>Examples : </b>
     * <ul>
     * <li>"name = 'James Gosling's Java'" returns "James Gosling's Java"</li>
     * <li>"pci.name = 'Realtek AC'97 Audio Device'" returns "Realtek AC'97 Audio
     * Device"</li>
     * </ul>
     *
     * @param line The "key-value" pair line.
     * @param c    The Trailing And Leading characters of the string line
     * @return : The value having the characters between them.
     */
    public static String getStringBetween(String line, char c) {
        int firstOcc = line.indexOf(c);
        if (firstOcc < 0) {
            return "";
        }
        return line.substring(firstOcc + 1, line.lastIndexOf(c)).trim();
    }

    /**
     * Parses a string such as "10.12.2" or "key = 1 (0x1) (int)" to find the
     * integer value of the first set of one or more consecutive digits
     *
     * @param line The entire string
     * @return the value of first integer if any; 0 otherwise
     */
    public static int getFirstIntValue(String line) {
        return getNthIntValue(line, 1);
    }

    /**
     * Parses a string such as "10.12.2" or "key = 1 (0x1) (int)" to find the
     * integer value of the nth set of one or more consecutive digits
     *
     * @param line The entire string
     * @param n    Which set of integers to return
     * @return the value of nth integer if any; 0 otherwise
     */
    public static int getNthIntValue(String line, int n) {
        // Split the string by non-digits,
        String[] split = notDigits.split(startWithNotDigits.matcher(line).replaceFirst(""));
        if (split.length >= n) {
            return parseIntOrDefault(split[n - 1], 0);
        }
        return 0;
    }

    /**
     * Removes all matching sub strings from the string. More efficient than regexp.
     *
     * @param original source String to remove from
     * @param toRemove the sub string to be removed
     * @return The string with all matching substrings removed
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
     * Parses a delimited string to an array of longs. Optimized for processing
     * predictable-length arrays such as outputs of reliably formatted Linux proc or
     * sys filesystem, minimizing new object creation. Users should perform other
     * sanity checks of data.
     * <p>
     * As a special case, non-numeric fields (such as UUIDs in OpenVZ) at the end of
     * the list are ignored. Values greater than the max long value return the max
     * long value.
     * <p>
     * The indices parameters are referenced assuming the length as specified, and
     * leading characters are ignored. For example, if the string is "foo 12 34 5"
     * and the length is 3, then index 0 is 12, index 1 is 34, and index 2 is 5.
     *
     * @param s         The string to parse
     * @param indices   An array indicating which indexes should be populated in the final
     *                  array; other values will be skipped. This idex is zero-referenced
     *                  assuming the rightmost delimited fields of the string contain the
     *                  array.
     * @param length    The total number of elements in the string array. It is
     *                  permissible for the string to have more elements than this;
     *                  leading elements will be ignored. This should be calculated once
     *                  per text format by {@link #countStringToLongArray}.
     * @param delimiter The character to delimit by.
     * @return If successful, an array of parsed longs. If parsing errors occurred,
     * will be an array of zeros.
     */
    public static long[] parseStringToLongArray(String s, int[] indices, int length, char delimiter) {
        long[] parsed = new long[indices.length];
        // Iterate from right-to-left of String
        // Fill right to left of result array using index array
        int charIndex = s.length();
        int parsedIndex = indices.length - 1;
        int stringIndex = length - 1;

        int power = 0;
        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean numberFound = false; // ignore nonnumeric at end
        boolean dashSeen = false; // to flag uuids as nonnumeric
        while (--charIndex > 0 && parsedIndex >= 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                // first parseable number?
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
            } else if (indices[parsedIndex] != stringIndex || c == '+' || !numeric) {
                // Doesn't impact parsing, ignore
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                if (power > 18 || power == 17 && c == '9' && parsed[parsedIndex] > 223_372_036_854_775_807L) {
                    parsed[parsedIndex] = Long.MAX_VALUE;
                } else {
                    parsed[parsedIndex] += (c - '0') * Builder.POWERS_OF_TEN[power++];
                }
                delimCurrent = false;
            } else if (c == '-') {
                parsed[parsedIndex] *= -1L;
                delimCurrent = false;
                dashSeen = true;
            } else {
                // Flag as nonnumeric and continue unless we've seen a numeric
                // error on everything else
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
                Logger.error("Not enough fields in string '{}' parsing to long array: {}", s,
                        indices.length - parsedIndex);
            }
            return new long[indices.length];
        }
        return parsed;
    }

    /**
     * Test whether to log this message
     *
     * @param s The string to log
     * @return True if the string begins with {@code NOLOG}
     */
    private static boolean noLog(String s) {
        return s.startsWith("NOLOG: ");
    }

    /**
     * Parses a delimited string to count elements of an array of longs. Intended to
     * be called once to calculate the {@code length} field for
     * {@link #parseStringToLongArray}.
     * <p>
     * As a special case, non-numeric fields (such as UUIDs in OpenVZ) at the end of
     * the list are ignored.
     *
     * @param s         The string to parse
     * @param delimiter The character to delimit by
     * @return The number of parsable long values which follow the last unparsable
     * value.
     */
    public static int countStringToLongArray(String s, char delimiter) {
        // Iterate from right-to-left of String
        // Fill right to left of result array using index array
        int charIndex = s.length();
        int numbers = 0;

        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean dashSeen = false; // to flag uuids as nonnumeric
        while (--charIndex > 0) {
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
            } else if (c == '+' || !numeric) {
                // Doesn't impact parsing, ignore
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                delimCurrent = false;
            } else if (c == '-') {
                delimCurrent = false;
                dashSeen = true;
            } else {
                // we found non-digit or delimiter. If not last field, exit
                if (numbers > 0) {
                    return numbers;
                }
                // Else flag as nonnumeric and continue
                numeric = false;
            }
        }
        // We got to beginning of string with only numbers, count start as a delimiter
        // and exit
        return numbers + 1;
    }

    /**
     * Get a String in a line of text between two marker strings
     *
     * @param text   Text to search for match
     * @param before Start matching after this text
     * @param after  End matching before this text
     * @return Text between the strings before and after, or empty string if either
     * marker does not exist
     */
    public static String getTextBetweenStrings(String text, String before, String after) {

        String result = "";

        if (text.indexOf(before) >= 0 && text.indexOf(after) >= 0) {
            result = text.substring(text.indexOf(before) + before.length());
            result = result.substring(0, result.indexOf(after));
        }
        return result;
    }

    /**
     * Convert a long representing filetime (100-ns since 1601 epoch) to ms since
     * 1970 epoch
     *
     * @param filetime A 64-bit value equivalent to FILETIME
     * @param local    True if converting from a local filetime (PDH counter); false if
     *                 already UTC (WMI PerfRawData classes)
     * @return Equivalent milliseconds since the epoch
     */
    public static long filetimeToUtcMs(long filetime, boolean local) {
        return filetime / 10_000L - EPOCH_DIFF - (local ? TZ_OFFSET : 0L);
    }

    /**
     * Parse a date in MM-DD-YYYY or MM/DD/YYYY to YYYY-MM-DD
     *
     * @param dateString The date in MM DD YYYY format
     * @return The date in ISO YYYY-MM-DD format if parseable, or the original
     * string
     */
    public static String parseMmDdYyyyToYyyyMmDD(String dateString) {
        try {
            // Date is MM-DD-YYYY, convert to YYYY-MM-DD
            return String.format(Locale.ROOT, "%s-%s-%s", dateString.substring(6, 10), dateString.substring(0, 2),
                    dateString.substring(3, 5));
        } catch (StringIndexOutOfBoundsException e) {
            return dateString;
        }
    }

    /**
     * Converts a string in CIM Date Format, as returned by WMI for DateTime types,
     * into a {@link java.time.OffsetDateTime}.
     *
     * @param cimDateTime A non-null DateTime String in CIM date format, e.g.,
     *                    <code>20160513072950.782000-420</code>
     * @return The parsed {@link java.time.OffsetDateTime} if the string is
     * parsable, otherwise {@link Builder#UNIX_EPOCH}.
     */
    public static OffsetDateTime parseCimDateTimeToOffset(String cimDateTime) {
        // Keep first 22 characters: digits, decimal, and + or - sign
        // But alter last 3 characters from a minute offset to hh:mm
        try {
            // From WMI as 20160513072950.782000-420,
            int tzInMinutes = Integer.parseInt(cimDateTime.substring(22));
            // modified to 20160513072950.782000-07:00 which can be parsed
            LocalTime offsetAsLocalTime = LocalTime.MIDNIGHT.plusMinutes(tzInMinutes);
            return OffsetDateTime.parse(
                    cimDateTime.substring(0, 22) + offsetAsLocalTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                    Builder.CIM_FORMAT);
        } catch (IndexOutOfBoundsException // if cimDate not 22+ chars
                 | NumberFormatException // if TZ minutes doesn't parse
                 | DateTimeParseException e) {
            Logger.trace("Unable to parse {} to CIM DateTime.", cimDateTime);
            return Builder.UNIX_EPOCH;
        }
    }

    /**
     * Checks if a file path equals or starts with an prefix in the given list
     *
     * @param prefixList A list of path prefixes
     * @param path       a string path to check
     * @return true if the path exactly equals, or starts with one of the strings in
     * prefixList
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
     * Parses a string like "53G" or "54.904 M" to its long value.
     *
     * @param count A count with a multiplyer like "4096 M"
     * @return the count parsed to a long
     */
    public static long parseMultipliedToLongs(String count) {
        Matcher matcher = UNITS_PATTERN.matcher(count.trim());
        String[] mem;
        if (matcher.find() && matcher.groupCount() == 3) {
            mem = new String[2];
            mem[0] = matcher.group(1);
            mem[1] = matcher.group(3);
        } else {
            mem = new String[]{count};
        }

        double number = Builder.parseDoubleOrDefault(mem[0], 0L);
        if (mem.length == 2 && mem[1] != null && mem[1].length() >= 1) {
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
     * Parses a string such as "4096 MB" to its long. Used to parse macOS and *nix
     * memory chip sizes. Although the units given are decimal they must parse to
     * binary units.
     *
     * @param size A string of memory sizes like "4096 MB"
     * @return the size parsed to a long
     */
    public static long parseDecimalMemorySizeToBinary(String size) {
        String[] mem = RegEx.SPACES.split(size);
        if (mem.length < 2) {
            // If no spaces, use regexp
            Matcher matcher = BYTES_PATTERN.matcher(size.trim());
            if (matcher.find() && matcher.groupCount() == 2) {
                mem = new String[2];
                mem[0] = matcher.group(1);
                mem[1] = matcher.group(2);
            }
        }
        long capacity = Builder.parseLongOrDefault(mem[0], 0L);
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
     * Parse a Windows DeviceID to get the vendor ID, product ID, and Serial Number
     *
     * @param deviceId The DeviceID
     * @return A {@link Triple} where the first element is the vendor ID, the
     * second element is the product ID, and the third element is either a
     * serial number or empty string if parsing was successful, or
     * {@code null} otherwise
     */
    public static Triple<String, String, String> parseDeviceIdToVendorProductSerial(String deviceId) {
        Matcher m = VENDOR_PRODUCT_ID_SERIAL.matcher(deviceId);
        if (m.matches()) {
            String vendorId = "0x" + m.group(1).toLowerCase(Locale.ROOT);
            String productId = "0x" + m.group(2).toLowerCase(Locale.ROOT);
            String serial = m.group(4);
            return Triple.of(vendorId, productId, !m.group(3).isEmpty() || serial.contains("&") ? "" : serial);
        }
        return null;
    }

    /**
     * Parse a Linux lshw resources string to calculate the memory size
     *
     * @param resources A string containing one or more elements of the form
     *                  {@code memory:b00000000-bffffffff}
     * @return The number of bytes consumed by the memory in the {@code resources}
     * string
     */
    public static long parseLshwResourceString(String resources) {
        long bytes = 0L;
        // First split by whitespace
        String[] resourceArray = RegEx.SPACES.split(resources);
        for (String r : resourceArray) {
            // Remove prefix
            if (r.startsWith("memory:")) {
                // Split to low and high
                String[] mem = r.substring(7).split("-");
                if (mem.length == 2) {
                    try {
                        // Parse the hex strings
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
     * Parse a Linux lspci machine readble line to its name and id
     *
     * @param line A string in the form Foo [bar]
     * @return A pair separating the String before the square brackets and within
     * them if found, null otherwise
     */
    public static Pair<String, String> parseLspciMachineReadable(String line) {
        Matcher matcher = LSPCI_MACHINE_READABLE.matcher(line);
        if (matcher.matches()) {
            return Pair.of(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    /**
     * Parse a Linux lspci line containing memory size
     *
     * @param line A string in the form Foo [size=256M]
     * @return A the memory size in bytes
     */
    public static long parseLspciMemorySize(String line) {
        Matcher matcher = LSPCI_MEMORY_SIZE.matcher(line);
        if (matcher.matches()) {
            return parseDecimalMemorySizeToBinary(matcher.group(1) + " " + matcher.group(2) + "B");
        }
        return 0;
    }

    /**
     * Parse a space-delimited list of integers which include hyphenated ranges to a
     * list of just the integers. For example, 0 1 4-7 parses to a list containing
     * 0, 1, 4, 5, 6, and 7.
     *
     * @param text A string containing space-delimited integers or ranges of integers
     *             with a hyphen
     * @return A list of integers representing the provided range(s).
     */
    public static List<Integer> parseHyphenatedIntList(String text) {
        List<Integer> result = new ArrayList<>();
        String[] csvTokens = text.split(Symbol.COMMA);
        for (String csvToken : csvTokens) {
            csvToken = csvToken.trim();
            for (String s : RegEx.SPACES.split(csvToken)) {
                if (s.contains("-")) {
                    int first = getFirstIntValue(s);
                    int last = getNthIntValue(s, 2);
                    for (int i = first; i <= last; i++) {
                        result.add(i);
                    }
                } else {
                    int only = Builder.parseIntOrDefault(s, -1);
                    if (only >= 0) {
                        result.add(only);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Parse an integer in big endian IP format to its component bytes representing
     * an IPv4 address
     *
     * @param ip The address as an integer
     * @return The address as an array of four bytes
     */
    public static byte[] parseIntToIP(int ip) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ip).array();
    }

    /**
     * Parse an integer array in big endian IP format to its component bytes
     * representing an IPv6 address
     *
     * @param ip6 The address as an integer array
     * @return The address as an array of sizteen bytes
     */
    public static byte[] parseIntArrayToIP(int[] ip6) {
        ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        for (int i : ip6) {
            bb.putInt(i);
        }
        return bb.array();
    }

    /**
     * TCP network addresses and ports are in big endian format by definition. The
     * order of the two bytes in the 16-bit unsigned short port value must be
     * reversed
     *
     * @param port The port number in big endian order
     * @return The port number
     * @see <a href=
     * "https://docs.microsoft.com/en-us/windows/win32/api/winsock/nf-winsock-ntohs">ntohs</a>
     */
    public static int bigEndian16ToLittleEndian(int port) {
        // 20480 = 0x5000 should be 0x0050 = 80
        // 47873 = 0xBB01 should be 0x01BB = 443
        return port >> 8 & 0xff | port << 8 & 0xff00;
    }

    /**
     * Parse an integer array to an IPv4 or IPv6 as appropriate.
     * <p>
     * Intended for use on Utmp structures's {@code ut_addr_v6} element.
     *
     * @param utAddrV6 An array of 4 integers representing an IPv6 address. IPv4 address
     *                 uses just utAddrV6[0]
     * @return A string representation of the IP address.
     */
    public static String parseUtAddrV6toIP(int[] utAddrV6) {
        if (utAddrV6.length != 4) {
            throw new IllegalArgumentException("ut_addr_v6 must have exactly 4 elements");
        }
        // IPv4 has only first element
        if (utAddrV6[1] == 0 && utAddrV6[2] == 0 && utAddrV6[3] == 0) {
            // Special case for all 0's
            if (utAddrV6[0] == 0) {
                return "::";
            }
            // Parse using InetAddress
            byte[] ipv4 = ByteBuffer.allocate(4).putInt(utAddrV6[0]).array();
            try {
                return InetAddress.getByAddress(ipv4).getHostAddress();
            } catch (UnknownHostException e) {
                // Shouldn't happen with length 4 or 16
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
            // Shouldn't happen with length 4 or 16
            return Normal.UNKNOWN;
        }
    }

    /**
     * Parses a string of hex digits to an int value.
     *
     * @param hexString    A sequence of hex digits
     * @param defaultValue default value to return if parsefails
     * @return The corresponding int value
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
        // Hex failed to parse, just return the default long
        return defaultValue;
    }

    /**
     * Parses a string of hex digits to a long value.
     *
     * @param hexString    A sequence of hex digits
     * @param defaultValue default value to return if parsefails
     * @return The corresponding long value
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
        // Hex failed to parse, just return the default long
        return defaultValue;
    }

    /**
     * Parses a String "....foo" to "foo"
     *
     * @param dotPrefixedStr A string with possibly leading dots
     * @return The string without the dots
     */
    public static String removeLeadingDots(String dotPrefixedStr) {
        int pos = 0;
        while (pos < dotPrefixedStr.length() && dotPrefixedStr.charAt(pos) == '.') {
            pos++;
        }
        return pos < dotPrefixedStr.length() ? dotPrefixedStr.substring(pos) : "";
    }

    /**
     * Parse a null-delimited byte array to a list of strings.
     *
     * @param bytes A byte array containing Strings delimited by null characters. Two
     *              consecutive null characters mark the end of the list.
     * @return A list of Strings between the nulls.
     */
    public static List<String> parseByteArrayToStrings(byte[] bytes) {
        List<String> strList = new ArrayList<>();
        int start = 0;
        int end = 0;
        // Iterate characters
        do {
            // If we've reached a delimiter or the end of the array, add to list
            if (end == bytes.length || bytes[end] == 0) {
                // Zero length string means two nulls, we're done
                if (start == end) {
                    break;
                }
                // Otherwise add string and reset start
                // Intentionally using platform default charset
                strList.add(new String(bytes, start, end - start, Charset.UTF_8));
                start = end + 1;
            }
        } while (end++ < bytes.length);
        return strList;
    }

    /**
     * Parse a null-delimited byte array to a map of string keys and values.
     *
     * @param bytes A byte array containing String key-value pairs with keys and
     *              values delimited by {@code =} and pairs delimited by null
     *              characters. Two consecutive null characters mark the end of the
     *              map.
     * @return A map of String key-value pairs between the nulls.
     */
    public static Map<String, String> parseByteArrayToStringMap(byte[] bytes) {
        // API does not specify any particular order of entries, but it is reasonable to
        // maintain whatever order the OS provided to the end user
        Map<String, String> strMap = new LinkedHashMap<>();
        int start = 0;
        int end = 0;
        String key = null;
        // Iterate characters
        do {
            // If we've reached a delimiter or the end of the array, add to list
            if (end == bytes.length || bytes[end] == 0) {
                // Zero length string with no key, we're done
                if (start == end && key == null) {
                    break;
                }
                // Otherwise add string (possibly empty) and reset start
                // Intentionally using platform default charset
                strMap.put(key, new String(bytes, start, end - start, Charset.UTF_8));
                key = null;
                start = end + 1;
            } else if (bytes[end] == '=' && key == null) {
                key = new String(bytes, start, end - start, Charset.UTF_8);
                start = end + 1;
            }
        } while (end++ < bytes.length);
        return strMap;
    }

    /**
     * Parse a null-delimited char array to a map of string keys and values.
     *
     * @param chars A char array containing String key-value pairs with keys and
     *              values delimited by {@code =} and pairs delimited by null
     *              characters. Two consecutive null characters mark the end of the
     *              map.
     * @return A map of String key-value pairs between the nulls.
     */
    public static Map<String, String> parseCharArrayToStringMap(char[] chars) {
        // API does not specify any particular order of entries, but it is reasonable to
        // maintain whatever order the OS provided to the end user
        Map<String, String> strMap = new LinkedHashMap<>();
        int start = 0;
        int end = 0;
        String key = null;
        // Iterate characters
        do {
            // If we've reached a delimiter or the end of the array, add to list
            if (end == chars.length || chars[end] == 0) {
                // Zero length string with no key, we're done
                if (start == end && key == null) {
                    break;
                }
                // Otherwise add string (possibly empty) and reset start
                // Intentionally using platform default charset
                strMap.put(key, new String(chars, start, end - start));
                key = null;
                start = end + 1;
            } else if (chars[end] == '=' && key == null) {
                key = new String(chars, start, end - start);
                start = end + 1;
            }
        } while (end++ < chars.length);
        return strMap;
    }

    /**
     * Parses a delimited String into an enum map. Multiple consecutive delimiters
     * are treated as one.
     *
     * @param <K>    a type extending Enum
     * @param clazz  The enum class
     * @param values A delimited String to be parsed into the map
     * @param delim  the delimiter to use
     * @return An EnumMap populated in order using the delimited String values. If
     * there are fewer String values than enum values, the later enum values
     * are not mapped. The final enum value will contain the remainder of
     * the String, including excess delimiters.
     */
    public static <K extends Enum<K>> Map<K, String> stringToEnumMap(Class<K> clazz, String values, char delim) {
        EnumMap<K, String> map = new EnumMap<>(clazz);
        int start = 0;
        int len = values.length();
        EnumSet<K> keys = EnumSet.allOf(clazz);
        int keySize = keys.size();
        for (K key : keys) {
            // If this is the last enum, put the index at the end of the string, otherwise
            // put at delimiter
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
     * Tests if a String matches another String with a wildcard pattern.
     *
     * @param text    The String to test
     * @param pattern The String containing a wildcard pattern where ? represents a
     *                single character and * represents any number of characters. If the
     *                first character of the pattern is a carat (^) the test is
     *                performed against the remaining characters and the result of the
     *                test is the opposite.
     * @return True if the String matches or if the first character is ^ and the
     * remainder of the String does not match.
     */
    public static boolean wildcardMatch(String text, String pattern) {
        if (pattern.length() > 0 && pattern.charAt(0) == '^') {
            return !wildcardMatch(text, pattern.substring(1));
        }
        return text.matches(pattern.replace("?", ".?").replace("*", ".*?"));
    }

    /**
     * Evaluates if file store (identified by {@code path} and {@code volume})
     * should be excluded or not based on configuration
     * {@code pathIncludes, pathExcludes, volumeIncludes, volumeExcludes}.
     * <p>
     * Inclusion has priority over exclusion. If no exclusion/inclusion pattern is
     * specified, then filestore is not excluded.
     *
     * @param path           Mountpoint of filestore.
     * @param volume         Filestore volume.
     * @param pathIncludes   List of patterns for path inclusions.
     * @param pathExcludes   List of patterns for path exclusions.
     * @param volumeIncludes List of patterns for volume inclusions.
     * @param volumeExcludes List of patterns for volume exclusions.
     * @return {@code true} if file store should be excluded or {@code false}
     * otherwise.
     */
    public static boolean isFileStoreExcluded(String path, String volume, List<PathMatcher> pathIncludes,
                                              List<PathMatcher> pathExcludes, List<PathMatcher> volumeIncludes, List<PathMatcher> volumeExcludes) {
        Path p = Paths.get(path);
        Path v = Paths.get(volume);
        if (matches(p, pathIncludes) || matches(v, volumeIncludes)) {
            return false;
        }
        return matches(p, pathExcludes) || matches(v, volumeExcludes);
    }

    /**
     * Load from config and parse file system include/exclude line.
     *
     * @param configPropertyName The config property containing the line to be parsed.
     * @return List of PathMatchers to be used to match filestore volume and path.
     */
    public static List<PathMatcher> loadAndParseFileSystemConfig(String configPropertyName) {
        String config = Config.get(configPropertyName, "");
        return parseFileSystemConfig(config);
    }

    /**
     * Parse file system include/exclude line.
     *
     * @param config The config line to be parsed.
     * @return List of PathMatchers to be used to match filestore volume and path.
     */
    public static List<PathMatcher> parseFileSystemConfig(String config) {
        FileSystem fs = FileSystems.getDefault(); // can't be closed
        List<PathMatcher> patterns = new ArrayList<>();
        for (String item : config.split(",")) {
            if (item.length() > 0) {
                // Using glob: prefix as the defult unless user has specified glob or regex. See
                // https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-
                if (!(item.startsWith(GLOB_PREFIX) || item.startsWith(REGEX_PREFIX))) {
                    item = GLOB_PREFIX + item;
                }
                patterns.add(fs.getPathMatcher(item));
            }
        }
        return patterns;
    }

    /**
     * Checks if {@code text} matches any of @param patterns}.
     *
     * @param text     The text to be matched.
     * @param patterns List of patterns.
     * @return {@code true} if given text matches at least one glob pattern or
     * {@code false} otherwise.
     * @see <a href="https://en.wikipedia.org/wiki/Glob_(programming)">Wikipedia -
     * glob (programming)</a>
     */
    public static boolean matches(Path text, List<PathMatcher> patterns) {
        for (PathMatcher pattern : patterns) {
            if (pattern.matches(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Read an entire file at one time. Intended primarily for Linux /proc
     * filesystem to avoid recalculating file contents on iterative reads.
     *
     * @param filename The file to read
     * @return A list of Strings representing each line of the file, or an empty
     * list if file could not be read or is empty
     */
    public static List<String> readFile(String filename) {
        return readFile(filename, true);
    }

    /**
     * Read an entire file at one time. Intended primarily for Linux /proc
     * filesystem to avoid recalculating file contents on iterative reads.
     *
     * @param filename    The file to read
     * @param reportError Whether to log errors reading the file
     * @return A list of Strings representing each line of the file, or an empty
     * list if file could not be read or is empty
     */
    public static List<String> readFile(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (Logger.isDebug()) {
                Logger.debug(READING_LOG, filename);
            }
            try {
                return Files.readAllLines(Paths.get(filename), Charset.UTF_8);
            } catch (IOException e) {
                if (reportError) {
                    Logger.error("Error reading file {}. {}", filename, e.getMessage());
                } else {
                    Logger.debug("Error reading file {}. {}", filename, e.getMessage());
                }
            }
        } else if (reportError) {
            Logger.warn("File not found or not readable: {}", filename);
        }
        return new ArrayList<>();
    }

    /**
     * Read an entire file at one time. Intended primarily for Linux /proc
     * filesystem to avoid recalculating file contents on iterative reads.
     *
     * @param filename The file to read
     * @return A byte array representing the file
     */
    public static byte[] readAllBytes(String filename) {
        return readAllBytes(filename, true);
    }

    /**
     * Read an entire file at one time. Intended primarily for Linux /proc
     * filesystem to avoid recalculating file contents on iterative reads.
     *
     * @param filename    The file to read
     * @param reportError Whether to log errors reading the file
     * @return A byte array representing the file
     */
    public static byte[] readAllBytes(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (Logger.isDebug()) {
                Logger.debug(READING_LOG, filename);
            }
            try {
                return Files.readAllBytes(Paths.get(filename));
            } catch (IOException e) {
                if (reportError) {
                    Logger.error("Error reading file {}. {}", filename, e.getMessage());
                } else {
                    Logger.debug("Error reading file {}. {}", filename, e.getMessage());
                }
            }
        } else if (reportError) {
            Logger.warn("File not found or not readable: {}", filename);
        }
        return new byte[0];
    }

    /**
     * Read an entire file at one time. Intended for unix /proc binary files to
     * avoid reading file contents on iterative reads.
     *
     * @param filename The file to read
     * @return A bytebuffer representing the file if read was successful; null
     * otherwise
     */
    public static ByteBuffer readAllBytesAsBuffer(String filename) {
        byte[] bytes = readAllBytes(filename, false);
        ByteBuffer buff = ByteBuffer.allocate(bytes.length);
        buff.order(ByteOrder.nativeOrder());
        for (byte b : bytes) {
            buff.put(b);
        }
        buff.flip();
        return buff;
    }

    /**
     * Reads a byte value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next byte value
     */
    public static byte readByteFromBuffer(ByteBuffer buff) {
        if (buff.position() < buff.limit()) {
            return buff.get();
        }
        return 0;
    }

    /**
     * Reads a short value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next short value
     */
    public static short readShortFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 2) {
            return buff.getShort();
        }
        return 0;
    }

    /**
     * Reads an int value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next int value
     */
    public static int readIntFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 4) {
            return buff.getInt();
        }
        return 0;
    }

    /**
     * Reads a long value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next long value
     */
    public static long readLongFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 8) {
            return buff.getLong();
        }
        return 0L;
    }

    /**
     * Reads a NativeLong value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next value
     */
    public static NativeLong readNativeLongFromBuffer(ByteBuffer buff) {
        return new NativeLong(Native.LONG_SIZE == 4 ? readIntFromBuffer(buff) : readLongFromBuffer(buff));
    }

    /**
     * Reads a size_t value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next value
     */
    public static LibCAPI.size_t readSizeTFromBuffer(ByteBuffer buff) {
        return new LibCAPI.size_t(Native.SIZE_T_SIZE == 4 ? readIntFromBuffer(buff) : readLongFromBuffer(buff));
    }

    /**
     * Reads a byte array value from a ByteBuffer
     *
     * @param buff  The bytebuffer to read from
     * @param array The array into which to read the data
     */
    public static void readByteArrayFromBuffer(ByteBuffer buff, byte[] array) {
        if (buff.position() <= buff.limit() - array.length) {
            buff.get(array);
        }
    }

    /**
     * Reads a byte array value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @param size The number of bytes to read
     * @return The next values
     */
    public static byte[] readByteArrayFromBuffer(ByteBuffer buff, int size) {
        byte[] bytes = new byte[size];
        if (buff.position() <= buff.limit() - size) {
            buff.get(bytes);
        }
        return bytes;
    }

    /**
     * Reads a Pointer value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next value
     */
    public static Pointer readPointerFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - Native.POINTER_SIZE) {
            return Native.POINTER_SIZE == 4 ? new Pointer(buff.getInt()) : new Pointer(buff.getLong());
        }
        return Pointer.NULL;
    }

    /**
     * Read a file and return the long value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static long getLongFromFile(String filename) {
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = Builder.readFile(filename, false);
        if (!read.isEmpty()) {
            if (Logger.isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return Builder.parseLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Read a file and return the unsigned long value contained therein as a long.
     * Intended primarily for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static long getUnsignedLongFromFile(String filename) {
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = Builder.readFile(filename, false);
        if (!read.isEmpty()) {
            if (Logger.isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return Builder.parseUnsignedLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Read a file and return the int value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static int getIntFromFile(String filename) {
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        try {
            List<String> read = Builder.readFile(filename, false);
            if (!read.isEmpty()) {
                if (Logger.isTrace()) {
                    Logger.trace(READ_LOG, read.get(0));
                }
                return Integer.parseInt(read.get(0));
            }
        } catch (NumberFormatException ex) {
            Logger.warn("Unable to read value from {}. {}", filename, ex.getMessage());
        }
        return 0;
    }

    /**
     * Read a file and return the String value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise empty string
     */
    public static String getStringFromFile(String filename) {
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = Builder.readFile(filename, false);
        if (!read.isEmpty()) {
            if (Logger.isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return read.get(0);
        }
        return "";
    }

    /**
     * Read a file and return a map of string keys to string values contained
     * therein. Intended primarily for Linux {@code /proc/[pid]} files to provide
     * more detailed or accurate information not available in the API.
     *
     * @param filename  The file to read
     * @param separator Character(s) in each line of the file that separate the key and
     *                  the value.
     * @return The map contained in the file, delimited by the separator, with the
     * value whitespace trimmed. If keys and values are not parsed, an empty
     * map is returned.
     */
    public static Map<String, String> getKeyValueMapFromFile(String filename, String separator) {
        Map<String, String> map = new HashMap<>();
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> lines = Builder.readFile(filename, false);
        for (String line : lines) {
            String[] parts = line.split(separator);
            if (parts.length == 2) {
                map.put(parts[0], parts[1].trim());
            }
        }
        return map;
    }

    /**
     * Reads the target of a symbolic link
     *
     * @param file The file to read
     * @return The symlink name, or null if the read failed
     */
    public static String readSymlinkTarget(File file) {
        try {
            return Files.readSymbolicLink(Paths.get(file.getAbsolutePath())).toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 输出到<code>StringBuilder</code>
     *
     * @param builder <code>StringBuilder</code>对象
     * @param caption 标题
     * @param value   值
     */
    public static void append(StringBuilder builder, String caption, Object value) {
        builder.append(caption).append(StringKit.nullToDefault(Convert.toString(value), "[n/a]")).append(Symbol.LF);
    }

    /**
     * If the given Pointer is of class Memory, executes the close method on it to
     * free its native allocation
     *
     * @param p A pointer
     */
    public static void freeMemory(Pointer p) {
        if (p instanceof Memory) {
            ((Memory) p).close();
        }
    }

    /**
     * /** Convert a pointer to a CFString into a String.
     *
     * @param result Pointer to the CFString
     * @return a CFString or "unknown" if it has no value
     */
    public static String cfPointerToString(Pointer result) {
        return cfPointerToString(result, true);
    }

    /**
     * Convert a pointer to a CFString into a String.
     *
     * @param result        Pointer to the CFString
     * @param returnUnknown Whether to return the "unknown" string
     * @return a CFString including a possible empty one if {@code returnUnknown} is
     * false, or "unknown" if it is true
     */
    public static String cfPointerToString(Pointer result, boolean returnUnknown) {
        String s = "";
        if (result != null) {
            CoreFoundation.CFStringRef cfs = new CoreFoundation.CFStringRef(result);
            s = cfs.stringValue();
        }
        if (returnUnknown && s.isEmpty()) {
            return Normal.UNKNOWN;
        }
        return s;
    }

    /**
     * Tests if session of a user logged in a device is valid or not.
     *
     * @param user      The user logged in
     * @param device    The device used by user
     * @param loginTime The login time of the user
     * @return True if Session is valid or False if the user of device is empty or the login time is lesser than zero or
     * greater than current time.
     */
    public static boolean isSessionValid(String user, String device, Long loginTime) {
        return !(user.isEmpty() || device.isEmpty() || loginTime < 0 || loginTime > System.currentTimeMillis());
    }

    /**
     * Checks if value exists in map for the given key or not and returns value or unknown based on it
     *
     * @param map A map of String key-value pairs
     * @param key Fetch value for the given key
     * @return Returns the value for the key if it exists in the map else it returns unknown
     */
    public static String getValueOrUnknown(Map<String, String> map, String key) {
        String value = map.getOrDefault(key, "");
        return value.isEmpty() ? Normal.UNKNOWN : value;
    }

}
