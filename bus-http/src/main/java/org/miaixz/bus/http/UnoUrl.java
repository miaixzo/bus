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

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.net.Protocol;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * A Uniform Resource Locator (URL) for HTTP or HTTPS.
 * <p>
 * This class provides a robust and exception-free way to build, parse, and handle URLs for HTTP and HTTPS protocols. It
 * avoids checked exceptions by returning null or throwing an {@link IllegalArgumentException} for invalid URLs.
 * Instances are immutable and support encoding and decoding of URL components.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class UnoUrl {

    /**
     * The character set for encoding usernames.
     */
    public static final String USERNAME_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    /**
     * The character set for encoding passwords.
     */
    public static final String PASSWORD_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    /**
     * The character set for encoding path segments.
     */
    public static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";
    /**
     * The character set for encoding URI path segments.
     */
    public static final String PATH_SEGMENT_ENCODE_SET_URI = Symbol.BRACKET;
    /**
     * The character set for encoding query parameters.
     */
    public static final String QUERY_ENCODE_SET = " \"'<>#";
    /**
     * The character set for re-encoding query components.
     */
    public static final String QUERY_COMPONENT_REENCODE_SET = " \"'<>#&=";
    /**
     * The character set for encoding query components.
     */
    public static final String QUERY_COMPONENT_ENCODE_SET = " !\"#$&'(),/:;<=>?@[]\\^`{|}~";
    /**
     * The character set for encoding URI query components.
     */
    public static final String QUERY_COMPONENT_ENCODE_SET_URI = "\\^`{|}";
    /**
     * The character set for encoding form data.
     */
    public static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
    /**
     * The character set for encoding fragments.
     */
    public static final String FRAGMENT_ENCODE_SET = Normal.EMPTY;
    /**
     * The character set for encoding URI fragments.
     */
    public static final String FRAGMENT_ENCODE_SET_URI = " \"#<>\\^`{|}";
    /**
     * The URL scheme (http or https).
     */
    final String scheme;
    /**
     * The canonicalized hostname.
     */
    final String host;
    /**
     * The port number (80, 443, or user-specified, in the range 1-65535).
     */
    final int port;
    /**
     * The decoded username.
     */
    private final String username;
    /**
     * The decoded password.
     */
    private final String password;
    /**
     * The list of decoded path segments.
     */
    private final List<String> pathSegments;
    /**
     * The list of decoded query parameter names and values.
     */
    private final List<String> queryNamesAndValues;
    /**
     * The decoded fragment.
     */
    private final String fragment;
    /**
     * The canonicalized URL string.
     */
    private final String url;

    /**
     * Constructs a new {@code UnoUrl} instance from a builder.
     *
     * @param builder The builder instance containing all URL components.
     */
    UnoUrl(Builder builder) {
        this.scheme = builder.scheme;
        this.username = percentDecode(builder.encodedUsername, false);
        this.password = percentDecode(builder.encodedPassword, false);
        this.host = builder.host;
        this.port = builder.effectivePort();
        this.pathSegments = percentDecode(builder.encodedPathSegments, false);
        this.queryNamesAndValues = null != builder.encodedQueryNamesAndValues
                ? percentDecode(builder.encodedQueryNamesAndValues, true)
                : null;
        this.fragment = null != builder.encodedFragment ? percentDecode(builder.encodedFragment, false) : null;
        this.url = builder.toString();
    }

    /**
     * Returns the default port for a given scheme.
     * <p>
     * Returns 80 for "http", 443 for "https", and -1 for other schemes.
     * </p>
     *
     * @param scheme The scheme name.
     * @return The default port number.
     */
    public static int defaultPort(String scheme) {
        if (Protocol.HTTP.name.equals(scheme)) {
            return PORT._80;
        } else if (Protocol.HTTPS.name.equals(scheme)) {
            return PORT._443;
        } else {
            return -1;
        }
    }

    /**
     * Appends a list of path segments to a string builder.
     *
     * @param out          The output string builder.
     * @param pathSegments The list of path segments.
     */
    static void pathSegmentsToString(StringBuilder out, List<String> pathSegments) {
        for (int i = 0, size = pathSegments.size(); i < size; i++) {
            out.append(Symbol.C_SLASH);
            out.append(pathSegments.get(i));
        }
    }

    /**
     * Appends a list of query parameter names and values to a string builder.
     *
     * @param out            The output string builder.
     * @param namesAndValues The list of names and values.
     */
    static void namesAndValuesToQueryString(StringBuilder out, List<String> namesAndValues) {
        for (int i = 0, size = namesAndValues.size(); i < size; i += 2) {
            String name = namesAndValues.get(i);
            String value = namesAndValues.get(i + 1);
            if (i > 0)
                out.append(Symbol.C_AND);
            out.append(name);
            if (null != value) {
                out.append(Symbol.C_EQUAL);
                out.append(value);
            }
        }
    }

    /**
     * Parses an encoded query string into a list of names and values.
     * <p>
     * For example, parsing "subject=math&easy&problem=5-2=3" yields ["subject", "math", "easy", null, "problem",
     * "5-2=3"].
     * </p>
     *
     * @param encodedQuery The encoded query string.
     * @return A list of names and values.
     */
    static List<String> queryStringToNamesAndValues(String encodedQuery) {
        List<String> result = new ArrayList<>();
        for (int pos = 0; pos <= encodedQuery.length();) {
            int ampersandOffset = encodedQuery.indexOf(Symbol.C_AND, pos);
            if (ampersandOffset == -1)
                ampersandOffset = encodedQuery.length();

            int equalsOffset = encodedQuery.indexOf(Symbol.C_EQUAL, pos);
            if (equalsOffset == -1 || equalsOffset > ampersandOffset) {
                result.add(encodedQuery.substring(pos, ampersandOffset));
                result.add(null); // No value for this name.
            } else {
                result.add(encodedQuery.substring(pos, equalsOffset));
                result.add(encodedQuery.substring(equalsOffset + 1, ampersandOffset));
            }
            pos = ampersandOffset + 1;
        }
        return result;
    }

    /**
     * Parses a URL string into a {@code UnoUrl} instance.
     * <p>
     * Returns a {@code UnoUrl} instance if the URL is well-formed, or null otherwise.
     * </p>
     *
     * @param url The URL string.
     * @return A {@code UnoUrl} instance or null.
     */
    public static UnoUrl parse(String url) {
        try {
            return get(url);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Builds a {@code UnoUrl} instance from a URL string.
     * <p>
     * Throws an {@link IllegalArgumentException} if the URL is not well-formed.
     * </p>
     *
     * @param url The URL string.
     * @return A {@code UnoUrl} instance.
     * @throws IllegalArgumentException if the URL is not well-formed.
     */
    public static UnoUrl get(String url) {
        return new Builder().parse(null, url).build();
    }

    /**
     * Builds a {@code UnoUrl} instance from a {@link URL} object.
     * <p>
     * Only supports http and https schemes. Returns null for other schemes.
     * </p>
     *
     * @param url The {@link URL} object.
     * @return A {@code UnoUrl} instance or null.
     */
    public static UnoUrl get(URL url) {
        return parse(url.toString());
    }

    /**
     * Builds a {@code UnoUrl} instance from a {@link URI} object.
     *
     * @param uri The {@link URI} object.
     * @return A {@code UnoUrl} instance or null.
     */
    public static UnoUrl get(URI uri) {
        return parse(uri.toString());
    }

    /**
     * Decodes a percent-encoded string.
     *
     * @param encoded     The encoded string.
     * @param plusIsSpace Whether to decode plus signs as spaces.
     * @return The decoded string.
     */
    public static String percentDecode(String encoded, boolean plusIsSpace) {
        return percentDecode(encoded, 0, encoded.length(), plusIsSpace);
    }

    /**
     * Decodes a percent-encoded string within a specified range.
     *
     * @param encoded     The encoded string.
     * @param pos         The starting position.
     * @param limit       The ending position.
     * @param plusIsSpace Whether to decode plus signs as spaces.
     * @return The decoded string.
     */
    public static String percentDecode(String encoded, int pos, int limit, boolean plusIsSpace) {
        for (int i = pos; i < limit; i++) {
            char c = encoded.charAt(i);
            if (c == Symbol.C_PERCENT || (c == Symbol.C_PLUS && plusIsSpace)) {
                // Slow path: the character at i requires decoding.
                Buffer out = new Buffer();
                out.writeUtf8(encoded, pos, i);
                percentDecode(out, encoded, i, limit, plusIsSpace);
                return out.readUtf8();
            }
        }
        // Fast path: no characters required decoding.
        return encoded.substring(pos, limit);
    }

    /**
     * Decodes a percent-encoded string into a buffer.
     *
     * @param out         The output buffer.
     * @param encoded     The encoded string.
     * @param pos         The starting position.
     * @param limit       The ending position.
     * @param plusIsSpace Whether to decode plus signs as spaces.
     */
    public static void percentDecode(Buffer out, String encoded, int pos, int limit, boolean plusIsSpace) {
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = encoded.codePointAt(i);
            if (codePoint == Symbol.C_PERCENT && i + 2 < limit) {
                int d1 = org.miaixz.bus.http.Builder.decodeHexDigit(encoded.charAt(i + 1));
                int d2 = org.miaixz.bus.http.Builder.decodeHexDigit(encoded.charAt(i + 2));
                if (d1 != -1 && d2 != -1) {
                    out.writeByte((d1 << 4) + d2);
                    i += 2;
                    continue;
                }
            } else if (codePoint == Symbol.C_PLUS && plusIsSpace) {
                out.writeByte(Symbol.C_SPACE);
                continue;
            }
            out.writeUtf8CodePoint(codePoint);
        }
    }

    /**
     * Checks if a string is percent-encoded.
     *
     * @param encoded The encoded string.
     * @param pos     The starting position.
     * @param limit   The ending position.
     * @return {@code true} if the string is percent-encoded.
     */
    public static boolean percentEncoded(String encoded, int pos, int limit) {
        return pos + 2 < limit && encoded.charAt(pos) == Symbol.C_PERCENT
                && org.miaixz.bus.http.Builder.decodeHexDigit(encoded.charAt(pos + 1)) != -1
                && org.miaixz.bus.http.Builder.decodeHexDigit(encoded.charAt(pos + 2)) != -1;
    }

    /**
     * Canonicalizes a string by encoding characters from a given set.
     *
     * @param input          The input string.
     * @param pos            The starting position.
     * @param limit          The ending position.
     * @param encodeSet      The set of characters to encode.
     * @param alreadyEncoded Whether the string is already encoded.
     * @param strict         Whether to use strict encoding.
     * @param plusIsSpace    Whether to encode plus signs as spaces.
     * @param asciiOnly      Whether to limit to ASCII characters.
     * @param charset        The character set to use (null for UTF-8).
     * @return The canonicalized string.
     */
    public static String canonicalize(
            String input,
            int pos,
            int limit,
            String encodeSet,
            boolean alreadyEncoded,
            boolean strict,
            boolean plusIsSpace,
            boolean asciiOnly,
            java.nio.charset.Charset charset) {
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (codePoint < 0x20 || codePoint == 0x7f || (codePoint >= 0x80 && asciiOnly)
                    || encodeSet.indexOf(codePoint) != -1
                    || (codePoint == Symbol.C_PERCENT
                            && (!alreadyEncoded || strict && !percentEncoded(input, i, limit)))
                    || (codePoint == Symbol.C_PLUS && plusIsSpace)) {
                // Slow path: a character at i requires encoding.
                Buffer out = new Buffer();
                out.writeUtf8(input, pos, i);
                canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, charset);
                return out.readUtf8();
            }
        }
        // Fast path: no characters required encoding.
        return input.substring(pos, limit);
    }

    /**
     * Canonicalizes a string into a buffer.
     *
     * @param out            The output buffer.
     * @param input          The input string.
     * @param pos            The starting position.
     * @param limit          The ending position.
     * @param encodeSet      The set of characters to encode.
     * @param alreadyEncoded Whether the string is already encoded.
     * @param strict         Whether to use strict encoding.
     * @param plusIsSpace    Whether to encode plus signs as spaces.
     * @param asciiOnly      Whether to limit to ASCII characters.
     * @param charset        The character set to use (null for UTF-8).
     */
    public static void canonicalize(
            Buffer out,
            String input,
            int pos,
            int limit,
            String encodeSet,
            boolean alreadyEncoded,
            boolean strict,
            boolean plusIsSpace,
            boolean asciiOnly,
            java.nio.charset.Charset charset) {
        Buffer encodedCharBuffer = null; // Lazily allocated.
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (alreadyEncoded && (codePoint == Symbol.C_HT || codePoint == Symbol.C_LF || codePoint == '\f'
                    || codePoint == Symbol.C_CR)) {
                // Skip encoding for whitespace characters in already-encoded strings.
            } else if (codePoint == Symbol.C_PLUS && plusIsSpace) {
                out.writeUtf8(alreadyEncoded ? Symbol.PLUS : "%2B");
            } else if (codePoint < 0x20 || codePoint == 0x7f || (codePoint >= 0x80 && asciiOnly)
                    || encodeSet.indexOf(codePoint) != -1 || (codePoint == Symbol.C_PERCENT
                            && (!alreadyEncoded || strict && !percentEncoded(input, i, limit)))) {
                // Percent-encode this character.
                if (null == encodedCharBuffer) {
                    encodedCharBuffer = new Buffer();
                }

                if (null == charset || charset.equals(Charset.UTF_8)) {
                    encodedCharBuffer.writeUtf8CodePoint(codePoint);
                } else {
                    encodedCharBuffer.writeString(input, i, i + Character.charCount(codePoint), charset);
                }

                while (!encodedCharBuffer.exhausted()) {
                    int b = encodedCharBuffer.readByte() & 0xff;
                    out.writeByte(Symbol.C_PERCENT);
                    out.writeByte(Normal.DIGITS_16_UPPER[(b >> 4) & 0xf]);
                    out.writeByte(Normal.DIGITS_16_UPPER[b & 0xf]);
                }
            } else {
                // This character doesn't need encoding. Just copy it over.
                out.writeUtf8CodePoint(codePoint);
            }
        }
    }

    /**
     * Canonicalizes a string with default UTF-8 encoding.
     *
     * @param input          The input string.
     * @param encodeSet      The set of characters to encode.
     * @param alreadyEncoded Whether the string is already encoded.
     * @param strict         Whether to use strict encoding.
     * @param plusIsSpace    Whether to encode plus signs as spaces.
     * @param asciiOnly      Whether to limit to ASCII characters.
     * @param charset        The character set to use (null for UTF-8).
     * @return The canonicalized string.
     */
    public static String canonicalize(
            String input,
            String encodeSet,
            boolean alreadyEncoded,
            boolean strict,
            boolean plusIsSpace,
            boolean asciiOnly,
            java.nio.charset.Charset charset) {
        return canonicalize(
                input,
                0,
                input.length(),
                encodeSet,
                alreadyEncoded,
                strict,
                plusIsSpace,
                asciiOnly,
                charset);
    }

    /**
     * Canonicalizes a string with default UTF-8 encoding.
     *
     * @param input          The input string.
     * @param encodeSet      The set of characters to encode.
     * @param alreadyEncoded Whether the string is already encoded.
     * @param strict         Whether to use strict encoding.
     * @param plusIsSpace    Whether to encode plus signs as spaces.
     * @param asciiOnly      Whether to limit to ASCII characters.
     * @return The canonicalized string.
     */
    public static String canonicalize(
            String input,
            String encodeSet,
            boolean alreadyEncoded,
            boolean strict,
            boolean plusIsSpace,
            boolean asciiOnly) {
        return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, null);
    }

    /**
     * Converts this {@code UnoUrl} to a {@link java.net.URL} object.
     *
     * @return The {@link URL} object.
     * @throws RuntimeException if the URL is malformed.
     */
    public URL url() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // Should not happen.
        }
    }

    /**
     * Converts this {@code UnoUrl} to a {@link java.net.URI} object.
     * <p>
     * Note: {@link URI} is more strict than {@code UnoUrl} and may escape or remove certain characters (like whitespace
     * in fragments). It is recommended to avoid using {@link URI} directly to prevent differences in server
     * interpretation.
     * </p>
     *
     * @return The {@link URI} object.
     * @throws RuntimeException if the URI syntax is invalid.
     */
    public URI uri() {
        String uri = newBuilder().reencodeForUri().toString();
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            // If the URI is invalid, try to strip illegal characters and create it again.
            try {
                String stripped = uri.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F\\p{javaWhitespace}]", Normal.EMPTY);
                return URI.create(stripped);
            } catch (Exception e1) {
                throw new RuntimeException(e); // Should not happen.
            }
        }
    }

    /**
     * Returns the scheme of this URL.
     *
     * @return The scheme (http or https).
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Returns {@code true} if this URL uses the HTTPS scheme.
     *
     * @return {@code true} if the scheme is HTTPS.
     */
    public boolean isHttps() {
        return Protocol.isHttps(scheme);
    }

    /**
     * Returns the encoded username.
     *
     * @return The encoded username, or an empty string if not set.
     */
    public String encodedUsername() {
        if (username.isEmpty())
            return Normal.EMPTY;
        int usernameStart = scheme.length() + 3;
        int usernameEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, usernameStart, url.length(), ":@");
        return url.substring(usernameStart, usernameEnd);
    }

    /**
     * Returns the decoded username.
     *
     * @return The decoded username.
     */
    public String username() {
        return username;
    }

    /**
     * Returns the encoded password.
     *
     * @return The encoded password, or an empty string if not set.
     */
    public String encodedPassword() {
        if (password.isEmpty())
            return Normal.EMPTY;
        int passwordStart = url.indexOf(Symbol.C_COLON, scheme.length() + 3) + 1;
        int passwordEnd = url.indexOf(Symbol.C_AT);
        return url.substring(passwordStart, passwordEnd);
    }

    /**
     * Returns the decoded password.
     *
     * @return The decoded password.
     */
    public String password() {
        return password;
    }

    /**
     * Returns the hostname.
     *
     * @return The hostname, which can be a regular hostname, an IPv4 address, an IPv6 address, or an encoded IDN.
     */
    public String host() {
        return host;
    }

    /**
     * Returns the port number.
     *
     * @return The port number.
     */
    public int port() {
        return port;
    }

    /**
     * Returns the number of path segments.
     *
     * @return The number of path segments.
     */
    public int pathSize() {
        return pathSegments.size();
    }

    /**
     * Returns the encoded path.
     *
     * @return The encoded path of the URL.
     */
    public String encodedPath() {
        int pathStart = url.indexOf(Symbol.C_SLASH, scheme.length() + 3);
        int pathEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, pathStart, url.length(), "?#");
        return url.substring(pathStart, pathEnd);
    }

    /**
     * Returns the list of encoded path segments.
     *
     * @return The list of encoded path segments.
     */
    public List<String> encodedPathSegments() {
        int pathStart = url.indexOf(Symbol.C_SLASH, scheme.length() + 3);
        int pathEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, pathStart, url.length(), "?#");
        List<String> result = new ArrayList<>();
        for (int i = pathStart; i < pathEnd;) {
            i++; // Skip the leading slash.
            int segmentEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, i, pathEnd, Symbol.C_SLASH);
            result.add(url.substring(i, segmentEnd));
            i = segmentEnd;
        }
        return result;
    }

    /**
     * Returns the list of decoded path segments.
     *
     * @return The list of decoded path segments.
     */
    public List<String> pathSegments() {
        return pathSegments;
    }

    /**
     * Returns the encoded query string.
     *
     * @return The encoded query string, or null if no query is present.
     */
    public String encodedQuery() {
        if (null == queryNamesAndValues)
            return null;
        int queryStart = url.indexOf(Symbol.C_QUESTION_MARK) + 1;
        int queryEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, queryStart, url.length(), Symbol.C_HASH);
        return url.substring(queryStart, queryEnd);
    }

    /**
     * Returns the decoded query string.
     *
     * @return The decoded query string, or null if no query is present.
     */
    public String query() {
        if (null == queryNamesAndValues)
            return null;
        StringBuilder result = new StringBuilder();
        namesAndValuesToQueryString(result, queryNamesAndValues);
        return result.toString();
    }

    /**
     * Returns the number of query parameters.
     *
     * @return The number of query parameters.
     */
    public int querySize() {
        return null != queryNamesAndValues ? queryNamesAndValues.size() / 2 : 0;
    }

    /**
     * Returns the first value for the given query parameter name.
     *
     * @param name The name of the query parameter.
     * @return The value of the query parameter, or null if not found.
     */
    public String queryParameter(String name) {
        if (null == queryNamesAndValues)
            return null;
        for (int i = 0, size = queryNamesAndValues.size(); i < size; i += 2) {
            if (name.equals(queryNamesAndValues.get(i))) {
                return queryNamesAndValues.get(i + 1);
            }
        }
        return null;
    }

    /**
     * Returns a set of all query parameter names.
     *
     * @return An unmodifiable set of query parameter names.
     */
    public Set<String> queryParameterNames() {
        if (null == queryNamesAndValues)
            return Collections.emptySet();
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0, size = queryNamesAndValues.size(); i < size; i += 2) {
            result.add(queryNamesAndValues.get(i));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Returns all values for the given query parameter name.
     *
     * @param name The name of the query parameter.
     * @return An unmodifiable list of query parameter values.
     */
    public List<String> queryParameterValues(String name) {
        if (null == queryNamesAndValues)
            return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (int i = 0, size = queryNamesAndValues.size(); i < size; i += 2) {
            if (name.equals(queryNamesAndValues.get(i))) {
                result.add(queryNamesAndValues.get(i + 1));
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the name of the query parameter at the given index.
     *
     * @param index The index of the query parameter.
     * @return The name of the query parameter.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public String queryParameterName(int index) {
        if (null == queryNamesAndValues)
            throw new IndexOutOfBoundsException();
        return queryNamesAndValues.get(index * 2);
    }

    /**
     * Returns the value of the query parameter at the given index.
     *
     * @param index The index of the query parameter.
     * @return The value of the query parameter.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public String queryParameterValue(int index) {
        if (null == queryNamesAndValues)
            throw new IndexOutOfBoundsException();
        return queryNamesAndValues.get(index * 2 + 1);
    }

    /**
     * Returns the encoded fragment.
     *
     * @return The encoded fragment, or null if no fragment is present.
     */
    public String encodedFragment() {
        if (null == fragment)
            return null;
        int fragmentStart = url.indexOf(Symbol.C_HASH) + 1;
        return url.substring(fragmentStart);
    }

    /**
     * Returns the decoded fragment.
     *
     * @return The decoded fragment, or null if no fragment is present.
     */
    public String fragment() {
        return fragment;
    }

    /**
     * Returns a new URL with sensitive information redacted.
     *
     * @return A new URL string with the username and password removed.
     */
    public String redact() {
        return newBuilder("/...").username(Normal.EMPTY).password(Normal.EMPTY).build().toString();
    }

    /**
     * Resolves a relative link against this URL.
     *
     * @param link The relative link.
     * @return The resolved {@code UnoUrl} instance, or null if the link is invalid.
     */
    public UnoUrl resolve(String link) {
        Builder builder = newBuilder(link);
        return null != builder ? builder.build() : null;
    }

    /**
     * Creates a new builder initialized with the components of this URL.
     *
     * @return A new builder instance.
     */
    public Builder newBuilder() {
        Builder result = new Builder();
        result.scheme = scheme;
        result.encodedUsername = encodedUsername();
        result.encodedPassword = encodedPassword();
        result.host = host;
        result.port = port != defaultPort(scheme) ? port : -1;
        result.encodedPathSegments.clear();
        result.encodedPathSegments.addAll(encodedPathSegments());
        result.encodedQuery(encodedQuery());
        result.encodedFragment = encodedFragment();
        return result;
    }

    /**
     * Creates a new builder for a relative link.
     *
     * @param link The relative link.
     * @return A new builder instance, or null if the link is invalid.
     */
    public Builder newBuilder(String link) {
        try {
            return new Builder().parse(this, link);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Compares this URL to another object for equality.
     *
     * @param other The other object to compare against.
     * @return {@code true} if the two URLs are equal.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof UnoUrl && ((UnoUrl) other).url.equals(url);
    }

    /**
     * Computes the hash code for this URL.
     *
     * @return The hash code value.
     */
    @Override
    public int hashCode() {
        return url.hashCode();
    }

    /**
     * Returns the string representation of this URL.
     *
     * @return The URL string.
     */
    @Override
    public String toString() {
        return url;
    }

    /**
     * Decodes a list of strings.
     *
     * @param list        The list of strings.
     * @param plusIsSpace Whether to decode plus signs as spaces.
     * @return The list of decoded strings.
     */
    private List<String> percentDecode(List<String> list, boolean plusIsSpace) {
        int size = list.size();
        List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String s = list.get(i);
            result.add(null != s ? percentDecode(s, plusIsSpace) : null);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * A builder for creating {@link UnoUrl} instances.
     */
    public static final class Builder {

        /**
         * The error message for an invalid host.
         */
        static final String INVALID_HOST = "Invalid URL host";
        /**
         * The list of encoded path segments.
         */
        final List<String> encodedPathSegments = new ArrayList<>();
        /**
         * The scheme of the URL.
         */
        String scheme;
        /**
         * The encoded username.
         */
        String encodedUsername = Normal.EMPTY;
        /**
         * The encoded password.
         */
        String encodedPassword = Normal.EMPTY;
        /**
         * The hostname.
         */
        String host;
        /**
         * The port number.
         */
        int port = -1;
        /**
         * The list of encoded query parameter names and values.
         */
        List<String> encodedQueryNamesAndValues;
        /**
         * The encoded fragment.
         */
        String encodedFragment;

        /**
         * Default constructor.
         */
        public Builder() {
            encodedPathSegments.add(Normal.EMPTY);
        }

        /**
         * Finds the offset of the scheme delimiter (:).
         *
         * @param input The input string.
         * @param pos   The starting position.
         * @param limit The ending position.
         * @return The offset of the delimiter, or -1 if not found.
         */
        private static int schemeDelimiterOffset(String input, int pos, int limit) {
            if (limit - pos < 2)
                return -1;

            char c0 = input.charAt(pos);
            if ((c0 < 'a' || c0 > 'z') && (c0 < 'A' || c0 > 'Z'))
                return -1; // Not a letter.

            for (int i = pos + 1; i < limit; i++) {
                char c = input.charAt(i);

                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= Symbol.C_ZERO && c <= Symbol.C_NINE)
                        || c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_DOT) {
                    continue; // Part of the scheme.
                } else if (c == Symbol.C_COLON) {
                    return i; // Found the scheme delimiter.
                } else {
                    return -1; // Not a scheme character.
                }
            }

            return -1;
        }

        /**
         * Counts the number of slashes at the beginning of a string.
         *
         * @param input The input string.
         * @param pos   The starting position.
         * @param limit The ending position.
         * @return The number of slashes.
         */
        private static int slashCount(String input, int pos, int limit) {
            int slashCount = 0;
            while (pos < limit) {
                char c = input.charAt(pos);
                if (c == Symbol.C_BACKSLASH || c == Symbol.C_SLASH) {
                    slashCount++;
                    pos++;
                } else {
                    break;
                }
            }
            return slashCount;
        }

        /**
         * Finds the offset of the port delimiter (:).
         *
         * @param input The input string.
         * @param pos   The starting position.
         * @param limit The ending position.
         * @return The offset of the delimiter.
         */
        private static int portColonOffset(String input, int pos, int limit) {
            for (int i = pos; i < limit; i++) {
                switch (input.charAt(i)) {
                    case Symbol.C_BRACKET_LEFT: // Skip IPv6 addresses.
                        while (++i < limit) {
                            if (input.charAt(i) == Symbol.C_BRACKET_RIGHT)
                                break;
                        }
                        break;

                    case Symbol.C_COLON:
                        return i;
                }
            }
            return limit; // No colon found.
        }

        /**
         * Canonicalizes a hostname.
         *
         * @param input The input string.
         * @param pos   The starting position.
         * @param limit The ending position.
         * @return The canonicalized hostname.
         */
        private static String canonicalizeHost(String input, int pos, int limit) {
            return org.miaixz.bus.http.Builder.canonicalizeHost(percentDecode(input, pos, limit, false));
        }

        /**
         * Parses a port number.
         *
         * @param input The input string.
         * @param pos   The starting position.
         * @param limit The ending position.
         * @return The parsed port number, or -1 if invalid.
         */
        private static int parsePort(String input, int pos, int limit) {
            try {
                String portString = canonicalize(input, pos, limit, Normal.EMPTY, false, false, false, true, null);
                int i = Integer.parseInt(portString);
                if (i > 0 && i <= 65535)
                    return i;
                return -1;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        /**
         * Sets the scheme of the URL.
         *
         * @param scheme The scheme (http or https).
         * @return this builder instance.
         * @throws NullPointerException     if scheme is null.
         * @throws IllegalArgumentException if the scheme is invalid.
         */
        public Builder scheme(String scheme) {
            if (null == scheme) {
                throw new NullPointerException("scheme == null");
            } else if (scheme.equalsIgnoreCase(Protocol.HTTP.name)) {
                this.scheme = Protocol.HTTP.name;
            } else if (scheme.equalsIgnoreCase(Protocol.HTTPS.name)) {
                this.scheme = Protocol.HTTPS.name;
            } else {
                throw new IllegalArgumentException("unexpected scheme: " + scheme);
            }
            return this;
        }

        /**
         * Sets the username.
         *
         * @param username The username.
         * @return this builder instance.
         * @throws NullPointerException if username is null.
         */
        public Builder username(String username) {
            if (null == username)
                throw new NullPointerException("username == null");
            this.encodedUsername = canonicalize(username, USERNAME_ENCODE_SET, false, false, false, true);
            return this;
        }

        /**
         * Sets the encoded username.
         *
         * @param encodedUsername The encoded username.
         * @return this builder instance.
         * @throws NullPointerException if encodedUsername is null.
         */
        public Builder encodedUsername(String encodedUsername) {
            if (null == encodedUsername)
                throw new NullPointerException("encodedUsername == null");
            this.encodedUsername = canonicalize(encodedUsername, USERNAME_ENCODE_SET, true, false, false, true);
            return this;
        }

        /**
         * Sets the password.
         *
         * @param password The password.
         * @return this builder instance.
         * @throws NullPointerException if password is null.
         */
        public Builder password(String password) {
            if (null == password)
                throw new NullPointerException("password == null");
            this.encodedPassword = canonicalize(password, PASSWORD_ENCODE_SET, false, false, false, true);
            return this;
        }

        /**
         * Sets the encoded password.
         *
         * @param encodedPassword The encoded password.
         * @return this builder instance.
         * @throws NullPointerException if encodedPassword is null.
         */
        public Builder encodedPassword(String encodedPassword) {
            if (null == encodedPassword)
                throw new NullPointerException("encodedPassword == null");
            this.encodedPassword = canonicalize(encodedPassword, PASSWORD_ENCODE_SET, true, false, false, true);
            return this;
        }

        /**
         * Sets the hostname.
         *
         * @param host The hostname (regular hostname, IPv4, IPv6, or encoded IDN).
         * @return this builder instance.
         * @throws NullPointerException     if host is null.
         * @throws IllegalArgumentException if the host is invalid.
         */
        public Builder host(String host) {
            if (null == host)
                throw new NullPointerException("host == null");
            String encoded = canonicalizeHost(host, 0, host.length());
            if (null == encoded)
                throw new IllegalArgumentException("unexpected host: " + host);
            this.host = encoded;
            return this;
        }

        /**
         * Sets the port number.
         *
         * @param port The port number.
         * @return this builder instance.
         * @throws IllegalArgumentException if the port number is invalid.
         */
        public Builder port(int port) {
            if (port <= 0 || port > 65535)
                throw new IllegalArgumentException("unexpected port: " + port);
            this.port = port;
            return this;
        }

        /**
         * Returns the effective port number.
         *
         * @return The effective port number.
         */
        int effectivePort() {
            return port != -1 ? port : defaultPort(scheme);
        }

        /**
         * Adds a path segment.
         *
         * @param pathSegment The path segment.
         * @return this builder instance.
         * @throws NullPointerException if pathSegment is null.
         */
        public Builder addPathSegment(String pathSegment) {
            if (null == pathSegment)
                throw new NullPointerException("pathSegment == null");
            push(pathSegment, 0, pathSegment.length(), false, false);
            return this;
        }

        /**
         * Adds a list of path segments.
         *
         * @param pathSegments The path segments string.
         * @return this builder instance.
         * @throws NullPointerException if pathSegments is null.
         */
        public Builder addPathSegments(String pathSegments) {
            if (null == pathSegments)
                throw new NullPointerException("pathSegments == null");
            return addPathSegments(pathSegments, false);
        }

        /**
         * Adds an encoded path segment.
         *
         * @param encodedPathSegment The encoded path segment.
         * @return this builder instance.
         * @throws NullPointerException if encodedPathSegment is null.
         */
        public Builder addEncodedPathSegment(String encodedPathSegment) {
            if (null == encodedPathSegment) {
                throw new NullPointerException("encodedPathSegment == null");
            }
            push(encodedPathSegment, 0, encodedPathSegment.length(), false, true);
            return this;
        }

        /**
         * Adds a list of encoded path segments.
         *
         * @param encodedPathSegments The encoded path segments string.
         * @return this builder instance.
         * @throws NullPointerException if encodedPathSegments is null.
         */
        public Builder addEncodedPathSegments(String encodedPathSegments) {
            if (null == encodedPathSegments) {
                throw new NullPointerException("encodedPathSegments == null");
            }
            return addPathSegments(encodedPathSegments, true);
        }

        /**
         * Internal implementation for adding path segments.
         *
         * @param pathSegments   The path segments string.
         * @param alreadyEncoded Whether the segments are already encoded.
         * @return this builder instance.
         */
        private Builder addPathSegments(String pathSegments, boolean alreadyEncoded) {
            int offset = 0;
            do {
                int segmentEnd = org.miaixz.bus.http.Builder
                        .delimiterOffset(pathSegments, offset, pathSegments.length(), "/\\");
                boolean addTrailingSlash = segmentEnd < pathSegments.length();
                push(pathSegments, offset, segmentEnd, addTrailingSlash, alreadyEncoded);
                offset = segmentEnd + 1;
            } while (offset <= pathSegments.length());
            return this;
        }

        /**
         * Sets a path segment at a specific index.
         *
         * @param index       The index.
         * @param pathSegment The path segment.
         * @return this builder instance.
         * @throws NullPointerException     if pathSegment is null.
         * @throws IllegalArgumentException if the path segment is invalid.
         */
        public Builder setPathSegment(int index, String pathSegment) {
            if (null == pathSegment)
                throw new NullPointerException("pathSegment == null");
            String canonicalPathSegment = canonicalize(
                    pathSegment,
                    0,
                    pathSegment.length(),
                    PATH_SEGMENT_ENCODE_SET,
                    false,
                    false,
                    false,
                    true,
                    null);
            if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
                throw new IllegalArgumentException("unexpected path segment: " + pathSegment);
            }
            encodedPathSegments.set(index, canonicalPathSegment);
            return this;
        }

        /**
         * Sets an encoded path segment at a specific index.
         *
         * @param index              The index.
         * @param encodedPathSegment The encoded path segment.
         * @return this builder instance.
         * @throws NullPointerException     if encodedPathSegment is null.
         * @throws IllegalArgumentException if the path segment is invalid.
         */
        public Builder setEncodedPathSegment(int index, String encodedPathSegment) {
            if (null == encodedPathSegment) {
                throw new NullPointerException("encodedPathSegment == null");
            }
            String canonicalPathSegment = canonicalize(
                    encodedPathSegment,
                    0,
                    encodedPathSegment.length(),
                    PATH_SEGMENT_ENCODE_SET,
                    true,
                    false,
                    false,
                    true,
                    null);
            encodedPathSegments.set(index, canonicalPathSegment);
            if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
                throw new IllegalArgumentException("unexpected path segment: " + encodedPathSegment);
            }
            return this;
        }

        /**
         * Removes a path segment at a specific index.
         *
         * @param index The index.
         * @return this builder instance.
         */
        public Builder removePathSegment(int index) {
            encodedPathSegments.remove(index);
            if (encodedPathSegments.isEmpty()) {
                encodedPathSegments.add(Normal.EMPTY); // Each URL must have at least one path segment.
            }
            return this;
        }

        /**
         * Sets the encoded path.
         *
         * @param encodedPath The encoded path.
         * @return this builder instance.
         * @throws NullPointerException     if encodedPath is null.
         * @throws IllegalArgumentException if the path is invalid.
         */
        public Builder encodedPath(String encodedPath) {
            if (null == encodedPath)
                throw new NullPointerException("encodedPath == null");
            if (!encodedPath.startsWith(Symbol.SLASH)) {
                throw new IllegalArgumentException("unexpected encodedPath: " + encodedPath);
            }
            resolvePath(encodedPath, 0, encodedPath.length());
            return this;
        }

        /**
         * Sets the query string.
         *
         * @param query The query string.
         * @return this builder instance.
         */
        public Builder query(String query) {
            this.encodedQueryNamesAndValues = null != query
                    ? queryStringToNamesAndValues(canonicalize(query, QUERY_ENCODE_SET, false, false, true, true))
                    : null;
            return this;
        }

        /**
         * Sets the encoded query string.
         *
         * @param encodedQuery The encoded query string.
         * @return this builder instance.
         */
        public Builder encodedQuery(String encodedQuery) {
            this.encodedQueryNamesAndValues = null != encodedQuery
                    ? queryStringToNamesAndValues(canonicalize(encodedQuery, QUERY_ENCODE_SET, true, false, true, true))
                    : null;
            return this;
        }

        /**
         * Adds a query parameter.
         *
         * @param name  The parameter name.
         * @param value The parameter value.
         * @return this builder instance.
         * @throws NullPointerException if name is null.
         */
        public Builder addQueryParameter(String name, String value) {
            if (null == name)
                throw new NullPointerException("name == null");
            if (null == encodedQueryNamesAndValues)
                encodedQueryNamesAndValues = new ArrayList<>();
            encodedQueryNamesAndValues.add(canonicalize(name, QUERY_COMPONENT_ENCODE_SET, false, false, true, true));
            encodedQueryNamesAndValues.add(
                    null != value ? canonicalize(value, QUERY_COMPONENT_ENCODE_SET, false, false, true, true) : null);
            return this;
        }

        /**
         * Adds an encoded query parameter.
         *
         * @param encodedName  The encoded parameter name.
         * @param encodedValue The encoded parameter value.
         * @return this builder instance.
         * @throws NullPointerException if encodedName is null.
         */
        public Builder addEncodedQueryParameter(String encodedName, String encodedValue) {
            if (null == encodedName)
                throw new NullPointerException("encodedName == null");
            if (null == encodedQueryNamesAndValues)
                encodedQueryNamesAndValues = new ArrayList<>();
            encodedQueryNamesAndValues
                    .add(canonicalize(encodedName, QUERY_COMPONENT_REENCODE_SET, true, false, true, true));
            encodedQueryNamesAndValues.add(
                    null != encodedValue
                            ? canonicalize(encodedValue, QUERY_COMPONENT_REENCODE_SET, true, false, true, true)
                            : null);
            return this;
        }

        /**
         * Sets a query parameter, replacing any existing parameters with the same name.
         *
         * @param name  The parameter name.
         * @param value The parameter value.
         * @return this builder instance.
         * @throws NullPointerException if name is null.
         */
        public Builder setQueryParameter(String name, String value) {
            removeAllQueryParameters(name);
            addQueryParameter(name, value);
            return this;
        }

        /**
         * Sets an encoded query parameter, replacing any existing parameters with the same name.
         *
         * @param encodedName  The encoded parameter name.
         * @param encodedValue The encoded parameter value.
         * @return this builder instance.
         * @throws NullPointerException if encodedName is null.
         */
        public Builder setEncodedQueryParameter(String encodedName, String encodedValue) {
            removeAllEncodedQueryParameters(encodedName);
            addEncodedQueryParameter(encodedName, encodedValue);
            return this;
        }

        /**
         * Removes all query parameters with the given name.
         *
         * @param name The parameter name.
         * @return this builder instance.
         * @throws NullPointerException if name is null.
         */
        public Builder removeAllQueryParameters(String name) {
            if (name == null)
                throw new NullPointerException("name == null");
            if (encodedQueryNamesAndValues == null)
                return this;
            String nameToRemove = canonicalize(name, QUERY_COMPONENT_ENCODE_SET, false, false, true, true);
            removeAllCanonicalQueryParameters(nameToRemove);
            return this;
        }

        /**
         * Removes all encoded query parameters with the given name.
         *
         * @param encodedName The encoded parameter name.
         * @return this builder instance.
         * @throws NullPointerException if encodedName is null.
         */
        public Builder removeAllEncodedQueryParameters(String encodedName) {
            if (null == encodedName)
                throw new NullPointerException("encodedName == null");
            if (null == encodedQueryNamesAndValues)
                return this;
            removeAllCanonicalQueryParameters(
                    canonicalize(encodedName, QUERY_COMPONENT_REENCODE_SET, true, false, true, true));
            return this;
        }

        /**
         * Removes all canonicalized query parameters with the given name.
         *
         * @param canonicalName The canonicalized parameter name.
         */
        private void removeAllCanonicalQueryParameters(String canonicalName) {
            for (int i = encodedQueryNamesAndValues.size() - 2; i >= 0; i -= 2) {
                if (canonicalName.equals(encodedQueryNamesAndValues.get(i))) {
                    encodedQueryNamesAndValues.remove(i + 1);
                    encodedQueryNamesAndValues.remove(i);
                    if (encodedQueryNamesAndValues.isEmpty()) {
                        encodedQueryNamesAndValues = null;
                        return;
                    }
                }
            }
        }

        /**
         * Sets the fragment.
         *
         * @param fragment The fragment.
         * @return this builder instance.
         */
        public Builder fragment(String fragment) {
            this.encodedFragment = null != fragment
                    ? canonicalize(fragment, FRAGMENT_ENCODE_SET, false, false, false, false)
                    : null;
            return this;
        }

        /**
         * Sets the encoded fragment.
         *
         * @param encodedFragment The encoded fragment.
         * @return this builder instance.
         */
        public Builder encodedFragment(String encodedFragment) {
            this.encodedFragment = null != encodedFragment
                    ? canonicalize(encodedFragment, FRAGMENT_ENCODE_SET, true, false, false, false)
                    : null;
            return this;
        }

        /**
         * Re-encodes the URL components for use in a URI.
         *
         * @return this builder instance.
         */
        Builder reencodeForUri() {
            for (int i = 0, size = encodedPathSegments.size(); i < size; i++) {
                String pathSegment = encodedPathSegments.get(i);
                encodedPathSegments
                        .set(i, canonicalize(pathSegment, PATH_SEGMENT_ENCODE_SET_URI, true, true, false, true));
            }
            if (null != encodedQueryNamesAndValues) {
                for (int i = 0, size = encodedQueryNamesAndValues.size(); i < size; i++) {
                    String component = encodedQueryNamesAndValues.get(i);
                    if (null != component) {
                        encodedQueryNamesAndValues.set(
                                i,
                                canonicalize(component, QUERY_COMPONENT_ENCODE_SET_URI, true, true, true, true));
                    }
                }
            }
            if (null != encodedFragment) {
                encodedFragment = canonicalize(encodedFragment, FRAGMENT_ENCODE_SET_URI, true, true, false, false);
            }
            return this;
        }

        /**
         * Builds a new {@link UnoUrl} instance.
         *
         * @return a new {@link UnoUrl} instance.
         * @throws IllegalStateException if the scheme or host is not set.
         */
        public UnoUrl build() {
            if (null == scheme)
                throw new IllegalStateException("scheme == null");
            if (null == host)
                throw new IllegalStateException("host == null");
            return new UnoUrl(this);
        }

        /**
         * Returns the string representation of the URL.
         *
         * @return The URL string.
         */
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            if (null != scheme) {
                result.append(scheme);
                result.append(Symbol.C_COLON + Symbol.FORWARDSLASH);
            } else {
                result.append(Symbol.FORWARDSLASH);
            }

            if (!encodedUsername.isEmpty() || !encodedPassword.isEmpty()) {
                result.append(encodedUsername);
                if (!encodedPassword.isEmpty()) {
                    result.append(Symbol.C_COLON);
                    result.append(encodedPassword);
                }
                result.append(Symbol.C_AT);
            }

            if (null != host) {
                if (host.indexOf(Symbol.C_COLON) != -1) {
                    // Host is an IPv6 address.
                    result.append(Symbol.C_BRACKET_LEFT);
                    result.append(host);
                    result.append(Symbol.C_BRACKET_RIGHT);
                } else {
                    result.append(host);
                }
            }

            if (port != -1 || null != scheme) {
                int effectivePort = effectivePort();
                if (null == scheme || effectivePort != defaultPort(scheme)) {
                    result.append(Symbol.C_COLON);
                    result.append(effectivePort);
                }
            }

            pathSegmentsToString(result, encodedPathSegments);

            if (null != encodedQueryNamesAndValues) {
                result.append(Symbol.C_QUESTION_MARK);
                namesAndValuesToQueryString(result, encodedQueryNamesAndValues);
            }

            if (null != encodedFragment) {
                result.append(Symbol.C_HASH);
                result.append(encodedFragment);
            }

            return result.toString();
        }

        /**
         * Parses a URL string.
         *
         * @param base  The base URL for resolving relative links.
         * @param input The input string.
         * @return this builder instance.
         * @throws IllegalArgumentException if the URL is malformed.
         */
        Builder parse(UnoUrl base, String input) {
            int pos = org.miaixz.bus.http.Builder.skipLeadingAsciiWhitespace(input, 0, input.length());
            int limit = org.miaixz.bus.http.Builder.skipTrailingAsciiWhitespace(input, pos, input.length());

            // Parse the scheme.
            int schemeDelimiterOffset = schemeDelimiterOffset(input, pos, limit);
            if (schemeDelimiterOffset != -1) {
                if (input.regionMatches(true, pos, Protocol.HTTPS.name + Symbol.C_COLON, 0, 6)) {
                    this.scheme = Protocol.HTTPS.name;
                    pos += (Protocol.HTTPS.name + Symbol.C_COLON).length();
                } else if (input.regionMatches(true, pos, Protocol.HTTP.name + Symbol.C_COLON, 0, 5)) {
                    this.scheme = Protocol.HTTP.name;
                    pos += (Protocol.HTTP.name + Symbol.C_COLON).length();
                } else {
                    throw new IllegalArgumentException("Expected URL scheme 'http' or 'https' but was '"
                            + input.substring(0, schemeDelimiterOffset) + Symbol.SINGLE_QUOTE);
                }
            } else if (null != base) {
                this.scheme = base.scheme;
            } else {
                throw new IllegalArgumentException("Expected URL scheme 'http' or 'https' but no colon was found");
            }

            // Parse the authority.
            boolean hasUsername = false;
            boolean hasPassword = false;
            int slashCount = slashCount(input, pos, limit);
            if (slashCount >= 2 || base == null || !base.scheme.equals(this.scheme)) {
                pos += slashCount;
                authority: while (true) {
                    int componentDelimiterOffset = org.miaixz.bus.http.Builder
                            .delimiterOffset(input, pos, limit, "@/\\?#");
                    int c = componentDelimiterOffset != limit ? input.charAt(componentDelimiterOffset) : -1;
                    switch (c) {
                        case Symbol.C_AT:
                            // Parse the username and password.
                            if (!hasPassword) {
                                int passwordColonOffset = org.miaixz.bus.http.Builder
                                        .delimiterOffset(input, pos, componentDelimiterOffset, Symbol.C_COLON);
                                String canonicalUsername = canonicalize(
                                        input,
                                        pos,
                                        passwordColonOffset,
                                        USERNAME_ENCODE_SET,
                                        true,
                                        false,
                                        false,
                                        true,
                                        null);
                                this.encodedUsername = hasUsername ? this.encodedUsername + "%40" + canonicalUsername
                                        : canonicalUsername;
                                if (passwordColonOffset != componentDelimiterOffset) {
                                    hasPassword = true;
                                    this.encodedPassword = canonicalize(
                                            input,
                                            passwordColonOffset + 1,
                                            componentDelimiterOffset,
                                            PASSWORD_ENCODE_SET,
                                            true,
                                            false,
                                            false,
                                            true,
                                            null);
                                }
                                hasUsername = true;
                            } else {
                                this.encodedPassword = this.encodedPassword + "%40"
                                        + canonicalize(
                                                input,
                                                pos,
                                                componentDelimiterOffset,
                                                PASSWORD_ENCODE_SET,
                                                true,
                                                false,
                                                false,
                                                true,
                                                null);
                            }
                            pos = componentDelimiterOffset + 1;
                            break;

                        case -1:
                        case Symbol.C_SLASH:
                        case Symbol.C_BACKSLASH:
                        case Symbol.C_QUESTION_MARK:
                        case Symbol.C_HASH:
                            // Parse the host and port.
                            int portColonOffset = portColonOffset(input, pos, componentDelimiterOffset);
                            if (portColonOffset + 1 < componentDelimiterOffset) {
                                host = canonicalizeHost(input, pos, portColonOffset);
                                port = parsePort(input, portColonOffset + 1, componentDelimiterOffset);
                                if (port == -1) {
                                    throw new IllegalArgumentException("Invalid URL port: "
                                            + input.substring(portColonOffset + 1, componentDelimiterOffset));
                                }
                            } else {
                                host = canonicalizeHost(input, pos, portColonOffset);
                                port = defaultPort(scheme);
                            }
                            if (null == host) {
                                throw new IllegalArgumentException(INVALID_HOST + ": "
                                        + input.substring(pos, portColonOffset) + Symbol.C_DOUBLE_QUOTES);
                            }
                            pos = componentDelimiterOffset;
                            break authority;
                    }
                }
            } else {
                // Inherit authority from the base URL.
                this.encodedUsername = base.encodedUsername();
                this.encodedPassword = base.encodedPassword();
                this.host = base.host;
                this.port = base.port;
                this.encodedPathSegments.clear();
                this.encodedPathSegments.addAll(base.encodedPathSegments());
                if (pos == limit || input.charAt(pos) == Symbol.C_HASH) {
                    encodedQuery(base.encodedQuery());
                }
            }

            // Parse the path.
            int pathDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, pos, limit, "?#");
            resolvePath(input, pos, pathDelimiterOffset);
            pos = pathDelimiterOffset;

            // Parse the query.
            if (pos < limit && input.charAt(pos) == Symbol.C_QUESTION_MARK) {
                int queryDelimiterOffset = org.miaixz.bus.http.Builder
                        .delimiterOffset(input, pos, limit, Symbol.C_HASH);
                this.encodedQueryNamesAndValues = queryStringToNamesAndValues(
                        canonicalize(
                                input,
                                pos + 1,
                                queryDelimiterOffset,
                                QUERY_ENCODE_SET,
                                true,
                                false,
                                true,
                                true,
                                null));
                pos = queryDelimiterOffset;
            }

            // Parse the fragment.
            if (pos < limit && input.charAt(pos) == Symbol.C_HASH) {
                this.encodedFragment = canonicalize(
                        input,
                        pos + 1,
                        limit,
                        FRAGMENT_ENCODE_SET,
                        true,
                        false,
                        false,
                        false,
                        null);
            }

            return this;
        }

        /**
         * Resolves the path.
         *
         * @param input The input string.
         * @param pos   The starting position.
         * @param limit The ending position.
         */
        private void resolvePath(String input, int pos, int limit) {
            if (pos == limit) {
                return; // No path to resolve.
            }
            char c = input.charAt(pos);
            if (c == Symbol.C_SLASH || c == Symbol.C_BACKSLASH) {
                // Absolute path.
                encodedPathSegments.clear();
                encodedPathSegments.add(Normal.EMPTY);
                pos++;
            } else {
                // Relative path.
                encodedPathSegments.set(encodedPathSegments.size() - 1, Normal.EMPTY);
            }

            for (int i = pos; i < limit;) {
                int pathSegmentDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, i, limit, "/\\");
                boolean segmentHasTrailingSlash = pathSegmentDelimiterOffset < limit;
                push(input, i, pathSegmentDelimiterOffset, segmentHasTrailingSlash, true);
                i = pathSegmentDelimiterOffset;
                if (segmentHasTrailingSlash)
                    i++;
            }
        }

        /**
         * Adds a path segment.
         *
         * @param input            The input string.
         * @param pos              The starting position.
         * @param limit            The ending position.
         * @param addTrailingSlash Whether to add a trailing slash.
         * @param alreadyEncoded   Whether the segment is already encoded.
         */
        private void push(String input, int pos, int limit, boolean addTrailingSlash, boolean alreadyEncoded) {
            String segment = canonicalize(
                    input,
                    pos,
                    limit,
                    PATH_SEGMENT_ENCODE_SET,
                    alreadyEncoded,
                    false,
                    false,
                    true,
                    null);
            if (isDot(segment)) {
                return; // Skip "." segments.
            }
            if (isDotDot(segment)) {
                pop();
                return; // Handle ".." segments.
            }
            if (encodedPathSegments.get(encodedPathSegments.size() - 1).isEmpty()) {
                encodedPathSegments.set(encodedPathSegments.size() - 1, segment);
            } else {
                encodedPathSegments.add(segment);
            }
            if (addTrailingSlash) {
                encodedPathSegments.add(Normal.EMPTY);
            }
        }

        /**
         * Checks if a segment is a dot (".").
         *
         * @param input The input string.
         * @return {@code true} if the segment is a dot.
         */
        private boolean isDot(String input) {
            return input.equals(Symbol.DOT) || input.equalsIgnoreCase("%2e");
        }

        /**
         * Checks if a segment is a double dot ("..").
         *
         * @param input The input string.
         * @return {@code true} if the segment is a double dot.
         */
        private boolean isDotDot(String input) {
            return input.equals(Symbol.DOUBLE_DOT) || input.equalsIgnoreCase("%2e.") || input.equalsIgnoreCase(".%2e")
                    || input.equalsIgnoreCase("%2e%2e");
        }

        /**
         * Removes the last path segment.
         */
        private void pop() {
            String removed = encodedPathSegments.remove(encodedPathSegments.size() - 1);
            // If the path is now empty, add back an empty segment.
            if (removed.isEmpty() && !encodedPathSegments.isEmpty()) {
                encodedPathSegments.set(encodedPathSegments.size() - 1, Normal.EMPTY);
            } else {
                encodedPathSegments.add(Normal.EMPTY);
            }
        }
    }

}
