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
package org.miaixz.bus.http;

import java.io.EOFException;
import java.time.Instant;
import java.util.*;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.metric.CookieJar;
import org.miaixz.bus.http.secure.Challenge;

/**
 * Header fields of an HTTP message.
 * <p>
 * Maintains the order of header fields. Values are stored as uninterpreted strings, with leading and trailing
 * whitespace removed. Instances are immutable. It is recommended to interpret header fields through {@link Request} and
 * {@link Response}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Headers {

    /**
     * Array of header names and values.
     */
    private final String[] namesAndValues;

    /**
     * Constructor that initializes a Headers instance from a Builder.
     *
     * @param builder The Builder instance containing header names and values.
     */
    Headers(Builder builder) {
        this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
    }

    /**
     * Constructor that directly uses an array of names and values.
     *
     * @param namesAndValues The array of names and values.
     */
    private Headers(String[] namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    /**
     * Gets the last header value for the given name.
     *
     * @param namesAndValues The array of names and values.
     * @param name           The header name.
     * @return The header value, or null if it does not exist.
     */
    private static String get(String[] namesAndValues, String name) {
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return namesAndValues[i + 1];
            }
        }
        return null;
    }

    /**
     * Creates a Headers instance from an array of names and values.
     * <p>
     * Requires an even number of arguments, alternating between names and values.
     * </p>
     *
     * @param namesAndValues The array of names and values.
     * @return A Headers instance.
     * @throws NullPointerException     if namesAndValues is null.
     * @throws IllegalArgumentException if the number of arguments is odd or contains nulls.
     */
    public static Headers of(String... namesAndValues) {
        if (namesAndValues == null)
            throw new NullPointerException("namesAndValues == null");
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating header names and values");
        }

        namesAndValues = namesAndValues.clone();
        for (int i = 0; i < namesAndValues.length; i++) {
            if (namesAndValues[i] == null)
                throw new IllegalArgumentException("Headers cannot be null");
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
     * Creates a Headers instance from a map.
     *
     * @param headers The map of header names and values.
     * @return A Headers instance.
     * @throws NullPointerException     if headers is null.
     * @throws IllegalArgumentException if a name or value is null.
     */
    public static Headers of(Map<String, String> headers) {
        if (headers == null)
            throw new NullPointerException("headers == null");

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

    /**
     * Validates a header name.
     *
     * @param name The header name.
     * @throws NullPointerException     if name is null.
     * @throws IllegalArgumentException if the name is empty or contains illegal characters.
     */
    static void checkName(String name) {
        if (null == name)
            throw new NullPointerException("name == null");
        if (name.isEmpty())
            throw new IllegalArgumentException("name is empty");
        for (int i = 0, length = name.length(); i < length; i++) {
            char c = name.charAt(i);
            if (c <= '\u0020' || c >= '\u007f') {
                throw new IllegalArgumentException(
                        String.format("Unexpected char %#04x at %d in header name: %s", (int) c, i, name));
            }
        }
    }

    /**
     * Validates a header value.
     *
     * @param value The header value.
     * @param name  The header name.
     * @throws NullPointerException     if value is null.
     * @throws IllegalArgumentException if the value contains illegal characters.
     */
    static void checkValue(String value, String name) {
        if (null == value)
            throw new NullPointerException("value for name " + name + " == null");
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
            if ((c <= '\u001f' && c != Symbol.C_HT) || c >= '\u007f') {
                throw new IllegalArgumentException(
                        String.format("Unexpected char %#04x at %d in %s value: %s", (int) c, i, name, value));
            }
        }
    }

    /**
     * Gets the Content-Length of a response.
     *
     * @param response The response.
     * @return The Content-Length value, or -1 if invalid.
     */
    public static long contentLength(Response response) {
        return contentLength(response.headers());
    }

    /**
     * Gets the Content-Length from headers.
     *
     * @param headers The headers.
     * @return The Content-Length value, or -1 if invalid.
     */
    public static long contentLength(Headers headers) {
        return stringToLong(headers.get("Content-Length"));
    }

    /**
     * Converts a string to a long.
     *
     * @param s The string.
     * @return The long value, or -1 if invalid.
     */
    private static long stringToLong(String s) {
        if (s == null)
            return -1;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Checks if the Vary header matches.
     *
     * @param cachedResponse The cached response.
     * @param cachedRequest  The cached request.
     * @param newRequest     The new request.
     * @return true if the Vary header matches.
     */
    public static boolean varyMatches(Response cachedResponse, Headers cachedRequest, Request newRequest) {
        for (String field : varyFields(cachedResponse)) {
            if (!Objects.equals(cachedRequest.values(field), newRequest.headers(field)))
                return false;
        }
        return true;
    }

    /**
     * Checks for the presence of a Vary: * header.
     *
     * @param response The response.
     * @return true if Vary: * is present.
     */
    public static boolean hasVaryAll(Response response) {
        return hasVaryAll(response.headers());
    }

    /**
     * Checks for the presence of a Vary: * header.
     *
     * @param responseHeaders The response headers.
     * @return true if Vary: * is present.
     */
    public static boolean hasVaryAll(Headers responseHeaders) {
        return varyFields(responseHeaders).contains(Symbol.STAR);
    }

    /**
     * Gets the Vary fields.
     *
     * @param response The response.
     * @return A set of Vary fields.
     */
    private static Set<String> varyFields(Response response) {
        return varyFields(response.headers());
    }

    /**
     * Gets the set of Vary fields.
     *
     * @param responseHeaders The response headers.
     * @return A set of Vary fields.
     */
    public static Set<String> varyFields(Headers responseHeaders) {
        Set<String> result = Collections.emptySet();
        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
            if (!"Vary".equalsIgnoreCase(responseHeaders.name(i)))
                continue;

            String value = responseHeaders.value(i);
            if (result.isEmpty()) {
                result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            }
            for (String varyField : value.split(Symbol.COMMA)) {
                result.add(varyField.trim());
            }
        }
        return result;
    }

    /**
     * Gets the request headers that affect the response body.
     *
     * @param response The response.
     * @return The headers that affect the response body.
     */
    public static Headers varyHeaders(Response response) {
        Headers requestHeaders = response.networkResponse().request().headers();
        Headers responseHeaders = response.headers();
        return varyHeaders(requestHeaders, responseHeaders);
    }

    /**
     * Gets the request headers that affect the response body.
     *
     * @param requestHeaders  The request headers.
     * @param responseHeaders The response headers.
     * @return The headers that affect the response body.
     */
    public static Headers varyHeaders(Headers requestHeaders, Headers responseHeaders) {
        Set<String> varyFields = varyFields(responseHeaders);
        if (varyFields.isEmpty())
            return org.miaixz.bus.http.Builder.EMPTY_HEADERS;

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
     * Parses RFC 7235 challenges.
     *
     * @param responseHeaders The response headers.
     * @param headerName      The header name.
     * @return A list of authentication challenges.
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

    /**
     * Parses a challenge header.
     *
     * @param result The list to add challenges to.
     * @param header The header buffer.
     */
    private static void parseChallengeHeader(List<Challenge> result, Buffer header) {
        String peek = null;

        while (true) {
            if (peek == null) {
                skipWhitespaceAndCommas(header);
                peek = readToken(header);
                if (peek == null)
                    return;
            }

            String schemeName = peek;

            boolean commaPrefixed = skipWhitespaceAndCommas(header);
            peek = readToken(header);
            if (peek == null) {
                if (!header.exhausted())
                    return;
                result.add(new Challenge(schemeName, Collections.emptyMap()));
                return;
            }

            int eqCount = skipAll(header, (byte) Symbol.C_EQUAL);
            boolean commaSuffixed = skipWhitespaceAndCommas(header);

            if (!commaPrefixed && (commaSuffixed || header.exhausted())) {
                result.add(
                        new Challenge(schemeName,
                                Collections.singletonMap(null, peek + repeat(Symbol.C_EQUAL, eqCount))));
                peek = null;
                continue;
            }

            Map<String, String> parameters = new LinkedHashMap<>();
            eqCount += skipAll(header, (byte) Symbol.C_EQUAL);
            while (true) {
                if (peek == null) {
                    peek = readToken(header);
                    if (skipWhitespaceAndCommas(header))
                        break;
                    eqCount = skipAll(header, (byte) Symbol.C_EQUAL);
                }
                if (eqCount == 0)
                    break;
                if (eqCount > 1)
                    return;
                if (skipWhitespaceAndCommas(header))
                    return;

                String parameterValue = !header.exhausted() && header.getByte(0) == '"' ? readQuotedString(header)
                        : readToken(header);
                if (parameterValue == null)
                    return;
                String replaced = parameters.put(peek, parameterValue);
                peek = null;
                if (replaced != null)
                    return;
                if (!skipWhitespaceAndCommas(header) && !header.exhausted())
                    return;
            }
            result.add(new Challenge(schemeName, parameters));
        }
    }

    /**
     * Skips whitespace and commas.
     *
     * @param buffer The buffer.
     * @return true if a comma was skipped.
     */
    private static boolean skipWhitespaceAndCommas(Buffer buffer) {
        boolean commaFound = false;
        while (!buffer.exhausted()) {
            byte b = buffer.getByte(0);
            if (b == Symbol.C_COMMA) {
                buffer.readByte();
                commaFound = true;
            } else if (b == Symbol.C_SPACE || b == '\t') {
                buffer.readByte();
            } else {
                break;
            }
        }
        return commaFound;
    }

    /**
     * Skips all occurrences of a specific byte.
     *
     * @param buffer The buffer.
     * @param b      The byte to skip.
     * @return The number of bytes skipped.
     */
    private static int skipAll(Buffer buffer, byte b) {
        int count = 0;
        while (!buffer.exhausted() && buffer.getByte(0) == b) {
            count++;
            buffer.readByte();
        }
        return count;
    }

    /**
     * Reads a quoted string.
     *
     * @param buffer The buffer.
     * @return The decoded string, or null if invalid.
     * @throws IllegalArgumentException if the string format is invalid.
     */
    private static String readQuotedString(Buffer buffer) {
        if (buffer.readByte() != '\"')
            throw new IllegalArgumentException();
        Buffer result = new Buffer();
        while (true) {
            long i = buffer.indexOfElement(org.miaixz.bus.http.Builder.QUOTED_STRING_DELIMITERS);
            if (i == -1L)
                return null;

            if (buffer.getByte(i) == '"') {
                result.write(buffer, i);
                buffer.readByte();
                return result.readUtf8();
            }

            if (buffer.size() == i + 1L)
                return null;
            result.write(buffer, i);
            buffer.readByte();
            result.write(buffer, 1L);
        }
    }

    /**
     * Reads a token.
     *
     * @param buffer The buffer.
     * @return The token string, or null if invalid.
     */
    private static String readToken(Buffer buffer) {
        try {
            long tokenSize = buffer.indexOfElement(org.miaixz.bus.http.Builder.TOKEN_DELIMITERS);
            if (tokenSize == -1L)
                tokenSize = buffer.size();

            return tokenSize != 0L ? buffer.readUtf8(tokenSize) : null;
        } catch (EOFException e) {
            throw new AssertionError();
        }
    }

    /**
     * Repeats a character.
     *
     * @param c     The character to repeat.
     * @param count The number of times to repeat.
     * @return The repeated string.
     */
    private static String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }

    /**
     * Handles received Cookie headers.
     *
     * @param cookieJar The cookie manager.
     * @param url       The URL.
     * @param headers   The headers.
     */
    public static void receiveHeaders(CookieJar cookieJar, UnoUrl url, Headers headers) {
        if (cookieJar == CookieJar.NO_COOKIES)
            return;

        List<Cookie> cookies = Cookie.parseAll(url, headers);
        if (cookies.isEmpty())
            return;

        cookieJar.saveFromResponse(url, cookies);
    }

    /**
     * Checks if the response includes a body.
     *
     * @param response The response.
     * @return true if the response includes a body.
     */
    public static boolean hasBody(Response response) {
        if (response.request().method().equals("HEAD")) {
            return false;
        }

        int responseCode = response.code();
        if ((responseCode < HTTP.HTTP_CONTINUE || responseCode >= 200) && responseCode != HTTP.HTTP_NO_CONTENT
                && responseCode != HTTP.HTTP_NOT_MODIFIED) {
            return true;
        }

        if (contentLength(response) != -1 || "chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return true;
        }

        return false;
    }

    /**
     * Skips until a character from the given set is found.
     *
     * @param input      The input string.
     * @param pos        The starting position.
     * @param characters The set of target characters.
     * @return The position of the target character.
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
     * Skips whitespace characters.
     *
     * @param input The input string.
     * @param pos   The starting position.
     * @return The position of the first non-whitespace character.
     */
    public static int skipWhitespace(String input, int pos) {
        for (; pos < input.length(); pos++) {
            char c = input.charAt(pos);
            if (c != Symbol.C_SPACE && c != '\t') {
                break;
            }
        }
        return pos;
    }

    /**
     * Parses seconds from a string.
     *
     * @param value        The string value.
     * @param defaultValue The default value.
     * @return The number of seconds.
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

    /**
     * Creates a new Builder instance.
     *
     * @return A Builder instance.
     */
    public Builder newBuilder() {
        Builder result = new Builder();
        Collections.addAll(result.namesAndValues, namesAndValues);
        return result;
    }

    /**
     * Calculates the hash code.
     *
     * @return The hash code value.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(namesAndValues);
    }

    /**
     * Gets the last value for the given header name.
     *
     * @param name The header name.
     * @return The header value, or null if it does not exist.
     */
    public String get(String name) {
        return get(namesAndValues, name);
    }

    /**
     * Gets the date value for the given header name.
     *
     * @param name The header name.
     * @return The date value, or null if invalid.
     */
    public Date getDate(String name) {
        String value = get(name);
        return value != null ? org.miaixz.bus.http.Builder.parse(value) : null;
    }

    /**
     * Gets the Instant value for the given header name.
     *
     * @param name The header name.
     * @return The Instant value, or null if invalid.
     */
    public Instant getInstant(String name) {
        Date value = getDate(name);
        return value != null ? value.toInstant() : null;
    }

    /**
     * Gets the number of headers.
     *
     * @return The number of headers.
     */
    public int size() {
        return namesAndValues.length / 2;
    }

    /**
     * Gets the header name at the specified index.
     *
     * @param index The index.
     * @return The header name.
     */
    public String name(int index) {
        return namesAndValues[index * 2];
    }

    /**
     * Gets the header value at the specified index.
     *
     * @param index The index.
     * @return The header value.
     */
    public String value(int index) {
        return namesAndValues[index * 2 + 1];
    }

    /**
     * Gets the set of header names.
     *
     * @return An unmodifiable set of header names.
     */
    public Set<String> names() {
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            result.add(name(i));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Gets the list of values for the given header name.
     *
     * @param name The header name.
     * @return An unmodifiable list of header values.
     */
    public List<String> values(String name) {
        List<String> result = null;
        for (int i = 0, size = size(); i < size; i++) {
            if (name.equalsIgnoreCase(name(i))) {
                if (result == null)
                    result = new ArrayList<>(2);
                result.add(value(i));
            }
        }
        return result != null ? Collections.unmodifiableList(result) : Collections.emptyList();
    }

    /**
     * Gets the encoded byte count of the headers.
     *
     * @return The encoded byte count.
     */
    public long byteCount() {
        long result = namesAndValues.length * 2;

        for (int i = 0, size = namesAndValues.length; i < size; i++) {
            result += namesAndValues[i].length();
        }

        return result;
    }

    /**
     * Compares two Headers objects for equality.
     *
     * @param other The other object.
     * @return true if the headers are exactly equal.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Headers && Arrays.equals(((Headers) other).namesAndValues, namesAndValues);
    }

    /**
     * Returns the string representation of the headers.
     *
     * @return The header string.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0, size = size(); i < size; i++) {
            result.append(name(i)).append(": ").append(value(i)).append(Symbol.LF);
        }
        return result.toString();
    }

    /**
     * Converts to a multi-valued map.
     *
     * @return A map of header names to lists of values.
     */
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

    /**
     * Headers Builder.
     */
    public static class Builder {

        /**
         * List of header names and values.
         */
        final List<String> namesAndValues = new ArrayList<>(20);

        /**
         * Adds an unvalidated header line.
         *
         * @param line The header line.
         * @return The current Builder instance.
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
         * Adds a header line.
         *
         * @param line The header line (format: name: value).
         * @return The current Builder instance.
         * @throws IllegalArgumentException if the format is invalid.
         */
        public Builder add(String line) {
            int index = line.indexOf(Symbol.COLON);
            if (index == -1) {
                throw new IllegalArgumentException("Unexpected header: " + line);
            }
            return add(line.substring(0, index).trim(), line.substring(index + 1));
        }

        /**
         * Adds a validated header.
         *
         * @param name  The header name.
         * @param value The header value.
         * @return The current Builder instance.
         * @throws NullPointerException     if name or value is null.
         * @throws IllegalArgumentException if the name or value is invalid.
         */
        public Builder add(String name, String value) {
            checkName(name);
            checkValue(value, name);
            return addLenient(name, value);
        }

        /**
         * Adds a non-ASCII header.
         *
         * @param name  The header name.
         * @param value The header value.
         * @return The current Builder instance.
         * @throws NullPointerException     if name is null.
         * @throws IllegalArgumentException if the name is invalid.
         */
        public Builder addUnsafeNonAscii(String name, String value) {
            checkName(name);
            return addLenient(name, value);
        }

        /**
         * Adds all headers from a Headers instance.
         *
         * @param headers The Headers instance.
         * @return The current Builder instance.
         */
        public Builder addAll(Headers headers) {
            for (int i = 0, size = headers.size(); i < size; i++) {
                addLenient(headers.name(i), headers.value(i));
            }
            return this;
        }

        /**
         * Adds a date header.
         *
         * @param name  The header name.
         * @param value The date value.
         * @return The current Builder instance.
         * @throws NullPointerException if name or value is null.
         */
        public Builder add(String name, Date value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            add(name, org.miaixz.bus.http.Builder.format(value));
            return this;
        }

        /**
         * Adds an Instant header.
         *
         * @param name  The header name.
         * @param value The Instant value.
         * @return The current Builder instance.
         * @throws NullPointerException if name or value is null.
         */
        public Builder add(String name, Instant value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            return add(name, new Date(value.toEpochMilli()));
        }

        /**
         * Sets a date header.
         *
         * @param name  The header name.
         * @param value The date value.
         * @return The current Builder instance.
         * @throws NullPointerException if name or value is null.
         */
        public Builder set(String name, Date value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            set(name, org.miaixz.bus.http.Builder.format(value));
            return this;
        }

        /**
         * Sets an Instant header.
         *
         * @param name  The header name.
         * @param value The Instant value.
         * @return The current Builder instance.
         * @throws NullPointerException if name or value is null.
         */
        public Builder set(String name, Instant value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            return set(name, new Date(value.toEpochMilli()));
        }

        /**
         * Adds an unvalidated header.
         *
         * @param name  The header name.
         * @param value The header value.
         * @return The current Builder instance.
         */
        Builder addLenient(String name, String value) {
            namesAndValues.add(name);
            namesAndValues.add(value.trim());
            return this;
        }

        /**
         * Removes all headers with the given name.
         *
         * @param name The header name.
         * @return The current Builder instance.
         */
        public Builder removeAll(String name) {
            for (int i = 0; i < namesAndValues.size(); i += 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    namesAndValues.remove(i);
                    namesAndValues.remove(i);
                    i -= 2;
                }
            }
            return this;
        }

        /**
         * Sets a header.
         *
         * @param name  The header name.
         * @param value The header value.
         * @return The current Builder instance.
         * @throws NullPointerException     if name or value is null.
         * @throws IllegalArgumentException if the name or value is invalid.
         */
        public Builder set(String name, String value) {
            checkName(name);
            checkValue(value, name);
            removeAll(name);
            addLenient(name, value);
            return this;
        }

        /**
         * Gets the last value for the given header name.
         *
         * @param name The header name.
         * @return The header value, or null if it does not exist.
         */
        public String get(String name) {
            for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    return namesAndValues.get(i + 1);
                }
            }
            return null;
        }

        /**
         * Builds a Headers instance.
         *
         * @return A Headers instance.
         */
        public Headers build() {
            return new Headers(this);
        }
    }

}
