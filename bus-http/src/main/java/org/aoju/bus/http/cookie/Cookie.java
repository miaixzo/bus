/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.http.cookie;

import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Url;
import org.aoju.bus.http.header.Headers;
import org.aoju.bus.http.internal.http.HttpDate;
import org.aoju.bus.http.internal.suffix.SuffixDatabase;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An <a href="http://tools.ietf.org/html/rfc6265">RFC 6265</a> Cookie.
 *
 * <p>This class doesn't support additional attributes on cookies, like <a
 * href="https://code.google.com/p/chromium/issues/detail?id=232693">Chromium's Priority=HIGH
 * extension</a>.
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public final class Cookie {

    private static final Pattern YEAR_PATTERN
            = Pattern.compile("(\\d{2,4})[^\\d]*");
    private static final Pattern MONTH_PATTERN
            = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*");
    private static final Pattern DAY_OF_MONTH_PATTERN
            = Pattern.compile("(\\d{1,2})[^\\d]*");
    private static final Pattern TIME_PATTERN
            = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})[^\\d]*");

    private final String name;
    private final String value;
    private final long expiresAt;
    private final String domain;
    private final String path;
    private final boolean secure;
    private final boolean httpOnly;

    private final boolean persistent; // True if 'expires' or 'max-age' is present.
    private final boolean hostOnly; // True unless 'entity' is present.

    private Cookie(String name, String value, long expiresAt, String domain, String path,
                   boolean secure, boolean httpOnly, boolean hostOnly, boolean persistent) {
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
        if (builder.name == null) throw new NullPointerException("builder.name == null");
        if (builder.value == null) throw new NullPointerException("builder.value == null");
        if (builder.domain == null) throw new NullPointerException("builder.entity == null");

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
            return true; // As in 'example.com' matching 'example.com'.
        }

        if (urlHost.endsWith(domain)
                && urlHost.charAt(urlHost.length() - domain.length() - 1) == '.'
                && !Internal.verifyAsIpAddress(urlHost)) {
            return true; // As in 'example.com' matching 'www.example.com'.
        }

        return false;
    }

    private static boolean pathMatch(Url url, String path) {
        String urlPath = url.encodedPath();

        if (urlPath.equals(path)) {
            return true; // As in '/foo' matching '/foo'.
        }

        if (urlPath.startsWith(path)) {
            if (path.endsWith("/")) return true; // As in '/' matching '/foo'.
            if (urlPath.charAt(path.length()) == '/') return true; // As in '/foo' matching '/foo/bar'.
        }

        return false;
    }

    public static Cookie parse(Url url, String setCookie) {
        return parse(System.currentTimeMillis(), url, setCookie);
    }

    static Cookie parse(long currentTimeMillis, Url url, String setCookie) {
        int pos = 0;
        int limit = setCookie.length();
        int cookiePairEnd = Internal.delimiterOffset(setCookie, pos, limit, ';');

        int pairEqualsSign = Internal.delimiterOffset(setCookie, pos, cookiePairEnd, '=');
        if (pairEqualsSign == cookiePairEnd) return null;

        String cookieName = Internal.trimSubstring(setCookie, pos, pairEqualsSign);
        if (cookieName.isEmpty() || Internal.indexOfControlOrNonAscii(cookieName) != -1) return null;

        String cookieValue = Internal.trimSubstring(setCookie, pairEqualsSign + 1, cookiePairEnd);
        if (Internal.indexOfControlOrNonAscii(cookieValue) != -1) return null;

        long expiresAt = HttpDate.MAX_DATE;
        long deltaSeconds = -1L;
        String domain = null;
        String path = null;
        boolean secureOnly = false;
        boolean httpOnly = false;
        boolean hostOnly = true;
        boolean persistent = false;

        pos = cookiePairEnd + 1;
        while (pos < limit) {
            int attributePairEnd = Internal.delimiterOffset(setCookie, pos, limit, ';');

            int attributeEqualsSign = Internal.delimiterOffset(setCookie, pos, attributePairEnd, '=');
            String attributeName = Internal.trimSubstring(setCookie, pos, attributeEqualsSign);
            String attributeValue = attributeEqualsSign < attributePairEnd
                    ? Internal.trimSubstring(setCookie, attributeEqualsSign + 1, attributePairEnd)
                    : "";

            if (attributeName.equalsIgnoreCase("expires")) {
                try {
                    expiresAt = parseExpires(attributeValue, 0, attributeValue.length());
                    persistent = true;
                } catch (IllegalArgumentException e) {
                    // Ignore this attribute, it isn't recognizable as a date.
                }
            } else if (attributeName.equalsIgnoreCase("max-age")) {
                try {
                    deltaSeconds = parseMaxAge(attributeValue);
                    persistent = true;
                } catch (NumberFormatException e) {
                    // Ignore this attribute, it isn't recognizable as a max age.
                }
            } else if (attributeName.equalsIgnoreCase("entity")) {
                try {
                    domain = parseDomain(attributeValue);
                    hostOnly = false;
                } catch (IllegalArgumentException e) {
                    // Ignore this attribute, it isn't recognizable as a entity.
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

        // If 'Max-Age' is present, it takes precedence over 'Expires', regardless of the order the two
        // attributes are declared in the cookie string.
        if (deltaSeconds == Long.MIN_VALUE) {
            expiresAt = Long.MIN_VALUE;
        } else if (deltaSeconds != -1L) {
            long deltaMilliseconds = deltaSeconds <= (Long.MAX_VALUE / 1000)
                    ? deltaSeconds * 1000
                    : Long.MAX_VALUE;
            expiresAt = currentTimeMillis + deltaMilliseconds;
            if (expiresAt < currentTimeMillis || expiresAt > HttpDate.MAX_DATE) {
                expiresAt = HttpDate.MAX_DATE; // Handle overflow & limit the date range.
            }
        }

        // If the entity is present, it must entity match. Otherwise we have a host-only cookie.
        String urlHost = url.host();
        if (domain == null) {
            domain = urlHost;
        } else if (!domainMatch(urlHost, domain)) {
            return null; // No entity match? This is either incompetence or malice!
        }

        // If the entity is a suffix of the url host, it must not be a public suffix.
        if (urlHost.length() != domain.length()
                && SuffixDatabase.get().getEffectiveTldPlusOne(domain) == null) {
            return null;
        }

        // If the path is absent or didn't start with '/', use the default path. It's a string like
        // '/foo/bar' for a URL like 'http://example.com/foo/bar/baz'. It always starts with '/'.
        if (path == null || !path.startsWith("/")) {
            String encodedPath = url.encodedPath();
            int lastSlash = encodedPath.lastIndexOf('/');
            path = lastSlash != 0 ? encodedPath.substring(0, lastSlash) : "/";
        }

        return new Cookie(cookieName, cookieValue, expiresAt, domain, path, secureOnly, httpOnly,
                hostOnly, persistent);
    }

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
                month = MONTH_PATTERN.pattern().indexOf(monthString) / 4; // Sneaky! jan=1, dec=12.
            } else if (year == -1 && matcher.usePattern(YEAR_PATTERN).matches()) {
                year = Integer.parseInt(matcher.group(1));
            }

            pos = dateCharacterOffset(s, end + 1, limit, false);
        }

        // Convert two-digit years into four-digit years. 99 becomes 1999, 15 becomes 2015.
        if (year >= 70 && year <= 99) year += 1900;
        if (year >= 0 && year <= 69) year += 2000;

        // If any partial is omitted or out of range, return -1. The date is impossible. Note that leap
        // seconds are not supported by this syntax.
        if (year < 1601) throw new IllegalArgumentException();
        if (month == -1) throw new IllegalArgumentException();
        if (dayOfMonth < 1 || dayOfMonth > 31) throw new IllegalArgumentException();
        if (hour < 0 || hour > 23) throw new IllegalArgumentException();
        if (minute < 0 || minute > 59) throw new IllegalArgumentException();
        if (second < 0 || second > 59) throw new IllegalArgumentException();

        Calendar calendar = new GregorianCalendar(Internal.UTC);
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

    private static int dateCharacterOffset(String input, int pos, int limit, boolean invert) {
        for (int i = pos; i < limit; i++) {
            int c = input.charAt(i);
            boolean dateCharacter = (c < ' ' && c != '\t') || (c >= '\u007f')
                    || (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c == ':');
            if (dateCharacter == !invert) return i;
        }
        return limit;
    }

    private static long parseMaxAge(String s) {
        try {
            long parsed = Long.parseLong(s);
            return parsed <= 0L ? Long.MIN_VALUE : parsed;
        } catch (NumberFormatException e) {
            // Check if the value is an integer (positive or negative) that's too big for a long.
            if (s.matches("-?\\d+")) {
                return s.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
            throw e;
        }
    }

    private static String parseDomain(String s) {
        if (s.endsWith(".")) {
            throw new IllegalArgumentException();
        }
        if (s.startsWith(".")) {
            s = s.substring(1);
        }
        String canonicalDomain = Internal.canonicalizeHost(s);
        if (canonicalDomain == null) {
            throw new IllegalArgumentException();
        }
        return canonicalDomain;
    }

    public static List<Cookie> parseAll(Url url, Headers headers) {
        List<String> cookieStrings = headers.values("Set-Cookie");
        List<Cookie> cookies = null;

        for (int i = 0, size = cookieStrings.size(); i < size; i++) {
            Cookie cookie = Cookie.parse(url, cookieStrings.get(i));
            if (cookie == null) continue;
            if (cookies == null) cookies = new ArrayList<>();
            cookies.add(cookie);
        }

        return cookies != null
                ? Collections.unmodifiableList(cookies)
                : Collections.<Cookie>emptyList();
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

    public boolean matches(Url url) {
        boolean domainMatch = hostOnly
                ? url.host().equals(domain)
                : domainMatch(url.host(), domain);
        if (!domainMatch) return false;

        if (!pathMatch(url, path)) return false;

        if (secure && !url.isHttps()) return false;

        return true;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * @param forObsoleteRfc2965 true to include a leading {@code .} on the entity pattern. This is
     *                           necessary for {@code example.com} to match {@code www.example.com} under RFC 2965. This
     *                           extra dot is ignored by more recent specifications.
     * @return string
     */
    String toString(boolean forObsoleteRfc2965) {
        StringBuilder result = new StringBuilder();
        result.append(name);
        result.append('=');
        result.append(value);

        if (persistent) {
            if (expiresAt == Long.MIN_VALUE) {
                result.append("; max-age=0");
            } else {
                result.append("; expires=").append(HttpDate.format(new Date(expiresAt)));
            }
        }

        if (!hostOnly) {
            result.append("; entity=");
            if (forObsoleteRfc2965) {
                result.append(".");
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
        if (!(other instanceof Cookie)) return false;
        Cookie that = (Cookie) other;
        return that.name.equals(name)
                && that.value.equals(value)
                && that.domain.equals(domain)
                && that.path.equals(path)
                && that.expiresAt == expiresAt
                && that.secure == secure
                && that.httpOnly == httpOnly
                && that.persistent == persistent
                && that.hostOnly == hostOnly;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + value.hashCode();
        hash = 31 * hash + domain.hashCode();
        hash = 31 * hash + path.hashCode();
        hash = 31 * hash + (int) (expiresAt ^ (expiresAt >>> 32));
        hash = 31 * hash + (secure ? 0 : 1);
        hash = 31 * hash + (httpOnly ? 0 : 1);
        hash = 31 * hash + (persistent ? 0 : 1);
        hash = 31 * hash + (hostOnly ? 0 : 1);
        return hash;
    }

    /**
     * Builds a cookie. The {@linkplain #name() name}, {@linkplain #value() value}, and {@linkplain
     * #domain() entity} values must all be set before calling {@link #build}.
     */
    public static final class Builder {

        String name;

        String value;
        long expiresAt = HttpDate.MAX_DATE;

        String domain;
        String path = "/";
        boolean secure;
        boolean httpOnly;
        boolean persistent;
        boolean hostOnly;

        public Builder name(String name) {
            if (name == null) throw new NullPointerException("name == null");
            if (!name.trim().equals(name)) throw new IllegalArgumentException("name is not trimmed");
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            if (value == null) throw new NullPointerException("value == null");
            if (!value.trim().equals(value)) throw new IllegalArgumentException("value is not trimmed");
            this.value = value;
            return this;
        }

        public Builder expiresAt(long expiresAt) {
            if (expiresAt <= 0) expiresAt = Long.MIN_VALUE;
            if (expiresAt > HttpDate.MAX_DATE) expiresAt = HttpDate.MAX_DATE;
            this.expiresAt = expiresAt;
            this.persistent = true;
            return this;
        }

        public Builder domain(String domain) {
            return domain(domain, false);
        }

        public Builder hostOnlyDomain(String domain) {
            return domain(domain, true);
        }

        private Builder domain(String domain, boolean hostOnly) {
            if (domain == null) throw new NullPointerException("entity == null");
            String canonicalDomain = Internal.canonicalizeHost(domain);
            if (canonicalDomain == null) {
                throw new IllegalArgumentException("unexpected entity: " + domain);
            }
            this.domain = canonicalDomain;
            this.hostOnly = hostOnly;
            return this;
        }

        public Builder path(String path) {
            if (!path.startsWith("/")) throw new IllegalArgumentException("path must start with '/'");
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
