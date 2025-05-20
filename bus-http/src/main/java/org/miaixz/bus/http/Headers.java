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
 * HTTP 消息的头部字段
 * <p>
 * 维护头部字段的顺序，值存储为未解释的字符串，移除首尾空白。 实例是不可变的，建议通过 {@link Request} 和 {@link Response} 解释头部信息。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Headers {

    /**
     * 头部名称和值数组
     */
    private final String[] namesAndValues;

    /**
     * 构造函数，基于 Builder 初始化 Headers 实例
     *
     * @param builder Builder 实例，包含头部名称和值
     */
    Headers(Builder builder) {
        this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
    }

    /**
     * 构造函数，直接使用名称和值数组
     *
     * @param namesAndValues 名称和值数组
     */
    private Headers(String[] namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    /**
     * 获取指定名称的最后一个头部值
     *
     * @param namesAndValues 名称和值数组
     * @param name           头部名称
     * @return 头部值（不存在时为 null）
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
     * 从名称和值数组创建 Headers 实例
     * <p>
     * 要求参数数量为偶数，交替为名称和值。
     * </p>
     *
     * @param namesAndValues 名称和值数组
     * @return Headers 实例
     * @throws NullPointerException     如果 namesAndValues 为 null
     * @throws IllegalArgumentException 如果参数数量奇数或包含 null
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
     * 从映射创建 Headers 实例
     *
     * @param headers 头部名称和值映射
     * @return Headers 实例
     * @throws NullPointerException     如果 headers 为 null
     * @throws IllegalArgumentException 如果名称或值包含 null
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
     * 验证头部名称
     *
     * @param name 头部名称
     * @throws NullPointerException     如果 name 为 null
     * @throws IllegalArgumentException 如果名称为空或包含非法字符
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
     * 验证头部值
     *
     * @param value 头部值
     * @param name  头部名称
     * @throws NullPointerException     如果 value 为 null
     * @throws IllegalArgumentException 如果值包含非法字符
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
     * 获取响应的 Content-Length
     *
     * @param response 响应
     * @return Content-Length 值（无效时为 -1）
     */
    public static long contentLength(Response response) {
        return contentLength(response.headers());
    }

    /**
     * 获取头部的 Content-Length
     *
     * @param headers 头部
     * @return Content-Length 值（无效时为 -1）
     */
    public static long contentLength(Headers headers) {
        return stringToLong(headers.get("Content-Length"));
    }

    /**
     * 将字符串转换为长整型
     *
     * @param s 字符串
     * @return 长整型值（无效时为 -1）
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
     * 检查 Vary 头部是否匹配
     *
     * @param cachedResponse 缓存响应
     * @param cachedRequest  缓存请求
     * @param newRequest     新请求
     * @return true 如果 Vary 头部匹配
     */
    public static boolean varyMatches(Response cachedResponse, Headers cachedRequest, Request newRequest) {
        for (String field : varyFields(cachedResponse)) {
            if (!Objects.equals(cachedRequest.values(field), newRequest.headers(field)))
                return false;
        }
        return true;
    }

    /**
     * 检查是否存在 Vary: * 头部
     *
     * @param response 响应
     * @return true 如果存在 Vary: *
     */
    public static boolean hasVaryAll(Response response) {
        return hasVaryAll(response.headers());
    }

    /**
     * 检查是否存在 Vary: * 头部
     *
     * @param responseHeaders 响应头部
     * @return true 如果存在 Vary: *
     */
    public static boolean hasVaryAll(Headers responseHeaders) {
        return varyFields(responseHeaders).contains(Symbol.STAR);
    }

    /**
     * 获取 Vary 字段
     *
     * @param response 响应
     * @return Vary 字段集合
     */
    private static Set<String> varyFields(Response response) {
        return varyFields(response.headers());
    }

    /**
     * 获取 Vary 字段集合
     *
     * @param responseHeaders 响应头部
     * @return Vary 字段集合
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
     * 获取影响响应体的请求头部
     *
     * @param response 响应
     * @return 影响响应体的头部
     */
    public static Headers varyHeaders(Response response) {
        Headers requestHeaders = response.networkResponse().request().headers();
        Headers responseHeaders = response.headers();
        return varyHeaders(requestHeaders, responseHeaders);
    }

    /**
     * 获取影响响应体的请求头部
     *
     * @param requestHeaders  请求头部
     * @param responseHeaders 响应头部
     * @return 影响响应体的头部
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
     * 解析 RFC 7235 认证
     *
     * @param responseHeaders 响应头部
     * @param headerName      头部名称
     * @return 认证挑战列表
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
     * 解析认证头部
     *
     * @param result 列表
     * @param header 头部缓冲区
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
                result.add(new Challenge(schemeName,
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
     * 跳过空白和逗号
     *
     * @param buffer 缓冲区
     * @return true 如果跳过了逗号
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
     * 跳过指定字节
     *
     * @param buffer 缓冲区
     * @param b      字节
     * @return 跳过的字节数
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
     * 读取双引号字符串
     *
     * @param buffer 缓冲区
     * @return 解码后的字符串（无效时为 null）
     * @throws IllegalArgumentException 如果字符串格式无效
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
     * 读取令牌
     *
     * @param buffer 缓冲区
     * @return 令牌字符串（无效时为 null）
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
     * 重复字符
     *
     * @param c     字符
     * @param count 重复次数
     * @return 重复后的字符串
     */
    private static String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }

    /**
     * 处理接收到的 Cookie 头部
     *
     * @param cookieJar Cookie 管理器
     * @param url       URL
     * @param headers   头部
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
     * 检查响应是否包含消息体
     *
     * @param response 响应
     * @return true 如果响应包含消息体
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
     * 跳到指定字符
     *
     * @param input      输入字符串
     * @param pos        起始位置
     * @param characters 目标字符集
     * @return 目标字符位置
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
     * 跳过空白字符
     *
     * @param input 输入字符串
     * @param pos   起始位置
     * @return 非空白字符位置
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
     * 解析秒数
     *
     * @param value        字符串值
     * @param defaultValue 默认值
     * @return 秒数
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
     * 创建新的 Builder 实例
     *
     * @return Builder 实例
     */
    public Builder newBuilder() {
        Builder result = new Builder();
        Collections.addAll(result.namesAndValues, namesAndValues);
        return result;
    }

    /**
     * 计算哈希码
     *
     * @return 哈希码值
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(namesAndValues);
    }

    /**
     * 获取指定名称的最后一个头部值
     *
     * @param name 头部名称
     * @return 头部值（不存在时为 null）
     */
    public String get(String name) {
        return get(namesAndValues, name);
    }

    /**
     * 获取指定名称的日期头部值
     *
     * @param name 头部名称
     * @return 日期值（无效时为 null）
     */
    public Date getDate(String name) {
        String value = get(name);
        return value != null ? org.miaixz.bus.http.Builder.parse(value) : null;
    }

    /**
     * 获取指定名称的 Instant 头部值
     *
     * @param name 头部名称
     * @return Instant 值（无效时为 null）
     */
    public Instant getInstant(String name) {
        Date value = getDate(name);
        return value != null ? value.toInstant() : null;
    }

    /**
     * 获取头部数量
     *
     * @return 头部数量
     */
    public int size() {
        return namesAndValues.length / 2;
    }

    /**
     * 获取指定索引的头部名称
     *
     * @param index 索引
     * @return 头部名称
     */
    public String name(int index) {
        return namesAndValues[index * 2];
    }

    /**
     * 获取指定索引的头部值
     *
     * @param index 索引
     * @return 头部值
     */
    public String value(int index) {
        return namesAndValues[index * 2 + 1];
    }

    /**
     * 获取头部名称集合
     *
     * @return 不可修改的头部名称集合
     */
    public Set<String> names() {
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            result.add(name(i));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 获取指定名称的头部值列表
     *
     * @param name 头部名称
     * @return 不可修改的头部值列表
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
     * 获取头部编码字节数
     *
     * @return 编码字节数
     */
    public long byteCount() {
        long result = namesAndValues.length * 2;

        for (int i = 0, size = namesAndValues.length; i < size; i++) {
            result += namesAndValues[i].length();
        }

        return result;
    }

    /**
     * 比较两个 Headers 对象是否相等
     *
     * @param other 另一个对象
     * @return true 如果头部完全相等
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Headers && Arrays.equals(((Headers) other).namesAndValues, namesAndValues);
    }

    /**
     * 返回头部的字符串表示
     *
     * @return 头部字符串
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
     * 转换为多值映射
     *
     * @return 头部名称到值列表的映射
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
     * Headers 构建器
     */
    public static class Builder {
        /** 头部名称和值列表 */
        final List<String> namesAndValues = new ArrayList<>(20);

        /**
         * 添加未验证的头部行
         *
         * @param line 头部行
         * @return 当前 Builder 实例
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
         * 添加头部行
         *
         * @param line 头部行（格式：name: value）
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果格式无效
         */
        public Builder add(String line) {
            int index = line.indexOf(Symbol.COLON);
            if (index == -1) {
                throw new IllegalArgumentException("Unexpected header: " + line);
            }
            return add(line.substring(0, index).trim(), line.substring(index + 1));
        }

        /**
         * 添加验证后的头部
         *
         * @param name  头部名称
         * @param value 头部值
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 name 或 value 为 null
         * @throws IllegalArgumentException 如果名称或值无效
         */
        public Builder add(String name, String value) {
            checkName(name);
            checkValue(value, name);
            return addLenient(name, value);
        }

        /**
         * 添加非 ASCII 头部
         *
         * @param name  头部名称
         * @param value 头部值
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 name 为 null
         * @throws IllegalArgumentException 如果名称无效
         */
        public Builder addUnsafeNonAscii(String name, String value) {
            checkName(name);
            return addLenient(name, value);
        }

        /**
         * 添加所有头部
         *
         * @param headers Headers 实例
         * @return 当前 Builder 实例
         */
        public Builder addAll(Headers headers) {
            for (int i = 0, size = headers.size(); i < size; i++) {
                addLenient(headers.name(i), headers.value(i));
            }
            return this;
        }

        /**
         * 添加日期头部
         *
         * @param name  头部名称
         * @param value 日期值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 或 value 为 null
         */
        public Builder add(String name, Date value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            add(name, org.miaixz.bus.http.Builder.format(value));
            return this;
        }

        /**
         * 添加 Instant 头部
         *
         * @param name  头部名称
         * @param value Instant 值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 或 value 为 null
         */
        public Builder add(String name, Instant value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            return add(name, new Date(value.toEpochMilli()));
        }

        /**
         * 设置日期头部
         *
         * @param name  头部名称
         * @param value 日期值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 或 value 为 null
         */
        public Builder set(String name, Date value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            set(name, org.miaixz.bus.http.Builder.format(value));
            return this;
        }

        /**
         * 设置 Instant 头部
         *
         * @param name  头部名称
         * @param value Instant 值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 或 value 为 null
         */
        public Builder set(String name, Instant value) {
            if (value == null)
                throw new NullPointerException("value for name " + name + " == null");
            return set(name, new Date(value.toEpochMilli()));
        }

        /**
         * 添加未验证的头部
         *
         * @param name  头部名称
         * @param value 头部值
         * @return 当前 Builder 实例
         */
        Builder addLenient(String name, String value) {
            namesAndValues.add(name);
            namesAndValues.add(value.trim());
            return this;
        }

        /**
         * 移除指定名称的所有头部
         *
         * @param name 头部名称
         * @return 当前 Builder 实例
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
         * 设置头部
         *
         * @param name  头部名称
         * @param value 头部值
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 name 或 value 为 null
         * @throws IllegalArgumentException 如果名称或值无效
         */
        public Builder set(String name, String value) {
            checkName(name);
            checkValue(value, name);
            removeAll(name);
            addLenient(name, value);
            return this;
        }

        /**
         * 获取指定名称的最后一个头部值
         *
         * @param name 头部名称
         * @return 头部值（不存在时为 null）
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
         * 构建 Headers 实例
         *
         * @return Headers 实例
         */
        public Headers build() {
            return new Headers(this);
        }
    }

}