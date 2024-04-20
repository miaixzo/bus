/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http;

import org.miaixz.bus.core.io.Blending;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.metric.Internal;

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

/**
 * 实用方法工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * 最后一个四位数的年份:"Fri, 31 Dec 9999 23:59:59 GMT"
     */
    public static final long MAX_DATE = 253402300799999L;

    public static final Headers EMPTY_HEADERS = Headers.of();
    public static final ResponseBody EMPTY_RESPONSE = ResponseBody.create(null, Normal.EMPTY_BYTE_ARRAY);
    /**
     * GMT and UTC are equivalent for our purposes
     */
    public static final TimeZone UTC = TimeZone.getTimeZone("GMT");
    public static final Comparator<String> NATURAL_ORDER = String::compareTo;
    public static final ByteString QUOTED_STRING_DELIMITERS = ByteString.encodeUtf8("\"\\");
    public static final ByteString TOKEN_DELIMITERS = ByteString.encodeUtf8("\t ,=");
    /**
     * 如果我们未能以非标准格式解析日期，请依次尝试这些格式.
     */
    public static final String[] BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM d HH:mm:ss yyyy",
            "EEE, dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MMM-yyyy HH-mm-ss z",
            "EEE, dd MMM yy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH:mm:ss z",
            "EEE dd MMM yyyy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH-mm-ss z",
            "EEE dd-MMM-yy HH:mm:ss z",
            "EEE dd MMM yy HH:mm:ss z",
            "EEE,dd-MMM-yy HH:mm:ss z",
            "EEE,dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MM-yyyy HH:mm:ss z",
            "EEE MMM d yyyy HH:mm:ss z",
    };
    public static final DateFormat[] BROWSER_COMPATIBLE_DATE_FORMATS =
            new DateFormat[BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length];
    public static final String CONNECT = "CONNECT";
    public static final String CONNECTED = "CONNECTED";
    public static final String SEND = "SEND";
    public static final String MESSAGE = "MESSAGE";
    public static final String SUBSCRIBE = "SUBSCRIBE";
    public static final String UNSUBSCRIBE = "UNSUBSCRIBE";
    public static final String ACK = "ACK";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String ERROR = "ERROR";
    /**
     * Byte order marks.
     */
    private static final Blending UNICODE_BOMS = Blending.of(
            ByteString.decodeHex("efbbbf"),   // UTF-8
            ByteString.decodeHex("feff"),     // UTF-16BE
            ByteString.decodeHex("fffe"),     // UTF-16LE
            ByteString.decodeHex("0000ffff"), // UTF-32BE
            ByteString.decodeHex("ffff0000")  // UTF-32LE
    );
    private static final Method addSuppressedExceptionMethod;
    /**
     * Most websites serve cookies in the blessed format. Eagerly create the parser to ensure such
     * cookies are on the fast path.
     */
    private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT =
            ThreadLocal.withInitial(() -> {
                // Date format specified by RFC 7231 section 7.1.1.1.
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

    public Builder() {

    }

    public static void addSuppressedIfPossible(Throwable e, Throwable suppressed) {
        if (addSuppressedExceptionMethod != null) {
            try {
                addSuppressedExceptionMethod.invoke(e, suppressed);
            } catch (InvocationTargetException | IllegalAccessException ignored) {
            }
        }
    }

    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Attempts to exhaust {@code source}, returning true if successful. This is useful when reading a
     * complete source is helpful, such as when doing so completes a cache body or frees a socket
     * connection for reuse.
     */
    public static boolean discard(Source source, int timeout, TimeUnit timeUnit) {
        try {
            return skipAll(source, timeout, timeUnit);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Reads until {@code in} is exhausted or the deadline has been reached. This is careful to not
     * extend the deadline if one exists already.
     */
    public static boolean skipAll(Source source, int duration, TimeUnit timeUnit) throws IOException {
        long now = System.nanoTime();
        long originalDuration = source.timeout().hasDeadline()
                ? source.timeout().deadlineNanoTime() - now
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
     * Returns an immutable copy of {@code list}.
     */
    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    /**
     * Returns an immutable copy of {@code map}.
     */
    public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
        return map.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    /**
     * Returns an immutable list containing {@code elements}.
     */
    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements.clone()));
    }

    public static ThreadFactory threadFactory(String name, boolean daemon) {
        return runnable -> {
            Thread result = new Thread(runnable, name);
            result.setDaemon(daemon);
            return result;
        };
    }

    /**
     * Returns an array containing only elements found in {@code first} and also in {@code
     * second}. The returned elements are in the same order as in {@code first}.
     */
    public static String[] intersect(
            Comparator<? super String> comparator, String[] first, String[] second) {
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
     * Returns true if there is an element in {@code first} that is also in {@code second}. This
     * method terminates if any intersection is found. The sizes of both arguments are assumed to be
     * so small, and the likelihood of an intersection so great, that it is not worth the CPU cost of
     * sorting or the memory cost of hashing.
     */
    public static boolean nonEmptyIntersection(
            Comparator<String> comparator, String[] first, String[] second) {
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

    public static String hostHeader(UnoUrl url, boolean includeDefaultPort) {
        String host = url.host().contains(":")
                ? "[" + url.host() + "]"
                : url.host();
        return includeDefaultPort || url.port() != UnoUrl.defaultPort(url.scheme())
                ? host + ":" + url.port()
                : host;
    }

    /**
     * Returns true if {@code e} is due to a firmware bug fixed after Android 4.2.2.
     * https://code.google.com/p/android/issues/detail?id=54072
     */
    public static boolean isAndroidGetsocknameError(AssertionError e) {
        return e.getCause() != null && e.getMessage() != null
                && e.getMessage().contains("getsockname failed");
    }

    public static int indexOf(Comparator<String> comparator, String[] array, String value) {
        for (int i = 0, size = array.length; i < size; i++) {
            if (comparator.compare(array[i], value) == 0) return i;
        }
        return -1;
    }

    public static String[] concat(String[] array, String value) {
        String[] result = new String[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[result.length - 1] = value;
        return result;
    }

    /**
     * Increments {@code pos} until {@code input[pos]} is not ASCII whitespace. Stops at {@code
     * limit}.
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
     * Decrements {@code limit} until {@code input[limit - 1]} is not ASCII whitespace. Stops at
     * {@code pos}.
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
     * Equivalent to {@code string.substring(pos, limit).trim()}.
     */
    public static String trimSubstring(String string, int pos, int limit) {
        int start = skipLeadingAsciiWhitespace(string, pos, limit);
        int end = skipTrailingAsciiWhitespace(string, start, limit);
        return string.substring(start, end);
    }

    /**
     * Returns the index of the first character in {@code input} that contains a character in {@code
     * delimiters}. Returns limit if there is no such character.
     */
    public static int delimiterOffset(String input, int pos, int limit, String delimiters) {
        for (int i = pos; i < limit; i++) {
            if (delimiters.indexOf(input.charAt(i)) != -1) return i;
        }
        return limit;
    }

    /**
     * Returns the index of the first character in {@code input} that is {@code delimiter}. Returns
     * limit if there is no such character.
     */
    public static int delimiterOffset(String input, int pos, int limit, char delimiter) {
        for (int i = pos; i < limit; i++) {
            if (input.charAt(i) == delimiter) return i;
        }
        return limit;
    }

    /**
     * If {@code host} is an IP address, this returns the IP address in canonical form.
     * <p>
     * Otherwise this performs IDN ToASCII encoding and canonicalize the result to lowercase. For
     * example this converts {@code ☃.net} to {@code xn--n3h.net}, and {@code WwW.GoOgLe.cOm} to
     * {@code www.google.com}. {@code null} will be returned if the host cannot be ToASCII encoded or
     * if the result contains unsupported ASCII characters.
     */
    public static String canonicalizeHost(String host) {
        // If the input contains a :, it’s an IPv6 address.
        if (host.contains(":")) {
            InetAddress inetAddress = host.startsWith("[") && host.endsWith("]")
                    ? decodeIpv6(host, 1, host.length() - 1)
                    : decodeIpv6(host, 0, host.length());
            if (inetAddress == null) return null;
            byte[] address = inetAddress.getAddress();
            if (address.length == 16) return inet6AddressToAscii(address);
            if (address.length == 4) return inetAddress.getHostAddress(); // An IPv4-mapped IPv6 address.
            throw new AssertionError("Invalid IPv6 address: '" + host + "'");
        }

        try {
            String result = IDN.toASCII(host).toLowerCase(Locale.US);
            if (result.isEmpty()) return null;

            if (containsInvalidHostnameAsciiCodes(result)) {
                return null;
            }
            return result;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

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
     * Returns the index of the first character in {@code input} that is either a control character
     * (like {@code \u0000 or \n}) or a non-ASCII character. Returns -1 if {@code input} has no such
     * characters.
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
     * Returns true if {@code host} is not a host name and might be an IP address.
     */
    public static boolean verifyAsIpAddress(String host) {
        return RegEx.IP_ADDRESS.matcher(host).matches();
    }

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

    public static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException(name + " < 0");
        if (null == unit) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0) throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }

    public static int decodeHexDigit(char c) {
        if (c >= Symbol.C_ZERO && c <= Symbol.C_NINE) return c - Symbol.C_ZERO;
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }

    /**
     * Decodes an IPv6 address like 1111:2222:3333:4444:5555:6666:7777:8888 or ::1.
     */
    private static InetAddress decodeIpv6(String input, int pos, int limit) {
        byte[] address = new byte[Normal._16];
        int b = 0;
        int compress = -1;
        int groupOffset = -1;

        for (int i = pos; i < limit; ) {
            if (b == address.length) return null;

            if (i + 2 <= limit && input.regionMatches(i, Symbol.COLON + Symbol.COLON, 0, 2)) {
                if (compress != -1) return null;
                i += 2;
                b += 2;
                compress = b;
                if (i == limit) break;
            } else if (b != 0) {
                if (input.regionMatches(i, Symbol.COLON, 0, 1)) {
                    i++;
                } else if (input.regionMatches(i, Symbol.DOT, 0, 1)) {
                    // If we see a '.', rewind to the beginning of the previous group and parse as IPv4.
                    if (!decodeIpv4Suffix(input, groupOffset, limit, address, b - 2)) return null;
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
                if (hexDigit == -1) break;
                value = (value << 4) + hexDigit;
            }
            int groupLength = i - groupOffset;
            if (groupLength == 0 || groupLength > 4) return null;

            address[b++] = (byte) ((value >>> 8) & 0xff);
            address[b++] = (byte) (value & 0xff);
        }

        if (b != address.length) {
            if (compress == -1) return null;
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
     * Decodes an IPv4 address suffix of an IPv6 address, like 1111::5555:6666:192.168.0.1.
     */
    private static boolean decodeIpv4Suffix(
            String input, int pos, int limit, byte[] address, int addressOffset) {
        int b = addressOffset;

        for (int i = pos; i < limit; ) {
            if (b == address.length) return false;

            // Read a delimiter.
            if (b != addressOffset) {
                if (input.charAt(i) != Symbol.C_DOT) return false;
                i++;
            }

            int value = 0;
            int groupOffset = i;
            for (; i < limit; i++) {
                char c = input.charAt(i);
                if (c < Symbol.C_ZERO || c > Symbol.C_NINE) break;
                if (value == 0 && groupOffset != i) return false;
                value = (value * 10) + c - Symbol.C_ZERO;
                if (value > 255) return false;
            }
            int groupLength = i - groupOffset;
            if (groupLength == 0) return false;

            address[b++] = (byte) value;
        }

        if (b != addressOffset + 4) return false;

        return true;
    }

    /**
     * Encodes an IPv6 address in canonical form according to RFC 5952.
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
        for (int i = 0; i < address.length; ) {
            if (i == longestRunOffset) {
                result.writeByte(Symbol.C_COLON);
                i += longestRunLength;
                if (i == Normal._16) result.writeByte(Symbol.C_COLON);
            } else {
                if (i > 0) result.writeByte(Symbol.C_COLON);
                int group = (address[i] & 0xff) << 8 | address[i + 1] & 0xff;
                result.writeHexadecimalUnsignedLong(group);
                i += 2;
            }
        }
        return result.readUtf8();
    }

    public static Headers toHeaders(List<Headers.Header> headerBlock) {
        Headers.Builder builder = new Headers.Builder();
        for (Headers.Header header : headerBlock) {
            Internal.instance.addLenient(builder, header.name.utf8(), header.value.utf8());
        }
        return builder.build();
    }

    public static List<Headers.Header> toHeaderBlock(Headers headers) {
        List<Headers.Header> result = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            result.add(new Headers.Header(headers.name(i), headers.value(i)));
        }
        return result;
    }

    /**
     * Returns true if an HTTP request for {@code a} and {@code b} can reuse a connection.
     */
    public static boolean sameConnection(UnoUrl a, UnoUrl b) {
        return a.host().equals(b.host())
                && a.port() == b.port()
                && a.scheme().equals(b.scheme());
    }

    /**
     * Returns the date for {@code value}. Returns null if the value couldn't be parsed.
     */
    public static Date parse(String value) {
        if (value.length() == 0) {
            return null;
        }

        ParsePosition position = new ParsePosition(0);
        Date result = STANDARD_DATE_FORMAT.get().parse(value, position);
        if (position.getIndex() == value.length()) {
            // STANDARD_DATE_FORMAT must match exactly; all text must be consumed, e.g. no ignored
            // non-standard trailing "+01:00". Those cases are covered below.
            return result;
        }
        synchronized (BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS) {
            for (int i = 0, count = BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length; i < count; i++) {
                DateFormat format = BROWSER_COMPATIBLE_DATE_FORMATS[i];
                if (format == null) {
                    format = new SimpleDateFormat(BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS[i], Locale.US);
                    // Set the timezone to use when interpreting formats that don't have a timezone. GMT is
                    // specified by RFC 7231.
                    format.setTimeZone(UTC);
                    BROWSER_COMPATIBLE_DATE_FORMATS[i] = format;
                }
                position.setIndex(0);
                result = format.parse(value, position);
                if (position.getIndex() != 0) {
                    // Something was parsed. It's possible the entire string was not consumed but we ignore
                    // that. If any of the BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS ended in "'GMT'" we'd have
                    // to also check that position.getIndex() == value.length() otherwise parsing might have
                    // terminated early, ignoring things like "+01:00". Leaving this as != 0 means that any
                    // trailing junk is ignored.
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Returns the string for {@code value}.
     */
    public static String format(Date value) {
        return STANDARD_DATE_FORMAT.get().format(value);
    }

}
