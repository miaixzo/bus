/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.core.net.Http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * 统一资源定位器(URL)，其模式为{@code http}或{@code https}。使用这个类来组合和分解Internet地址
 * 这个类有一个现代的API。它避免了惩罚性的检查异常:{@link #get get()}对无效的输入抛出{@link IllegalArgumentException}，
 * 或者{@link #parse parse()}如果输入是无效的URL，则返回null。您甚至可以明确每个组件是否已经编码
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UnoUrl {

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
     * "http" or "https"
     */
    final String scheme;
    /**
     * 规范的主机名
     */
    final String host;
    /**
     * 要么 80, 443 或用户指定的端口。范围内(1 . . 65535)
     */
    final int port;
    /**
     * 解码的用户名
     */
    private final String username;
    /**
     * 解码的密码
     */
    private final String password;
    /**
     * 规范路径段的列表。此列表始终包含至少一个元素，该元素可以是空字符串。
     * 每个段的格式是前导的‘/’，所以如果路径段是["a"， "b"， ""]，那么编码的路径就是"/a/b/".
     */
    private final List<String> pathSegments;

    /**
     * 交替，解码的查询名称和值，或空无查询。名称可以为空或非空，但绝不为空
     * 如果名称没有对应的'='分隔符，或为空，或为非空，则值为空.
     */
    private final List<String> queryNamesAndValues;

    /**
     * 解码片段
     */
    private final String fragment;

    /**
     * 规范的URL
     */
    private final String url;

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
        this.fragment = null != builder.encodedFragment
                ? percentDecode(builder.encodedFragment, false)
                : null;
        this.url = builder.toString();
    }

    /**
     * Returns 80 if {@code scheme.equals("http")}, 443 if {@code scheme.equals("https")} and -1
     * otherwise.
     */
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
            if (null != value) {
                out.append(Symbol.C_EQUAL);
                out.append(value);
            }
        }
    }

    /**
     * Cuts {@code encodedQuery} up into alternating parameter names and values. This divides a query
     * string like {@code subject=math&easy&problem=5-2=3} into the list {@code ["subject", "math",
     * "easy", null, "problem", "5-2=3"]}. Note that values may be null and may contain '='
     * characters.
     */
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

    /**
     * Returns a new {@code HttpUrl} representing {@code url} if it is a well-formed HTTP or HTTPS
     * URL, or null if it isn't.
     */
    public static UnoUrl parse(String url) {
        try {
            return get(url);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Returns a new {@code HttpUrl} representing {@code url}.
     *
     * @throws IllegalArgumentException If {@code url} is not a well-formed HTTP or HTTPS URL.
     */
    public static UnoUrl get(String url) {
        return new Builder().parse(null, url).build();
    }

    /**
     * Returns an {@link UnoUrl} for {@code url} if its protocol is {@code http} or {@code https}, or
     * null if it has any other protocol.
     */
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

    static boolean percentEncoded(String encoded, int pos, int limit) {
        return pos + 2 < limit
                && encoded.charAt(pos) == Symbol.C_PERCENT
                && org.miaixz.bus.http.Builder.decodeHexDigit(encoded.charAt(pos + 1)) != -1
                && org.miaixz.bus.http.Builder.decodeHexDigit(encoded.charAt(pos + 2)) != -1;
    }

    /**
     * Returns a substring of {@code input} on the range {@code [pos..limit)} with the following
     * transformations:
     * <ul>
     *   <li>Tabs, newlines, form feeds and carriage returns are skipped.
     *   <li>In queries, ' ' is encoded to '+' and '+' is encoded to "%2B".
     *   <li>Characters in {@code encodeSet} are percent-encoded.
     *   <li>Control characters and non-ASCII characters are percent-encoded.
     *   <li>All other characters are copied without transformation.
     * </ul>
     *
     * @param alreadyEncoded true to leave '%' as-is; false to convert it to '%25'.
     * @param strict         true to encode '%' if it is not the prefix of a valid percent encoding.
     * @param plusIsSpace    true to encode '+' as "%2B" if it is not already encoded.
     * @param asciiOnly      true to encode all non-ASCII codepoints.
     * @param charset        which charset to use, null equals UTF-8.
     */
    static String canonicalize(String input, int pos, int limit, String encodeSet,
                               boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly,
                               java.nio.charset.Charset charset) {
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
                             java.nio.charset.Charset charset) {
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
                out.writeUtf8CodePoint(codePoint);
            }
        }
    }

    public static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict,
                                      boolean plusIsSpace, boolean asciiOnly, java.nio.charset.Charset charset) {
        return canonicalize(
                input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly,
                charset);
    }

    static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict,
                               boolean plusIsSpace, boolean asciiOnly) {
        return canonicalize(
                input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, null);
    }

    /**
     * Returns this URL as a {@link URL java.net.URL}.
     */
    public URL url() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // Unexpected!
        }
    }

    /**
     * Returns this URL as a {@link URI java.net.URI}. Because {@code URI} is more strict than this
     * class, the returned URI may be semantically different from this URL:
     *
     * <ul>
     *     <li>Characters forbidden by URI like {@code [} and {@code |} will be escaped.
     *     <li>Invalid percent-encoded sequences like {@code %xx} will be encoded like {@code %25xx}.
     *     <li>Whitespace and control characters in the fragment will be stripped.
     * </ul>
     * <p>
     * These differences may have a significant consequence when the URI is interpreted by a
     * webserver. For this reason the {@linkplain URI URI class} and this method should be avoided.
     */
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

    /**
     * Returns either "http" or "https".
     */
    public String scheme() {
        return scheme;
    }

    public boolean isHttps() {
        return scheme.equals("https");
    }

    /**
     * Returns the username, or an empty string if none is set.
     */
    public String encodedUsername() {
        if (username.isEmpty()) return Normal.EMPTY;
        int usernameStart = scheme.length() + 3;
        int usernameEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, usernameStart, url.length(), ":@");
        return url.substring(usernameStart, usernameEnd);
    }

    /**
     * 返回已解码的用户名，如果不存在，则返回空字符串.
     *
     * <ul>
     * <li>{@code http://host/}{@code ""}</li>
     * <li>{@code http://username@host/}{@code "username"}</li>
     * <li>{@code http://username:password@host/}{@code "username"}</li>
     * <li>{@code http://a%20b:c%20d@host/}{@code "a b"}</li>
     * </ul>
     *
     * @return 用户信息
     */
    public String username() {
        return username;
    }

    /**
     * 返回密码，如果没有设置则返回空字符串.
     *
     * <ul>
     * <li>{@code http://host/}{@code ""}</li>
     * <li>{@code http://username@host/}{@code ""}</li>
     * <li>{@code http://username:password@host/}{@code "password"}</li>
     * <li>{@code http://a%20b:c%20d@host/}{@code "c%20d"}</li>
     * </ul>
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
     * <ul>
     * <li>{@code http://host/}{@code ""}</li>
     * <li>{@code http://username@host/}{@code ""}</li>
     * <li>{@code http://username:password@host/}{@code "password"}</li>
     * <li>{@code http://a%20b:c%20d@host/}{@code "c d"}</li>
     * </ul>
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
     * <ul>
     *   <li>{@code http://android.com/}{@code "android.com"}</li>
     *   <li>{@code http://127.0.0.1/}{@code "127.0.0.1"}</li>
     *   <li>{@code http://[::1]/}{@code "::1"}</li>
     *   <li>{@code http://xn--n3h.net/}{@code "xn--n3h.net"}</li>
     * </ul>
     *
     * @return 主机host
     */
    public String host() {
        return host;
    }

    /**
     * <ul>
     * <li>{@code http://host/}{@code 80}</li>
     * <li>{@code http://host:8000/}{@code 8000}</li>
     * <li>{@code https://host/}{@code 443}</li>
     * </ul>
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
     * <ul>
     * <li>{@code http://host/}{@code 1}</li>
     * <li>{@code http://host/a/b/c}{@code 3}</li>
     * <li>{@code http://host/a/b/c/}{@code 4}</li>
     * </ul>
     *
     * @return the size
     */
    public int pathSize() {
        return pathSegments.size();
    }

    /**
     * 该URL编码后用于HTTP资源解析。返回的路径将以{@code /}开始
     *
     * <ul>
     * <li>{@code http://host/}{@code /}</li>
     * <li>{@code http://host/a/b/c}{@code "/a/b/c"}</li>
     * <li>{@code http://host/a/b%20c/d}{@code "/a/b%20c/d"}</li>
     * </ul>
     *
     * @return URL的完整路径
     */
    public String encodedPath() {
        int pathStart = url.indexOf(Symbol.C_SLASH, scheme.length() + 3);
        int pathEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, pathStart, url.length(), "?#");
        return url.substring(pathStart, pathEnd);
    }

    /**
     * 返回一个已编码的路径段列表 {@code ["a", "b", "c"]} for the URL {@code
     * http://host/a/b/c}. 这个列表从不为空，尽管它可能包含一个空字符串.
     *
     * <ul>
     * <li>{@code http://host/}{@code [""]}</li>
     * <li>{@code http://host/a/b/c}{@code ["a", "b", "c"]}</li>
     * <li>{@code http://host/a/b%20c/d}{@code ["a", "b%20c", "d"]}</li>
     * </ul>
     *
     * @return 路径段列表
     */
    public List<String> encodedPathSegments() {
        int pathStart = url.indexOf(Symbol.C_SLASH, scheme.length() + 3);
        int pathEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, pathStart, url.length(), "?#");
        List<String> result = new ArrayList<>();
        for (int i = pathStart; i < pathEnd; ) {
            i++;
            int segmentEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, i, pathEnd, Symbol.C_SLASH);
            result.add(url.substring(i, segmentEnd));
            i = segmentEnd;
        }
        return result;
    }

    /**
     * Returns a list of path segments like {@code ["a", "b", "c"]} for the URL {@code
     * http://host/a/b/c}. This list is never empty though it may contain a single empty string.
     *
     * <ul>
     * <li>{@code http://host/}{@code [""]}</li>
     * <li>{@code http://host/a/b/c"}{@code ["a", "b", "c"]}</li>
     * <li>{@code http://host/a/b%20c/d"}{@code ["a", "b c", "d"]}</li>
     * </ul>
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
     * <ul>
     * <li>{@code http://host/}null</li>
     * <li>{@code http://host/?}{@code ""}</li>
     * <li>{@code http://host/?a=apple&k=key+lime}{@code
     * "a=apple&k=key+lime"}</li>
     * <li>{@code http://host/?a=apple&a=apricot}{@code "a=apple&a=apricot"}</li>
     * <li>{@code http://host/?a=apple&b}{@code "a=apple&b"}</li>
     * </ul>
     *
     * @return the string
     */
    public String encodedQuery() {
        if (null == queryNamesAndValues) return null;
        int queryStart = url.indexOf(Symbol.C_QUESTION_MARK) + 1;
        int queryEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, queryStart, url.length(), Symbol.C_SHAPE);
        return url.substring(queryStart, queryEnd);
    }

    /**
     * Returns this URL's query, like {@code "abc"} for {@code http://host/?abc}. Most callers should
     * prefer {@link #queryParameterName} and {@link #queryParameterValue} because these methods offer
     * direct access to individual query parameters.
     *
     * <ul>
     * <li>{@code http://host/}null</li>
     * <li>{@code http://host/?}{@code ""}</li>
     * <li>{@code http://host/?a=apple&k=key+lime}{@code "a=apple&k=key
     * lime"}</li>
     * <li>{@code http://host/?a=apple&a=apricot}{@code "a=apple&a=apricot"}</li>
     * <li>{@code http://host/?a=apple&b}{@code "a=apple&b"}</li>
     * </ul>
     *
     * @return the string
     */
    public String query() {
        if (null == queryNamesAndValues) return null;
        StringBuilder result = new StringBuilder();
        namesAndValuesToQueryString(result, queryNamesAndValues);
        return result.toString();
    }

    /**
     * Returns the number of query parameters in this URL, like 2 for {@code
     * http://host/?a=apple&b=banana}. If this URL has no query this returns 0. Otherwise it returns
     * one more than the number of {@code "&"} separators in the query.
     *
     * <ul>
     * <li>{@code http://host/}{@code 0}</li>
     * <li>{@code http://host/?}{@code 1}</li>
     * <li>{@code http://host/?a=apple&k=key+lime}{@code 2}</li>
     * <li>{@code http://host/?a=apple&a=apricot}{@code 2}</li>
     * <li>{@code http://host/?a=apple&b}{@code 2}</li>
     * </ul>
     *
     * @return the int
     */
    public int querySize() {
        return null != queryNamesAndValues ? queryNamesAndValues.size() / 2 : 0;
    }

    /**
     * Returns the first query parameter named {@code name} decoded using UTF-8, or null if there is
     * no such query parameter.
     *
     * <ul>
     * <li>{@code http://host/}null</li>
     * <li>{@code http://host/?}null</li>
     * <li>{@code http://host/?a=apple&k=key+lime}{@code "apple"}</li>
     * <li>{@code http://host/?a=apple&a=apricot}{@code "apple"}</li>
     * <li>{@code http://host/?a=apple&b}{@code "apple"}</li>
     * </ul>
     *
     * @param name 名称
     * @return the string
     */
    public String queryParameter(String name) {
        if (null == queryNamesAndValues) return null;
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
     * <ul>
     * <li>{@code http://host/}{@code []}</li>
     * <li>{@code http://host/?}{@code [""]}</li>
     * <li>{@code http://host/?a=apple&k=key+lime}{@code ["a", "k"]}</li>
     * <li>{@code http://host/?a=apple&a=apricot}{@code ["a"]}</li>
     * <li>{@code http://host/?a=apple&b}{@code ["a", "b"]}</li>
     * </ul>
     *
     * @return the set
     */
    public Set<String> queryParameterNames() {
        if (null == queryNamesAndValues) return Collections.emptySet();
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
     * <ul>
     * <li>{@code http://host/}{@code []}{@code []}</li>
     * <li>{@code http://host/?}{@code []}{@code []}</li>
     * <li>{@code http://host/?a=apple&k=key+lime}{@code ["apple"]}{@code
     * []}</li>
     * <li>{@code http://host/?a=apple&a=apricot}{@code ["apple",
     * "apricot"]}{@code []}</li>
     * <li>{@code http://host/?a=apple&b}{@code ["apple"]}{@code
     * [null]}</li>
     * </ul>
     *
     * @param name 名称
     * @return the list
     */
    public List<String> queryParameterValues(String name) {
        if (null == queryNamesAndValues) return Collections.emptyList();
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
     * <ul>
     * <li>{@code http://host/}exceptionexception</li>
     * <li>{@code http://host/?}{@code ""}exception</li>
     * <li>{@code http://host/?a=apple&k=key+lime}{@code "a"}{@code
     * "k"}</li>
     * <li>{@code http://host/?a=apple&a=apricot}{@code "a"}{@code
     * "a"}</li>
     * <li>{@code http://host/?a=apple&b}{@code "a"}{@code "b"}</li>
     * </ul>
     *
     * @param index 索引
     * @return the string
     */
    public String queryParameterName(int index) {
        if (null == queryNamesAndValues) throw new IndexOutOfBoundsException();
        return queryNamesAndValues.get(index * 2);
    }

    /**
     * Returns the value of the query parameter at {@code index}. For example this returns {@code
     * "apple"} for {@code queryParameterName(0)} on {@code http://host/?a=apple&b=banana}. This
     * throws if {@code index} is not less than the {@linkplain #querySize query size}.
     *
     * <ul>
     * <li>{@code http://host/}exceptionexception</li>
     * <li>{@code http://host/?}nullexception</li>
     * <li>{@code http://host/?a=apple&k=key+lime}{@code "apple"}{@code
     * "key lime"}</li>
     * <li>{@code http://host/?a=apple&a=apricot}{@code "apple"}{@code
     * "apricot"}</li>
     * <li>{@code http://host/?a=apple&b}{@code "apple"}null</li>
     * </ul>
     *
     * @param index 索引
     * @return the string
     */
    public String queryParameterValue(int index) {
        if (null == queryNamesAndValues) throw new IndexOutOfBoundsException();
        return queryNamesAndValues.get(index * 2 + 1);
    }

    /**
     * 返回这个URL的片段 {@code "abc"} for {@code http://host/#abc}. 如果URL没有片段，则返回null
     * <ul>
     * <li>{@code http://host/}null</li>
     * <li>{@code http://host/#}{@code ""}</li>
     * <li>{@code http://host/#abc}{@code "abc"}</li>
     * <li>{@code http://host/#abc|def}{@code "abc|def"}</li>
     * </ul>
     *
     * @return the string
     */
    public String encodedFragment() {
        if (null == fragment) return null;
        int fragmentStart = url.indexOf(Symbol.C_SHAPE) + 1;
        return url.substring(fragmentStart);
    }

    /**
     * 返回这个URL的片段 {@code "abc"} for {@code http://host/#abc}. 如果URL没有片段，则返回null
     * <ul>
     * <li>{@code http://host/}null</li>
     * <li>{@code http://host/#}{@code ""}</li>
     * <li>{@code http://host/#abc}{@code "abc"}</li>
     * <li>{@code http://host/#abc|def}{@code "abc|def"}</li>
     * </ul>
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
        return null != builder ? builder.build() : null;
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

    /**
     * Returns a builder for the URL that would be retrieved by following {@code link} from this URL,
     * or null if the resulting URL is not well-formed.
     */
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

    private List<String> percentDecode(List<String> list, boolean plusIsSpace) {
        int size = list.size();
        List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String s = list.get(i);
            result.add(null != s ? percentDecode(s, plusIsSpace) : null);
        }
        return Collections.unmodifiableList(result);
    }

    public static class Builder {

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

        /**
         * Returns the index of the ':' in {@code input} that is after scheme characters. Returns -1 if
         * {@code input} does not have a scheme that starts at {@code pos}.
         */
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
                        || c == Symbol.C_MINUS
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

        /**
         * Returns the number of '/' and '\' slashes in {@code input}, starting at {@code pos}.
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
         * 在{@code input}中查找第一个“:”，跳过方括号“[…]”之间的字符
         */
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
            return org.miaixz.bus.http.Builder.canonicalizeHost(percentDecode(input, pos, limit, false));
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
            if (null == scheme) {
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
            if (null == username) throw new NullPointerException("username == null");
            this.encodedUsername = canonicalize(username, USERNAME_ENCODE_SET, false, false, false, true);
            return this;
        }

        public Builder encodedUsername(String encodedUsername) {
            if (null == encodedUsername) throw new NullPointerException("encodedUsername == null");
            this.encodedUsername = canonicalize(
                    encodedUsername, USERNAME_ENCODE_SET, true, false, false, true);
            return this;
        }

        public Builder password(String password) {
            if (null == password) throw new NullPointerException("password == null");
            this.encodedPassword = canonicalize(password, PASSWORD_ENCODE_SET, false, false, false, true);
            return this;
        }

        public Builder encodedPassword(String encodedPassword) {
            if (null == encodedPassword) throw new NullPointerException("encodedPassword == null");
            this.encodedPassword = canonicalize(
                    encodedPassword, PASSWORD_ENCODE_SET, true, false, false, true);
            return this;
        }

        /**
         * @param host either a regular hostname, International Domain Name, IPv4 address, or IPv6
         *             address.
         */
        public Builder host(String host) {
            if (null == host) throw new NullPointerException("host == null");
            String encoded = canonicalizeHost(host, 0, host.length());
            if (null == encoded) throw new IllegalArgumentException("unexpected host: " + host);
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
            if (null == pathSegment) throw new NullPointerException("pathSegment == null");
            push(pathSegment, 0, pathSegment.length(), false, false);
            return this;
        }

        /**
         * Adds a set of path segments separated by a slash (either {@code \} or {@code /}). If
         * {@code pathSegments} starts with a slash, the resulting URL will have empty path segment.
         */
        public Builder addPathSegments(String pathSegments) {
            if (null == pathSegments) throw new NullPointerException("pathSegments == null");
            return addPathSegments(pathSegments, false);
        }

        public Builder addEncodedPathSegment(String encodedPathSegment) {
            if (null == encodedPathSegment) {
                throw new NullPointerException("encodedPathSegment == null");
            }
            push(encodedPathSegment, 0, encodedPathSegment.length(), false, true);
            return this;
        }

        /**
         * Adds a set of encoded path segments separated by a slash (either {@code \} or {@code /}). If
         * {@code encodedPathSegments} starts with a slash, the resulting URL will have empty path
         * segment.
         */
        public Builder addEncodedPathSegments(String encodedPathSegments) {
            if (null == encodedPathSegments) {
                throw new NullPointerException("encodedPathSegments == null");
            }
            return addPathSegments(encodedPathSegments, true);
        }

        private Builder addPathSegments(String pathSegments, boolean alreadyEncoded) {
            int offset = 0;
            do {
                int segmentEnd = org.miaixz.bus.http.Builder.delimiterOffset(pathSegments, offset, pathSegments.length(), "/\\");
                boolean addTrailingSlash = segmentEnd < pathSegments.length();
                push(pathSegments, offset, segmentEnd, addTrailingSlash, alreadyEncoded);
                offset = segmentEnd + 1;
            } while (offset <= pathSegments.length());
            return this;
        }

        public Builder setPathSegment(int index, String pathSegment) {
            if (null == pathSegment) throw new NullPointerException("pathSegment == null");
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
            if (null == encodedPathSegment) {
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
            if (null == encodedPath) throw new NullPointerException("encodedPath == null");
            if (!encodedPath.startsWith(Symbol.SLASH)) {
                throw new IllegalArgumentException("unexpected encodedPath: " + encodedPath);
            }
            resolvePath(encodedPath, 0, encodedPath.length());
            return this;
        }

        public Builder query(String query) {
            this.encodedQueryNamesAndValues = null != query
                    ? queryStringToNamesAndValues(canonicalize(
                    query, QUERY_ENCODE_SET, false, false, true, true))
                    : null;
            return this;
        }

        public Builder encodedQuery(String encodedQuery) {
            this.encodedQueryNamesAndValues = null != encodedQuery
                    ? queryStringToNamesAndValues(
                    canonicalize(encodedQuery, QUERY_ENCODE_SET, true, false, true, true))
                    : null;
            return this;
        }

        /**
         * Encodes the query parameter using UTF-8 and adds it to this URL's query string.
         */
        public Builder addQueryParameter(String name, String value) {
            if (null == name) throw new NullPointerException("name == null");
            if (null == encodedQueryNamesAndValues) encodedQueryNamesAndValues = new ArrayList<>();
            encodedQueryNamesAndValues.add(
                    canonicalize(name, QUERY_COMPONENT_ENCODE_SET, false, false, true, true));
            encodedQueryNamesAndValues.add(null != value
                    ? canonicalize(value, QUERY_COMPONENT_ENCODE_SET, false, false, true, true)
                    : null);
            return this;
        }

        /**
         * Adds the pre-encoded query parameter to this URL's query string.
         */
        public Builder addEncodedQueryParameter(String encodedName, String encodedValue) {
            if (null == encodedName) throw new NullPointerException("encodedName == null");
            if (null == encodedQueryNamesAndValues) encodedQueryNamesAndValues = new ArrayList<>();
            encodedQueryNamesAndValues.add(
                    canonicalize(encodedName, QUERY_COMPONENT_REENCODE_SET, true, false, true, true));
            encodedQueryNamesAndValues.add(null != encodedValue
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
            if (null == encodedName) throw new NullPointerException("encodedName == null");
            if (null == encodedQueryNamesAndValues) return this;
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
            this.encodedFragment = null != fragment
                    ? canonicalize(fragment, FRAGMENT_ENCODE_SET, false, false, false, false)
                    : null;
            return this;
        }

        public Builder encodedFragment(String encodedFragment) {
            this.encodedFragment = null != encodedFragment
                    ? canonicalize(encodedFragment, FRAGMENT_ENCODE_SET, true, false, false, false)
                    : null;
            return this;
        }

        /**
         * Re-encodes the components of this URL so that it satisfies (obsolete) RFC 2396, which is
         * particularly strict for certain components.
         */
        Builder reencodeForUri() {
            for (int i = 0, size = encodedPathSegments.size(); i < size; i++) {
                String pathSegment = encodedPathSegments.get(i);
                encodedPathSegments.set(i,
                        canonicalize(pathSegment, PATH_SEGMENT_ENCODE_SET_URI, true, true, false, true));
            }
            if (null != encodedQueryNamesAndValues) {
                for (int i = 0, size = encodedQueryNamesAndValues.size(); i < size; i++) {
                    String component = encodedQueryNamesAndValues.get(i);
                    if (null != component) {
                        encodedQueryNamesAndValues.set(i,
                                canonicalize(component, QUERY_COMPONENT_ENCODE_SET_URI, true, true, true, true));
                    }
                }
            }
            if (null != encodedFragment) {
                encodedFragment = canonicalize(
                        encodedFragment, FRAGMENT_ENCODE_SET_URI, true, true, false, false);
            }
            return this;
        }

        public UnoUrl build() {
            if (null == scheme) throw new IllegalStateException("scheme == null");
            if (null == host) throw new IllegalStateException("host == null");
            return new UnoUrl(this);
        }

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
                result.append(Symbol.C_SHAPE);
                result.append(encodedFragment);
            }

            return result.toString();
        }

        Builder parse(UnoUrl base, String input) {
            int pos = org.miaixz.bus.http.Builder.skipLeadingAsciiWhitespace(input, 0, input.length());
            int limit = org.miaixz.bus.http.Builder.skipTrailingAsciiWhitespace(input, pos, input.length());

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
            } else if (null != base) {
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
                    int componentDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, pos, limit, "@/\\?#");
                    int c = componentDelimiterOffset != limit
                            ? input.charAt(componentDelimiterOffset)
                            : -1;
                    switch (c) {
                        case Symbol.C_AT:
                            if (!hasPassword) {
                                int passwordColonOffset = org.miaixz.bus.http.Builder.delimiterOffset(
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
                            if (null == host) {
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

            int pathDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, pos, limit, "?#");
            resolvePath(input, pos, pathDelimiterOffset);
            pos = pathDelimiterOffset;

            if (pos < limit && input.charAt(pos) == Symbol.C_QUESTION_MARK) {
                int queryDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, pos, limit, Symbol.C_SHAPE);
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
                int pathSegmentDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, i, limit, "/\\");
                boolean segmentHasTrailingSlash = pathSegmentDelimiterOffset < limit;
                push(input, i, pathSegmentDelimiterOffset, segmentHasTrailingSlash, true);
                i = pathSegmentDelimiterOffset;
                if (segmentHasTrailingSlash) i++;
            }
        }

        /**
         * Adds a path segment. If the input is ".." or equivalent, this pops a path segment.
         */
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
