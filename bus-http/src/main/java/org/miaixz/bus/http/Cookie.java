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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * HTTP Cookie 处理工具
 * <p>
 * 支持 Cookie 的创建、解析和匹配，遵循 RFC 6265 标准。 不支持附加属性（如 Chromium 的 Priority=HIGH 扩展）。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Cookie {

    /**
     * 年份正则表达式
     */
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{2,4})[^\\d]*");
    /**
     * 月份正则表达式
     */
    private static final Pattern MONTH_PATTERN = Pattern
            .compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*");
    /**
     * 日期正则表达式
     */
    private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d{1,2})[^\\d]*");
    /**
     * 时间正则表达式
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})[^\\d]*");

    /**
     * Cookie 名称
     */
    private final String name;
    /**
     * Cookie 值
     */
    private final String value;
    /**
     * 过期时间
     */
    private final long expiresAt;
    /**
     * 域名
     */
    private final String domain;
    /**
     * 路径
     */
    private final String path;
    /**
     * 是否仅限 HTTPS
     */
    private final boolean secure;
    /**
     * 是否仅限 HTTP API
     */
    private final boolean httpOnly;
    /**
     * 是否为持久化 Cookie
     */
    private final boolean persistent;
    /**
     * 是否仅限主机
     */
    private final boolean hostOnly;

    /**
     * 构造函数，初始化 Cookie 实例
     *
     * @param name       Cookie 名称
     * @param value      Cookie 值
     * @param expiresAt  过期时间
     * @param domain     域名
     * @param path       路径
     * @param secure     是否仅限 HTTPS
     * @param httpOnly   是否仅限 HTTP API
     * @param hostOnly   是否仅限主机
     * @param persistent 是否为持久化 Cookie
     */
    private Cookie(String name, String value, long expiresAt, String domain, String path, boolean secure,
            boolean httpOnly, boolean hostOnly, boolean persistent) {
        this.name = name;
        this.value = value;
        this.expiresAt = expiresAt;
        this.domain = domain;
        this.path = path;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.hostOnly = hostOnly;
        this.persistent = persistent;
    }

    /**
     * 构造函数，基于 Builder 初始化 Cookie 实例
     *
     * @param builder Builder 实例
     * @throws NullPointerException 如果名称、值或域为空
     */
    Cookie(Builder builder) {
        if (null == builder.name)
            throw new NullPointerException("builder.name == null");
        if (null == builder.value)
            throw new NullPointerException("builder.value == null");
        if (null == builder.domain)
            throw new NullPointerException("builder.domain == null");

        this.name = builder.name;
        this.value = builder.value;
        this.expiresAt = builder.expiresAt;
        this.domain = builder.domain;
        this.path = builder.path;
        this.secure = builder.secure;
        this.httpOnly = builder.httpOnly;
        this.persistent = builder.persistent;
        this.hostOnly = builder.hostOnly;
    }

    /**
     * 检查域名是否匹配
     *
     * @param urlHost URL 主机名
     * @param domain  Cookie 域名
     * @return true 如果域名匹配
     */
    private static boolean domainMatch(String urlHost, String domain) {
        if (urlHost.equals(domain)) {
            return true;
        }

        if (urlHost.endsWith(domain) && urlHost.charAt(urlHost.length() - domain.length() - 1) == Symbol.C_DOT
                && !org.miaixz.bus.http.Builder.verifyAsIpAddress(urlHost)) {
            return true;
        }

        return false;
    }

    /**
     * 检查路径是否匹配
     *
     * @param url  URL
     * @param path Cookie 路径
     * @return true 如果路径匹配
     */
    private static boolean pathMatch(UnoUrl url, String path) {
        String urlPath = url.encodedPath();

        if (urlPath.equals(path)) {
            return true;
        }

        if (urlPath.startsWith(path)) {
            if (path.endsWith(Symbol.SLASH))
                return true;
            if (urlPath.charAt(path.length()) == Symbol.C_SLASH)
                return true;
        }

        return false;
    }

    /**
     * 解析 Set-Cookie 头部
     *
     * @param url       URL
     * @param setCookie Set-Cookie 头部值
     * @return Cookie 实例（无效时为 null）
     */
    public static Cookie parse(UnoUrl url, String setCookie) {
        return parse(System.currentTimeMillis(), url, setCookie);
    }

    /**
     * 解析 Set-Cookie 头部（指定时间）
     *
     * @param currentTimeMillis 当前时间
     * @param url               URL
     * @param setCookie         Set-Cookie 头部值
     * @return Cookie 实例（无效时为 null）
     */
    static Cookie parse(long currentTimeMillis, UnoUrl url, String setCookie) {
        int pos = 0;
        int limit = setCookie.length();
        int cookiePairEnd = org.miaixz.bus.http.Builder.delimiterOffset(setCookie, pos, limit, Symbol.C_SEMICOLON);

        int pairEqualsSign = org.miaixz.bus.http.Builder.delimiterOffset(setCookie, pos, cookiePairEnd, Symbol.C_EQUAL);
        if (pairEqualsSign == cookiePairEnd)
            return null;

        String cookieName = org.miaixz.bus.http.Builder.trimSubstring(setCookie, pos, pairEqualsSign);
        if (cookieName.isEmpty() || org.miaixz.bus.http.Builder.indexOfControlOrNonAscii(cookieName) != -1)
            return null;

        String cookieValue = org.miaixz.bus.http.Builder.trimSubstring(setCookie, pairEqualsSign + 1, cookiePairEnd);
        if (org.miaixz.bus.http.Builder.indexOfControlOrNonAscii(cookieValue) != -1)
            return null;

        long expiresAt = org.miaixz.bus.http.Builder.MAX_DATE;
        long deltaSeconds = -1L;
        String domain = null;
        String path = null;
        boolean secureOnly = false;
        boolean httpOnly = false;
        boolean hostOnly = true;
        boolean persistent = false;

        pos = cookiePairEnd + 1;
        while (pos < limit) {
            int attributePairEnd = org.miaixz.bus.http.Builder.delimiterOffset(setCookie, pos, limit,
                    Symbol.C_SEMICOLON);

            int attributeEqualsSign = org.miaixz.bus.http.Builder.delimiterOffset(setCookie, pos, attributePairEnd,
                    Symbol.C_EQUAL);
            String attributeName = org.miaixz.bus.http.Builder.trimSubstring(setCookie, pos, attributeEqualsSign);
            String attributeValue = attributeEqualsSign < attributePairEnd
                    ? org.miaixz.bus.http.Builder.trimSubstring(setCookie, attributeEqualsSign + 1, attributePairEnd)
                    : Normal.EMPTY;

            if (attributeName.equalsIgnoreCase("expires")) {
                try {
                    expiresAt = parseExpires(attributeValue, 0, attributeValue.length());
                    persistent = true;
                } catch (IllegalArgumentException e) {
                    // 忽略此属性，它无法识别为日期
                }
            } else if (attributeName.equalsIgnoreCase("max-age")) {
                try {
                    deltaSeconds = parseMaxAge(attributeValue);
                    persistent = true;
                } catch (NumberFormatException e) {
                    // 忽略此属性，它无法识别为最大值.
                }
            } else if (attributeName.equalsIgnoreCase("domain")) {
                try {
                    domain = parseDomain(attributeValue);
                    hostOnly = false;
                } catch (IllegalArgumentException e) {
                    // 忽略此属性，它无法识别为域名.
                }
            } else if (attributeName.equalsIgnoreCase("path")) {
                path = attributeValue;
            } else if (attributeName.equalsIgnoreCase("secure")) {
                secureOnly = true;
            } else if (attributeName.equalsIgnoreCase("httponly")) {
                httpOnly = true;
            }

            pos = attributePairEnd + 1;
        }

        // 如果“Max-Age”出现，它将优先于“Expires”，而不管这两个属性在cookie字符串中声明的顺序如何.
        if (deltaSeconds == Long.MIN_VALUE) {
            expiresAt = Long.MIN_VALUE;
        } else if (deltaSeconds != -1L) {
            long deltaMilliseconds = deltaSeconds <= (Long.MAX_VALUE / 1000) ? deltaSeconds * 1000 : Long.MAX_VALUE;
            expiresAt = currentTimeMillis + deltaMilliseconds;
            if (expiresAt < currentTimeMillis || expiresAt > org.miaixz.bus.http.Builder.MAX_DATE) {
                expiresAt = org.miaixz.bus.http.Builder.MAX_DATE;
            }
        }

        // 如果存在域，则必须匹配域。否则我们只有一个主机cookie.
        String urlHost = url.host();
        if (null == domain) {
            domain = urlHost;
        } else if (!domainMatch(urlHost, domain)) {
            return null;
        }

        // 如果域名是url主机的后缀，则它不能是公共后缀
        if (urlHost.length() != domain.length()) {
            return null;
        }

        if (null == path || !path.startsWith(Symbol.SLASH)) {
            String encodedPath = url.encodedPath();
            int lastSlash = encodedPath.lastIndexOf(Symbol.C_SLASH);
            path = lastSlash != 0 ? encodedPath.substring(0, lastSlash) : Symbol.SLASH;
        }

        return new Cookie(cookieName, cookieValue, expiresAt, domain, path, secureOnly, httpOnly, hostOnly, persistent);
    }

    /**
     * 解析过期时间（RFC 6265, Section 5.1.1）
     *
     * @param s     时间字符串
     * @param pos   起始位置
     * @param limit 结束位置
     * @return 过期时间（毫秒）
     * @throws IllegalArgumentException 如果时间格式无效
     */
    private static long parseExpires(String s, int pos, int limit) {
        pos = dateCharacterOffset(s, pos, limit, false);

        int hour = -1;
        int minute = -1;
        int second = -1;
        int dayOfMonth = -1;
        int month = -1;
        int year = -1;
        Matcher matcher = TIME_PATTERN.matcher(s);

        while (pos < limit) {
            int end = dateCharacterOffset(s, pos + 1, limit, true);
            matcher.region(pos, end);

            if (hour == -1 && matcher.usePattern(TIME_PATTERN).matches()) {
                hour = Integer.parseInt(matcher.group(1));
                minute = Integer.parseInt(matcher.group(2));
                second = Integer.parseInt(matcher.group(3));
            } else if (dayOfMonth == -1 && matcher.usePattern(DAY_OF_MONTH_PATTERN).matches()) {
                dayOfMonth = Integer.parseInt(matcher.group(1));
            } else if (month == -1 && matcher.usePattern(MONTH_PATTERN).matches()) {
                String monthString = matcher.group(1).toLowerCase(Locale.US);
                month = MONTH_PATTERN.pattern().indexOf(monthString) / 4;
            } else if (year == -1 && matcher.usePattern(YEAR_PATTERN).matches()) {
                year = Integer.parseInt(matcher.group(1));
            }

            pos = dateCharacterOffset(s, end + 1, limit, false);
        }

        // 将两位数的年份转换为四位数的年份。99变成1999,15变成2015.
        if (year >= 70 && year <= 99)
            year += 1900;
        if (year >= 0 && year <= 69)
            year += 2000;

        // 如果任何部分被省略或超出范围，则返回-1。这个日期是不可能的。注意，该语法不支持闰秒.
        if (year < 1601)
            throw new IllegalArgumentException();
        if (month == -1)
            throw new IllegalArgumentException();
        if (dayOfMonth < 1 || dayOfMonth > 31)
            throw new IllegalArgumentException();
        if (hour < 0 || hour > 23)
            throw new IllegalArgumentException();
        if (minute < 0 || minute > 59)
            throw new IllegalArgumentException();
        if (second < 0 || second > 59)
            throw new IllegalArgumentException();

        Calendar calendar = new GregorianCalendar(org.miaixz.bus.http.Builder.UTC);
        calendar.setLenient(false);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 查找日期字符位置
     *
     * @param input  输入字符串
     * @param pos    起始位置
     * @param limit  结束位置
     * @param invert 是否反向查找
     * @return 日期字符位置
     */
    private static int dateCharacterOffset(String input, int pos, int limit, boolean invert) {
        for (int i = pos; i < limit; i++) {
            int c = input.charAt(i);
            boolean dateCharacter = (c < Symbol.C_SPACE && c != Symbol.C_HT) || (c >= '\u007f')
                    || (c >= Symbol.C_ZERO && c <= Symbol.C_NINE) || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c == Symbol.C_COLON);
            if (dateCharacter == !invert)
                return i;
        }
        return limit;
    }

    /**
     * 解析 Max-Age 属性
     *
     * @param s Max-Age 字符串
     * @return Max-Age 值
     * @throws NumberFormatException 如果值无效
     */
    private static long parseMaxAge(String s) {
        try {
            long parsed = Long.parseLong(s);
            return parsed <= 0L ? Long.MIN_VALUE : parsed;
        } catch (NumberFormatException e) {
            if (s.matches("-?\\d+")) {
                return s.startsWith(Symbol.MINUS) ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
            throw e;
        }
    }

    /**
     * 解析域名
     *
     * @param s 域名字符串
     * @return 规范化域名
     * @throws IllegalArgumentException 如果域名无效
     */
    private static String parseDomain(String s) {
        if (s.endsWith(Symbol.DOT)) {
            throw new IllegalArgumentException();
        }
        if (s.startsWith(Symbol.DOT)) {
            s = s.substring(1);
        }
        String canonicalDomain = org.miaixz.bus.http.Builder.canonicalizeHost(s);
        if (null == canonicalDomain) {
            throw new IllegalArgumentException();
        }
        return canonicalDomain;
    }

    /**
     * 解析所有 Set-Cookie 头部
     *
     * @param url     URL
     * @param headers 响应头部
     * @return Cookie 列表
     */
    public static List<Cookie> parseAll(UnoUrl url, Headers headers) {
        List<String> cookieStrings = headers.values("Set-Cookie");
        List<Cookie> cookies = null;

        for (int i = 0, size = cookieStrings.size(); i < size; i++) {
            Cookie cookie = Cookie.parse(url, cookieStrings.get(i));
            if (null == cookie)
                continue;
            if (null == cookies)
                cookies = new ArrayList<>();
            cookies.add(cookie);
        }

        return null != cookies ? Collections.unmodifiableList(cookies) : Collections.emptyList();
    }

    /**
     * 获取 Cookie 名称
     *
     * @return Cookie 名称
     */
    public String name() {
        return name;
    }

    /**
     * 获取 Cookie 值
     *
     * @return Cookie 值
     */
    public String value() {
        return value;
    }

    /**
     * 检查是否为持久化 Cookie
     *
     * @return true 如果为持久化 Cookie
     */
    public boolean persistent() {
        return persistent;
    }

    /**
     * 获取过期时间
     *
     * @return 过期时间（毫秒）
     */
    public long expiresAt() {
        return expiresAt;
    }

    /**
     * 检查是否仅限主机
     *
     * @return true 如果仅限主机
     */
    public boolean hostOnly() {
        return hostOnly;
    }

    /**
     * 获取域名
     *
     * @return 域名
     */
    public String domain() {
        return domain;
    }

    /**
     * 获取路径
     *
     * @return 路径
     */
    public String path() {
        return path;
    }

    /**
     * 检查是否仅限 HTTP API
     *
     * @return true 如果仅限 HTTP API
     */
    public boolean httpOnly() {
        return httpOnly;
    }

    /**
     * 检查是否仅限 HTTPS
     *
     * @return true 如果仅限 HTTPS
     */
    public boolean secure() {
        return secure;
    }

    /**
     * 检查 Cookie 是否匹配 URL
     *
     * @param url URL
     * @return true 如果 Cookie 匹配 URL
     */
    public boolean matches(UnoUrl url) {
        boolean domainMatch = hostOnly ? url.host().equals(domain) : domainMatch(url.host(), domain);
        if (!domainMatch)
            return false;

        if (!pathMatch(url, path))
            return false;

        if (secure && !url.isHttps())
            return false;

        return true;
    }

    /**
     * 返回 Cookie 的字符串表示
     *
     * @return Cookie 字符串
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * 返回 Cookie 的字符串表示
     *
     * @param forObsoleteRfc2965 是否为 RFC 2965 格式
     * @return Cookie 字符串
     */
    String toString(boolean forObsoleteRfc2965) {
        StringBuilder result = new StringBuilder();
        result.append(name);
        result.append(Symbol.C_EQUAL);
        result.append(value);

        if (persistent) {
            if (expiresAt == Long.MIN_VALUE) {
                result.append("; max-age=0");
            } else {
                result.append("; expires=").append(org.miaixz.bus.http.Builder.format(new Date(expiresAt)));
            }
        }

        if (!hostOnly) {
            result.append("; domain=");
            if (forObsoleteRfc2965) {
                result.append(Symbol.DOT);
            }
            result.append(domain);
        }

        result.append("; path=").append(path);

        if (secure) {
            result.append("; secure");
        }

        if (httpOnly) {
            result.append("; httponly");
        }

        return result.toString();
    }

    /**
     * 比较两个 Cookie 是否相等
     *
     * @param other 另一个对象
     * @return true 如果两个 Cookie 相等
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Cookie))
            return false;
        Cookie that = (Cookie) other;
        return that.name.equals(name) && that.value.equals(value) && that.domain.equals(domain)
                && that.path.equals(path) && that.expiresAt == expiresAt && that.secure == secure
                && that.httpOnly == httpOnly && that.persistent == persistent && that.hostOnly == hostOnly;
    }

    /**
     * 计算 Cookie 的哈希码
     *
     * @return 哈希码值
     */
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + value.hashCode();
        hash = 31 * hash + domain.hashCode();
        hash = 31 * hash + path.hashCode();
        hash = 31 * hash + (int) (expiresAt ^ (expiresAt >>> Normal._32));
        hash = 31 * hash + (secure ? 0 : 1);
        hash = 31 * hash + (httpOnly ? 0 : 1);
        hash = 31 * hash + (persistent ? 0 : 1);
        hash = 31 * hash + (hostOnly ? 0 : 1);
        return hash;
    }

    /**
     * Cookie 构建器
     */
    public static class Builder {

        /**
         * Cookie 名称
         */
        String name;
        /**
         * Cookie 值
         */
        String value;
        /**
         * 过期时间
         */
        long expiresAt = org.miaixz.bus.http.Builder.MAX_DATE;
        /**
         * 域名
         */
        String domain;
        /**
         * 路径
         */
        String path = Symbol.SLASH;
        /**
         * 是否仅限 HTTPS
         */
        boolean secure;
        /**
         * 是否仅限 HTTP API
         */
        boolean httpOnly;
        /**
         * 是否为持久化 Cookie
         */
        boolean persistent;
        /**
         * 是否仅限主机
         */
        boolean hostOnly;

        /**
         * 设置 Cookie 名称
         *
         * @param name Cookie 名称
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 name 为 null
         * @throws IllegalArgumentException 如果名称包含空白
         */
        public Builder name(String name) {
            if (null == name)
                throw new NullPointerException("name == null");
            if (!name.trim().equals(name))
                throw new IllegalArgumentException("name is not trimmed");
            this.name = name;
            return this;
        }

        /**
         * 设置 Cookie 值
         *
         * @param value Cookie 值
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 value 为 null
         * @throws IllegalArgumentException 如果值包含空白
         */
        public Builder value(String value) {
            if (null == value)
                throw new NullPointerException("value == null");
            if (!value.trim().equals(value))
                throw new IllegalArgumentException("value is not trimmed");
            this.value = value;
            return this;
        }

        /**
         * 设置过期时间
         *
         * @param expiresAt 过期时间（毫秒）
         * @return 当前 Builder 实例
         */
        public Builder expiresAt(long expiresAt) {
            if (expiresAt <= 0)
                expiresAt = Long.MIN_VALUE;
            if (expiresAt > org.miaixz.bus.http.Builder.MAX_DATE)
                expiresAt = org.miaixz.bus.http.Builder.MAX_DATE;
            this.expiresAt = expiresAt;
            this.persistent = true;
            return this;
        }

        /**
         * 设置域名（匹配域名及其子域）
         *
         * @param domain 域名
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 domain 为 null
         * @throws IllegalArgumentException 如果域名无效
         */
        public Builder domain(String domain) {
            return domain(domain, false);
        }

        /**
         * 设置仅限主机的域名
         *
         * @param domain 域名
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 domain 为 null
         * @throws IllegalArgumentException 如果域名无效
         */
        public Builder hostOnlyDomain(String domain) {
            return domain(domain, true);
        }

        /**
         * 设置域名（内部实现）
         *
         * @param domain   域名
         * @param hostOnly 是否仅限主机
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 domain 为 null
         * @throws IllegalArgumentException 如果域名无效
         */
        private Builder domain(String domain, boolean hostOnly) {
            if (null == domain)
                throw new NullPointerException("domain == null");
            String canonicalDomain = org.miaixz.bus.http.Builder.canonicalizeHost(domain);
            if (null == canonicalDomain) {
                throw new IllegalArgumentException("unexpected domain: " + domain);
            }
            this.domain = canonicalDomain;
            this.hostOnly = hostOnly;
            return this;
        }

        /**
         * 设置路径
         *
         * @param path 路径
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果路径不以 / 开头
         */
        public Builder path(String path) {
            if (!path.startsWith(Symbol.SLASH))
                throw new IllegalArgumentException("path must start with /");
            this.path = path;
            return this;
        }

        /**
         * 设置仅限 HTTPS
         *
         * @return 当前 Builder 实例
         */
        public Builder secure() {
            this.secure = true;
            return this;
        }

        /**
         * 设置仅限 HTTP API
         *
         * @return 当前 Builder 实例
         */
        public Builder httpOnly() {
            this.httpOnly = true;
            return this;
        }

        /**
         * 构建 Cookie 实例
         *
         * @return Cookie 实例
         * @throws NullPointerException 如果 name、value 或 domain 未设置
         */
        public Cookie build() {
            return new Cookie(this);
        }
    }

}