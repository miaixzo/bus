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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;

/**
 * HTTP 或 HTTPS 的统一资源定位器（URL）
 * <p>
 * 提供对 URL 的构建、解析和编码处理，支持 HTTP 和 HTTPS 协议。 避免使用检查型异常，解析无效 URL 时返回 null 或抛出 IllegalArgumentException。
 * 实例是不可变的，支持组件的编码和解码操作。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UnoUrl {

    /**
     * 用户名编码字符集
     */
    public static final String USERNAME_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    /**
     * 密码编码字符集
     */
    public static final String PASSWORD_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    /**
     * 路径段编码字符集
     */
    public static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";
    /**
     * URI 路径段编码字符集
     */
    public static final String PATH_SEGMENT_ENCODE_SET_URI = Symbol.BRACKET;
    /**
     * 查询参数编码字符集
     */
    public static final String QUERY_ENCODE_SET = " \"'<>#";
    /**
     * 查询组件重新编码字符集
     */
    public static final String QUERY_COMPONENT_REENCODE_SET = " \"'<>#&=";
    /**
     * 查询组件编码字符集
     */
    public static final String QUERY_COMPONENT_ENCODE_SET = " !\"#$&'(),/:;<=>?@[]\\^`{|}~";
    /**
     * URI 查询组件编码字符集
     */
    public static final String QUERY_COMPONENT_ENCODE_SET_URI = "\\^`{|}";
    /**
     * 表单编码字符集
     */
    public static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
    /**
     * 片段编码字符集
     */
    public static final String FRAGMENT_ENCODE_SET = Normal.EMPTY;
    /**
     * URI 片段编码字符集
     */
    public static final String FRAGMENT_ENCODE_SET_URI = " \"#<>\\^`{|}";
    /**
     * URL 协议（http 或 https）
     */
    final String scheme;
    /**
     * 规范化的主机名
     */
    final String host;
    /**
     * 端口号（80、443 或用户指定，范围 1-65535）
     */
    final int port;
    /**
     * 解码后的用户名
     */
    private final String username;
    /**
     * 解码后的密码
     */
    private final String password;
    /**
     * 解码后的路径段列表
     */
    private final List<String> pathSegments;
    /**
     * 解码后的查询参数名称和值列表
     */
    private final List<String> queryNamesAndValues;
    /**
     * 解码后的片段
     */
    private final String fragment;
    /**
     * 规范化的 URL 字符串
     */
    private final String url;

    /**
     * 构造函数，基于 Builder 初始化 UnoUrl 实例
     *
     * @param builder Builder 实例，包含所有 URL 组件
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
     * 获取默认端口号
     * <p>
     * 返回 80（http）、443（https）或 -1（其他协议）。
     * </p>
     *
     * @param scheme 协议名称
     * @return 默认端口号
     */
    public static int defaultPort(String scheme) {
        if (Protocol.HTTP.name.equals(scheme)) {
            return 80;
        } else if (Protocol.HTTPS.name.equals(scheme)) {
            return 443;
        } else {
            return -1;
        }
    }

    /**
     * 将路径段列表拼接为字符串
     *
     * @param out          输出 StringBuilder
     * @param pathSegments 路径段列表
     */
    static void pathSegmentsToString(StringBuilder out, List<String> pathSegments) {
        for (int i = 0, size = pathSegments.size(); i < size; i++) {
            out.append(Symbol.C_SLASH);
            out.append(pathSegments.get(i));
        }
    }

    /**
     * 将查询参数名称和值拼接为查询字符串
     *
     * @param out            输出 StringBuilder
     * @param namesAndValues 名称和值列表
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
     * 将编码的查询字符串解析为名称和值列表
     * <p>
     * 例如，解析 "subject=math&easy&problem=5-2=3" 得到 ["subject", "math", "easy", null, "problem", "5-2=3"]。
     * </p>
     *
     * @param encodedQuery 编码的查询字符串
     * @return 名称和值列表
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
                result.add(null);
            } else {
                result.add(encodedQuery.substring(pos, equalsOffset));
                result.add(encodedQuery.substring(equalsOffset + 1, ampersandOffset));
            }
            pos = ampersandOffset + 1;
        }
        return result;
    }

    /**
     * 解析 URL 字符串为 UnoUrl 实例
     * <p>
     * 如果 URL 格式有效，返回 UnoUrl 实例；否则返回 null。
     * </p>
     *
     * @param url URL 字符串
     * @return UnoUrl 实例或 null
     */
    public static UnoUrl parse(String url) {
        try {
            return get(url);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 构建 UnoUrl 实例
     * <p>
     * 如果 URL 格式无效，抛出 IllegalArgumentException。
     * </p>
     *
     * @param url URL 字符串
     * @return UnoUrl 实例
     * @throws IllegalArgumentException 如果 URL 格式无效
     */
    public static UnoUrl get(String url) {
        return new Builder().parse(null, url).build();
    }

    /**
     * 从 URL 对象构建 UnoUrl 实例
     * <p>
     * 仅支持 http 和 https 协议，非有效协议返回 null。
     * </p>
     *
     * @param url URL 对象
     * @return UnoUrl 实例或 null
     */
    public static UnoUrl get(URL url) {
        return parse(url.toString());
    }

    /**
     * 从 URI 对象构建 UnoUrl 实例
     *
     * @param uri URI 对象
     * @return UnoUrl 实例或 null
     */
    public static UnoUrl get(URI uri) {
        return parse(uri.toString());
    }

    /**
     * 解码百分比编码字符串
     *
     * @param encoded     编码字符串
     * @param plusIsSpace 是否将加号解码为空格
     * @return 解码后的字符串
     */
    public static String percentDecode(String encoded, boolean plusIsSpace) {
        return percentDecode(encoded, 0, encoded.length(), plusIsSpace);
    }

    /**
     * 解码百分比编码字符串（指定范围）
     *
     * @param encoded     编码字符串
     * @param pos         起始位置
     * @param limit       结束位置
     * @param plusIsSpace 是否将加号解码为空格
     * @return 解码后的字符串
     */
    public static String percentDecode(String encoded, int pos, int limit, boolean plusIsSpace) {
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

    /**
     * 解码百分比编码字符串到缓冲区
     *
     * @param out         输出缓冲区
     * @param encoded     编码字符串
     * @param pos         起始位置
     * @param limit       结束位置
     * @param plusIsSpace 是否将加号解码为空格
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
     * 检查字符串是否为百分比编码
     *
     * @param encoded 编码字符串
     * @param pos     起始位置
     * @param limit   结束位置
     * @return true 如果是有效的百分比编码
     */
    public static boolean percentEncoded(String encoded, int pos, int limit) {
        return pos + 2 < limit && encoded.charAt(pos) == Symbol.C_PERCENT
                && org.miaixz.bus.http.Builder.decodeHexDigit(encoded.charAt(pos + 1)) != -1
                && org.miaixz.bus.http.Builder.decodeHexDigit(encoded.charAt(pos + 2)) != -1;
    }

    /**
     * 规范化字符串
     * <p>
     * 跳过控制字符，编码指定字符集中的字符，处理百分比编码和加号。
     * </p>
     *
     * @param input          输入字符串
     * @param pos            起始位置
     * @param limit          结束位置
     * @param encodeSet      编码字符集
     * @param alreadyEncoded 是否已编码
     * @param strict         是否严格编码
     * @param plusIsSpace    是否将加号编码为空格
     * @param asciiOnly      是否仅限 ASCII
     * @param charset        字符集（null 为 UTF-8）
     * @return 规范化字符串
     */
    public static String canonicalize(String input, int pos, int limit, String encodeSet, boolean alreadyEncoded,
            boolean strict, boolean plusIsSpace, boolean asciiOnly, java.nio.charset.Charset charset) {
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (codePoint < 0x20 || codePoint == 0x7f || codePoint >= 0x80 && asciiOnly
                    || encodeSet.indexOf(codePoint) != -1
                    || codePoint == Symbol.C_PERCENT && (!alreadyEncoded || strict && !percentEncoded(input, i, limit))
                    || codePoint == Symbol.C_PLUS && plusIsSpace) {
                Buffer out = new Buffer();
                out.writeUtf8(input, pos, i);
                canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, charset);
                return out.readUtf8();
            }
        }
        return input.substring(pos, limit);
    }

    /**
     * 规范化字符串到缓冲区
     *
     * @param out            输出缓冲区
     * @param input          输入字符串
     * @param pos            起始位置
     * @param limit          结束位置
     * @param encodeSet      编码字符集
     * @param alreadyEncoded 是否已编码
     * @param strict         是否严格编码
     * @param plusIsSpace    是否将加号编码为空格
     * @param asciiOnly      是否仅限 ASCII
     * @param charset        字符集（null 为 UTF-8）
     */
    public static void canonicalize(Buffer out, String input, int pos, int limit, String encodeSet,
            boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly,
            java.nio.charset.Charset charset) {
        Buffer encodedCharBuffer = null;
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (alreadyEncoded && (codePoint == Symbol.C_HT || codePoint == Symbol.C_LF || codePoint == '\f'
                    || codePoint == Symbol.C_CR)) {
            } else if (codePoint == Symbol.C_PLUS && plusIsSpace) {
                out.writeUtf8(alreadyEncoded ? Symbol.PLUS : "%2B");
            } else if (codePoint < 0x20 || codePoint == 0x7f || codePoint >= 0x80 && asciiOnly
                    || encodeSet.indexOf(codePoint) != -1 || codePoint == Symbol.C_PERCENT
                            && (!alreadyEncoded || strict && !percentEncoded(input, i, limit))) {
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

    /**
     * 规范化字符串（默认 UTF-8）
     *
     * @param input          输入字符串
     * @param encodeSet      编码字符集
     * @param alreadyEncoded 是否已编码
     * @param strict         是否严格编码
     * @param plusIsSpace    是否将加号编码为空格
     * @param asciiOnly      是否仅限 ASCII
     * @param charset        字符集（null 为 UTF-8）
     * @return 规范化字符串
     */
    public static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict,
            boolean plusIsSpace, boolean asciiOnly, java.nio.charset.Charset charset) {
        return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly,
                charset);
    }

    /**
     * 规范化字符串（默认 UTF-8）
     *
     * @param input          输入字符串
     * @param encodeSet      编码字符集
     * @param alreadyEncoded 是否已编码
     * @param strict         是否严格编码
     * @param plusIsSpace    是否将加号编码为空格
     * @param asciiOnly      是否仅限 ASCII
     * @return 规范化字符串
     */
    public static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict,
            boolean plusIsSpace, boolean asciiOnly) {
        return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, null);
    }

    /**
     * 转换为 java.net.URL 对象
     *
     * @return URL 对象
     * @throws RuntimeException 如果 URL 格式无效
     */
    public URL url() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换为 java.net.URI 对象
     * <p>
     * 注意：URI 比 UnoUrl 更严格，可能对某些字符进行转义或移除（如片段中的空白）。 建议避免直接使用 URI，以免服务器解释差异。
     * </p>
     *
     * @return URI 对象
     * @throws RuntimeException 如果 URI 格式无效
     */
    public URI uri() {
        String uri = newBuilder().reencodeForUri().toString();
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            try {
                String stripped = uri.replaceAll("[\u0000-\u001F\u007F-\u009F\\p{javaWhitespace}]", Normal.EMPTY);
                return URI.create(stripped);
            } catch (Exception e1) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取协议
     *
     * @return 协议（http 或 https）
     */
    public String scheme() {
        return scheme;
    }

    /**
     * 检查是否为 HTTPS 协议
     *
     * @return true 如果协议为 HTTPS
     */
    public boolean isHttps() {
        return Protocol.isHttps(scheme);
    }

    /**
     * 获取编码后的用户名
     *
     * @return 编码后的用户名（空字符串如果未设置）
     */
    public String encodedUsername() {
        if (username.isEmpty())
            return Normal.EMPTY;
        int usernameStart = scheme.length() + 3;
        int usernameEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, usernameStart, url.length(), ":@");
        return url.substring(usernameStart, usernameEnd);
    }

    /**
     * 获取解码后的用户名
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
     * 获取编码后的密码
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
        if (password.isEmpty())
            return Normal.EMPTY;
        int passwordStart = url.indexOf(Symbol.C_COLON, scheme.length() + 3) + 1;
        int passwordEnd = url.indexOf(Symbol.C_AT);
        return url.substring(passwordStart, passwordEnd);
    }

    /**
     * 获取解码后的密码
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
     * 获取主机名
     *
     * <ul>
     * <li>A regular host name, like {@code android.com}.
     * <li>An IPv4 address, like {@code 127.0.0.1}.
     * <li>An IPv6 address, like {@code ::1}.
     * <li>An encoded IDN, like {@code xn--n3h.net}.
     * </ul>
     *
     * <ul>
     * <li>{@code http://android.com/}{@code "android.com"}</li>
     * <li>{@code http://127.0.0.1/}{@code "127.0.0.1"}</li>
     * <li>{@code http://[::1]/}{@code "::1"}</li>
     * <li>{@code http://xn--n3h.net/}{@code "xn--n3h.net"}</li>
     * </ul>
     *
     * @return 主机host
     */
    public String host() {
        return host;
    }

    /**
     * 获取端口号
     *
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
     * 获取路径段数量
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
     * 获取编码后的路径
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
     * 获取编码后的路径段列表
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
        for (int i = pathStart; i < pathEnd;) {
            i++;
            int segmentEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, i, pathEnd, Symbol.C_SLASH);
            result.add(url.substring(i, segmentEnd));
            i = segmentEnd;
        }
        return result;
    }

    /**
     * 获取解码后的路径段列表
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
     * 获取编码后的查询字符串
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
        if (null == queryNamesAndValues)
            return null;
        int queryStart = url.indexOf(Symbol.C_QUESTION_MARK) + 1;
        int queryEnd = org.miaixz.bus.http.Builder.delimiterOffset(url, queryStart, url.length(), Symbol.C_HASH);
        return url.substring(queryStart, queryEnd);
    }

    /**
     * 获取解码后的查询字符串
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
        if (null == queryNamesAndValues)
            return null;
        StringBuilder result = new StringBuilder();
        namesAndValuesToQueryString(result, queryNamesAndValues);
        return result.toString();
    }

    /**
     * 获取查询参数数量
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
     * 获取指定名称的第一个查询参数值
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
     * 获取查询参数名称集合
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
        if (null == queryNamesAndValues)
            return Collections.emptySet();
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0, size = queryNamesAndValues.size(); i < size; i += 2) {
            result.add(queryNamesAndValues.get(i));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 获取指定名称的所有查询参数值
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
     * 获取指定索引的查询参数名称
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
     * @return 参数名称
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public String queryParameterName(int index) {
        if (null == queryNamesAndValues)
            throw new IndexOutOfBoundsException();
        return queryNamesAndValues.get(index * 2);
    }

    /**
     * 获取指定索引的查询参数值
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
     * @return 参数值
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public String queryParameterValue(int index) {
        if (null == queryNamesAndValues)
            throw new IndexOutOfBoundsException();
        return queryNamesAndValues.get(index * 2 + 1);
    }

    /**
     * 获取编码后的片段
     *
     * <ul>
     * <li>{@code http://host/}null</li>
     * <li>{@code http://host/#}{@code ""}</li>
     * <li>{@code http://host/#abc}{@code "abc"}</li>
     * <li>{@code http://host/#abc|def}{@code "abc|def"}</li>
     * </ul>
     *
     * @return 编码后的片段（可能为 null）
     */
    public String encodedFragment() {
        if (null == fragment)
            return null;
        int fragmentStart = url.indexOf(Symbol.C_HASH) + 1;
        return url.substring(fragmentStart);
    }

    /**
     * 获取解码后的片段
     *
     * <ul>
     * <li>{@code http://host/}null</li>
     * <li>{@code http://host/#}{@code ""}</li>
     * <li>{@code http://host/#abc}{@code "abc"}</li>
     * <li>{@code http://host/#abc|def}{@code "abc|def"}</li>
     * </ul>
     *
     * @return 解码后的片段（可能为 null）
     */
    public String fragment() {
        return fragment;
    }

    /**
     * 返回隐藏敏感信息的 URL
     *
     * @return 隐藏用户名和密码的 URL 字符串
     */
    public String redact() {
        return newBuilder("/...").username(Normal.EMPTY).password(Normal.EMPTY).build().toString();
    }

    /**
     * 解析相对链接
     *
     * @param link 相对链接
     * @return 解析后的 UnoUrl 实例（无效时为 null）
     */
    public UnoUrl resolve(String link) {
        Builder builder = newBuilder(link);
        return null != builder ? builder.build() : null;
    }

    /**
     * 创建新的 Builder 实例
     *
     * @return Builder 实例
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
     * 创建相对链接的 Builder 实例
     *
     * @param link 相对链接
     * @return Builder 实例（无效时为 null）
     */
    public Builder newBuilder(String link) {
        try {
            return new Builder().parse(this, link);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 比较两个 UnoUrl 对象是否相等
     *
     * @param other 另一个对象
     * @return true 如果两个 URL 相等
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof UnoUrl && ((UnoUrl) other).url.equals(url);
    }

    /**
     * 计算 URL 的哈希码
     *
     * @return 哈希码值
     */
    @Override
    public int hashCode() {
        return url.hashCode();
    }

    /**
     * 返回 URL 的字符串表示
     *
     * @return URL 字符串
     */
    @Override
    public String toString() {
        return url;
    }

    /**
     * 解码字符串列表
     *
     * @param list        字符串列表
     * @param plusIsSpace 是否将加号解码为空格
     * @return 解码后的字符串列表
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
     * URL 构建器
     */
    public static class Builder {

        /**
         * 无效主机名错误消息
         */
        static final String INVALID_HOST = "Invalid URL host";
        /**
         * 编码后的路径段列表
         */
        final List<String> encodedPathSegments = new ArrayList<>();
        /**
         * 协议
         */
        String scheme;
        /**
         * 编码后的用户名
         */
        String encodedUsername = Normal.EMPTY;
        /**
         * 编码后的密码
         */
        String encodedPassword = Normal.EMPTY;
        /**
         * 主机名
         */
        String host;
        /**
         * 端口号
         */
        int port = -1;
        /**
         * 编码后的查询参数名称和值
         */
        List<String> encodedQueryNamesAndValues;
        /**
         * 编码后的片段
         */
        String encodedFragment;

        /**
         * 默认构造函数
         */
        public Builder() {
            encodedPathSegments.add(Normal.EMPTY);
        }

        /**
         * 查找协议分隔符（:）的位置
         *
         * @param input 输入字符串
         * @param pos   起始位置
         * @param limit 结束位置
         * @return 分隔符位置（不存在时为 -1）
         */
        private static int schemeDelimiterOffset(String input, int pos, int limit) {
            if (limit - pos < 2)
                return -1;

            char c0 = input.charAt(pos);
            if ((c0 < 'a' || c0 > 'z') && (c0 < 'A' || c0 > 'Z'))
                return -1;

            for (int i = pos + 1; i < limit; i++) {
                char c = input.charAt(i);

                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= Symbol.C_ZERO && c <= Symbol.C_NINE)
                        || c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_DOT) {
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
         * 计算斜杠数量
         *
         * @param input 输入字符串
         * @param pos   起始位置
         * @param limit 结束位置
         * @return 斜杠数量
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
         * 查找端口分隔符（:）的位置
         *
         * @param input 输入字符串
         * @param pos   起始位置
         * @param limit 结束位置
         * @return 分隔符位置
         */
        private static int portColonOffset(String input, int pos, int limit) {
            for (int i = pos; i < limit; i++) {
                switch (input.charAt(i)) {
                case Symbol.C_BRACKET_LEFT:
                    while (++i < limit) {
                        if (input.charAt(i) == Symbol.C_BRACKET_RIGHT)
                            break;
                    }
                    break;
                case Symbol.C_COLON:
                    return i;
                }
            }
            return limit;
        }

        /**
         * 规范化主机名
         *
         * @param input 输入字符串
         * @param pos   起始位置
         * @param limit 结束位置
         * @return 规范化主机名
         */
        private static String canonicalizeHost(String input, int pos, int limit) {
            return org.miaixz.bus.http.Builder.canonicalizeHost(percentDecode(input, pos, limit, false));
        }

        /**
         * 解析端口号
         *
         * @param input 输入字符串
         * @param pos   起始位置
         * @param limit 结束位置
         * @return 端口号（无效时为 -1）
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
         * 设置协议
         *
         * @param scheme 协议（http 或 https）
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 scheme 为 null
         * @throws IllegalArgumentException 如果协议无效
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
         * 设置用户名
         *
         * @param username 用户名
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 username 为 null
         */
        public Builder username(String username) {
            if (null == username)
                throw new NullPointerException("username == null");
            this.encodedUsername = canonicalize(username, USERNAME_ENCODE_SET, false, false, false, true);
            return this;
        }

        /**
         * 设置编码后的用户名
         *
         * @param encodedUsername 编码后的用户名
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 encodedUsername 为 null
         */
        public Builder encodedUsername(String encodedUsername) {
            if (null == encodedUsername)
                throw new NullPointerException("encodedUsername == null");
            this.encodedUsername = canonicalize(encodedUsername, USERNAME_ENCODE_SET, true, false, false, true);
            return this;
        }

        /**
         * 设置密码
         *
         * @param password 密码
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 password 为 null
         */
        public Builder password(String password) {
            if (null == password)
                throw new NullPointerException("password == null");
            this.encodedPassword = canonicalize(password, PASSWORD_ENCODE_SET, false, false, false, true);
            return this;
        }

        /**
         * 设置编码后的密码
         *
         * @param encodedPassword 编码后的密码
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 encodedPassword 为 null
         */
        public Builder encodedPassword(String encodedPassword) {
            if (null == encodedPassword)
                throw new NullPointerException("encodedPassword == null");
            this.encodedPassword = canonicalize(encodedPassword, PASSWORD_ENCODE_SET, true, false, false, true);
            return this;
        }

        /**
         * 设置主机名
         *
         * @param host 主机名（普通主机名、IPv4、IPv6 或编码的 IDN）
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 host 为 null
         * @throws IllegalArgumentException 如果主机名无效
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
         * 设置端口号
         *
         * @param port 端口号
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果端口号无效
         */
        public Builder port(int port) {
            if (port <= 0 || port > 65535)
                throw new IllegalArgumentException("unexpected port: " + port);
            this.port = port;
            return this;
        }

        /**
         * 获取有效端口号
         *
         * @return 有效端口号
         */
        int effectivePort() {
            return port != -1 ? port : defaultPort(scheme);
        }

        /**
         * 添加路径段
         *
         * @param pathSegment 路径段
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 pathSegment 为 null
         */
        public Builder addPathSegment(String pathSegment) {
            if (null == pathSegment)
                throw new NullPointerException("pathSegment == null");
            push(pathSegment, 0, pathSegment.length(), false, false);
            return this;
        }

        /**
         * 添加路径段列表
         *
         * @param pathSegments 路径段字符串
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 pathSegments 为 null
         */
        public Builder addPathSegments(String pathSegments) {
            if (null == pathSegments)
                throw new NullPointerException("pathSegments == null");
            return addPathSegments(pathSegments, false);
        }

        /**
         * 添加编码后的路径段
         *
         * @param encodedPathSegment 编码后的路径段
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 encodedPathSegment 为 null
         */
        public Builder addEncodedPathSegment(String encodedPathSegment) {
            if (null == encodedPathSegment) {
                throw new NullPointerException("encodedPathSegment == null");
            }
            push(encodedPathSegment, 0, encodedPathSegment.length(), false, true);
            return this;
        }

        /**
         * 添加编码后的路径段列表
         *
         * @param encodedPathSegments 编码后的路径段字符串
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 encodedPathSegments 为 null
         */
        public Builder addEncodedPathSegments(String encodedPathSegments) {
            if (null == encodedPathSegments) {
                throw new NullPointerException("encodedPathSegments == null");
            }
            return addPathSegments(encodedPathSegments, true);
        }

        /**
         * 添加路径段（内部实现）
         *
         * @param pathSegments   路径段字符串
         * @param alreadyEncoded 是否已编码
         * @return 当前 Builder 实例
         */
        private Builder addPathSegments(String pathSegments, boolean alreadyEncoded) {
            int offset = 0;
            do {
                int segmentEnd = org.miaixz.bus.http.Builder.delimiterOffset(pathSegments, offset,
                        pathSegments.length(), "/\\");
                boolean addTrailingSlash = segmentEnd < pathSegments.length();
                push(pathSegments, offset, segmentEnd, addTrailingSlash, alreadyEncoded);
                offset = segmentEnd + 1;
            } while (offset <= pathSegments.length());
            return this;
        }

        /**
         * 设置路径段
         *
         * @param index       索引
         * @param pathSegment 路径段
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 pathSegment 为 null
         * @throws IllegalArgumentException 如果路径段无效
         */
        public Builder setPathSegment(int index, String pathSegment) {
            if (null == pathSegment)
                throw new NullPointerException("pathSegment == null");
            String canonicalPathSegment = canonicalize(pathSegment, 0, pathSegment.length(), PATH_SEGMENT_ENCODE_SET,
                    false, false, false, true, null);
            if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
                throw new IllegalArgumentException("unexpected path segment: " + pathSegment);
            }
            encodedPathSegments.set(index, canonicalPathSegment);
            return this;
        }

        /**
         * 设置编码后的路径段
         *
         * @param index              索引
         * @param encodedPathSegment 编码后的路径段
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 encodedPathSegment 为 null
         * @throws IllegalArgumentException 如果路径段无效
         */
        public Builder setEncodedPathSegment(int index, String encodedPathSegment) {
            if (null == encodedPathSegment) {
                throw new NullPointerException("encodedPathSegment == null");
            }
            String canonicalPathSegment = canonicalize(encodedPathSegment, 0, encodedPathSegment.length(),
                    PATH_SEGMENT_ENCODE_SET, true, false, false, true, null);
            encodedPathSegments.set(index, canonicalPathSegment);
            if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
                throw new IllegalArgumentException("unexpected path segment: " + encodedPathSegment);
            }
            return this;
        }

        /**
         * 移除路径段
         *
         * @param index 索引
         * @return 当前 Builder 实例
         */
        public Builder removePathSegment(int index) {
            encodedPathSegments.remove(index);
            if (encodedPathSegments.isEmpty()) {
                encodedPathSegments.add(Normal.EMPTY);
            }
            return this;
        }

        /**
         * 设置编码后的路径
         *
         * @param encodedPath 编码后的路径
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 encodedPath 为 null
         * @throws IllegalArgumentException 如果路径无效
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
         * 设置查询字符串
         *
         * @param query 查询字符串
         * @return 当前 Builder 实例
         */
        public Builder query(String query) {
            this.encodedQueryNamesAndValues = null != query
                    ? queryStringToNamesAndValues(canonicalize(query, QUERY_ENCODE_SET, false, false, true, true))
                    : null;
            return this;
        }

        /**
         * 设置编码后的查询字符串
         *
         * @param encodedQuery 编码后的查询字符串
         * @return 当前 Builder 实例
         */
        public Builder encodedQuery(String encodedQuery) {
            this.encodedQueryNamesAndValues = null != encodedQuery
                    ? queryStringToNamesAndValues(canonicalize(encodedQuery, QUERY_ENCODE_SET, true, false, true, true))
                    : null;
            return this;
        }

        /**
         * 添加查询参数
         *
         * @param name  参数名称
         * @param value 参数值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 为 null
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
         * 添加编码后的查询参数
         *
         * @param encodedName  编码后的参数名称
         * @param encodedValue 编码后的参数值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 encodedName 为 null
         */
        public Builder addEncodedQueryParameter(String encodedName, String encodedValue) {
            if (null == encodedName)
                throw new NullPointerException("encodedName == null");
            if (null == encodedQueryNamesAndValues)
                encodedQueryNamesAndValues = new ArrayList<>();
            encodedQueryNamesAndValues
                    .add(canonicalize(encodedName, QUERY_COMPONENT_REENCODE_SET, true, false, true, true));
            encodedQueryNamesAndValues.add(null != encodedValue
                    ? canonicalize(encodedValue, QUERY_COMPONENT_REENCODE_SET, true, false, true, true)
                    : null);
            return this;
        }

        /**
         * 设置查询参数（替换现有参数）
         *
         * @param name  参数名称
         * @param value 参数值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 为 null
         */
        public Builder setQueryParameter(String name, String value) {
            removeAllQueryParameters(name);
            addQueryParameter(name, value);
            return this;
        }

        /**
         * 设置编码后的查询参数（替换现有参数）
         *
         * @param encodedName  编码后的参数名称
         * @param encodedValue 编码后的参数值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 encodedName 为 null
         */
        public Builder setEncodedQueryParameter(String encodedName, String encodedValue) {
            removeAllEncodedQueryParameters(encodedName);
            addEncodedQueryParameter(encodedName, encodedValue);
            return this;
        }

        /**
         * 移除指定名称的所有查询参数
         *
         * @param name 参数名称
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 为 null
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
         * 移除编码后的所有查询参数
         *
         * @param encodedName 编码后的参数名称
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 encodedName 为 null
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
         * 移除规范化的查询参数
         *
         * @param canonicalName 规范化参数名称
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
         * 设置片段
         *
         * @param fragment 片段
         * @return 当前 Builder 实例
         */
        public Builder fragment(String fragment) {
            this.encodedFragment = null != fragment
                    ? canonicalize(fragment, FRAGMENT_ENCODE_SET, false, false, false, false)
                    : null;
            return this;
        }

        /**
         * 设置编码后的片段
         *
         * @param encodedFragment 编码后的片段
         * @return 当前 Builder 实例
         */
        public Builder encodedFragment(String encodedFragment) {
            this.encodedFragment = null != encodedFragment
                    ? canonicalize(encodedFragment, FRAGMENT_ENCODE_SET, true, false, false, false)
                    : null;
            return this;
        }

        /**
         * 为 URI 重新编码 URL 组件
         *
         * @return 当前 Builder 实例
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
                encodedFragment = canonicalize(encodedFragment, FRAGMENT_ENCODE_SET_URI, true, true, false, false);
            }
            return this;
        }

        /**
         * 构建 UnoUrl 实例
         *
         * @return UnoUrl 实例
         * @throws IllegalStateException 如果 scheme 或 host 未设置
         */
        public UnoUrl build() {
            if (null == scheme)
                throw new IllegalStateException("scheme == null");
            if (null == host)
                throw new IllegalStateException("host == null");
            return new UnoUrl(this);
        }

        /**
         * 返回 URL 的字符串表示
         *
         * @return URL 字符串
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
         * 解析 URL 字符串
         *
         * @param base  基础 URL
         * @param input 输入字符串
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果 URL 格式无效
         */
        Builder parse(UnoUrl base, String input) {
            int pos = org.miaixz.bus.http.Builder.skipLeadingAsciiWhitespace(input, 0, input.length());
            int limit = org.miaixz.bus.http.Builder.skipTrailingAsciiWhitespace(input, pos, input.length());

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

            boolean hasUsername = false;
            boolean hasPassword = false;
            int slashCount = slashCount(input, pos, limit);
            if (slashCount >= 2 || base == null || !base.scheme.equals(this.scheme)) {
                pos += slashCount;
                authority: while (true) {
                    int componentDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, pos, limit,
                            "@/\\?#");
                    int c = componentDelimiterOffset != limit ? input.charAt(componentDelimiterOffset) : -1;
                    switch (c) {
                    case Symbol.C_AT:
                        if (!hasPassword) {
                            int passwordColonOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, pos,
                                    componentDelimiterOffset, Symbol.C_COLON);
                            String canonicalUsername = canonicalize(input, pos, passwordColonOffset,
                                    USERNAME_ENCODE_SET, true, false, false, true, null);
                            this.encodedUsername = hasUsername ? this.encodedUsername + "%40" + canonicalUsername
                                    : canonicalUsername;
                            if (passwordColonOffset != componentDelimiterOffset) {
                                hasPassword = true;
                                this.encodedPassword = canonicalize(input, passwordColonOffset + 1,
                                        componentDelimiterOffset, PASSWORD_ENCODE_SET, true, false, false, true, null);
                            }
                            hasUsername = true;
                        } else {
                            this.encodedPassword = this.encodedPassword + "%40" + canonicalize(input, pos,
                                    componentDelimiterOffset, PASSWORD_ENCODE_SET, true, false, false, true, null);
                        }
                        pos = componentDelimiterOffset + 1;
                        break;

                    case -1:
                    case Symbol.C_SLASH:
                    case Symbol.C_BACKSLASH:
                    case Symbol.C_QUESTION_MARK:
                    case Symbol.C_HASH:
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

            int pathDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, pos, limit, "?#");
            resolvePath(input, pos, pathDelimiterOffset);
            pos = pathDelimiterOffset;

            if (pos < limit && input.charAt(pos) == Symbol.C_QUESTION_MARK) {
                int queryDelimiterOffset = org.miaixz.bus.http.Builder.delimiterOffset(input, pos, limit,
                        Symbol.C_HASH);
                this.encodedQueryNamesAndValues = queryStringToNamesAndValues(canonicalize(input, pos + 1,
                        queryDelimiterOffset, QUERY_ENCODE_SET, true, false, true, true, null));
                pos = queryDelimiterOffset;
            }

            if (pos < limit && input.charAt(pos) == Symbol.C_HASH) {
                this.encodedFragment = canonicalize(input, pos + 1, limit, FRAGMENT_ENCODE_SET, true, false, false,
                        false, null);
            }

            return this;
        }

        /**
         * 解析路径
         *
         * @param input 输入字符串
         * @param pos   起始位置
         * @param limit 结束位置
         */
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
         * 添加路径段
         *
         * @param input            输入字符串
         * @param pos              起始位置
         * @param limit            结束位置
         * @param addTrailingSlash 是否添加尾部斜杠
         * @param alreadyEncoded   是否已编码
         */
        private void push(String input, int pos, int limit, boolean addTrailingSlash, boolean alreadyEncoded) {
            String segment = canonicalize(input, pos, limit, PATH_SEGMENT_ENCODE_SET, alreadyEncoded, false, false,
                    true, null);
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

        /**
         * 检查是否为点（.）
         *
         * @param input 输入字符串
         * @return true 如果是点
         */
        private boolean isDot(String input) {
            return input.equals(Symbol.DOT) || input.equalsIgnoreCase("%2e");
        }

        /**
         * 检查是否为双点（..）
         *
         * @param input 输入字符串
         * @return true 如果是双点
         */
        private boolean isDotDot(String input) {
            return input.equals(Symbol.DOUBLE_DOT) || input.equalsIgnoreCase("%2e.") || input.equalsIgnoreCase(".%2e")
                    || input.equalsIgnoreCase("%2e%2e");
        }

        /**
         * 移除路径段
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