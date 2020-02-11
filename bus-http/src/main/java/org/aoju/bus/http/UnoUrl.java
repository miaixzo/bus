/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.http;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.metric.suffix.SuffixDatabase;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 统一资源定位器(URL)，其模式为{@code http}或{@code https}。使用这个类来组合和分解Internet地址
 * 这个类有一个现代的API。它避免了惩罚性的检查异常:{@link #get get()}对无效的输入抛出{@link IllegalArgumentException}，
 * 或者{@link #parse parse()}如果输入是无效的URL，则返回null。您甚至可以明确每个组件是否已经编码
 *
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8+
 */
public final class UnoUrl {

    public static final String USERNAME_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    public static final String PASSWORD_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    public static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";
    public static final String PATH_SEGMENT_ENCODE_SET_URI = Symbol.BRACKET;
    public static final String QUERY_ENCODE_SET = " \"'<>#";
    public static final String QUERY_COMPONENT_REENCODE_SET = " \"'<>#&=";
    public static final String QUERY_COMPONENT_ENCODE_SET = " !\"#$&'(),/:;<=>?@[]\\^`{|}~";
    public static final String QUERY_COMPONENT_ENCODE_SET_URI = "\\^`{|}";
    public static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
    public static final String FRAGMENT_ENCODE_SET = Normal.EMPTY;
    public static final String FRAGMENT_ENCODE_SET_URI = " \"#<>\\^`{|}";

    /**
     * 要么 "http" or "https".
     */
    final String scheme;
    /**
     * 规范的主机名.
     */
    final String host;
    /**
     * 要么 80, 443 或用户指定的端口。范围内(1 . . 65535).
     */
    final int port;
    /**
     * 解码的用户名.
     */
    private final String username;
    /**
     * 解码的密码.
     */
    private final String password;
    /**
     * 规范路径段的列表。此列表始终包含至少一个元素，该元素可以是空字符串。
     * 每个段的格式是前导的‘/’，所以如果路径段是["a"， "b"， ""]，那么编码的路径就是"/a/b/".
     */
    private final List<String> pathSegments;

    /**
     * 交替，解码的查询名称和值，或空无查询。名称可以为空或非空，但绝不为空。
     * 如果名称没有对应的'='分隔符，或为空，或为非空，则值为空.
     */
    private final List<String> queryNamesAndValues;

    /**
     * 解码片段.
     */
    private final String fragment;

    /**
     * 规范的URL.
     */
    private final String url;

    UnoUrl(Builder builder) {
        this.scheme = builder.scheme;
        this.username = percentDecode(builder.encodedUsername, false);
        this.password = percentDecode(builder.encodedPassword, false);
        this.host = builder.host;
        this.port = builder.effectivePort();
        this.pathSegments = percentDecode(builder.encodedPathSegments, false);
        this.queryNamesAndValues = builder.encodedQueryNamesAndValues != null
                ? percentDecode(builder.encodedQueryNamesAndValues, true)
                : null;
        this.fragment = builder.encodedFragment != null
                ? percentDecode(builder.encodedFragment, false)
                : null;
        this.url = builder.toString();
    }

    public static int defaultPort(String scheme) {
        if (Http.HTTP.equals(scheme)) {
            return 80;
        } else if (Http.HTTPS.equals(scheme)) {
            return 443;
        } else {
            return -1;
        }
    }

    static void pathSegmentsToString(StringBuilder out, List<String> pathSegments) {
        for (int i = 0, size = pathSegments.size(); i < size; i++) {
            out.append(Symbol.C_SLASH);
            out.append(pathSegments.get(i));
        }
    }

    static void namesAndValuesToQueryString(StringBuilder out, List<String> namesAndValues) {
        for (int i = 0, size = namesAndValues.size(); i < size; i += 2) {
            String name = namesAndValues.get(i);
            String value = namesAndValues.get(i + 1);
            if (i > 0) out.append(Symbol.C_AND);
            out.append(name);
            if (value != null) {
                out.append(Symbol.C_EQUAL);
                out.append(value);
            }
        }
    }

    static List<String> queryStringToNamesAndValues(String encodedQuery) {
        List<String> result = new ArrayList<>();
        for (int pos = 0; pos <= encodedQuery.length(); ) {
            int ampersandOffset = encodedQuery.indexOf(Symbol.C_AND, pos);
            if (ampersandOffset == -1) ampersandOffset = encodedQuery.length();

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

    public static UnoUrl parse(String url) {
        try {
            return get(url);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static UnoUrl get(String url) {
        return new Builder().parse(null, url).build();
    }

    public static UnoUrl get(URL url) {
        return parse(url.toString());
    }

    public static UnoUrl get(URI uri) {
        return parse(uri.toString());
    }

    public static String percentDecode(String encoded, boolean plusIsSpace) {
        return percentDecode(encoded, 0, encoded.length(), plusIsSpace);
    }

    static String percentDecode(String encoded, int pos, int limit, boolean plusIsSpace) {
        for (int i = pos; i < limit; i++) {
            char c = encoded.charAt(i);
            if (c == Symbol.C_PERCENT || (c == Symbol.C_PLUS && plusIsSpace)) {
                Buffer out = new Buffer();
                out.writeUtf8(encoded, pos, i);
                percentDecode(out, encoded, i, limit, plusIsSpace);
                return out.readUtf8();
            }
        }

        return encoded.substring(pos, limit);
    }

    static void percentDecode(Buffer out, String encoded, int pos, int limit, boolean plusIsSpace) {
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = encoded.codePointAt(i);
            if (codePoint == Symbol.C_PERCENT && i + 2 < limit) {
                int d1 = org.aoju.bus.http.Builder.decodeHexDigit(encoded.charAt(i + 1));
                int d2 = org.aoju.bus.http.Builder.decodeHexDigit(encoded.charAt(i + 2));
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

    static boolean percentEncoded(String encoded, int pos, int limit) {
        return pos + 2 < limit
                && encoded.charAt(pos) == Symbol.C_PERCENT
                && org.aoju.bus.http.Builder.decodeHexDigit(encoded.charAt(pos + 1)) != -1
                && org.aoju.bus.http.Builder.decodeHexDigit(encoded.charAt(pos + 2)) != -1;
    }

    static String canonicalize(String input, int pos, int limit, String encodeSet,
                               boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly,
                               Charset charset) {
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (codePoint < 0x20
                    || codePoint == 0x7f
                    || codePoint >= 0x80 && asciiOnly
                    || encodeSet.indexOf(codePoint) != -1
                    || codePoint == Symbol.C_PERCENT && (!alreadyEncoded || strict && !percentEncoded(input, i, limit))
                    || codePoint == Symbol.C_PLUS && plusIsSpace) {
                Buffer out = new Buffer();
                out.writeUtf8(input, pos, i);
                canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, strict, plusIsSpace,
                        asciiOnly, charset);
                return out.readUtf8();
            }
        }
        return input.substring(pos, limit);
    }

    static void canonicalize(Buffer out, String input, int pos, int limit, String encodeSet,
                             boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly,
                             Charset charset) {
        Buffer encodedCharBuffer = null;
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (alreadyEncoded
                    && (codePoint == Symbol.C_HT || codePoint == Symbol.C_LF || codePoint == '\f' || codePoint == Symbol.C_CR)) {

            } else if (codePoint == Symbol.C_PLUS && plusIsSpace) {
                out.writeUtf8(alreadyEncoded ? Symbol.PLUS : "%2B");
            } else if (codePoint < 0x20
                    || codePoint == 0x7f
                    || codePoint >= 0x80 && asciiOnly
                    || encodeSet.indexOf(codePoint) != -1
                    || codePoint == Symbol.C_PERCENT && (!alreadyEncoded || strict && !percentEncoded(input, i, limit))) {

                if (encodedCharBuffer == null) {
                    encodedCharBuffer = new Buffer();
                }

                if (charset == null || charset.equals(org.aoju.bus.core.lang.Charset.UTF_8)) {
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
                out.writeUtf8CodePoint(codePoint);
            }
        }
    }

    public static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict,
                                      boolean plusIsSpace, boolean asciiOnly, Charset charset) {
        return canonicalize(
                input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly,
                charset);
    }

    static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict,
                               boolean plusIsSpace, boolean asciiOnly) {
        return canonicalize(
                input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, null);
    }

    public URL url() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // Unexpected!
        }
    }

    public URI uri() {
        String uri = newBuilder().reencodeForUri().toString();
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            try {
                String stripped = uri.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F\\p{javaWhitespace}]", Normal.EMPTY);
                return URI.create(stripped);
            } catch (Exception e1) {
                throw new RuntimeException(e);
            }
        }
    }

    public String scheme() {
        return scheme;
    }

    public boolean isHttps() {
        return Http.HTTPS.equals(scheme);
    }

    public String encodedUsername() {
        if (username.isEmpty()) return Normal.EMPTY;
        int usernameStart = scheme.length() + 3;
        int usernameEnd = org.aoju.bus.http.Builder.delimiterOffset(url, usernameStart, url.length(), ":@");
        return url.substring(usernameStart, usernameEnd);
    }

    /**
     * 返回已解码的用户名，如果不存在，则返回空字符串.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code username()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://username@host/}</td><td>{@code "username"}</td></tr>
     * <tr><td>{@code http://username:password@host/}</td><td>{@code "username"}</td></tr>
     * <tr><td>{@code http://a%20b:c%20d@host/}</td><td>{@code "a b"}</td></tr>
     * </table>
     *
     * @return 用户信息
     */
    public String username() {
        return username;
    }

    /**
     * 返回密码，如果没有设置则返回空字符串.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code encodedPassword()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://username@host/}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://username:password@host/}</td><td>{@code "password"}</td></tr>
     * <tr><td>{@code http://a%20b:c%20d@host/}</td><td>{@code "c%20d"}</td></tr>
     * </table>
     *
     * @return 返回密码
     */
    public String encodedPassword() {
        if (password.isEmpty()) return Normal.EMPTY;
        int passwordStart = url.indexOf(Symbol.C_COLON, scheme.length() + 3) + 1;
        int passwordEnd = url.indexOf(Symbol.C_AT);
        return url.substring(passwordStart, passwordEnd);
    }

    /**
     * 返回已解码的密码，如果不存在，则返回空字符串.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code password()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://username@host/}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://username:password@host/}</td><td>{@code "password"}</td></tr>
     * <tr><td>{@code http://a%20b:c%20d@host/}</td><td>{@code "c d"}</td></tr>
     * </table>
     *
     * @return 返回已解码的密码
     */
    public String password() {
        return password;
    }

    /**
     * <ul>
     *   <li>A regular host name, like {@code android.com}.
     *   <li>An IPv4 address, like {@code 127.0.0.1}.
     *   <li>An IPv6 address, like {@code ::1}.
     *   <li>An encoded IDN, like {@code xn--n3h.net}.
     * </ul>
     *
     * <table summary="">
     *   <tr><th>URL</th><th>{@code host()}</th></tr>
     *   <tr><td>{@code http://android.com/}</td><td>{@code "android.com"}</td></tr>
     *   <tr><td>{@code http://127.0.0.1/}</td><td>{@code "127.0.0.1"}</td></tr>
     *   <tr><td>{@code http://[::1]/}</td><td>{@code "::1"}</td></tr>
     *   <tr><td>{@code http://xn--n3h.net/}</td><td>{@code "xn--n3h.net"}</td></tr>
     * </table>
     *
     * @return 主机host
     */
    public String host() {
        return host;
    }

    /**
     * <table summary="">
     * <tr><th>URL</th><th>{@code port()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code 80}</td></tr>
     * <tr><td>{@code http://host:8000/}</td><td>{@code 8000}</td></tr>
     * <tr><td>{@code https://host/}</td><td>{@code 443}</td></tr>
     * </table>
     *
     * @return 端口
     */
    public int port() {
        return port;
    }

    /**
     * Returns the number of segments in this URL's path. This is also the number of slashes in the
     * URL's path, like 3 in {@code http://host/a/b/c}. This is always at least 1.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code pathSize()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code 1}</td></tr>
     * <tr><td>{@code http://host/a/b/c}</td><td>{@code 3}</td></tr>
     * <tr><td>{@code http://host/a/b/c/}</td><td>{@code 4}</td></tr>
     * </table>
     *
     * @return the size
     */
    public int pathSize() {
        return pathSegments.size();
    }

    /**
     * 该URL编码后用于HTTP资源解析。返回的路径将以{@code /}开始
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code encodedPath()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code /}</td></tr>
     * <tr><td>{@code http://host/a/b/c}</td><td>{@code "/a/b/c"}</td></tr>
     * <tr><td>{@code http://host/a/b%20c/d}</td><td>{@code "/a/b%20c/d"}</td></tr>
     * </table>
     *
     * @return URL的完整路径
     */
    public String encodedPath() {
        int pathStart = url.indexOf(Symbol.C_SLASH, scheme.length() + 3);
        int pathEnd = org.aoju.bus.http.Builder.delimiterOffset(url, pathStart, url.length(), "?#");
        return url.substring(pathStart, pathEnd);
    }

    /**
     * 返回一个已编码的路径段列表 {@code ["a", "b", "c"]} for the URL {@code
     * http://host/a/b/c}. 这个列表从不为空，尽管它可能包含一个空字符串.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code encodedPathSegments()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code [""]}</td></tr>
     * <tr><td>{@code http://host/a/b/c}</td><td>{@code ["a", "b", "c"]}</td></tr>
     * <tr><td>{@code http://host/a/b%20c/d}</td><td>{@code ["a", "b%20c", "d"]}</td></tr>
     * </table>
     *
     * @return 路径段列表
     */
    public List<String> encodedPathSegments() {
        int pathStart = url.indexOf(Symbol.C_SLASH, scheme.length() + 3);
        int pathEnd = org.aoju.bus.http.Builder.delimiterOffset(url, pathStart, url.length(), "?#");
        List<String> result = new ArrayList<>();
        for (int i = pathStart; i < pathEnd; ) {
            i++;
            int segmentEnd = org.aoju.bus.http.Builder.delimiterOffset(url, i, pathEnd, Symbol.C_SLASH);
            result.add(url.substring(i, segmentEnd));
            i = segmentEnd;
        }
        return result;
    }

    /**
     * Returns a list of path segments like {@code ["a", "b", "c"]} for the URL {@code
     * http://host/a/b/c}. This list is never empty though it may contain a single empty string.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code pathSegments()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code [""]}</td></tr>
     * <tr><td>{@code http://host/a/b/c"}</td><td>{@code ["a", "b", "c"]}</td></tr>
     * <tr><td>{@code http://host/a/b%20c/d"}</td><td>{@code ["a", "b c", "d"]}</td></tr>
     * </table>
     *
     * @return the string
     */
    public List<String> pathSegments() {
        return pathSegments;
    }

    /**
     * Returns the query of this URL, encoded for use in HTTP resource resolution. The returned string
     * may be null (for URLs with no query), empty (for URLs with an empty query) or non-empty (all
     * other URLs).
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code encodedQuery()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>null</td></tr>
     * <tr><td>{@code http://host/?}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://host/?a=apple&k=key+lime}</td><td>{@code
     * "a=apple&k=key+lime"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&a=apricot}</td><td>{@code "a=apple&a=apricot"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&b}</td><td>{@code "a=apple&b"}</td></tr>
     * </table>
     *
     * @return the string
     */
    public String encodedQuery() {
        if (queryNamesAndValues == null) return null;
        int queryStart = url.indexOf(Symbol.C_QUESTION_MARK) + 1;
        int queryEnd = org.aoju.bus.http.Builder.delimiterOffset(url, queryStart, url.length(), Symbol.C_SHAPE);
        return url.substring(queryStart, queryEnd);
    }

    /**
     * Returns this URL's query, like {@code "abc"} for {@code http://host/?abc}. Most callers should
     * prefer {@link #queryParameterName} and {@link #queryParameterValue} because these methods offer
     * direct access to individual query parameters.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code query()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>null</td></tr>
     * <tr><td>{@code http://host/?}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://host/?a=apple&k=key+lime}</td><td>{@code "a=apple&k=key
     * lime"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&a=apricot}</td><td>{@code "a=apple&a=apricot"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&b}</td><td>{@code "a=apple&b"}</td></tr>
     * </table>
     *
     * @return the string
     */
    public String query() {
        if (queryNamesAndValues == null) return null;
        StringBuilder result = new StringBuilder();
        namesAndValuesToQueryString(result, queryNamesAndValues);
        return result.toString();
    }

    /**
     * Returns the number of query parameters in this URL, like 2 for {@code
     * http://host/?a=apple&b=banana}. If this URL has no query this returns 0. Otherwise it returns
     * one more than the number of {@code "&"} separators in the query.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code querySize()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code 0}</td></tr>
     * <tr><td>{@code http://host/?}</td><td>{@code 1}</td></tr>
     * <tr><td>{@code http://host/?a=apple&k=key+lime}</td><td>{@code 2}</td></tr>
     * <tr><td>{@code http://host/?a=apple&a=apricot}</td><td>{@code 2}</td></tr>
     * <tr><td>{@code http://host/?a=apple&b}</td><td>{@code 2}</td></tr>
     * </table>
     *
     * @return the int
     */
    public int querySize() {
        return queryNamesAndValues != null ? queryNamesAndValues.size() / 2 : 0;
    }

    /**
     * Returns the first query parameter named {@code name} decoded using UTF-8, or null if there is
     * no such query parameter.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code queryParameter("a")}</th></tr>
     * <tr><td>{@code http://host/}</td><td>null</td></tr>
     * <tr><td>{@code http://host/?}</td><td>null</td></tr>
     * <tr><td>{@code http://host/?a=apple&k=key+lime}</td><td>{@code "apple"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&a=apricot}</td><td>{@code "apple"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&b}</td><td>{@code "apple"}</td></tr>
     * </table>
     *
     * @param name 名称
     * @return the string
     */
    public String queryParameter(String name) {
        if (queryNamesAndValues == null) return null;
        for (int i = 0, size = queryNamesAndValues.size(); i < size; i += 2) {
            if (name.equals(queryNamesAndValues.get(i))) {
                return queryNamesAndValues.get(i + 1);
            }
        }
        return null;
    }

    /**
     * Returns the distinct query parameter names in this URL, like {@code ["a", "b"]} for {@code
     * http://host/?a=apple&b=banana}. If this URL has no query this returns the empty set.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code queryParameterNames()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code []}</td></tr>
     * <tr><td>{@code http://host/?}</td><td>{@code [""]}</td></tr>
     * <tr><td>{@code http://host/?a=apple&k=key+lime}</td><td>{@code ["a", "k"]}</td></tr>
     * <tr><td>{@code http://host/?a=apple&a=apricot}</td><td>{@code ["a"]}</td></tr>
     * <tr><td>{@code http://host/?a=apple&b}</td><td>{@code ["a", "b"]}</td></tr>
     * </table>
     *
     * @return the set
     */
    public Set<String> queryParameterNames() {
        if (queryNamesAndValues == null) return Collections.emptySet();
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0, size = queryNamesAndValues.size(); i < size; i += 2) {
            result.add(queryNamesAndValues.get(i));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Returns all values for the query parameter {@code name} ordered by their appearance in this
     * URL. For example this returns {@code ["banana"]} for {@code queryParameterValue("b")} on {@code
     * http://host/?a=apple&b=banana}.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code queryParameterValues("a")}</th><th>{@code
     * queryParameterValues("b")}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code []}</td><td>{@code []}</td></tr>
     * <tr><td>{@code http://host/?}</td><td>{@code []}</td><td>{@code []}</td></tr>
     * <tr><td>{@code http://host/?a=apple&k=key+lime}</td><td>{@code ["apple"]}</td><td>{@code
     * []}</td></tr>
     * <tr><td>{@code http://host/?a=apple&a=apricot}</td><td>{@code ["apple",
     * "apricot"]}</td><td>{@code []}</td></tr>
     * <tr><td>{@code http://host/?a=apple&b}</td><td>{@code ["apple"]}</td><td>{@code
     * [null]}</td></tr>
     * </table>
     *
     * @param name 名称
     * @return the list
     */
    public List<String> queryParameterValues(String name) {
        if (queryNamesAndValues == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (int i = 0, size = queryNamesAndValues.size(); i < size; i += 2) {
            if (name.equals(queryNamesAndValues.get(i))) {
                result.add(queryNamesAndValues.get(i + 1));
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the name of the query parameter at {@code index}. For example this returns {@code "a"}
     * for {@code queryParameterName(0)} on {@code http://host/?a=apple&b=banana}. This throws if
     * {@code index} is not less than the {@linkplain #querySize query size}.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code queryParameterName(0)}</th><th>{@code
     * queryParameterName(1)}</th></tr>
     * <tr><td>{@code http://host/}</td><td>exception</td><td>exception</td></tr>
     * <tr><td>{@code http://host/?}</td><td>{@code ""}</td><td>exception</td></tr>
     * <tr><td>{@code http://host/?a=apple&k=key+lime}</td><td>{@code "a"}</td><td>{@code
     * "k"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&a=apricot}</td><td>{@code "a"}</td><td>{@code
     * "a"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&b}</td><td>{@code "a"}</td><td>{@code "b"}</td></tr>
     * </table>
     *
     * @param index 索引
     * @return the string
     */
    public String queryParameterName(int index) {
        if (queryNamesAndValues == null) throw new IndexOutOfBoundsException();
        return queryNamesAndValues.get(index * 2);
    }

    /**
     * Returns the value of the query parameter at {@code index}. For example this returns {@code
     * "apple"} for {@code queryParameterName(0)} on {@code http://host/?a=apple&b=banana}. This
     * throws if {@code index} is not less than the {@linkplain #querySize query size}.
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code queryParameterValue(0)}</th><th>{@code
     * queryParameterValue(1)}</th></tr>
     * <tr><td>{@code http://host/}</td><td>exception</td><td>exception</td></tr>
     * <tr><td>{@code http://host/?}</td><td>null</td><td>exception</td></tr>
     * <tr><td>{@code http://host/?a=apple&k=key+lime}</td><td>{@code "apple"}</td><td>{@code
     * "key lime"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&a=apricot}</td><td>{@code "apple"}</td><td>{@code
     * "apricot"}</td></tr>
     * <tr><td>{@code http://host/?a=apple&b}</td><td>{@code "apple"}</td><td>null</td></tr>
     * </table>
     *
     * @param index 索引
     * @return the string
     */
    public String queryParameterValue(int index) {
        if (queryNamesAndValues == null) throw new IndexOutOfBoundsException();
        return queryNamesAndValues.get(index * 2 + 1);
    }

    /**
     * 返回这个URL的片段 {@code "abc"} for {@code http://host/#abc}. 如果URL没有片段，则返回null
     * <table summary="">
     * <tr><th>URL</th><th>{@code encodedFragment()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>null</td></tr>
     * <tr><td>{@code http://host/#}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://host/#abc}</td><td>{@code "abc"}</td></tr>
     * <tr><td>{@code http://host/#abc|def}</td><td>{@code "abc|def"}</td></tr>
     * </table>
     *
     * @return the string
     */
    public String encodedFragment() {
        if (fragment == null) return null;
        int fragmentStart = url.indexOf(Symbol.C_SHAPE) + 1;
        return url.substring(fragmentStart);
    }

    /**
     * 返回这个URL的片段 {@code "abc"} for {@code http://host/#abc}. 如果URL没有片段，则返回null
     * <table summary="">
     * <tr><th>URL</th><th>{@code fragment()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>null</td></tr>
     * <tr><td>{@code http://host/#}</td><td>{@code ""}</td></tr>
     * <tr><td>{@code http://host/#abc}</td><td>{@code "abc"}</td></tr>
     * <tr><td>{@code http://host/#abc|def}</td><td>{@code "abc|def"}</td></tr>
     * </table>
     *
     * @return the string
     */
    public String fragment() {
        return fragment;
    }

    public String redact() {
        return newBuilder("/...")
                .username(Normal.EMPTY)
                .password(Normal.EMPTY)
                .build()
                .toString();
    }

    public UnoUrl resolve(String link) {
        Builder builder = newBuilder(link);
        return builder != null ? builder.build() : null;
    }

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

    public Builder newBuilder(String link) {
        try {
            return new Builder().parse(this, link);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof UnoUrl && ((UnoUrl) other).url.equals(url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return url;
    }

    /**
     * 通常，这个方法不应该用来测试一个域是否有效或可路由。相反，DNS是推荐的信息来源
     *
     * <table summary="">
     * <tr><th>URL</th><th>{@code topPrivateDomain()}</th></tr>
     * <tr><td>{@code http://google.com}</td><td>{@code "google.com"}</td></tr>
     * <tr><td>{@code http://adwords.google.co.uk}</td><td>{@code "google.co.uk"}</td></tr>
     * <tr><td>{@code http://co.uk}</td><td>null</td></tr>
     * <tr><td>{@code http://localhost}</td><td>null</td></tr>
     * <tr><td>{@code http://127.0.0.1}</td><td>null</td></tr>
     * </table>
     *
     * @return the string
     */
    public String topPrivateDomain() {
        if (org.aoju.bus.http.Builder.verifyAsIpAddress(host)) return null;
        return SuffixDatabase.get().getEffectiveTldPlusOne(host);
    }

    private List<String> percentDecode(List<String> list, boolean plusIsSpace) {
        int size = list.size();
        List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String s = list.get(i);
            result.add(s != null ? percentDecode(s, plusIsSpace) : null);
        }
        return Collections.unmodifiableList(result);
    }

    public static final class Builder {
        static final String INVALID_HOST = "Invalid URL host";
        final List<String> encodedPathSegments = new ArrayList<>();
        String scheme;
        String encodedUsername = Normal.EMPTY;
        String encodedPassword = Normal.EMPTY;
        String host;
        int port = -1;
        List<String> encodedQueryNamesAndValues;
        String encodedFragment;

        public Builder() {
            encodedPathSegments.add(Normal.EMPTY);
        }

        private static int schemeDelimiterOffset(String input, int pos, int limit) {
            if (limit - pos < 2) return -1;

            char c0 = input.charAt(pos);
            if ((c0 < 'a' || c0 > 'z') && (c0 < 'A' || c0 > 'Z')) return -1;

            for (int i = pos + 1; i < limit; i++) {
                char c = input.charAt(i);

                if ((c >= 'a' && c <= 'z')
                        || (c >= 'A' && c <= 'Z')
                        || (c >= Symbol.C_ZERO && c <= Symbol.C_NINE)
                        || c == Symbol.C_PLUS
                        || c == Symbol.C_HYPHEN
                        || c == Symbol.C_DOT) {
                    continue;
                } else if (c == Symbol.C_COLON) {
                    return i;
                } else {
                    return -1;
                }
            }

            return -1;
        }

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

        private static int portColonOffset(String input, int pos, int limit) {
            for (int i = pos; i < limit; i++) {
                switch (input.charAt(i)) {
                    case Symbol.C_BRACKET_LEFT:
                        while (++i < limit) {
                            if (input.charAt(i) == Symbol.C_BRACKET_RIGHT) break;
                        }
                        break;
                    case Symbol.C_COLON:
                        return i;
                }
            }
            return limit;
        }

        private static String canonicalizeHost(String input, int pos, int limit) {
            return org.aoju.bus.http.Builder.canonicalizeHost(percentDecode(input, pos, limit, false));
        }

        private static int parsePort(String input, int pos, int limit) {
            try {
                String portString = canonicalize(input, pos, limit, Normal.EMPTY, false, false, false, true, null);
                int i = Integer.parseInt(portString);
                if (i > 0 && i <= 65535) return i;
                return -1;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        public Builder scheme(String scheme) {
            if (scheme == null) {
                throw new NullPointerException("scheme == null");
            } else if (scheme.equalsIgnoreCase(Http.HTTP)) {
                this.scheme = Http.HTTP;
            } else if (scheme.equalsIgnoreCase(Http.HTTPS)) {
                this.scheme = Http.HTTPS;
            } else {
                throw new IllegalArgumentException("unexpected scheme: " + scheme);
            }
            return this;
        }

        public Builder username(String username) {
            if (username == null) throw new NullPointerException("username == null");
            this.encodedUsername = canonicalize(username, USERNAME_ENCODE_SET, false, false, false, true);
            return this;
        }

        public Builder encodedUsername(String encodedUsername) {
            if (encodedUsername == null) throw new NullPointerException("encodedUsername == null");
            this.encodedUsername = canonicalize(
                    encodedUsername, USERNAME_ENCODE_SET, true, false, false, true);
            return this;
        }

        public Builder password(String password) {
            if (password == null) throw new NullPointerException("password == null");
            this.encodedPassword = canonicalize(password, PASSWORD_ENCODE_SET, false, false, false, true);
            return this;
        }

        public Builder encodedPassword(String encodedPassword) {
            if (encodedPassword == null) throw new NullPointerException("encodedPassword == null");
            this.encodedPassword = canonicalize(
                    encodedPassword, PASSWORD_ENCODE_SET, true, false, false, true);
            return this;
        }

        public Builder host(String host) {
            if (host == null) throw new NullPointerException("host == null");
            String encoded = canonicalizeHost(host, 0, host.length());
            if (encoded == null) throw new IllegalArgumentException("unexpected host: " + host);
            this.host = encoded;
            return this;
        }

        public Builder port(int port) {
            if (port <= 0 || port > 65535) throw new IllegalArgumentException("unexpected port: " + port);
            this.port = port;
            return this;
        }

        int effectivePort() {
            return port != -1 ? port : defaultPort(scheme);
        }

        public Builder addPathSegment(String pathSegment) {
            if (pathSegment == null) throw new NullPointerException("pathSegment == null");
            push(pathSegment, 0, pathSegment.length(), false, false);
            return this;
        }

        public Builder addPathSegments(String pathSegments) {
            if (pathSegments == null) throw new NullPointerException("pathSegments == null");
            return addPathSegments(pathSegments, false);
        }

        public Builder addEncodedPathSegment(String encodedPathSegment) {
            if (encodedPathSegment == null) {
                throw new NullPointerException("encodedPathSegment == null");
            }
            push(encodedPathSegment, 0, encodedPathSegment.length(), false, true);
            return this;
        }

        public Builder addEncodedPathSegments(String encodedPathSegments) {
            if (encodedPathSegments == null) {
                throw new NullPointerException("encodedPathSegments == null");
            }
            return addPathSegments(encodedPathSegments, true);
        }

        private Builder addPathSegments(String pathSegments, boolean alreadyEncoded) {
            int offset = 0;
            do {
                int segmentEnd = org.aoju.bus.http.Builder.delimiterOffset(pathSegments, offset, pathSegments.length(), "/\\");
                boolean addTrailingSlash = segmentEnd < pathSegments.length();
                push(pathSegments, offset, segmentEnd, addTrailingSlash, alreadyEncoded);
                offset = segmentEnd + 1;
            } while (offset <= pathSegments.length());
            return this;
        }

        public Builder setPathSegment(int index, String pathSegment) {
            if (pathSegment == null) throw new NullPointerException("pathSegment == null");
            String canonicalPathSegment = canonicalize(
                    pathSegment, 0, pathSegment.length(), PATH_SEGMENT_ENCODE_SET, false, false, false, true,
                    null);
            if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
                throw new IllegalArgumentException("unexpected path segment: " + pathSegment);
            }
            encodedPathSegments.set(index, canonicalPathSegment);
            return this;
        }

        public Builder setEncodedPathSegment(int index, String encodedPathSegment) {
            if (encodedPathSegment == null) {
                throw new NullPointerException("encodedPathSegment == null");
            }
            String canonicalPathSegment = canonicalize(encodedPathSegment,
                    0, encodedPathSegment.length(), PATH_SEGMENT_ENCODE_SET, true, false, false, true,
                    null);
            encodedPathSegments.set(index, canonicalPathSegment);
            if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
                throw new IllegalArgumentException("unexpected path segment: " + encodedPathSegment);
            }
            return this;
        }

        public Builder removePathSegment(int index) {
            encodedPathSegments.remove(index);
            if (encodedPathSegments.isEmpty()) {
                encodedPathSegments.add(Normal.EMPTY);
            }
            return this;
        }

        public Builder encodedPath(String encodedPath) {
            if (encodedPath == null) throw new NullPointerException("encodedPath == null");
            if (!encodedPath.startsWith(Symbol.SLASH)) {
                throw new IllegalArgumentException("unexpected encodedPath: " + encodedPath);
            }
            resolvePath(encodedPath, 0, encodedPath.length());
            return this;
        }

        public Builder query(String query) {
            this.encodedQueryNamesAndValues = query != null
                    ? queryStringToNamesAndValues(canonicalize(
                    query, QUERY_ENCODE_SET, false, false, true, true))
                    : null;
            return this;
        }

        public Builder encodedQuery(String encodedQuery) {
            this.encodedQueryNamesAndValues = encodedQuery != null
                    ? queryStringToNamesAndValues(
                    canonicalize(encodedQuery, QUERY_ENCODE_SET, true, false, true, true))
                    : null;
            return this;
        }

        public Builder addQueryParameter(String name, String value) {
            if (name == null) throw new NullPointerException("name == null");
            if (encodedQueryNamesAndValues == null) encodedQueryNamesAndValues = new ArrayList<>();
            encodedQueryNamesAndValues.add(
                    canonicalize(name, QUERY_COMPONENT_ENCODE_SET, false, false, true, true));
            encodedQueryNamesAndValues.add(value != null
                    ? canonicalize(value, QUERY_COMPONENT_ENCODE_SET, false, false, true, true)
                    : null);
            return this;
        }

        public Builder addEncodedQueryParameter(String encodedName, String encodedValue) {
            if (encodedName == null) throw new NullPointerException("encodedName == null");
            if (encodedQueryNamesAndValues == null) encodedQueryNamesAndValues = new ArrayList<>();
            encodedQueryNamesAndValues.add(
                    canonicalize(encodedName, QUERY_COMPONENT_REENCODE_SET, true, false, true, true));
            encodedQueryNamesAndValues.add(encodedValue != null
                    ? canonicalize(encodedValue, QUERY_COMPONENT_REENCODE_SET, true, false, true, true)
                    : null);
            return this;
        }

        public Builder setQueryParameter(String name, String value) {
            removeAllQueryParameters(name);
            addQueryParameter(name, value);
            return this;
        }

        public Builder setEncodedQueryParameter(String encodedName, String encodedValue) {
            removeAllEncodedQueryParameters(encodedName);
            addEncodedQueryParameter(encodedName, encodedValue);
            return this;
        }

        public Builder removeAllQueryParameters(String name) {
            if (name == null) throw new NullPointerException("name == null");
            if (encodedQueryNamesAndValues == null) return this;
            String nameToRemove = canonicalize(
                    name, QUERY_COMPONENT_ENCODE_SET, false, false, true, true);
            removeAllCanonicalQueryParameters(nameToRemove);
            return this;
        }

        public Builder removeAllEncodedQueryParameters(String encodedName) {
            if (encodedName == null) throw new NullPointerException("encodedName == null");
            if (encodedQueryNamesAndValues == null) return this;
            removeAllCanonicalQueryParameters(
                    canonicalize(encodedName, QUERY_COMPONENT_REENCODE_SET, true, false, true, true));
            return this;
        }

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

        public Builder fragment(String fragment) {
            this.encodedFragment = fragment != null
                    ? canonicalize(fragment, FRAGMENT_ENCODE_SET, false, false, false, false)
                    : null;
            return this;
        }

        public Builder encodedFragment(String encodedFragment) {
            this.encodedFragment = encodedFragment != null
                    ? canonicalize(encodedFragment, FRAGMENT_ENCODE_SET, true, false, false, false)
                    : null;
            return this;
        }

        Builder reencodeForUri() {
            for (int i = 0, size = encodedPathSegments.size(); i < size; i++) {
                String pathSegment = encodedPathSegments.get(i);
                encodedPathSegments.set(i,
                        canonicalize(pathSegment, PATH_SEGMENT_ENCODE_SET_URI, true, true, false, true));
            }
            if (encodedQueryNamesAndValues != null) {
                for (int i = 0, size = encodedQueryNamesAndValues.size(); i < size; i++) {
                    String component = encodedQueryNamesAndValues.get(i);
                    if (component != null) {
                        encodedQueryNamesAndValues.set(i,
                                canonicalize(component, QUERY_COMPONENT_ENCODE_SET_URI, true, true, true, true));
                    }
                }
            }
            if (encodedFragment != null) {
                encodedFragment = canonicalize(
                        encodedFragment, FRAGMENT_ENCODE_SET_URI, true, true, false, false);
            }
            return this;
        }

        public UnoUrl build() {
            if (scheme == null) throw new IllegalStateException("scheme == null");
            if (host == null) throw new IllegalStateException("host == null");
            return new UnoUrl(this);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            if (scheme != null) {
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

            if (host != null) {
                if (host.indexOf(Symbol.C_COLON) != -1) {
                    result.append(Symbol.C_BRACKET_LEFT);
                    result.append(host);
                    result.append(Symbol.C_BRACKET_RIGHT);
                } else {
                    result.append(host);
                }
            }

            if (port != -1 || scheme != null) {
                int effectivePort = effectivePort();
                if (scheme == null || effectivePort != defaultPort(scheme)) {
                    result.append(Symbol.C_COLON);
                    result.append(effectivePort);
                }
            }

            pathSegmentsToString(result, encodedPathSegments);

            if (encodedQueryNamesAndValues != null) {
                result.append(Symbol.C_QUESTION_MARK);
                namesAndValuesToQueryString(result, encodedQueryNamesAndValues);
            }

            if (encodedFragment != null) {
                result.append(Symbol.C_SHAPE);
                result.append(encodedFragment);
            }

            return result.toString();
        }

        Builder parse(UnoUrl base, String input) {
            int pos = org.aoju.bus.http.Builder.skipLeadingAsciiWhitespace(input, 0, input.length());
            int limit = org.aoju.bus.http.Builder.skipTrailingAsciiWhitespace(input, pos, input.length());

            int schemeDelimiterOffset = schemeDelimiterOffset(input, pos, limit);
            if (schemeDelimiterOffset != -1) {
                if (input.regionMatches(true, pos, Http.HTTPS + Symbol.COLON, 0, 6)) {
                    this.scheme = Http.HTTPS;
                    pos += (Http.HTTPS + Symbol.COLON).length();
                } else if (input.regionMatches(true, pos, Http.HTTP + Symbol.COLON, 0, 5)) {
                    this.scheme = Http.HTTP;
                    pos += (Http.HTTP + Symbol.COLON).length();
                } else {
                    throw new IllegalArgumentException("Expected URL scheme 'http' or 'https' but was '"
                            + input.substring(0, schemeDelimiterOffset) + Symbol.SINGLE_QUOTE);
                }
            } else if (base != null) {
                this.scheme = base.scheme;
            } else {
                throw new IllegalArgumentException(
                        "Expected URL scheme 'http' or 'https' but no colon was found");
            }

            boolean hasUsername = false;
            boolean hasPassword = false;
            int slashCount = slashCount(input, pos, limit);
            if (slashCount >= 2 || base == null || !base.scheme.equals(this.scheme)) {
                pos += slashCount;
                authority:
                while (true) {
                    int componentDelimiterOffset = org.aoju.bus.http.Builder.delimiterOffset(input, pos, limit, "@/\\?#");
                    int c = componentDelimiterOffset != limit
                            ? input.charAt(componentDelimiterOffset)
                            : -1;
                    switch (c) {
                        case Symbol.C_AT:
                            if (!hasPassword) {
                                int passwordColonOffset = org.aoju.bus.http.Builder.delimiterOffset(
                                        input, pos, componentDelimiterOffset, Symbol.C_COLON);
                                String canonicalUsername = canonicalize(
                                        input, pos, passwordColonOffset, USERNAME_ENCODE_SET, true, false, false, true,
                                        null);
                                this.encodedUsername = hasUsername
                                        ? this.encodedUsername + "%40" + canonicalUsername
                                        : canonicalUsername;
                                if (passwordColonOffset != componentDelimiterOffset) {
                                    hasPassword = true;
                                    this.encodedPassword = canonicalize(input, passwordColonOffset + 1,
                                            componentDelimiterOffset, PASSWORD_ENCODE_SET, true, false, false, true,
                                            null);
                                }
                                hasUsername = true;
                            } else {
                                this.encodedPassword = this.encodedPassword + "%40" + canonicalize(input, pos,
                                        componentDelimiterOffset, PASSWORD_ENCODE_SET, true, false, false, true,
                                        null);
                            }
                            pos = componentDelimiterOffset + 1;
                            break;

                        case -1:
                        case Symbol.C_SLASH:
                        case Symbol.C_BACKSLASH:
                        case Symbol.C_QUESTION_MARK:
                        case Symbol.C_SHAPE:
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
                            if (host == null) {
                                throw new IllegalArgumentException(
                                        INVALID_HOST + ": " + input.substring(pos, portColonOffset) + Symbol.C_DOUBLE_QUOTES);
                            }
                            pos = componentDelimiterOffset;
                            break authority;
                    }
                }
            } else {
                this.encodedUsername = base.encodedUsername();
                this.encodedPassword = base.encodedPassword();
                this.host = base.host;
                this.port = base.port;
                this.encodedPathSegments.clear();
                this.encodedPathSegments.addAll(base.encodedPathSegments());
                if (pos == limit || input.charAt(pos) == Symbol.C_SHAPE) {
                    encodedQuery(base.encodedQuery());
                }
            }

            int pathDelimiterOffset = org.aoju.bus.http.Builder.delimiterOffset(input, pos, limit, "?#");
            resolvePath(input, pos, pathDelimiterOffset);
            pos = pathDelimiterOffset;

            if (pos < limit && input.charAt(pos) == Symbol.C_QUESTION_MARK) {
                int queryDelimiterOffset = org.aoju.bus.http.Builder.delimiterOffset(input, pos, limit, Symbol.C_SHAPE);
                this.encodedQueryNamesAndValues = queryStringToNamesAndValues(canonicalize(
                        input, pos + 1, queryDelimiterOffset, QUERY_ENCODE_SET, true, false, true, true, null));
                pos = queryDelimiterOffset;
            }

            if (pos < limit && input.charAt(pos) == Symbol.C_SHAPE) {
                this.encodedFragment = canonicalize(
                        input, pos + 1, limit, FRAGMENT_ENCODE_SET, true, false, false, false, null);
            }

            return this;
        }

        private void resolvePath(String input, int pos, int limit) {
            if (pos == limit) {
                return;
            }
            char c = input.charAt(pos);
            if (c == Symbol.C_SLASH || c == Symbol.C_BACKSLASH) {
                encodedPathSegments.clear();
                encodedPathSegments.add(Normal.EMPTY);
                pos++;
            } else {
                encodedPathSegments.set(encodedPathSegments.size() - 1, Normal.EMPTY);
            }

            for (int i = pos; i < limit; ) {
                int pathSegmentDelimiterOffset = org.aoju.bus.http.Builder.delimiterOffset(input, i, limit, "/\\");
                boolean segmentHasTrailingSlash = pathSegmentDelimiterOffset < limit;
                push(input, i, pathSegmentDelimiterOffset, segmentHasTrailingSlash, true);
                i = pathSegmentDelimiterOffset;
                if (segmentHasTrailingSlash) i++;
            }
        }

        private void push(String input, int pos, int limit, boolean addTrailingSlash,
                          boolean alreadyEncoded) {
            String segment = canonicalize(
                    input, pos, limit, PATH_SEGMENT_ENCODE_SET, alreadyEncoded, false, false, true, null);
            if (isDot(segment)) {
                return;
            }
            if (isDotDot(segment)) {
                pop();
                return;
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

        private boolean isDot(String input) {
            return input.equals(Symbol.DOT) || input.equalsIgnoreCase("%2e");
        }

        private boolean isDotDot(String input) {
            return input.equals(Symbol.DOUBLE_DOT)
                    || input.equalsIgnoreCase("%2e.")
                    || input.equalsIgnoreCase(".%2e")
                    || input.equalsIgnoreCase("%2e%2e");
        }

        /**
         * 删除路径段。当这个方法返回时，最后一个段总是""，这意味着编码后的路径将以/结尾
         * 1. 出现 "/a/b/c/" yields "/a/b/". 在本例中，路径段的
         * 列表从["a", "b", "c", ""] to ["a", "b", ""].
         * 2. 出现 "/a/b/c" also yields "/a/b/". 路径段的
         * 列表从["a", "b", "c"] to ["a", "b", ""].
         */
        private void pop() {
            String removed = encodedPathSegments.remove(encodedPathSegments.size() - 1);
            if (removed.isEmpty() && !encodedPathSegments.isEmpty()) {
                encodedPathSegments.set(encodedPathSegments.size() - 1, Normal.EMPTY);
            } else {
                encodedPathSegments.add(Normal.EMPTY);
            }
        }
    }

}
