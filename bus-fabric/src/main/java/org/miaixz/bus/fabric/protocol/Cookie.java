/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.protocol;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.network.dns.suffix.PublicSuffix;
import org.miaixz.bus.fabric.observe.tags.Tags;

/**
 * Immutable protocol cookie with HTTP-style host, domain, path, secure, and expiry matching.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Cookie {

    /**
     * Cookie name.
     */
    private final String name;

    /**
     * Cookie value.
     */
    private final String value;

    /**
     * Domain cookie suffix, or null for host-only cookies.
     */
    private final String domain;

    /**
     * Host saved for host-only cookies.
     */
    private final String host;

    /**
     * Cookie path.
     */
    private final String path;

    /**
     * Explicit expiration time, or null for a session cookie.
     */
    private final Instant expires;

    /**
     * Secure flag.
     */
    private final boolean secure;

    /**
     * HTTP-only flag.
     */
    private final boolean httpOnly;

    /**
     * Creates an immutable cookie.
     *
     * @param name     cookie name
     * @param value    cookie value
     * @param domain   normalized domain suffix, or null for a host-only cookie
     * @param host     normalized source host for a parsed host-only cookie, otherwise null
     * @param path     cookie path
     * @param expires  explicit expiration time, or null for a session cookie
     * @param secure   secure flag
     * @param httpOnly HTTP-only flag
     */
    private Cookie(final String name, final String value, final String domain, final String host, final String path,
            final Instant expires, final boolean secure, final boolean httpOnly) {
        this.name = validateToken(name, "Cookie name");
        this.value = validateToken(value, "Cookie value");
        this.domain = domain == null ? null : validateCookieDomain(domain);
        this.host = host == null ? null : validateHost(host);
        this.path = normalizePath(path);
        this.expires = expires;
        this.secure = secure;
        this.httpOnly = httpOnly;
    }

    /**
     * Parses a Set-Cookie header value for a URL.
     *
     * @param header Set-Cookie header value to parse
     * @param url    URL from which the header was received
     * @return cookie bound to the supplied URL host when no Domain attribute is present
     */
    public static Cookie parse(final String header, final UnoUrl url) {
        final String checkedHeader = Assert
                .notBlank(header, () -> new ValidateException("Cookie header must be non-blank"));
        final UnoUrl checkedUrl = Assert.notNull(url, () -> new ValidateException("URL must not be null"));
        final String[] parts = checkedHeader.split(Symbol.SEMICOLON);
        final int separator = parts[0].indexOf(Symbol.C_EQUAL);
        if (separator <= 0) {
            throw new ProtocolException("Invalid cookie header");
        }
        final Builder builder = builder(
                parts[0].substring(0, separator).trim(),
                parts[0].substring(separator + 1).trim());
        String domain = null;
        String path = null;
        Instant expires = null;
        for (int i = 1; i < parts.length; i++) {
            final String attribute = parts[i].trim();
            if (attribute.isEmpty()) {
                continue;
            }
            final int attributeSeparator = attribute.indexOf(Symbol.C_EQUAL);
            final String name = (attributeSeparator >= 0 ? attribute.substring(0, attributeSeparator) : attribute)
                    .trim().toLowerCase(Locale.ROOT);
            final String value = attributeSeparator >= 0 ? attribute.substring(attributeSeparator + 1).trim()
                    : Normal.EMPTY;
            switch (name) {
                case "domain" -> domain = validateCookieDomain(value);
                case "path" -> path = normalizePath(value);
                case "expires" -> expires = parseExpires(value);
                case "max-age" -> expires = parseMaxAge(value);
                case "secure" -> builder.secure(true);
                case "httponly" -> builder.httpOnly(true);
                default -> {
                    // Unknown cookie attributes are ignored so future attributes do not break parsing.
                }
            }
        }
        final String sourceHost = validateHost(checkedUrl.address().host());
        if (domain != null) {
            if (!domainMatches(sourceHost, domain)) {
                throw new ProtocolException("Cookie domain does not match URL host");
            }
            builder.domain(domain);
        }
        builder.path(path == null ? defaultPath(checkedUrl.path()) : path);
        if (expires != null) {
            builder.expires(expires);
        }
        return builder.buildWithHost(domain == null ? sourceHost : null);
    }

    /**
     * Creates a cookie builder.
     *
     * @param name  non-blank cookie name without line breaks or semicolons
     * @param value non-blank cookie value without line breaks or semicolons
     * @return new builder initialized with the validated name and value
     */
    public static Builder builder(final String name, final String value) {
        return new Builder(validateToken(name, "Cookie name"), validateToken(value, "Cookie value"));
    }

    /**
     * Returns the cookie name.
     *
     * @return cookie name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the cookie value.
     *
     * @return cookie value
     */
    public String value() {
        return value;
    }

    /**
     * Returns the cookie domain, or null for host-only cookies.
     *
     * @return normalized domain suffix, or null for a host-only cookie
     */
    public String domain() {
        return domain;
    }

    /**
     * Returns the source host for host-only cookies.
     *
     * @return source host, or null for domain cookies
     */
    public String host() {
        return host;
    }

    /**
     * Returns the cookie path.
     *
     * @return cookie path
     */
    public String path() {
        return path;
    }

    /**
     * Returns the cookie expiration time.
     *
     * @return explicit expiration time, or null for a session cookie
     */
    public Instant expires() {
        return expires;
    }

    /**
     * Returns the expiration timestamp in epoch milliseconds.
     *
     * @return expiration time in milliseconds, or a far-future value for session cookies
     */
    public long expiresAt() {
        return expires == null ? org.miaixz.bus.fabric.Builder.COOKIE_MAX_DATE_MILLIS : expires.toEpochMilli();
    }

    /**
     * Returns whether this cookie has an explicit expiration.
     *
     * @return true when persistent
     */
    public boolean persistent() {
        return expires != null;
    }

    /**
     * Returns whether this cookie is host-only.
     *
     * @return true for host-only cookies
     */
    public boolean hostOnly() {
        return domain == null;
    }

    /**
     * Returns whether the cookie is secure.
     *
     * @return true when secure
     */
    public boolean secure() {
        return secure;
    }

    /**
     * Returns whether the cookie is HTTP-only.
     *
     * @return true when HTTP-only
     */
    public boolean httpOnly() {
        return httpOnly;
    }

    /**
     * Returns whether this cookie matches a URL.
     *
     * @param url candidate request URL
     * @return true if the cookie is unexpired and its security, host, domain, and path constraints match the URL
     */
    public boolean matches(final UnoUrl url) {
        final UnoUrl checkedUrl = Assert.notNull(url, () -> new ValidateException("URL must not be null"));
        if (expires != null && !Instant.now().isBefore(expires)) {
            return false;
        }
        if (secure && !checkedUrl.address().secure()) {
            return false;
        }
        final String urlHost = validateHost(checkedUrl.address().host());
        if (domain == null) {
            if (host != null && !host.equals(urlHost)) {
                return false;
            }
        } else if (!urlHost.equals(domain) && !urlHost.endsWith(Symbol.C_DOT + domain)) {
            return false;
        }
        final String urlPath = checkedUrl.path();
        return Symbol.SLASH.equals(path) || urlPath.equals(path)
                || urlPath.startsWith(path.endsWith(Symbol.SLASH) ? path : path + Symbol.SLASH);
    }

    /**
     * Returns a Set-Cookie header value.
     *
     * @return Set-Cookie-compatible header value containing this cookie's configured attributes
     */
    public String header() {
        final StringBuilder builder = new StringBuilder();
        builder.append(name).append(Symbol.C_EQUAL).append(value);
        if (domain != null) {
            builder.append("; Domain=").append(domain);
        }
        builder.append("; Path=").append(path);
        if (expires != null) {
            builder.append("; ").append(Http.Header.EXPIRES).append(Symbol.C_EQUAL)
                    .append(DateTimeFormatter.RFC_1123_DATE_TIME.format(expires.atZone(ZoneOffset.UTC)));
        }
        if (secure) {
            builder.append("; Secure");
        }
        if (httpOnly) {
            builder.append("; HttpOnly");
        }
        return builder.toString();
    }

    /**
     * Returns a redacted Set-Cookie header value for logs and metrics.
     *
     * @return Set-Cookie-compatible header value with the cookie value redacted
     */
    public String redactedHeader() {
        final StringBuilder builder = new StringBuilder();
        builder.append(name).append(Symbol.C_EQUAL).append(Tags.redact(value));
        if (domain != null) {
            builder.append("; Domain=").append(domain);
        }
        builder.append("; Path=").append(path);
        if (expires != null) {
            builder.append("; ").append(Http.Header.EXPIRES).append(Symbol.C_EQUAL)
                    .append(DateTimeFormatter.RFC_1123_DATE_TIME.format(expires.atZone(ZoneOffset.UTC)));
        }
        if (secure) {
            builder.append("; Secure");
        }
        if (httpOnly) {
            builder.append("; HttpOnly");
        }
        return builder.toString();
    }

    /**
     * Parses an Expires attribute.
     *
     * @param value RFC 1123 Expires attribute value
     * @return parsed expiration instant
     */
    private static Instant parseExpires(final String value) {
        try {
            return DateTimeFormatter.RFC_1123_DATE_TIME.parse(value, Instant::from);
        } catch (final DateTimeParseException e) {
            throw new ProtocolException("Invalid cookie expires attribute", e);
        }
    }

    /**
     * Parses a Max-Age attribute.
     *
     * @param value Max-Age attribute value expressed as a signed number of seconds
     * @return instant obtained by adding the supplied seconds to the current time
     */
    private static Instant parseMaxAge(final String value) {
        try {
            return Instant.now().plusSeconds(Long.parseLong(value));
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid cookie max-age attribute", e);
        }
    }

    /**
     * Derives a default cookie path from a URL path.
     *
     * @param value request URL path
     * @return containing directory path, or {@code /} when the path has no containing directory
     */
    private static String defaultPath(final String value) {
        final String normalized = normalizePath(value);
        final int slash = normalized.lastIndexOf(Symbol.SLASH);
        if (slash <= 0) {
            return Symbol.SLASH;
        }
        return normalized.substring(0, slash);
    }

    /**
     * Normalizes a cookie path.
     *
     * @param value non-blank, single-line cookie path
     * @return path with a leading slash
     */
    private static String normalizePath(final String value) {
        final String checked = Assert
                .notBlank(value, () -> new ValidateException("Cookie path must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Cookie path must be non-blank and single-line"));
        return checked.startsWith(Symbol.SLASH) ? checked : Symbol.SLASH + checked;
    }

    /**
     * Validates and normalizes a domain.
     *
     * @param value non-blank, single-line host or domain text
     * @return normalized address literal or lowercase domain without a leading dot
     */
    private static String validateDomain(final String value) {
        final String checked = Assert
                .notBlank(value, () -> new ValidateException("Cookie domain must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Cookie domain must be non-blank and single-line"));
        final String source = checked.startsWith(Symbol.DOT) ? checked.substring(1) : checked;
        final String normalized = NetKit.normalizeHost(source, "Cookie domain");
        if (StringKit.isBlank(normalized)) {
            throw new ValidateException("Cookie domain must be non-blank");
        }
        if (isAddressLiteral(normalized)) {
            return normalized;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    /**
     * Validates and normalizes a host-only source host.
     *
     * @param value source host text
     * @return normalized address literal or lowercase host name
     */
    private static String validateHost(final String value) {
        return validateDomain(value);
    }

    /**
     * Validates and normalizes a Domain attribute.
     *
     * @param value Domain attribute value
     * @return normalized non-public domain suffix
     */
    private static String validateCookieDomain(final String value) {
        final String normalized = validateDomain(value);
        if (isAddressLiteral(normalized) || PublicSuffix.isPublic(normalized)) {
            throw new ValidateException("Cookie domain must not be a public suffix");
        }
        return normalized;
    }

    /**
     * Returns whether a normalized domain is an address literal.
     *
     * @param value normalized domain
     * @return true when address literal
     */
    private static boolean isAddressLiteral(final String value) {
        return value.indexOf(Symbol.C_COLON) >= 0 || Pattern.IPV4_PATTERN.matcher(value).matches();
    }

    /**
     * Returns whether a Domain attribute belongs to a source host.
     *
     * @param host   normalized source host
     * @param domain normalized cookie domain
     * @return true if the host equals the domain or is one of its subdomains
     */
    private static boolean domainMatches(final String host, final String domain) {
        return host.equals(domain) || host.endsWith(Symbol.C_DOT + domain);
    }

    /**
     * Validates a cookie name or value token.
     *
     * @param value cookie name or value to validate
     * @param name  field label used in validation errors
     * @return validated non-blank, single-line text without semicolons
     */
    private static String validateToken(final String value, final String name) {
        final String checked = Assert.notBlank(
                value,
                () -> new ValidateException(name + " must be non-blank, single-line, and semicolon-free"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF) || checked.indexOf(Symbol.C_SEMICOLON) >= 0,
                () -> new ValidateException(name + " must be non-blank, single-line, and semicolon-free"));
        return checked;
    }

    /**
     * Cookie builder.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Cookie name.
         */
        private final String name;

        /**
         * Cookie value.
         */
        private final String value;

        /**
         * Normalized domain suffix, or null when no domain has been configured.
         */
        private String domain;

        /**
         * Cookie path.
         */
        private String path;

        /**
         * Explicit expiration time, or null for a session cookie.
         */
        private Instant expires;

        /**
         * Secure flag.
         */
        private boolean secure;

        /**
         * HTTP-only flag.
         */
        private boolean httpOnly;

        /**
         * Creates a builder.
         *
         * @param name  validated cookie name
         * @param value validated cookie value
         */
        private Builder(final String name, final String value) {
            this.name = name;
            this.value = value;
            this.path = Symbol.SLASH;
        }

        /**
         * Sets the cookie domain.
         *
         * @param domain non-public cookie domain suffix to normalize and store
         * @return this builder
         */
        public Builder domain(final String domain) {
            this.domain = validateCookieDomain(domain);
            return this;
        }

        /**
         * Sets the cookie path.
         *
         * @param path non-blank, single-line cookie path
         * @return this builder
         */
        public Builder path(final String path) {
            this.path = normalizePath(path);
            return this;
        }

        /**
         * Sets the expiration time.
         *
         * @param expires non-null expiration instant
         * @return this builder
         */
        public Builder expires(final Instant expires) {
            this.expires = Assert.notNull(expires, () -> new ValidateException("Cookie expires must not be null"));
            return this;
        }

        /**
         * Sets the expiration timestamp in epoch milliseconds.
         *
         * @param expiresAt epoch-millisecond expiration; non-positive values expire immediately and future values are
         *                  capped
         * @return this builder
         */
        public Builder expiresAt(final long expiresAt) {
            this.expires = expiresAt <= 0 ? Instant.EPOCH
                    : Instant.ofEpochMilli(Math.min(expiresAt, org.miaixz.bus.fabric.Builder.COOKIE_MAX_DATE_MILLIS));
            return this;
        }

        /**
         * Sets the secure flag.
         *
         * @param secure secure flag
         * @return this builder
         */
        public Builder secure(final boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Enables the secure flag.
         *
         * @return this builder
         */
        public Builder secure() {
            return secure(true);
        }

        /**
         * Sets the HTTP-only flag.
         *
         * @param httpOnly HTTP-only flag
         * @return this builder
         */
        public Builder httpOnly(final boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        /**
         * Enables the HTTP-only flag.
         *
         * @return this builder
         */
        public Builder httpOnly() {
            return httpOnly(true);
        }

        /**
         * Builds an immutable cookie.
         *
         * @return immutable cookie without a parser-supplied source-host binding
         */
        public Cookie build() {
            return buildWithHost(null);
        }

        /**
         * Builds an immutable cookie with a source host.
         *
         * @param host source host for a host-only cookie, or null when no binding is required
         * @return immutable cookie carrying the supplied source-host binding
         */
        private Cookie buildWithHost(final String host) {
            return new Cookie(name, value, domain, host, path, expires, secure, httpOnly);
        }

    }

}
