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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.*;
import org.aoju.bus.http.metric.CookieJar;
import org.aoju.bus.http.secure.Challenge;

import java.io.EOFException;
import java.util.*;

/**
 * Header实用工具
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
public final class HttpHeaders {

    public static final ByteString PSEUDO_PREFIX = ByteString.encodeUtf8(Symbol.COLON);
    public static final ByteString RESPONSE_STATUS = ByteString.encodeUtf8(Http.RESPONSE_STATUS_UTF8);
    public static final ByteString TARGET_METHOD = ByteString.encodeUtf8(Http.TARGET_METHOD_UTF8);
    public static final ByteString TARGET_PATH = ByteString.encodeUtf8(Http.TARGET_PATH_UTF8);
    public static final ByteString TARGET_SCHEME = ByteString.encodeUtf8(Http.TARGET_SCHEME_UTF8);
    public static final ByteString TARGET_AUTHORITY = ByteString.encodeUtf8(Http.TARGET_AUTHORITY_UTF8);
    private static final ByteString QUOTED_STRING_DELIMITERS = ByteString.encodeUtf8("\"\\");
    private static final ByteString TOKEN_DELIMITERS = ByteString.encodeUtf8("\t ,=");
    /**
     * 不区分大小写的ASCII编码中的名称
     */
    public ByteString name;
    /**
     * TF-8编码中的值.
     */
    public ByteString value;
    public int hpackSize;

    private HttpHeaders() {
    }

    public HttpHeaders(String name, String value) {
        this(ByteString.encodeUtf8(name), ByteString.encodeUtf8(value));
    }

    public HttpHeaders(ByteString name, String value) {
        this(name, ByteString.encodeUtf8(value));
    }

    public HttpHeaders(ByteString name, ByteString value) {
        this.name = name;
        this.value = value;
        this.hpackSize = 32 + name.size() + value.size();
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

    public static boolean varyMatches(
            Response cachedResponse, Headers cachedRequest, Request newRequest) {
        for (String field : varyFields(cachedResponse)) {
            if (!ObjectKit.equal(cachedRequest.values(field), newRequest.headers(field))) return false;
        }
        return true;
    }

    public static boolean hasVaryAll(Response response) {
        return hasVaryAll(response.headers());
    }

    public static boolean hasVaryAll(Headers responseHeaders) {
        return varyFields(responseHeaders).contains(Symbol.STAR);
    }

    private static Set<String> varyFields(Response response) {
        return varyFields(response.headers());
    }

    public static Set<String> varyFields(Headers responseHeaders) {
        Set<String> result = Collections.emptySet();
        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
            if (!"Vary".equalsIgnoreCase(responseHeaders.name(i))) continue;

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

    public static Headers varyHeaders(Response response) {
        Headers requestHeaders = response.networkResponse().request().headers();
        Headers responseHeaders = response.headers();
        return varyHeaders(requestHeaders, responseHeaders);
    }

    public static Headers varyHeaders(Headers requestHeaders, Headers responseHeaders) {
        Set<String> varyFields = varyFields(responseHeaders);
        if (varyFields.isEmpty()) return new Headers.Builder().build();

        Headers.Builder result = new Headers.Builder();
        for (int i = 0, size = requestHeaders.size(); i < size; i++) {
            String fieldName = requestHeaders.name(i);
            if (varyFields.contains(fieldName)) {
                result.add(fieldName, requestHeaders.value(i));
            }
        }
        return result.build();
    }

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
            if (peek == null) {
                skipWhitespaceAndCommas(header);
                peek = readToken(header);
                if (peek == null) return;
            }

            String schemeName = peek;

            boolean commaPrefixed = skipWhitespaceAndCommas(header);
            peek = readToken(header);
            if (peek == null) {
                if (!header.exhausted()) return;
                result.add(new Challenge(schemeName, Collections.emptyMap()));
                return;
            }

            int eqCount = skipAll(header, (byte) Symbol.C_EQUAL);
            boolean commaSuffixed = skipWhitespaceAndCommas(header);

            if (!commaPrefixed && (commaSuffixed || header.exhausted())) {
                result.add(new Challenge(schemeName, Collections.singletonMap(
                        null, peek + repeat(Symbol.C_EQUAL, eqCount))));
                peek = null;
                continue;
            }

            Map<String, String> parameters = new LinkedHashMap<>();
            eqCount += skipAll(header, (byte) Symbol.C_EQUAL);
            while (true) {
                if (peek == null) {
                    peek = readToken(header);
                    if (skipWhitespaceAndCommas(header)) break;
                    eqCount = skipAll(header, (byte) Symbol.C_EQUAL);
                }
                if (eqCount == 0) break;
                if (eqCount > 1) return;
                if (skipWhitespaceAndCommas(header)) return;

                String parameterValue = !header.exhausted() && header.getByte(0) == Symbol.C_DOUBLE_QUOTES
                        ? readQuotedString(header)
                        : readToken(header);
                if (parameterValue == null) return;
                String replaced = parameters.put(peek, parameterValue);
                peek = null;
                if (replaced != null) return;
                if (!skipWhitespaceAndCommas(header) && !header.exhausted()) return;
            }
            result.add(new Challenge(schemeName, parameters));
        }
    }

    private static boolean skipWhitespaceAndCommas(Buffer buffer) {
        boolean commaFound = false;
        while (!buffer.exhausted()) {
            byte b = buffer.getByte(0);
            if (b == Symbol.C_COMMA) {
                buffer.readByte();
                commaFound = true;
            } else if (b == Symbol.C_SPACE || b == Symbol.C_HT) {
                buffer.readByte();
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

    private static String readQuotedString(Buffer buffer) {
        if (buffer.readByte() != '\"') throw new IllegalArgumentException();
        Buffer result = new Buffer();
        while (true) {
            long i = buffer.indexOfElement(QUOTED_STRING_DELIMITERS);
            if (i == -1L) return null;

            if (buffer.getByte(i) == Symbol.C_DOUBLE_QUOTES) {
                result.write(buffer, i);
                buffer.readByte();
                return result.readUtf8();
            }

            if (buffer.size() == i + 1L) return null;
            result.write(buffer, i);
            buffer.readByte();
            result.write(buffer, 1L);
        }
    }

    private static String readToken(Buffer buffer) {
        try {
            long tokenSize = buffer.indexOfElement(TOKEN_DELIMITERS);
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

    public static boolean hasBody(Response response) {
        if (response.request().method().equals("HEAD")) {
            return false;
        }

        int responseCode = response.code();
        if ((responseCode < Http.HTTP_CONTINUE || responseCode >= Http.HTTP_OK)
                && responseCode != Http.HTTP_NO_CONTENT
                && responseCode != Http.HTTP_NOT_MODIFIED) {
            return true;
        }

        if (contentLength(response) != -1
                || "chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return true;
        }

        return false;
    }

    public static int skipUntil(String input, int pos, String characters) {
        for (; pos < input.length(); pos++) {
            if (characters.indexOf(input.charAt(pos)) != -1) {
                break;
            }
        }
        return pos;
    }

    public static int skipWhitespace(String input, int pos) {
        for (; pos < input.length(); pos++) {
            char c = input.charAt(pos);
            if (c != Symbol.C_SPACE && c != Symbol.C_HT) {
                break;
            }
        }
        return pos;
    }

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

    @Override
    public boolean equals(Object other) {
        if (other instanceof HttpHeaders) {
            HttpHeaders that = (HttpHeaders) other;
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
        return StringKit.format("%s: %s", name.utf8(), value.utf8());
    }

    interface Listener {
        void onHeaders(Headers headers);
    }

}
