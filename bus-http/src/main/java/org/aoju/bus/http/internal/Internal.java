/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.internal;

import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.core.io.segment.BufferedSource;
import org.aoju.bus.core.io.segment.ByteString;
import org.aoju.bus.core.io.segment.Source;
import org.aoju.bus.http.*;
import org.aoju.bus.http.internal.cache.InternalCache;
import org.aoju.bus.http.internal.connection.RealConnection;
import org.aoju.bus.http.internal.connection.RouteDatabase;
import org.aoju.bus.http.internal.connection.StreamAllocation;
import org.aoju.bus.http.internal.http.second.Header;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * utility methods.
 *
 * @author Kimi Liu
 * @version 3.6.2
 * @since JDK 1.8
 */
public abstract class Internal {

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static final ResponseBody EMPTY_RESPONSE = ResponseBody.create(null, EMPTY_BYTE_ARRAY);
    public static final RequestBody EMPTY_REQUEST = RequestBody.create(null, EMPTY_BYTE_ARRAY);
    public static final TimeZone UTC = TimeZone.getTimeZone("GMT");
    public static final Comparator<String> NATURAL_ORDER = new Comparator<String>() {
        @Override
        public int compare(String a, String b) {
            return a.compareTo(b);
        }
    };
    private static final ByteString UTF_8_BOM = ByteString.decodeHex("efbbbf");
    private static final ByteString UTF_16_BE_BOM = ByteString.decodeHex("feff");
    private static final ByteString UTF_16_LE_BOM = ByteString.decodeHex("fffe");
    private static final ByteString UTF_32_BE_BOM = ByteString.decodeHex("0000ffff");
    private static final ByteString UTF_32_LE_BOM = ByteString.decodeHex("ffff0000");
    private static final Method addSuppressedExceptionMethod;
    /**
     * Quick and dirty pattern to differentiate IP addresses from hostnames. This is an approximation
     * of Android's private InetAddress#isNumeric API.
     *
     * <p>This matches IPv6 addresses as a hex string containing at least first colon, and possibly
     * including dots after the first colon. It matches IPv4 addresses as strings containing only
     * decimal digits and dots. This pattern matches strings like "a:.23" and "54" that are neither IP
     * addresses nor hostnames; they will be verified as IP addresses (which is a more strict
     * verification).
     */
    private static final Pattern VERIFY_AS_IP_ADDRESS = Pattern.compile(
            "([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)");
    public static Internal instance;

