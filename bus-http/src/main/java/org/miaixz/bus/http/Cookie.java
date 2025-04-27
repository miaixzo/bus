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
 * Cookie's相关工具支持 这个类不支持cookies上的附加属性，比如Chromium的Priority=HIGH extension
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Cookie {

    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{2,4})[^\\d]*");
    private static final Pattern MONTH_PATTERN = Pattern
            .compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*");
    private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d{1,2})[^\\d]*");
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})[^\\d]*");

    /**
     * 带有此cookie名称的非空字符串
     */
    private final String name;
    /**
     * 使用此cookie的值返回一个可能为空的字符串
     */
    private final String value;
    /**
     * 以与{@link System#currentTimeMillis()}相同的格式返回此cookie过期的时间。 这是9999年12月31日，如果cookie是{@linkplain #persistent() not
     * persistent}，那么它将在当前会话结束时终止
     */
    private final long expiresAt;
    /**
     * 返回cookie的域。如果{@link #hostOnly()}返回true，这是唯一匹配此cookie的域;否则它将匹配此域和所有子域
     */
    private final String domain;
    /**
     * 返回此cookie的路径。此cookie匹配前缀为与此路径段匹配的路径段的url。例如，如果这个路径是{@code /foo}，
     * 那么这个cookie将匹配对{@code /foo}和{@code /foo/bar}的请求，而不是{@code /}或{@code /football}的请求。
     */
    private final String path;
    /**
     * 如果此cookie仅限于HTTPS请求，则返回true
     */
    private final boolean secure;
    /**
     * 如果此cookie仅限于HTTP api，则返回true。在web浏览器中，这会阻止脚本访问cookie
     */
    private final boolean httpOnly;
    /**
     * 如果此cookie在当前会话结束时未过期，则返回true
     */
    private final boolean persistent;
    /**
     * 如果此cookie的域应解释为单个主机名，则返回true;如果应解释为模式，则返回false。 如果它的{@code Set-Cookie}头包含{@code domain}属性，则此标志为false
     */
    private final boolean hostOnly;

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

    public static Cookie parse(UnoUrl url, String setCookie) {
        return parse(System.currentTimeMillis(), url, setCookie);
    }

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
     * Parse a date as specified in RFC 6265, section 5.1.1.
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
     * Returns the index of the next date character in {@code input}, or if {@code invert} the index of the next
     * non-date character in {@code input}.
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
     * Returns the positive value if {@code attributeValue} is positive, or {@link Long#MIN_VALUE} if it is either 0 or
     * negative. If the value is positive but out of range, this returns {@link Long#MAX_VALUE}.
     *
     * @throws NumberFormatException if {@code s} is not an integer of any precision.
     */
    private static long parseMaxAge(String s) {
        try {
            long parsed = Long.parseLong(s);
            return parsed <= 0L ? Long.MIN_VALUE : parsed;
        } catch (NumberFormatException e) {
            // 检查值是否是一个整数(正的或负的)
            if (s.matches("-?\\d+")) {
                return s.startsWith(Symbol.MINUS) ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
            throw e;
        }
    }

    /**
     * Returns a domain string like {@code example.com} for an input domain like {@code EXAMPLE.COM} or
     * {@code .example.com}.
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
     * Returns all of the cookies from a set of HTTP response headers.
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

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public boolean persistent() {
        return persistent;
    }

    public long expiresAt() {
        return expiresAt;
    }

    public boolean hostOnly() {
        return hostOnly;
    }

    public String domain() {
        return domain;
    }

    public String path() {
        return path;
    }

    public boolean httpOnly() {
        return httpOnly;
    }

    public boolean secure() {
        return secure;
    }

    /**
     * Returns true if this cookie should be included on a request to {@code url}. In addition to this check callers
     * should also confirm that this cookie has not expired.
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

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * @param forObsoleteRfc2965 true to include a leading {@code .} on the domain pattern. This is necessary for
     *                           {@code example.com} to match {@code www.example.com} under RFC 2965. This extra dot is
     *                           ignored by more recent specifications.
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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Cookie))
            return false;
        Cookie that = (Cookie) other;
        return that.name.equals(name) && that.value.equals(value) && that.domain.equals(domain)
                && that.path.equals(path) && that.expiresAt == expiresAt && that.secure == secure
                && that.httpOnly == httpOnly && that.persistent == persistent && that.hostOnly == hostOnly;
    }

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
     * 构建一个饼干。在调用{@link #build}之前，必须设置 {@linkplain #name() name}、{@linkplain #value() value} 和{@linkplain #domain()
     * domain}.
     */
    public static class Builder {

        String name;
        String value;
        long expiresAt = org.miaixz.bus.http.Builder.MAX_DATE;
        /**
         * 设置此cookie的域模式。cookie将匹配{@code domain}及其所有子域
         */
        String domain;
        String path = Symbol.SLASH;
        boolean secure;
        boolean httpOnly;
        boolean persistent;
        boolean hostOnly;

        public Builder name(String name) {
            if (null == name)
                throw new NullPointerException("name == null");
            if (!name.trim().equals(name))
                throw new IllegalArgumentException("name is not trimmed");
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            if (null == value)
                throw new NullPointerException("value == null");
            if (!value.trim().equals(value))
                throw new IllegalArgumentException("value is not trimmed");
            this.value = value;
            return this;
        }

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
         * Set the domain pattern for this cookie. The cookie will match {@code domain} and all of its subdomains.
         */
        public Builder domain(String domain) {
            return domain(domain, false);
        }

        /**
         * Set the host-only domain for this cookie. The cookie will match {@code domain} but none of its subdomains.
         */
        public Builder hostOnlyDomain(String domain) {
            return domain(domain, true);
        }

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

        public Builder path(String path) {
            if (!path.startsWith(Symbol.SLASH))
                throw new IllegalArgumentException("path must start with /");
            this.path = path;
            return this;
        }

        public Builder secure() {
            this.secure = true;
            return this;
        }

        public Builder httpOnly() {
            this.httpOnly = true;
            return this;
        }

        public Cookie build() {
            return new Cookie(this);
        }
    }

}
