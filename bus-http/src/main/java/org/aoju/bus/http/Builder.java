/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.http;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.BufferSource;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.io.Source;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.accord.*;
import org.aoju.bus.http.cache.InternalCache;
import org.aoju.bus.http.metric.http.HttpHeaders;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.IDN;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 实用方法工具
 *
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
public abstract class Builder {

    /**
     * 最后一个四位数的年份:"Fri, 31 Dec 9999 23:59:59 GMT".
     */
    public static final long MAX_DATE = 253402300799999L;
    public static final String X_509 = "X.509";
    public static final TimeZone UTC = TimeZone.getTimeZone("GMT");
    public static final ByteString UTF_8_BOM = ByteString.decodeHex("efbbbf");
    public static final ByteString UTF_16_BE_BOM = ByteString.decodeHex("feff");
    public static final ByteString UTF_16_LE_BOM = ByteString.decodeHex("fffe");
    public static final ByteString UTF_32_BE_BOM = ByteString.decodeHex("0000ffff");
    public static final ByteString UTF_32_LE_BOM = ByteString.decodeHex("ffff0000");
    public static final Method addSuppressedExceptionMethod;
    /**
     * 大多数网站都提供祝福格式的cookies。创建解析器，以确保此类cookie处于快速路径上
     */
    public static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT =
            ThreadLocal.withInitial(() -> {
                DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                rfc1123.setLenient(false);
                rfc1123.setTimeZone(Builder.UTC);
                return rfc1123;
            });

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

    /**
     * 快速和正则模式区分IP地址从主机名，这是Android私有的InetAddress#isNumeric API的近似值
     */
    public static final Pattern VERIFY_AS_IP_ADDRESS = Pattern.compile("([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)");
    public static final String FORM = "form";
    public static final String JSON = "json";
    public static final String XML = "xml";
    public static final String PROTOBUF = "protobuf";
    public static Builder instance;

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
                    format.setTimeZone(Builder.UTC);
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

    public static String format(Date value) {
        return STANDARD_DATE_FORMAT.get().format(value);
    }

