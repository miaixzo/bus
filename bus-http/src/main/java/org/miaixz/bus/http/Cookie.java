/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An RFC 6265-compliant HTTP cookie.
 * <p>
 * This class supports cookie creation, parsing, and matching. It does not support additional attributes like Chromium's
 * Priority=HIGH extension.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Cookie {

    /**
     * Regular expression for parsing the year part of a date.
     */
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{2,4})[^\\d]*");
    /**
     * Regular expression for parsing the month part of a date.
     */
    private static final Pattern MONTH_PATTERN = Pattern
            .compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*");
    /**
     * Regular expression for parsing the day of the month part of a date.
     */
    private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d{1,2})[^\\d]*");
    /**
     * Regular expression for parsing the time part of a date.
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})[^\\d]*");

    /**
     * The name of the cookie.
     */
    private final String name;
    /**
     * The value of the cookie.
     */
    private final String value;
    /**
     * The expiration time of the cookie.
     */
    private final long expiresAt;
    /**
     * The domain of the cookie.
     */
    private final String domain;
    /**
     * The path of the cookie.
     */
    private final String path;
    /**
     * Whether the cookie is secure (HTTPS only).
     */
    private final boolean secure;
    /**
     * Whether the cookie is HTTP only.
     */
    private final boolean httpOnly;
    /**
     * Whether the cookie is persistent.
     */
    private final boolean persistent;
    /**
     * Whether the cookie is host only.
     */
    private final boolean hostOnly;

    /**
     * Constructs a new {@code Cookie} instance.
     *
     * @param name       The name of the cookie.
     * @param value      The value of the cookie.
     * @param expiresAt  The expiration time in milliseconds.
     * @param domain     The domain of the cookie.
     * @param path       The path of the cookie.
     * @param secure     Whether the cookie is secure.
     * @param httpOnly   Whether the cookie is HTTP only.
     * @param hostOnly   Whether the cookie is host only.
     * @param persistent Whether the cookie is persistent.
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
     * Constructs a new {@code Cookie} instance from a builder.
     *
     * @param builder The builder instance.
     * @throws NullPointerException if name, value, or domain is null.
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
     * Checks if a domain matches the cookie's domain.
     *
     * @param urlHost The host from the URL.
     * @param domain  The domain from the cookie.
     * @return {@code true} if the domain matches.
     */
    private static boolean domainMatch(String urlHost, String domain) {
        if (urlHost.equals(domain)) {
            return true; // The domain is an exact match.
        }

        // The domain is a suffix of the URL host.
        return urlHost.endsWith(domain) && urlHost.charAt(urlHost.length() - domain.length() - 1) == Symbol.C_DOT
                && !org.miaixz.bus.http.Builder.verifyAsIpAddress(urlHost);
    }

    /**
     * Checks if a path matches the cookie's path.
     *
     * @param url  The URL.
     * @param path The path from the cookie.
     * @return {@code true} if the path matches.
     */
    private static boolean pathMatch(UnoUrl url, String path) {
        String urlPath = url.encodedPath();

        if (urlPath.equals(path)) {
            return true; // The path is an exact match.
        }

        if (urlPath.startsWith(path)) {
            if (path.endsWith(Symbol.SLASH))
                return true; // The path is a prefix of the URL path.
            if (urlPath.charAt(path.length()) == Symbol.C_SLASH)
                return true; // The path is a directory of the URL path.
        }

        return false;
    }

    /**
     * Parses a {@code Set-Cookie} header into a {@code Cookie}.
     *
     * @param url       The URL that sent this cookie.
     * @param setCookie The {@code Set-Cookie} header value.
     * @return The parsed cookie, or null if the cookie is invalid.
     */
    public static Cookie parse(UnoUrl url, String setCookie) {
        return parse(System.currentTimeMillis(), url, setCookie);
    }

    /**
     * Parses a {@code Set-Cookie} header into a {@code Cookie} at a specific time.
     *
     * @param currentTimeMillis The current time in milliseconds.
     * @param url               The URL that sent this cookie.
     * @param setCookie         The {@code Set-Cookie} header value.
     * @return The parsed cookie, or null if the cookie is invalid.
     */
    static Cookie parse(long currentTimeMillis, UnoUrl url, String setCookie) {
        int pos = 0;
        int limit = setCookie.length();
        int cookiePairEnd = org.miaixz.bus.http.Builder.delimiterOffset(setCookie, pos, limit, Symbol.C_SEMICOLON);

        int pairEqualsSign = org.miaixz.bus.http.Builder.delimiterOffset(setCookie, pos, cookiePairEnd, Symbol.C_EQUAL);
        if (pairEqualsSign == cookiePairEnd)
            return null;

        String cookieName = org.miaixz.bus.http.Builder.trimSubstring(setCookie, pos, pairEqualsSign);
        if (cookieName.isEmpty() || org.miaixz.bus.http.Builder.indexOfControlOrNonAscii(cookieName) != -1) {
            return null;
        }

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
            int attributePairEnd = org.miaixz.bus.http.Builder
                    .delimiterOffset(setCookie, pos, limit, Symbol.C_SEMICOLON);

            int attributeEqualsSign = org.miaixz.bus.http.Builder
                    .delimiterOffset(setCookie, pos, attributePairEnd, Symbol.C_EQUAL);
            String attributeName = org.miaixz.bus.http.Builder.trimSubstring(setCookie, pos, attributeEqualsSign);
            String attributeValue = attributeEqualsSign < attributePairEnd
                    ? org.miaixz.bus.http.Builder.trimSubstring(setCookie, attributeEqualsSign + 1, attributePairEnd)
                    : Normal.EMPTY;

            if (attributeName.equalsIgnoreCase("expires")) {
                try {
                    expiresAt = parseExpires(attributeValue, 0, attributeValue.length());
                    persistent = true;
                } catch (IllegalArgumentException e) {
                    // Ignore this attribute, it is not a valid date.
                }
            } else if (attributeName.equalsIgnoreCase("max-age")) {
                try {
                    deltaSeconds = parseMaxAge(attributeValue);
                    persistent = true;
                } catch (NumberFormatException e) {
                    // Ignore this attribute, it is not a valid max-age.
                }
            } else if (attributeName.equalsIgnoreCase("domain")) {
                try {
                    domain = parseDomain(attributeValue);
                    hostOnly = false;
                } catch (IllegalArgumentException e) {
                    // Ignore this attribute, it is not a valid domain.
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

        // If "Max-Age" is present, it takes precedence over "Expires", regardless of the order of attributes.
        if (deltaSeconds == Long.MIN_VALUE) {
            expiresAt = Long.MIN_VALUE;
        } else if (deltaSeconds != -1L) {
            long deltaMilliseconds = deltaSeconds <= (Long.MAX_VALUE / 1000) ? deltaSeconds * 1000 : Long.MAX_VALUE;
            expiresAt = currentTimeMillis + deltaMilliseconds;
            if (expiresAt < currentTimeMillis || expiresAt > org.miaixz.bus.http.Builder.MAX_DATE) {
                expiresAt = org.miaixz.bus.http.Builder.MAX_DATE; // Handle overflow.
            }
        }

        // If a domain is present, it must domain-match. Otherwise, we have a host-only cookie.
        String urlHost = url.host();
        if (null == domain) {
            domain = urlHost;
        } else if (!domainMatch(urlHost, domain)) {
            return null; // The cookie's domain is not a suffix of the URL's host.
        }

        // If the domain is a suffix of the URL host, it must not be a public suffix.
        if (urlHost.length() != domain.length()) {
            return null;
        }

        // If the path is not present, use the default path.
        if (null == path || !path.startsWith(Symbol.SLASH)) {
            String encodedPath = url.encodedPath();
            int lastSlash = encodedPath.lastIndexOf(Symbol.C_SLASH);
            path = lastSlash != 0 ? encodedPath.substring(0, lastSlash) : Symbol.SLASH;
        }

        return new Cookie(cookieName, cookieValue, expiresAt, domain, path, secureOnly, httpOnly, hostOnly, persistent);
    }

    /**
     * Parses the {@code Expires} attribute value (RFC 6265, Section 5.1.1).
     *
     * @param s     The date string.
     * @param pos   The starting position.
     * @param limit The ending position.
     * @return The expiration time in milliseconds.
     * @throws IllegalArgumentException if the date format is invalid.
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

        // Convert two-digit years to four-digit years. 99 becomes 1999, 15 becomes 2015.
        if (year >= 70 && year <= 99)
            year += 1900;
        if (year >= 0 && year <= 69)
            year += 2000;

        // If any part is omitted or out of range, return -1. This date is impossible.
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
     * Finds the offset of a date character.
     *
     * @param input  The input string.
     * @param pos    The starting position.
     * @param limit  The ending position.
     * @param invert Whether to invert the search.
     * @return The offset of the date character.
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
     * Parses the {@code Max-Age} attribute.
     *
     * @param s The {@code Max-Age} string.
     * @return The {@code Max-Age} value.
     * @throws NumberFormatException if the value is invalid.
     */
    private static long parseMaxAge(String s) {
        try {
            long parsed = Long.parseLong(s);
            return parsed <= 0L ? Long.MIN_VALUE : parsed;
        } catch (NumberFormatException e) {
            // If the value is not a valid long, check if it's a large number that can be clamped.
            if (s.matches("-?\\d+")) {
                return s.startsWith(Symbol.MINUS) ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
            throw e;
        }
    }

    /**
     * Parses the domain attribute.
     *
     * @param s The domain string.
     * @return The canonicalized domain.
     * @throws IllegalArgumentException if the domain is invalid.
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
     * Parses all {@code Set-Cookie} headers from an HTTP response.
     *
     * @param url     The URL that sent these cookies.
     * @param headers The response headers.
     * @return A list of parsed cookies.
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
     * Returns the name of this cookie.
     *
     * @return The cookie name.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the value of this cookie.
     *
     * @return The cookie value.
     */
    public String value() {
        return value;
    }

    /**
     * Returns {@code true} if this cookie is persistent.
     *
     * @return {@code true} if this cookie is persistent.
     */
    public boolean persistent() {
        return persistent;
    }

    /**
     * Returns the expiration time of this cookie.
     *
     * @return The expiration time in milliseconds.
     */
    public long expiresAt() {
        return expiresAt;
    }

    /**
     * Returns {@code true} if this cookie is host-only.
     *
     * @return {@code true} if this cookie is host-only.
     */
    public boolean hostOnly() {
        return hostOnly;
    }

    /**
     * Returns the domain of this cookie.
     *
     * @return The domain.
     */
    public String domain() {
        return domain;
    }

    /**
     * Returns the path of this cookie.
     *
     * @return The path.
     */
    public String path() {
        return path;
    }

    /**
     * Returns {@code true} if this cookie should only be sent over HTTP APIs.
     *
     * @return {@code true} if this cookie is HTTP-only.
     */
    public boolean httpOnly() {
        return httpOnly;
    }

    /**
     * Returns {@code true} if this cookie should only be sent over HTTPS.
     *
     * @return {@code true} if this cookie is secure.
     */
    public boolean secure() {
        return secure;
    }

    /**
     * Returns {@code true} if this cookie matches the given URL.
     *
     * @param url The URL to check.
     * @return {@code true} if this cookie matches the URL.
     */
    public boolean matches(UnoUrl url) {
        boolean domainMatch = hostOnly ? url.host().equals(domain) : domainMatch(url.host(), domain);
        if (!domainMatch)
            return false;

        if (!pathMatch(url, path))
            return false;

        return !secure || url.isHttps();
    }

    /**
     * Returns the string representation of this cookie.
     *
     * @return The cookie string.
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Returns the string representation of this cookie.
     *
     * @param forObsoleteRfc2965 Whether to format for RFC 2965.
     * @return The cookie string.
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
     * Compares this cookie to another object for equality.
     *
     * @param other The other object to compare against.
     * @return {@code true} if the two cookies are equal.
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
     * Computes the hash code for this cookie.
     *
     * @return The hash code value.
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
     * A builder for creating {@link Cookie} instances.
     */
    public static class Builder {

        /**
         * The name of the cookie.
         */
        String name;
        /**
         * The value of the cookie.
         */
        String value;
        /**
         * The expiration time of the cookie.
         */
        long expiresAt = org.miaixz.bus.http.Builder.MAX_DATE;
        /**
         * The domain of the cookie.
         */
        String domain;
        /**
         * The path of the cookie.
         */
        String path = Symbol.SLASH;
        /**
         * Whether the cookie is secure.
         */
        boolean secure;
        /**
         * Whether the cookie is HTTP only.
         */
        boolean httpOnly;
        /**
         * Whether the cookie is persistent.
         */
        boolean persistent;
        /**
         * Whether the cookie is host only.
         */
        boolean hostOnly;

        /**
         * Sets the name of the cookie.
         *
         * @param name The cookie name.
         * @return this builder instance.
         * @throws NullPointerException     if name is null.
         * @throws IllegalArgumentException if name contains whitespace.
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
         * Sets the value of the cookie.
         *
         * @param value The cookie value.
         * @return this builder instance.
         * @throws NullPointerException     if value is null.
         * @throws IllegalArgumentException if value contains whitespace.
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
         * Sets the expiration time of the cookie.
         *
         * @param expiresAt The expiration time in milliseconds.
         * @return this builder instance.
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
         * Sets the domain of the cookie, which will match the domain and its subdomains.
         *
         * @param domain The domain.
         * @return this builder instance.
         * @throws NullPointerException     if domain is null.
         * @throws IllegalArgumentException if the domain is invalid.
         */
        public Builder domain(String domain) {
            return domain(domain, false);
        }

        /**
         * Sets the domain of the cookie to be host-only.
         *
         * @param domain The domain.
         * @return this builder instance.
         * @throws NullPointerException     if domain is null.
         * @throws IllegalArgumentException if the domain is invalid.
         */
        public Builder hostOnlyDomain(String domain) {
            return domain(domain, true);
        }

        /**
         * Internal implementation for setting the domain.
         *
         * @param domain   The domain.
         * @param hostOnly Whether the domain is host-only.
         * @return this builder instance.
         * @throws NullPointerException     if domain is null.
         * @throws IllegalArgumentException if the domain is invalid.
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
         * Sets the path of the cookie.
         *
         * @param path The path.
         * @return this builder instance.
         * @throws IllegalArgumentException if the path does not start with "/".
         */
        public Builder path(String path) {
            if (!path.startsWith(Symbol.SLASH))
                throw new IllegalArgumentException("path must start with '/'");
            this.path = path;
            return this;
        }

        /**
         * Sets the cookie to be secure (HTTPS only).
         *
         * @return this builder instance.
         */
        public Builder secure() {
            this.secure = true;
            return this;
        }

        /**
         * Sets the cookie to be HTTP-only.
         *
         * @return this builder instance.
         */
        public Builder httpOnly() {
            this.httpOnly = true;
            return this;
        }

        /**
         * Builds a new {@link Cookie} instance.
         *
         * @return a new {@link Cookie} instance.
         * @throws NullPointerException if name, value, or domain is not set.
         */
        public Cookie build() {
            return new Cookie(this);
        }
    }

}