    static {
        Method m;
        try {
            m = Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class);
        } catch (Exception e) {
            m = null;
        }
        addSuppressedExceptionMethod = m;
    }

    public static void initializeInstanceForTests() {
        new HttpClient();
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

    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (AssertionError e) {
                if (!isAndroidGetsocknameError(e)) throw e;
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public static void closeQuietly(ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
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
            return true; // Success! The source has been exhausted.
        } catch (InterruptedIOException e) {
            return false; // We ran out of time before exhausting the source.
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
                ? Collections.<K, V>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements.clone()));
    }

    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
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

    public static String hostHeader(HttpUrl url, boolean includeDefaultPort) {
        String host = url.host().contains(":")
                ? "[" + url.host() + "]"
                : url.host();
        return includeDefaultPort || url.port() != HttpUrl.defaultPort(url.scheme())
                ? host + ":" + url.port()
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
                case '\t':
                case '\n':
                case '\f':
                case '\r':
                case ' ':
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
                case '\t':
                case '\n':
                case '\f':
                case '\r':
                case ' ':
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
        // If the input contains a :, it’s an IPv6 address.
        if (host.contains(":")) {
            // If the input is encased in square braces "[...]", drop 'em.
            InetAddress inetAddress = host.startsWith("[") && host.endsWith("]")
                    ? decodeIpv6(host, 1, host.length() - 1)
                    : decodeIpv6(host, 0, host.length());
            if (inetAddress == null) return null;
            byte[] address = inetAddress.getAddress();
            if (address.length == 16) return inet6AddressToAscii(address);
            throw new AssertionError("Invalid IPv6 address: '" + host + "'");
        }

        try {
            String result = IDN.toASCII(host).toLowerCase(Locale.US);
            if (result.isEmpty()) return null;

            // Confirm that the IDN ToASCII result doesn't contain any illegal characters.
            if (containsInvalidHostnameAsciiCodes(result)) {
                return null;
            }
            // TODO: implement all label limits.
            return result;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean containsInvalidHostnameAsciiCodes(String hostnameAscii) {
        for (int i = 0; i < hostnameAscii.length(); i++) {
            char c = hostnameAscii.charAt(i);
            // The WHATWG Host parsing rules accepts some character codes which are invalid by
            // definition for HttpClient's host header checks (and the WHATWG Host syntax definition). Here
            // we rule out characters that would cause problems in host headers.
            if (c <= '\u001f' || c >= '\u007f') {
                return true;
            }
            // Check for the characters mentioned in the WHATWG Host parsing spec:
            // U+0000, U+0009, U+000A, U+000D, U+0020, "#", "%", "/", ":", "?", "@", "[", "\", and "]"
            // (excluding the characters covered above).
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

    public static Charset bomAwareCharset(BufferedSource source, Charset charset) throws IOException {
        if (source.rangeEquals(0, UTF_8_BOM)) {
            source.skip(UTF_8_BOM.size());
            return org.aoju.bus.core.consts.Charset.UTF_8;
        }
        if (source.rangeEquals(0, UTF_16_BE_BOM)) {
            source.skip(UTF_16_BE_BOM.size());
            return org.aoju.bus.core.consts.Charset.UTF_16_BE;
        }
        if (source.rangeEquals(0, UTF_16_LE_BOM)) {
            source.skip(UTF_16_LE_BOM.size());
            return org.aoju.bus.core.consts.Charset.UTF_16_LE;
        }
        if (source.rangeEquals(0, UTF_32_BE_BOM)) {
            source.skip(UTF_32_BE_BOM.size());
            return org.aoju.bus.core.consts.Charset.UTF_32_BE;
        }
        if (source.rangeEquals(0, UTF_32_LE_BOM)) {
            source.skip(UTF_32_LE_BOM.size());
            return org.aoju.bus.core.consts.Charset.UTF_32_LE;
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
            // ignored, shouldn't happen
        }
        return assertionError;
    }

    public static int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') return c - '0';
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
            if (b == address.length) return null; // Too many groups.

            // Read a delimiter.
            if (i + 2 <= limit && input.regionMatches(i, "::", 0, 2)) {
                // Compression "::" delimiter, which is anywhere in the input, including its prefix.
                if (compress != -1) return null; // Multiple "::" delimiters.
                i += 2;
                b += 2;
                compress = b;
                if (i == limit) break;
            } else if (b != 0) {
                // Group separator ":" delimiter.
                if (input.regionMatches(i, ":", 0, 1)) {
                    i++;
                } else if (input.regionMatches(i, ".", 0, 1)) {
                    // If we see a '.', rewind to the beginning of the previous group and parse as IPv4.
                    if (!decodeIpv4Suffix(input, groupOffset, limit, address, b - 2)) return null;
                    b += 2; // We rewound two bytes and then added four.
                    break;
                } else {
                    return null; // Wrong delimiter.
                }
            }

            // Read a group, first to four hex digits.
            int value = 0;
            groupOffset = i;
            for (; i < limit; i++) {
                char c = input.charAt(i);
                int hexDigit = decodeHexDigit(c);
                if (hexDigit == -1) break;
                value = (value << 4) + hexDigit;
            }
            int groupLength = i - groupOffset;
            if (groupLength == 0 || groupLength > 4) return null; // Group is the wrong size.

            // We've successfully read a group. Assign its value to our byte array.
            address[b++] = (byte) ((value >>> 8) & 0xff);
            address[b++] = (byte) (value & 0xff);
        }

        // All done. If compression happened, we need to move bytes to the right place in the
        // address. Here's a sample:
        //
        //      input: "1111:2222:3333::7777:8888"
        //     before: { 11, 11, 22, 22, 33, 33, 00, 00, 77, 77, 88, 88, 00, 00, 00, 00  }
        //   compress: 6
        //          b: 10
        //      after: { 11, 11, 22, 22, 33, 33, 00, 00, 00, 00, 00, 00, 77, 77, 88, 88 }
        //
        if (b != address.length) {
            if (compress == -1) return null; // Address didn't have compression or enough groups.
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
            if (b == address.length) return false; // Too many groups.

            // Read a delimiter.
            if (b != addressOffset) {
                if (input.charAt(i) != '.') return false; // Wrong delimiter.
                i++;
            }

            // Read 1 or more decimal digits for a value in 0..255.
            int value = 0;
            int groupOffset = i;
            for (; i < limit; i++) {
                char c = input.charAt(i);
                if (c < '0' || c > '9') break;
                if (value == 0 && groupOffset != i) return false; // Reject unnecessary leading '0's.
                value = (value * 10) + c - '0';
                if (value > 255) return false; // Value out of range.
            }
            int groupLength = i - groupOffset;
            if (groupLength == 0) return false; // No digits.

            // We've successfully read a byte.
            address[b++] = (byte) value;
        }

        if (b != addressOffset + 4) return false; // Too few groups. We wanted exactly four.
        return true; // Success.
    }

    private static String inet6AddressToAscii(byte[] address) {
        // Go through the address looking for the longest run of 0s. Each group is 2-bytes.
        // A run must be longer than first group (section 4.2.2).
        // If there are multiple equal runs, the first first must be used (section 4.2.3).
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

        // Emit each 2-byte group in hex, separated by ':'. The longest run of zeroes is "::".
        Buffer result = new Buffer();
        for (int i = 0; i < address.length; ) {
            if (i == longestRunOffset) {
                result.writeByte(':');
                i += longestRunLength;
                if (i == 16) result.writeByte(':');
            } else {
                if (i > 0) result.writeByte(':');
                int group = (address[i] & 0xff) << 8 | address[i + 1] & 0xff;
                result.writeHexadecimalUnsignedLong(group);
                i += 2;
            }
        }
        return result.readUtf8();
    }

    public static X509TrustManager platformTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {
            throw assertionError("No System TLS", e); // The system has no TLS. Just give up.
        }
    }

    public static Headers toHeaders(List<Header> headerBlock) {
        Headers.Builder builder = new Headers.Builder();
        for (Header header : headerBlock) {
            Internal.instance.addLenient(builder, header.name.utf8(), header.value.utf8());
        }
        return builder.build();
    }

    public abstract void addLenient(Headers.Builder builder, String line);

    public abstract void addLenient(Headers.Builder builder, String name, String value);

    public abstract void setCache(HttpClient.Builder builder, InternalCache internalCache);

    public abstract RealConnection get(ConnectionPool pool, Address address,
                                       StreamAllocation streamAllocation, Route route);

    public abstract boolean equalsNonHost(Address a, Address b);

    public abstract Socket deduplicate(
            ConnectionPool pool, Address address, StreamAllocation streamAllocation);

    public abstract void put(ConnectionPool pool, RealConnection connection);

    public abstract boolean connectionBecameIdle(ConnectionPool pool, RealConnection connection);

    public abstract RouteDatabase routeDatabase(ConnectionPool connectionPool);

    public abstract int code(Response.Builder responseBuilder);

    public abstract void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket,
                               boolean isFallback);

    public abstract boolean isInvalidHttpUrlHost(IllegalArgumentException e);

    public abstract StreamAllocation streamAllocation(Call call);

    public abstract IOException timeoutExit(Call call, IOException e);

    public abstract Call newWebSocketCall(HttpClient client, Request request);
}
