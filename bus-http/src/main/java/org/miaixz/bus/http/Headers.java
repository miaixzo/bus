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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.metric.CookieJar;
import org.miaixz.bus.http.secure.Challenge;

import java.io.EOFException;
import java.time.Instant;
import java.util.*;

/**
 * 单个HTTP消息的头字段。值是未解释的字符串;
 * 使用{@code Request}和{@code Response}解释头信息
 * 该类维护HTTP消息中的头字段的顺序
 * 这个类从值中删除空白。它从不返回带开头或结尾空白的值
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Headers {

    private final String[] namesAndValues;

    Headers(Builder builder) {
        this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
    }

    private Headers(String[] namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    private static String get(String[] namesAndValues, String name) {
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return namesAndValues[i + 1];
            }
        }
        return null;
    }

    /**
     * Returns headers for the alternating header names and values. There must be an even number of
     * arguments, and they must alternate between header names and values.
     */
    public static Headers of(String... namesAndValues) {
        if (namesAndValues == null) throw new NullPointerException("namesAndValues == null");
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating header names and values");
        }

        // Make a defensive copy and clean it up.
        namesAndValues = namesAndValues.clone();
        for (int i = 0; i < namesAndValues.length; i++) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("Headers cannot be null");
            namesAndValues[i] = namesAndValues[i].trim();
        }

        for (int i = 0; i < namesAndValues.length; i += 2) {
            String name = namesAndValues[i];
            String value = namesAndValues[i + 1];
            checkName(name);
            checkValue(value, name);
        }

        return new Headers(namesAndValues);
    }

    /**
     * Returns headers for the header names and values in the {@link Map}.
     */
    public static Headers of(Map<String, String> headers) {
        if (headers == null) throw new NullPointerException("headers == null");

        String[] namesAndValues = new String[headers.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (null == header.getKey() || null == header.getValue()) {
                throw new IllegalArgumentException("Headers cannot be null");
            }
            String name = header.getKey().trim();
            String value = header.getValue().trim();
            checkName(name);
            checkValue(value, name);
            namesAndValues[i] = name;
            namesAndValues[i + 1] = value;
            i += 2;
        }

        return new Headers(namesAndValues);
    }

    static void checkName(String name) {
        if (null == name) throw new NullPointerException("name == null");
        if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
        for (int i = 0, length = name.length(); i < length; i++) {
            char c = name.charAt(i);
            if (c <= '\u0020' || c >= '\u007f') {
                throw new IllegalArgumentException(String.format(
                        "Unexpected char %#04x at %d in header name: %s", (int) c, i, name));
            }
        }
    }

    static void checkValue(String value, String name) {
        if (null == value) throw new NullPointerException("value for name " + name + " == null");
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
            if ((c <= '\u001f' && c != Symbol.C_HT) || c >= '\u007f') {
                throw new IllegalArgumentException(String.format(
                        "Unexpected char %#04x at %d in %s value: %s", (int) c, i, name, value));
            }
        }
    }

    public static long contentLength(Response response) {
        return contentLength(response.headers());
    }

    public static long contentLength(Headers headers) {
        return stringToLong(headers.get("Content-Length"));
    }

    private static long stringToLong(String s) {
        if (s == null) return -1;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Returns true if none of the Vary headers have changed between {@code cachedRequest} and {@code
     * newRequest}.
     */
    public static boolean varyMatches(
            Response cachedResponse, Headers cachedRequest, Request newRequest) {
        for (String field : varyFields(cachedResponse)) {
            if (!Objects.equals(cachedRequest.values(field), newRequest.headers(field))) return false;
        }
        return true;
    }

    /**
     * Returns true if a Vary header contains an asterisk. Such responses cannot be cached.
     */
    public static boolean hasVaryAll(Response response) {
        return hasVaryAll(response.headers());
    }

    /**
     * Returns true if a Vary header contains an asterisk. Such responses cannot be cached.
     */
    public static boolean hasVaryAll(Headers responseHeaders) {
        return varyFields(responseHeaders).contains("*");
    }

    private static Set<String> varyFields(Response response) {
        return varyFields(response.headers());
    }

    /**
     * Returns the names of the request headers that need to be checked for equality when caching.
     */
    public static Set<String> varyFields(Headers responseHeaders) {
        Set<String> result = Collections.emptySet();
        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
            if (!"Vary".equalsIgnoreCase(responseHeaders.name(i))) continue;

            String value = responseHeaders.value(i);
            if (result.isEmpty()) {
                result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            }
            for (String varyField : value.split(",")) {
                result.add(varyField.trim());
            }
        }
        return result;
    }

    /**
     * Returns the subset of the headers in {@code response}'s request that impact the content of
     * response's body.
     */
    public static Headers varyHeaders(Response response) {
        // Use the request headers sent over the network, since that's what the
        // response varies on. Otherwise Http-supplied headers like
        // "Accept-Encoding: gzip" may be lost.
        Headers requestHeaders = response.networkResponse().request().headers();
        Headers responseHeaders = response.headers();
        return varyHeaders(requestHeaders, responseHeaders);
    }

    /**
     * Returns the subset of the headers in {@code requestHeaders} that impact the content of
     * response's body.
     */
    public static Headers varyHeaders(Headers requestHeaders, Headers responseHeaders) {
        Set<String> varyFields = varyFields(responseHeaders);
        if (varyFields.isEmpty()) return org.miaixz.bus.http.Builder.EMPTY_HEADERS;

        Headers.Builder result = new Headers.Builder();
        for (int i = 0, size = requestHeaders.size(); i < size; i++) {
            String fieldName = requestHeaders.name(i);
            if (varyFields.contains(fieldName)) {
                result.add(fieldName, requestHeaders.value(i));
            }
        }
        return result.build();
    }

    /**
     * Parse RFC 7235 challenges. This is awkward because we need to look ahead to know how to
     * interpret a token.
     * <p>
     * For example, the first line has a parameter name/value pair and the second line has a single
     * token68:
     *
     * <pre>   {@code
     *
     *   WWW-Authenticate: Digest foo=bar
     *   WWW-Authenticate: Digest foo=
     * }</pre>
     * <p>
     * Similarly, the first line has one challenge and the second line has two challenges:
     *
     * <pre>   {@code
     *
     *   WWW-Authenticate: Digest ,foo=bar
     *   WWW-Authenticate: Digest ,foo
     * }</pre>
     */
    public static List<Challenge> parseChallenges(Headers responseHeaders, String headerName) {
        List<Challenge> result = new ArrayList<>();
        for (int h = 0; h < responseHeaders.size(); h++) {
            if (headerName.equalsIgnoreCase(responseHeaders.name(h))) {
                Buffer header = new Buffer().writeUtf8(responseHeaders.value(h));
                parseChallengeHeader(result, header);
            }
        }
        return result;
    }

    private static void parseChallengeHeader(List<Challenge> result, Buffer header) {
        String peek = null;

        while (true) {
            // Read a scheme name for this challenge if we don't have one already.
            if (peek == null) {
                skipWhitespaceAndCommas(header);
                peek = readToken(header);
                if (peek == null) return;
            }

            String schemeName = peek;

            // Read a token68, a sequence of parameters, or nothing.
            boolean commaPrefixed = skipWhitespaceAndCommas(header);
            peek = readToken(header);
            if (peek == null) {
                if (!header.exhausted()) return; // Expected a token; got something else.
                result.add(new Challenge(schemeName, Collections.emptyMap()));
                return;
            }

            int eqCount = skipAll(header, (byte) '=');
            boolean commaSuffixed = skipWhitespaceAndCommas(header);

            // It's a token68 because there isn't a value after it.
            if (!commaPrefixed && (commaSuffixed || header.exhausted())) {
                result.add(new Challenge(schemeName, Collections.singletonMap(
                        null, peek + repeat('=', eqCount))));
                peek = null;
                continue;
            }

            // It's a series of parameter names and values.
            Map<String, String> parameters = new LinkedHashMap<>();
            eqCount += skipAll(header, (byte) '=');
            while (true) {
                if (peek == null) {
                    peek = readToken(header);
                    if (skipWhitespaceAndCommas(header)) break; // We peeked a scheme name followed by ','.
                    eqCount = skipAll(header, (byte) '=');
                }
                if (eqCount == 0) break; // We peeked a scheme name.
                if (eqCount > 1) return; // Unexpected '=' characters.
                if (skipWhitespaceAndCommas(header)) return; // Unexpected ','.

                String parameterValue = !header.exhausted() && header.getByte(0) == '"'
                        ? readQuotedString(header)
                        : readToken(header);
                if (parameterValue == null) return; // Expected a value.
                String replaced = parameters.put(peek, parameterValue);
                peek = null;
                if (replaced != null) return; // Unexpected duplicate parameter.
                if (!skipWhitespaceAndCommas(header) && !header.exhausted()) return; // Expected ',' or EOF.
            }
            result.add(new Challenge(schemeName, parameters));
        }
    }

    /**
     * Returns true if any commas were skipped.
     */
    private static boolean skipWhitespaceAndCommas(Buffer buffer) {
        boolean commaFound = false;
        while (!buffer.exhausted()) {
            byte b = buffer.getByte(0);
            if (b == ',') {
                buffer.readByte(); // Consume ','.
                commaFound = true;
            } else if (b == ' ' || b == '\t') {
                buffer.readByte(); // Consume space or tab.
            } else {
                break;
            }
        }
        return commaFound;
    }

    private static int skipAll(Buffer buffer, byte b) {
        int count = 0;
        while (!buffer.exhausted() && buffer.getByte(0) == b) {
            count++;
            buffer.readByte();
        }
        return count;
    }

    /**
     * Reads a double-quoted string, unescaping quoted pairs like {@code \"} to the 2nd character in
     * each sequence. Returns the unescaped string, or null if the buffer isn't prefixed with a
     * double-quoted string.
     */
    private static String readQuotedString(Buffer buffer) {
        if (buffer.readByte() != '\"') throw new IllegalArgumentException();
        Buffer result = new Buffer();
        while (true) {
            long i = buffer.indexOfElement(org.miaixz.bus.http.Builder.QUOTED_STRING_DELIMITERS);
            if (i == -1L) return null; // Unterminated quoted string.

            if (buffer.getByte(i) == '"') {
                result.write(buffer, i);
                buffer.readByte(); // Consume '"'.
                return result.readUtf8();
            }

            if (buffer.size() == i + 1L) return null; // Dangling escape.
            result.write(buffer, i);
            buffer.readByte(); // Consume '\'.
            result.write(buffer, 1L); // The escaped character.
        }
    }

    /**
     * Consumes and returns a non-empty token, terminating at special characters . Returns null if the buffer is empty or prefixed with a delimiter.
     */
    private static String readToken(Buffer buffer) {
        try {
            long tokenSize = buffer.indexOfElement(org.miaixz.bus.http.Builder.TOKEN_DELIMITERS);
            if (tokenSize == -1L) tokenSize = buffer.size();

            return tokenSize != 0L
                    ? buffer.readUtf8(tokenSize)
                    : null;
        } catch (EOFException e) {
            throw new AssertionError();
        }
    }

    private static String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }

    public static void receiveHeaders(CookieJar cookieJar, UnoUrl url, Headers headers) {
        if (cookieJar == CookieJar.NO_COOKIES) return;

        List<Cookie> cookies = Cookie.parseAll(url, headers);
        if (cookies.isEmpty()) return;

        cookieJar.saveFromResponse(url, cookies);
    }

    /**
     * Returns true if the response must have a (possibly 0-length) body. See RFC 7231.
     */
    public static boolean hasBody(Response response) {
        // HEAD requests never yield a body regardless of the response headers.
        if (response.request().method().equals("HEAD")) {
            return false;
        }

        int responseCode = response.code();
        if ((responseCode < Http.HTTP_CONTINUE || responseCode >= 200)
                && responseCode != Http.HTTP_NO_CONTENT
                && responseCode != Http.HTTP_NOT_MODIFIED) {
            return true;
        }

        // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
        // response is malformed. For best compatibility, we honor the headers.
        if (contentLength(response) != -1
                || "chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return true;
        }

        return false;
    }

    /**
     * Returns the next index in {@code input} at or after {@code pos} that contains a character from
     * {@code characters}. Returns the input length if none of the requested characters can be found.
     */
    public static int skipUntil(String input, int pos, String characters) {
        for (; pos < input.length(); pos++) {
            if (characters.indexOf(input.charAt(pos)) != -1) {
                break;
            }
        }
        return pos;
    }

    /**
     * Returns the next non-whitespace character in {@code input} that is white space. Result is
     * undefined if input contains newline characters.
     */
    public static int skipWhitespace(String input, int pos) {
        for (; pos < input.length(); pos++) {
            char c = input.charAt(pos);
            if (c != ' ' && c != '\t') {
                break;
            }
        }
        return pos;
    }

    /**
     * Returns {@code value} as a positive integer, or 0 if it is negative, or {@code defaultValue} if
     * it cannot be parsed.
     */
    public static int parseSeconds(String value, int defaultValue) {
        try {
            long seconds = Long.parseLong(value);
            if (seconds > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else if (seconds < 0) {
                return 0;
            } else {
                return (int) seconds;
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Builder newBuilder() {
        Builder result = new Builder();
        Collections.addAll(result.namesAndValues, namesAndValues);
        return result;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(namesAndValues);
    }

    /**
     * Returns the last value corresponding to the specified field, or null.
     */
    public String get(String name) {
        return get(namesAndValues, name);
    }

    /**
     * Returns the last value corresponding to the specified field parsed as an HTTP date, or null if
     * either the field is absent or cannot be parsed as a date.
     */
    public Date getDate(String name) {
        String value = get(name);
        return value != null ? org.miaixz.bus.http.Builder.parse(value) : null;
    }

    /**
     * Returns the last value corresponding to the specified field parsed as an HTTP date, or null if
     * either the field is absent or cannot be parsed as a date.
     */
    public Instant getInstant(String name) {
        Date value = getDate(name);
        return value != null ? value.toInstant() : null;
    }

    /**
     * Returns the number of field values.
     */
    public int size() {
        return namesAndValues.length / 2;
    }

    /**
     * Returns the field at {@code position}.
     */
    public String name(int index) {
        return namesAndValues[index * 2];
    }

    /**
     * Returns the value at {@code index}.
     */
    public String value(int index) {
        return namesAndValues[index * 2 + 1];
    }

    /**
     * Returns an immutable case-insensitive set of header names.
     */
    public Set<String> names() {
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            result.add(name(i));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Returns an immutable list of the header values for {@code name}.
     */
    public List<String> values(String name) {
        List<String> result = null;
        for (int i = 0, size = size(); i < size; i++) {
            if (name.equalsIgnoreCase(name(i))) {
                if (result == null) result = new ArrayList<>(2);
                result.add(value(i));
            }
        }
        return result != null
                ? Collections.unmodifiableList(result)
                : Collections.emptyList();
    }

    /**
     * Returns the number of bytes required to encode these headers using HTTP/1.1. This is also the
     * approximate size of HTTP/2 headers before they are compressed with HPACK. This value is
     * intended to be used as a metric: smaller headers are more efficient to encode and transmit.
     */
    public long byteCount() {
        // Each header name has 2 bytes of overhead for ': ' and every header value has 2 bytes of
        // overhead for '\r\n'.
        long result = namesAndValues.length * 2;

        for (int i = 0, size = namesAndValues.length; i < size; i++) {
            result += namesAndValues[i].length();
        }

        return result;
    }

    /**
     * Returns true if {@code other} is a {@code Headers} object with the same headers, with the same
     * casing, in the same order. Note that two headers instances may be <i>semantically</i> equal
     * but not equal according to this method. In particular, none of the following sets of headers
     * are equal according to this method: <pre>   {@code
     *
     *   1. Original
     *   Content-Type: text/html
     *   Content-Length: 50
     *
     *   2. Different order
     *   Content-Length: 50
     *   Content-Type: text/html
     *
     *   3. Different case
     *   content-type: text/html
     *   content-length: 50
     *
     *   4. Different values
     *   Content-Type: text/html
     *   Content-Length: 050
     * }</pre>
     * <p>
     * Applications that require semantically equal headers should convert them into a canonical form
     * before comparing them for equality.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Headers
                && Arrays.equals(((Headers) other).namesAndValues, namesAndValues);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0, size = size(); i < size; i++) {
            result.append(name(i)).append(": ").append(value(i)).append(Symbol.LF);
        }
        return result.toString();
    }

    public Map<String, List<String>> toMultimap() {
        Map<String, List<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            String name = name(i).toLowerCase(Locale.US);
            List<String> values = result.get(name);
            if (null == values) {
                values = new ArrayList<>(2);
                result.put(name, values);
            }
            values.add(value(i));
        }
        return result;
    }

    public static class Builder {
        final List<String> namesAndValues = new ArrayList<>(20);

        /**
         * Add a header line without any validation. Only appropriate for headers from the remote peer
         * or cache.
         */
        public Builder addLenient(String line) {
            int index = line.indexOf(Symbol.COLON, 1);
            if (index != -1) {
                return addLenient(line.substring(0, index), line.substring(index + 1));
            } else if (line.startsWith(Symbol.COLON)) {
                return addLenient(Normal.EMPTY, line.substring(1));
            } else {
                return addLenient(Normal.EMPTY, line);
            }
        }

        /**
         * Add an header line containing a field name, a literal colon, and a value.
         */
        public Builder add(String line) {
            int index = line.indexOf(Symbol.COLON);
            if (index == -1) {
                throw new IllegalArgumentException("Unexpected header: " + line);
            }
            return add(line.substring(0, index).trim(), line.substring(index + 1));
        }

        /**
         * Add a header with the specified name and value. Does validation of header names and values.
         */
        public Builder add(String name, String value) {
            checkName(name);
            checkValue(value, name);
            return addLenient(name, value);
        }

        /**
         * Add a header with the specified name and value. Does validation of header names, allowing
         * non-ASCII values.
         */
        public Builder addUnsafeNonAscii(String name, String value) {
            checkName(name);
            return addLenient(name, value);
        }

        /**
         * Adds all headers from an existing collection.
         */
        public Builder addAll(Headers headers) {
            for (int i = 0, size = headers.size(); i < size; i++) {
                addLenient(headers.name(i), headers.value(i));
            }

            return this;
        }

        /**
         * Add a header with the specified name and formatted date. Does validation of header names and
         * value.
         */
        public Builder add(String name, Date value) {
            if (value == null) throw new NullPointerException("value for name " + name + " == null");
            add(name, org.miaixz.bus.http.Builder.format(value));
            return this;
        }

        /**
         * Add a header with the specified name and formatted instant. Does validation of header names
         * and value.
         */
        public Builder add(String name, Instant value) {
            if (value == null) throw new NullPointerException("value for name " + name + " == null");
            return add(name, new Date(value.toEpochMilli()));
        }

        /**
         * Set a field with the specified date. If the field is not found, it is added. If the field is
         * found, the existing values are replaced.
         */
        public Builder set(String name, Date value) {
            if (value == null) throw new NullPointerException("value for name " + name + " == null");
            set(name, org.miaixz.bus.http.Builder.format(value));
            return this;
        }

        /**
         * Set a field with the specified instant. If the field is not found, it is added. If the field
         * is found, the existing values are replaced.
         */
        public Builder set(String name, Instant value) {
            if (value == null) throw new NullPointerException("value for name " + name + " == null");
            return set(name, new Date(value.toEpochMilli()));
        }

        /**
         * Add a field with the specified value without any validation. Only appropriate for headers
         * from the remote peer or cache.
         */
        Builder addLenient(String name, String value) {
            namesAndValues.add(name);
            namesAndValues.add(value.trim());
            return this;
        }

        public Builder removeAll(String name) {
            for (int i = 0; i < namesAndValues.size(); i += 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    namesAndValues.remove(i); // name
                    namesAndValues.remove(i); // value
                    i -= 2;
                }
            }
            return this;
        }

        /**
         * Set a field with the specified value. If the field is not found, it is added. If the field is
         * found, the existing values are replaced.
         */
        public Builder set(String name, String value) {
            checkName(name);
            checkValue(value, name);
            removeAll(name);
            addLenient(name, value);
            return this;
        }

        /**
         * Equivalent to {@code build().get(name)}, but potentially faster.
         */
        public String get(String name) {
            for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    return namesAndValues.get(i + 1);
                }
            }
            return null;
        }

        public Headers build() {
            return new Headers(this);
        }
    }

    /**
     * HTTP header: the name is an ASCII string, but the value can be UTF-8.
     */
    public static class Header {

        // Special header names defined in HTTP/2 spec.
        public static final ByteString PSEUDO_PREFIX = ByteString.encodeUtf8(Symbol.COLON);
        public static final ByteString RESPONSE_STATUS = ByteString.encodeUtf8(Http.RESPONSE_STATUS_UTF8);
        public static final ByteString TARGET_METHOD = ByteString.encodeUtf8(Http.TARGET_METHOD_UTF8);
        public static final ByteString TARGET_PATH = ByteString.encodeUtf8(Http.TARGET_PATH_UTF8);
        public static final ByteString TARGET_SCHEME = ByteString.encodeUtf8(Http.TARGET_SCHEME_UTF8);
        public static final ByteString TARGET_AUTHORITY = ByteString.encodeUtf8(Http.TARGET_AUTHORITY_UTF8);


        /**
         * Name in case-insensitive ASCII encoding.
         */
        public final ByteString name;
        /**
         * Value in UTF-8 encoding.
         */
        public final ByteString value;
        public final int hpackSize;

        // TODO: search for toLowerCase and consider moving logic here.
        public Header(String name, String value) {
            this(ByteString.encodeUtf8(name), ByteString.encodeUtf8(value));
        }

        public Header(ByteString name, String value) {
            this(name, ByteString.encodeUtf8(value));
        }

        public Header(ByteString name, ByteString value) {
            this.name = name;
            this.value = value;
            this.hpackSize = 32 + name.size() + value.size();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Header) {
                Header that = (Header) other;
                return this.name.equals(that.name)
                        && this.value.equals(that.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + name.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", name.utf8(), value.utf8());
        }
    }

}
