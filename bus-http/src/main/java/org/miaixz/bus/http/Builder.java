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
package org.miaixz.bus.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.http.Http2Header;

/**
 * Utility class for HTTP-related operations.
 * <p>
 * Provides utility methods for handling HTTP requests and responses, including data parsing, encoding, collection
 * operations, date formatting, and more.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * Maximum date value (December 31, 9999).
     */
    public static final long MAX_DATE = 253402300799999L;
    /**
     * An empty {@link Headers} instance.
     */
    public static final Headers EMPTY_HEADERS = Headers.of();
    /**
     * An empty {@link ResponseBody} instance.
     */
    public static final ResponseBody EMPTY_RESPONSE = ResponseBody.of(null, Normal.EMPTY_BYTE_ARRAY);
    /**
     * The UTC (Coordinated Universal Time) timezone.
     */
    public static final TimeZone UTC = TimeZone.getTimeZone("GMT");
    /**
     * A comparator that orders strings naturally.
     */
    public static final Comparator<String> NATURAL_ORDER = String::compareTo;
    /**
     * Byte string containing characters that delimit quoted strings.
     */
    public static final ByteString QUOTED_STRING_DELIMITERS = ByteString.encodeUtf8("\"\\");
    /**
     * Byte string containing characters that delimit tokens.
     */
    public static final ByteString TOKEN_DELIMITERS = ByteString.encodeUtf8("\t ,=");
    /**
     * Array of browser-compatible date format strings.
     */
    public static final String[] BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS = new String[] {
            "EEE, dd MMM yyyy HH:mm:ss zzz", "EEEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM d HH:mm:ss yyyy",
            "EEE, dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MMM-yyyy HH-mm-ss z", "EEE, dd MMM yy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH:mm:ss z", "EEE dd MMM yyyy HH:mm:ss z", "EEE dd-MMM-yyyy HH-mm-ss z",
            "EEE dd-MMM-yy HH:mm:ss z", "EEE dd MMM yy HH:mm:ss z", "EEE,dd-MMM-yy HH:mm:ss z",
            "EEE,dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MM-yyyy HH:mm:ss z", "EEE MMM d yyyy HH:mm:ss z", };
    /**
     * Array of browser-compatible date formatters.
     */
    public static final DateFormat[] BROWSER_COMPATIBLE_DATE_FORMATS = new DateFormat[BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length];
    /**
     * The HTTP CONNECT method.
     */
    public static final String CONNECT = "CONNECT";
    /**
     * The STOMP CONNECTED frame command.
     */
    public static final String CONNECTED = "CONNECTED";
    /**
     * The STOMP SEND frame command.
     */
    public static final String SEND = "SEND";
    /**
     * The STOMP MESSAGE frame command.
     */
    public static final String MESSAGE = "MESSAGE";
    /**
     * The STOMP SUBSCRIBE frame command.
     */
    public static final String SUBSCRIBE = "SUBSCRIBE";
    /**
     * The STOMP UNSUBSCRIBE frame command.
     */
    public static final String UNSUBSCRIBE = "UNSUBSCRIBE";
    /**
     * The STOMP ACK frame command.
     */
    public static final String ACK = "ACK";
    /**
     * The STOMP UNKNOWN frame command.
     */
    public static final String UNKNOWN = "UNKNOWN";
    /**
     * The STOMP ERROR frame command.
     */
    public static final String ERROR = "ERROR";
    /**
     * A {@link SegmentBuffer} containing common Unicode Byte Order Marks (BOMs).
     */
    private static final SegmentBuffer UNICODE_BOMS = SegmentBuffer.of(
            ByteString.decodeHex("efbbbf"),
            ByteString.decodeHex("feff"),
            ByteString.decodeHex("fffe"),
            ByteString.decodeHex("0000ffff"),
            ByteString.decodeHex("ffff0000"));
    /**
     * Reflective method for adding suppressed exceptions to a {@link Throwable}.
     */
    private static final Method addSuppressedExceptionMethod;
    /**
     * A thread-local {@link DateFormat} for RFC 1123 date formatting.
     */
    private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT = ThreadLocal.withInitial(() -> {
        DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        rfc1123.setLenient(false);
        rfc1123.setTimeZone(UTC);
        return rfc1123;
    });

    static {
        Method m;
        try {
            m = Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class);
        } catch (Exception e) {
            m = null;
        }
        addSuppressedExceptionMethod = m;
    }

    /**
     * Constructs a new {@code Builder} instance.
     */
    public Builder() {
    }

    /**
     * Adds a suppressed exception to the given throwable if possible.
     *
     * @param e          The primary throwable.
     * @param suppressed The throwable to be suppressed.
     */
    public static void addSuppressedIfPossible(Throwable e, Throwable suppressed) {
        if (addSuppressedExceptionMethod != null) {
            try {
                addSuppressedExceptionMethod.invoke(e, suppressed);
            } catch (InvocationTargetException | IllegalAccessException ignored) {
            }
        }
    }

    /**
     * Checks if the given offset and count are within the bounds of an array of a specified length.
     *
     * @param arrayLength The total length of the array.
     * @param offset      The starting offset.
     * @param count       The number of elements.
     * @throws ArrayIndexOutOfBoundsException If the offset or count are invalid for the given array length.
     */
    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Attempts to exhaust the given source within a specified timeout.
     *
     * @param source   The source to discard.
     * @param timeout  The maximum time to wait.
     * @param timeUnit The unit of time for the timeout.
     * @return {@code true} if the source was successfully discarded, {@code false} otherwise.
     */
    public static boolean discard(Source source, int timeout, TimeUnit timeUnit) {
        try {
            return skipAll(source, timeout, timeUnit);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Skips all bytes from the source until it is exhausted or the timeout is reached.
     *
     * @param source   The source to skip.
     * @param duration The maximum time to wait.
     * @param timeUnit The unit of time for the duration.
     * @return {@code true} if all bytes were skipped, {@code false} if the timeout was reached.
     * @throws IOException If an I/O error occurs during skipping.
     */
    public static boolean skipAll(Source source, int duration, TimeUnit timeUnit) throws IOException {
        long now = System.nanoTime();
        long originalDuration = source.timeout().hasDeadline() ? source.timeout().deadlineNanoTime() - now
                : Long.MAX_VALUE;
        source.timeout().deadlineNanoTime(now + Math.min(originalDuration, timeUnit.toNanos(duration)));
        try {
            Buffer skipBuffer = new Buffer();
            while (source.read(skipBuffer, 8192) != -1) {
                skipBuffer.clear();
            }
            return true;
        } catch (InterruptedIOException e) {
            return false;
        } finally {
            if (originalDuration == Long.MAX_VALUE) {
                source.timeout().clearDeadline();
            } else {
                source.timeout().deadlineNanoTime(now + originalDuration);
            }
        }
    }

    /**
     * Returns an immutable copy of the given list.
     *
     * @param list The list to make immutable.
     * @param <T>  The type of elements in the list.
     * @return An immutable list.
     */
    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    /**
     * Returns an immutable copy of the given map.
     *
     * @param map The map to make immutable.
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @return An immutable map.
     */
    public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
        return map.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    /**
     * Returns an immutable list containing the given elements.
     *
     * @param elements The elements to include in the list.
     * @param <T>      The type of elements.
     * @return An immutable list.
     */
    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements.clone()));
    }

    /**
     * Creates a {@link ThreadFactory} that produces threads with a given name and daemon status.
     *
     * @param name   The name prefix for the created threads.
     * @param daemon {@code true} if the created threads should be daemon threads, {@code false} otherwise.
     * @return A {@link ThreadFactory} instance.
     */
    public static ThreadFactory threadFactory(String name, boolean daemon) {
        return runnable -> {
            Thread result = new Thread(runnable, name);
            result.setDaemon(daemon);
            return result;
        };
    }

    /**
     * Returns the intersection of two string arrays based on a given comparator.
     *
     * @param comparator The comparator to use for string comparison.
     * @param first      The first array of strings.
     * @param second     The second array of strings.
     * @return A new array containing strings present in both input arrays.
     */
    public static String[] intersect(Comparator<? super String> comparator, String[] first, String[] second) {
        List<String> result = new ArrayList<>();
        for (String a : first) {
            for (String b : second) {
                if (comparator.compare(a, b) == 0) {
                    result.add(a);
                    break;
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Checks if there is any common string between two arrays based on a given comparator.
     *
     * @param comparator The comparator to use for string comparison.
     * @param first      The first array of strings.
     * @param second     The second array of strings.
     * @return {@code true} if an intersection exists, {@code false} otherwise.
     */
    public static boolean nonEmptyIntersection(Comparator<String> comparator, String[] first, String[] second) {
        if (first == null || second == null || first.length == 0 || second.length == 0) {
            return false;
        }
        for (String a : first) {
            for (String b : second) {
                if (comparator.compare(a, b) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generates a host header string for a given URL.
     *
     * @param url                The URL for which to generate the host header.
     * @param includeDefaultPort {@code true} to include the port even if it's the default for the scheme, {@code false}
     *                           otherwise.
     * @return The host header string.
     */
    public static String hostHeader(UnoUrl url, boolean includeDefaultPort) {
        String host = url.host().contains(Symbol.COLON) ? "[" + url.host() + "]" : url.host();
        return includeDefaultPort || url.port() != UnoUrl.defaultPort(url.scheme()) ? host + Symbol.COLON + url.port()
                : host;
    }

    /**
     * Finds the index of a string in an array using a custom comparator.
     *
     * @param comparator The comparator to use for string comparison.
     * @param array      The array to search.
     * @param value      The string value to find.
     * @return The index of the value in the array, or -1 if not found.
     */
    public static int indexOf(Comparator<String> comparator, String[] array, String value) {
        for (int i = 0, size = array.length; i < size; i++) {
            if (comparator.compare(array[i], value) == 0)
                return i;
        }
        return -1;
    }

    /**
     * Concatenates a string to an existing string array, returning a new array.
     *
     * @param array The original array.
     * @param value The string value to append.
     * @return A new array with the appended string.
     */
    public static String[] concat(String[] array, String value) {
        String[] result = new String[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[result.length - 1] = value;
        return result;
    }

    /**
     * Skips leading ASCII whitespace characters in a substring.
     *
     * @param input The input string.
     * @param pos   The starting position (inclusive).
     * @param limit The ending position (exclusive).
     * @return The index of the first non-whitespace character, or {@code limit} if all are whitespace.
     */
    public static int skipLeadingAsciiWhitespace(String input, int pos, int limit) {
        for (int i = pos; i < limit; i++) {
            switch (input.charAt(i)) {
                case Symbol.C_HT:
                case Symbol.C_LF:
                case '\f':
                case Symbol.C_CR:
                case Symbol.C_SPACE:
                    continue;

                default:
                    return i;
            }
        }
        return limit;
    }

    /**
     * Skips trailing ASCII whitespace characters in a substring.
     *
     * @param input The input string.
     * @param pos   The starting position (inclusive).
     * @param limit The ending position (exclusive).
     * @return The index of the character after the last non-whitespace character, or {@code pos} if all are whitespace.
     */
    public static int skipTrailingAsciiWhitespace(String input, int pos, int limit) {
        for (int i = limit - 1; i >= pos; i--) {
            switch (input.charAt(i)) {
                case Symbol.C_HT:
                case Symbol.C_LF:
                case '\f':
                case Symbol.C_CR:
                case Symbol.C_SPACE:
                    continue;

                default:
                    return i + 1;
            }
        }
        return pos;
    }

    /**
     * Trims leading and trailing ASCII whitespace from a substring.
     *
     * @param string The input string.
     * @param pos    The starting position (inclusive).
     * @param limit  The ending position (exclusive).
     * @return The trimmed substring.
     */
    public static String trimSubstring(String string, int pos, int limit) {
        int start = skipLeadingAsciiWhitespace(string, pos, limit);
        int end = skipTrailingAsciiWhitespace(string, start, limit);
        return string.substring(start, end);
    }

    /**
     * Finds the first occurrence of any delimiter character within a substring.
     *
     * @param input      The input string.
     * @param pos        The starting position (inclusive).
     * @param limit      The ending position (exclusive).
     * @param delimiters A string containing all possible delimiter characters.
     * @return The index of the first delimiter, or {@code limit} if no delimiter is found.
     */
    public static int delimiterOffset(String input, int pos, int limit, String delimiters) {
        for (int i = pos; i < limit; i++) {
            if (delimiters.indexOf(input.charAt(i)) != -1)
                return i;
        }
        return limit;
    }

    /**
     * Finds the first occurrence of a specific delimiter character within a substring.
     *
     * @param input     The input string.
     * @param pos       The starting position (inclusive).
     * @param limit     The ending position (exclusive).
     * @param delimiter The single delimiter character to find.
     * @return The index of the delimiter, or {@code limit} if no delimiter is found.
     */
    public static int delimiterOffset(String input, int pos, int limit, char delimiter) {
        for (int i = pos; i < limit; i++) {
            if (input.charAt(i) == delimiter)
                return i;
        }
        return limit;
    }

    /**
     * Canonicalizes a hostname, converting it to its ASCII representation and validating it.
     *
     * @param host The hostname to canonicalize.
     * @return The canonicalized hostname, or {@code null} if the host is invalid.
     */
    public static String canonicalizeHost(String host) {
        if (host.contains(Symbol.COLON)) {
            InetAddress inetAddress = host.startsWith("[") && host.endsWith("]")
                    ? decodeIpv6(host, 1, host.length() - 1)
                    : decodeIpv6(host, 0, host.length());
            if (inetAddress == null)
                return null;
            byte[] address = inetAddress.getAddress();
            if (address.length == 16)
                return inet6AddressToAscii(address);
            if (address.length == 4)
                return inetAddress.getHostAddress();
            throw new AssertionError("Invalid IPv6 address: '" + host + "'");
        }

        try {
            String result = IDN.toASCII(host).toLowerCase(Locale.US);
            if (result.isEmpty())
                return null;

            if (containsInvalidHostnameAsciiCodes(result)) {
                return null;
            }
            return result;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if the given ASCII hostname contains any invalid characters.
     *
     * @param hostnameAscii The ASCII hostname to check.
     * @return {@code true} if invalid characters are found, {@code false} otherwise.
     */
    private static boolean containsInvalidHostnameAsciiCodes(String hostnameAscii) {
        for (int i = 0; i < hostnameAscii.length(); i++) {
            char c = hostnameAscii.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                return true;
            }

            if (" #%/:?@[\\]".indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the index of the first control character or non-ASCII character in a string.
     *
     * @param input The input string.
     * @return The index of the first invalid character, or -1 if all characters are valid ASCII.
     */
    public static int indexOfControlOrNonAscii(String input) {
        for (int i = 0, length = input.length(); i < length; i++) {
            char c = input.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Verifies if the given host string is a valid IP address.
     *
     * @param host The host string to check.
     * @return {@code true} if the host is an IP address, {@code false} otherwise.
     */
    public static boolean verifyAsIpAddress(String host) {
        return Pattern.IP_ADDRESS_PATTERN.matcher(host).matches();
    }

    /**
     * Detects the character set of a {@link BufferSource} based on its Byte Order Mark (BOM), falling back to a default
     * charset if no BOM is found.
     *
     * @param source  The buffer source to check for BOM.
     * @param charset The default charset to use if no BOM is detected.
     * @return The detected or default {@link Charset}.
     * @throws IOException If an I/O error occurs while reading from the source.
     */
    public static Charset bomAwareCharset(BufferSource source, Charset charset) throws IOException {
        switch (source.select(UNICODE_BOMS)) {
            case 0:
                return org.miaixz.bus.core.lang.Charset.UTF_8;

            case 1:
                return org.miaixz.bus.core.lang.Charset.UTF_16_BE;

            case 2:
                return org.miaixz.bus.core.lang.Charset.UTF_16_LE;

            case 3:
                return org.miaixz.bus.core.lang.Charset.UTF_32_BE;

            case 4:
                return org.miaixz.bus.core.lang.Charset.UTF_32_LE;

            case -1:
                return charset;

            default:
                throw new AssertionError();
        }
    }

    /**
     * Validates a duration value, ensuring it's non-negative and within integer limits when converted to milliseconds.
     *
     * @param name     The name of the duration parameter (for error messages).
     * @param duration The duration value.
     * @param unit     The {@link TimeUnit} of the duration.
     * @return The duration in milliseconds as an integer.
     * @throws IllegalArgumentException If the duration is negative, the unit is null, or the duration is too
     *                                  large/small.
     */
    public static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0)
            throw new IllegalArgumentException(name + " < 0");
        if (null == unit)
            throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE)
            throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0)
            throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }

    /**
     * Decodes a hexadecimal character to its integer value.
     *
     * @param c The hexadecimal character.
     * @return The integer value (0-15), or -1 if the character is not a valid hex digit.
     */
    public static int decodeHexDigit(char c) {
        if (c >= Symbol.C_ZERO && c <= Symbol.C_NINE)
            return c - Symbol.C_ZERO;
        if (c >= 'a' && c <= 'f')
            return c - 'a' + 10;
        if (c >= 'A' && c <= 'F')
            return c - 'A' + 10;
        return -1;
    }

    /**
     * Decodes an IPv6 address from a substring.
     *
     * @param input The input string containing the IPv6 address.
     * @param pos   The starting position (inclusive).
     * @param limit The ending position (exclusive).
     * @return An {@link InetAddress} object representing the IPv6 address, or {@code null} if decoding fails.
     */
    private static InetAddress decodeIpv6(String input, int pos, int limit) {
        byte[] address = new byte[Normal._16];
        int b = 0;
        int compress = -1;
        int groupOffset = -1;

        for (int i = pos; i < limit;) {
            if (b == address.length)
                return null;

            if (i + 2 <= limit && input.regionMatches(i, Symbol.COLON + Symbol.COLON, 0, 2)) {
                if (compress != -1)
                    return null;
                i += 2;
                b += 2;
                compress = b;
                if (i == limit)
                    break;
            } else if (b != 0) {
                if (input.regionMatches(i, Symbol.COLON, 0, 1)) {
                    i++;
                } else if (input.regionMatches(i, Symbol.DOT, 0, 1)) {
                    if (!decodeIpv4Suffix(input, groupOffset, limit, address, b - 2))
                        return null;
                    b += 2;
                    break;
                } else {
                    return null;
                }
            }

            int value = 0;
            groupOffset = i;
            for (; i < limit; i++) {
                char c = input.charAt(i);
                int hexDigit = decodeHexDigit(c);
                if (hexDigit == -1)
                    break;
                value = (value << 4) + hexDigit;
            }
            int groupLength = i - groupOffset;
            if (groupLength == 0 || groupLength > 4)
                return null;

            address[b++] = (byte) ((value >>> 8) & 0xff);
            address[b++] = (byte) (value & 0xff);
        }

        if (b != address.length) {
            if (compress == -1)
                return null;
            System.arraycopy(address, compress, address, address.length - (b - compress), b - compress);
            Arrays.fill(address, compress, compress + (address.length - b), (byte) 0);
        }

        try {
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    /**
     * Decodes an IPv4 suffix within an IPv6 address string.
     *
     * @param input         The input string.
     * @param pos           The starting position (inclusive).
     * @param limit         The ending position (exclusive).
     * @param address       The byte array to store the decoded IPv4 address.
     * @param addressOffset The offset in the {@code address} array where the IPv4 bytes should be written.
     * @return {@code true} if the IPv4 suffix was successfully decoded, {@code false} otherwise.
     */
    private static boolean decodeIpv4Suffix(String input, int pos, int limit, byte[] address, int addressOffset) {
        int b = addressOffset;

        for (int i = pos; i < limit;) {
            if (b == address.length)
                return false;

            if (b != addressOffset) {
                if (input.charAt(i) != Symbol.C_DOT)
                    return false;
                i++;
            }

            int value = 0;
            int groupOffset = i;
            for (; i < limit; i++) {
                char c = input.charAt(i);
                if (c < Symbol.C_ZERO || c > Symbol.C_NINE)
                    break;
                if (value == 0 && groupOffset != i)
                    return false;
                value = (value * 10) + c - Symbol.C_ZERO;
                if (value > 255)
                    return false;
            }
            int groupLength = i - groupOffset;
            if (groupLength == 0)
                return false;

            address[b++] = (byte) value;
        }

        if (b != addressOffset + 4)
            return false;

        return true;
    }

    /**
     * Converts an IPv6 address byte array to its ASCII string representation.
     *
     * @param address The byte array representing the IPv6 address.
     * @return The ASCII string representation of the IPv6 address.
     */
    private static String inet6AddressToAscii(byte[] address) {
        int longestRunOffset = -1;
        int longestRunLength = 0;
        for (int i = 0; i < address.length; i += 2) {
            int currentRunOffset = i;
            while (i < Normal._16 && address[i] == 0 && address[i + 1] == 0) {
                i += 2;
            }
            int currentRunLength = i - currentRunOffset;
            if (currentRunLength > longestRunLength && currentRunLength >= 4) {
                longestRunOffset = currentRunOffset;
                longestRunLength = currentRunLength;
            }
        }

        Buffer result = new Buffer();
        for (int i = 0; i < address.length;) {
            if (i == longestRunOffset) {
                result.writeByte(Symbol.C_COLON);
                i += longestRunLength;
                if (i == Normal._16)
                    result.writeByte(Symbol.C_COLON);
            } else {
                if (i > 0)
                    result.writeByte(Symbol.C_COLON);
                int group = (address[i] & 0xff) << 8 | address[i + 1] & 0xff;
                result.writeHexadecimalUnsignedLong(group);
                i += 2;
            }
        }
        return result.readUtf8();
    }

    /**
     * Converts a list of HTTP/2 headers to a {@link Headers} object.
     *
     * @param headerBlock The list of {@link Http2Header} objects.
     * @return A {@link Headers} instance built from the HTTP/2 headers.
     */
    public static Headers toHeaders(List<Http2Header> headerBlock) {
        Headers.Builder builder = new Headers.Builder();
        for (Http2Header header : headerBlock) {
            Internal.instance.addLenient(builder, header.name.utf8(), header.value.utf8());
        }
        return builder.build();
    }

    /**
     * Converts a {@link Headers} object to a list of HTTP/2 headers.
     *
     * @param headers The {@link Headers} object to convert.
     * @return A list of {@link Http2Header} objects.
     */
    public static List<Http2Header> toHeaderBlock(Headers headers) {
        List<Http2Header> result = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            result.add(new Http2Header(headers.name(i), headers.value(i)));
        }
        return result;
    }

    /**
     * Checks if two {@link UnoUrl} instances can share the same connection.
     *
     * @param a The first URL.
     * @param b The second URL.
     * @return {@code true} if the URLs can share a connection, {@code false} otherwise.
     */
    public static boolean sameConnection(UnoUrl a, UnoUrl b) {
        return a.host().equals(b.host()) && a.port() == b.port() && a.scheme().equals(b.scheme());
    }

    /**
     * Parses a date string into a {@link Date} object, trying various browser-compatible formats.
     *
     * @param value The date string to parse.
     * @return A {@link Date} object if parsing is successful, {@code null} otherwise.
     */
    public static Date parse(String value) {
        if (value.length() == 0) {
            return null;
        }

        ParsePosition position = new ParsePosition(0);
        Date result = STANDARD_DATE_FORMAT.get().parse(value, position);
        if (position.getIndex() == value.length()) {
            return result;
        }
        synchronized (BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS) {
            for (int i = 0, count = BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length; i < count; i++) {
                DateFormat format = BROWSER_COMPATIBLE_DATE_FORMATS[i];
                if (format == null) {
                    format = new SimpleDateFormat(BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS[i], Locale.US);
                    format.setTimeZone(UTC);
                    BROWSER_COMPATIBLE_DATE_FORMATS[i] = format;
                }
                position.setIndex(0);
                result = format.parse(value, position);
                if (position.getIndex() != 0) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Formats a {@link Date} object into an RFC 1123 compliant string.
     *
     * @param value The {@link Date} object to format.
     * @return The formatted date string.
     */
    public static String format(Date value) {
        return STANDARD_DATE_FORMAT.get().format(value);
    }

}