    public static void initializeInstanceForTests() {
        new Httpd();
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

    public static boolean discard(Source source, int timeout, TimeUnit timeUnit) {
        try {
            return skipAll(source, timeout, timeUnit);
        } catch (IOException e) {
            return false;
        }
    }

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

    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
        return map.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements.clone()));
    }

    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return runnable -> {
            Thread result = new Thread(runnable, name);
            result.setDaemon(daemon);
            return result;
        };
    }

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
        String host = url.host().contains(Symbol.COLON)
                ? Symbol.BRACKET_LEFT + url.host() + Symbol.BRACKET_RIGHT
                : url.host();
        return includeDefaultPort || url.port() != UnoUrl.defaultPort(url.scheme())
                ? host + Symbol.COLON + url.port()
                : host;
    }

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

    public static String trimSubstring(String string, int pos, int limit) {
        int start = skipLeadingAsciiWhitespace(string, pos, limit);
        int end = skipTrailingAsciiWhitespace(string, start, limit);
        return string.substring(start, end);
    }

    public static int delimiterOffset(String input, int pos, int limit, String delimiters) {
        for (int i = pos; i < limit; i++) {
            if (delimiters.indexOf(input.charAt(i)) != -1) return i;
        }
        return limit;
    }

    public static int delimiterOffset(String input, int pos, int limit, char delimiter) {
        for (int i = pos; i < limit; i++) {
            if (input.charAt(i) == delimiter) return i;
        }
        return limit;
    }

    public static String canonicalizeHost(String host) {
        if (host.contains(Symbol.COLON)) {
            InetAddress inetAddress = host.startsWith(Symbol.BRACKET_LEFT) && host.endsWith(Symbol.BRACKET_RIGHT)
                    ? decodeIpv6(host, 1, host.length() - 1)
                    : decodeIpv6(host, 0, host.length());
            if (inetAddress == null) return null;
            byte[] address = inetAddress.getAddress();
            if (address.length == 16) return inet6AddressToAscii(address);
            throw new AssertionError("Invalid IPv6 address: '" + host + Symbol.SINGLE_QUOTE);
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

    public static int indexOfControlOrNonAscii(String input) {
        for (int i = 0, length = input.length(); i < length; i++) {
            char c = input.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                return i;
            }
        }
        return -1;
    }

    public static boolean verifyAsIpAddress(String host) {
        return VERIFY_AS_IP_ADDRESS.matcher(host).matches();
    }

    public static java.nio.charset.Charset bomAwareCharset(BufferSource source, java.nio.charset.Charset charset) throws IOException {
        if (source.rangeEquals(0, UTF_8_BOM)) {
            source.skip(UTF_8_BOM.size());
            return Charset.UTF_8;
        }
        if (source.rangeEquals(0, UTF_16_BE_BOM)) {
            source.skip(UTF_16_BE_BOM.size());
            return Charset.UTF_16_BE;
        }
        if (source.rangeEquals(0, UTF_16_LE_BOM)) {
            source.skip(UTF_16_LE_BOM.size());
            return Charset.UTF_16_LE;
        }
        if (source.rangeEquals(0, UTF_32_BE_BOM)) {
            source.skip(UTF_32_BE_BOM.size());
            return Charset.UTF_32_BE;
        }
        if (source.rangeEquals(0, UTF_32_LE_BOM)) {
            source.skip(UTF_32_LE_BOM.size());
            return Charset.UTF_32_LE;
        }
        return charset;
    }

    public static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException(name + " < 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0) throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }

    public static AssertionError assertionError(String message, Exception e) {
        AssertionError assertionError = new AssertionError(message);
        try {
            assertionError.initCause(e);
        } catch (IllegalStateException ise) {
        }
        return assertionError;
    }

    public static int decodeHexDigit(char c) {
        if (c >= Symbol.C_ZERO && c <= Symbol.C_NINE) return c - Symbol.C_ZERO;
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }

    private static InetAddress decodeIpv6(String input, int pos, int limit) {
        byte[] address = new byte[16];
        int b = 0;
        int compress = -1;
        int groupOffset = -1;

        for (int i = pos; i < limit; ) {
            if (b == address.length) return null;

            if (i + 2 <= limit && input.regionMatches(i, "::", 0, 2)) {
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
            while (i < 16 && address[i] == 0 && address[i + 1] == 0) {
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
                if (i == 16) result.writeByte(Symbol.C_COLON);
            } else {
                if (i > 0) result.writeByte(Symbol.C_COLON);
                int group = (address[i] & 0xff) << 8 | address[i + 1] & 0xff;
                result.writeHexadecimalUnsignedLong(group);
                i += 2;
            }
        }
        return result.readUtf8();
    }

    public static Headers toHeaders(List<HttpHeaders> headersBlock) {
        Headers.Builder builder = new Headers.Builder();
        for (HttpHeaders headers : headersBlock) {
            Builder.instance.addLenient(builder, headers.name.utf8(), headers.value.utf8());
        }
        return builder.build();
    }

    public abstract void addLenient(Headers.Builder builder,
                                    String line);

    public abstract void addLenient(Headers.Builder builder,
                                    String name,
                                    String value);

    public abstract void setCache(Httpd.Builder builder,
                                  InternalCache internalCache);

    public abstract RealConnection get(ConnectionPool pool,
                                       Address address,
                                       StreamAllocation streamAllocation,
                                       Route route);

    public abstract boolean equalsNonHost(Address a,
                                          Address b);

    public abstract Socket deduplicate(
            ConnectionPool pool,
            Address address,
            StreamAllocation streamAllocation);

    public abstract void put(ConnectionPool pool,
                             RealConnection connection);

    public abstract boolean connectionBecameIdle(ConnectionPool pool,
                                                 RealConnection connection);

    public abstract RouteDatabase routeDatabase(ConnectionPool connectionPool);

    public abstract int code(Response.Builder responseBuilder);

    public abstract void apply(ConnectionSuite tlsConfiguration,
                               SSLSocket sslSocket,
                               boolean isFallback);

    public abstract boolean isInvalidHttpUrlHost(IllegalArgumentException e);

    public abstract StreamAllocation streamAllocation(NewCall call);

    public abstract IOException timeoutExit(NewCall call,
                                            IOException e);

    public abstract NewCall newWebSocketCall(Httpd client,
                                             Request request);

}
