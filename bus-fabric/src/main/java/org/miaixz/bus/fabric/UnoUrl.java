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
package org.miaixz.bus.fabric;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable URL value object with centralized parsing and query encoding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class UnoUrl {

    /**
     * Parsed protocol address.
     */
    private final Address address;

    /**
     * Normalized URL path.
     */
    private final String path;

    /**
     * Decoded username.
     */
    private final String username;

    /**
     * Decoded password.
     */
    private final String password;

    /**
     * Decoded fragment.
     */
    private final String fragment;

    /**
     * Decoded query pairs in URL order.
     */
    private final List<QueryParameter> queryParameters;

    /**
     * Decoded query values in insertion order.
     */
    private final Map<String, List<String>> query;

    /**
     * Cached encoded URL.
     */
    private volatile String encoded;

    /**
     * Creates an immutable URL.
     *
     * @param address parsed address
     * @param path    normalized path
     * @param query   decoded query values
     */
    UnoUrl(final Address address, final String path, final Map<String, List<String>> query) {
        this(address, path, parametersFromMap(query), Normal.EMPTY, Normal.EMPTY, null, null);
    }

    /**
     * Creates an immutable URL.
     *
     * @param address parsed address
     * @param path    normalized path
     * @param query   decoded query values
     * @param encoded cached encoded URL
     */
    UnoUrl(final Address address, final String path, final Map<String, List<String>> query, final String encoded) {
        this(address, path, parametersFromMap(query), Normal.EMPTY, Normal.EMPTY, null, encoded);
    }

    /**
     * Creates an immutable URL.
     *
     * @param address         parsed address
     * @param path            normalized path
     * @param queryParameters decoded query pairs
     * @param username        decoded username
     * @param password        decoded password
     * @param fragment        decoded fragment
     * @param encoded         cached encoded URL
     */
    private UnoUrl(final Address address, final String path, final List<QueryParameter> queryParameters,
            final String username, final String password, final String fragment, final String encoded) {
        if (address == null) {
            throw new ValidateException("Address must not be null");
        }
        this.address = address;
        this.path = normalizePath(path);
        this.username = validateComponent(username == null ? Normal.EMPTY : username, "Username");
        this.password = validateComponent(password == null ? Normal.EMPTY : password, "Password");
        this.fragment = fragment == null ? null : validateComponent(fragment, "Fragment");
        this.queryParameters = immutableParameters(queryParameters);
        this.query = immutableQuery(queryFromParameters(this.queryParameters));
        this.encoded = encoded;
    }

    /**
     * Parses a complete URL.
     *
     * @param value URL value
     * @return parsed URL
     */
    public static UnoUrl parse(final String value) {
        if (StringKit.isBlank(value)) {
            throw new ValidateException("URL value must be non-blank");
        }
        final String source = StringKit.trim(value);
        final UnoUrl fast = fastParse(source);
        if (fast != null) {
            return fast;
        }
        try {
            final URI uri = new URI(source);
            final Address parsedAddress = Address.from(uri);
            final String decodedPath = normalizePath(uri.getPath());
            final Address address = new Address(parsedAddress.scheme(), parsedAddress.host(), parsedAddress.port(),
                    decodedPath);
            final String cached = reusableEncoded(uri, source) ? source : null;
            final String[] userInfo = parseUserInfo(uri.getRawUserInfo());
            final String decodedFragment = uri.getRawFragment() == null ? null : decode(uri.getRawFragment());
            return new UnoUrl(address, decodedPath, parseQueryParameters(uri.getRawQuery()), userInfo[0], userInfo[1],
                    decodedFragment, cached);
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Invalid URL", e);
        } catch (final IllegalArgumentException e) {
            throw new ProtocolException("Invalid URL encoding", e);
        }
    }

    /**
     * Parses a complete URL.
     *
     * @param value URL value
     * @return parsed URL
     */
    public static UnoUrl get(final String value) {
        return parse(value);
    }

    /**
     * Returns the default port for a supported scheme.
     *
     * @param scheme scheme
     * @return default port, or -1 when unsupported
     */
    public static int defaultPort(final String scheme) {
        if (scheme == null) {
            return -1;
        }
        return switch (scheme.toLowerCase(Locale.ROOT)) {
            case "http", "ws" -> 80;
            case "https", "wss" -> 443;
            default -> -1;
        };
    }

    /**
     * Creates a URL builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the parsed address.
     *
     * @return address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns the URL scheme.
     *
     * @return scheme
     */
    public String scheme() {
        return address.scheme();
    }

    /**
     * Returns the URL host.
     *
     * @return host
     */
    public String host() {
        return address.host();
    }

    /**
     * Returns the effective URL port.
     *
     * @return port
     */
    public int port() {
        return address.port();
    }

    /**
     * Returns the normalized path.
     *
     * @return path
     */
    public String path() {
        return path;
    }

    /**
     * Returns a query snapshot.
     *
     * @return query snapshot
     */
    public Map<String, List<String>> query() {
        return immutableQuery(query);
    }

    /**
     * Reads the first value for a query key.
     *
     * @param key query key
     * @return first value or null
     */
    public String query(final String key) {
        final List<String> values = query.get(validateKey(key));
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    /**
     * Reads the first value for a query parameter name.
     *
     * @param name query key
     * @return first value or null
     */
    public String queryParameter(final String name) {
        return query(name);
    }

    /**
     * Returns a URL with an appended query value.
     *
     * @param key   query key
     * @param value query value
     * @return updated URL
     */
    public UnoUrl withQuery(final String key, final String value) {
        validateKey(key);
        if (value == null) {
            throw new ValidateException("Query value must not be null");
        }
        final ArrayList<QueryParameter> copy = new ArrayList<>(queryParameters);
        copy.add(new QueryParameter(key, value));
        return new UnoUrl(address, path, copy, username, password, fragment, null);
    }

    /**
     * Returns a URL without all values for a query key.
     *
     * @param key query key
     * @return updated URL
     */
    public UnoUrl withoutQuery(final String key) {
        final String checkedKey = validateKey(key);
        final ArrayList<QueryParameter> copy = new ArrayList<>();
        for (final QueryParameter parameter : queryParameters) {
            if (!parameter.name().equals(checkedKey)) {
                copy.add(parameter);
            }
        }
        return new UnoUrl(address, path, copy, username, password, fragment, null);
    }

    /**
     * Returns the encoded URL.
     *
     * @return encoded URL
     */
    public String encoded() {
        String current = encoded;
        if (current != null) {
            return current;
        }
        current = buildEncoded(address, path, queryParameters, username, password, fragment);
        encoded = current;
        return current;
    }

    /**
     * Returns encoded username.
     *
     * @return encoded username
     */
    public String encodedUsername() {
        return username.isEmpty() ? Normal.EMPTY : encode(username);
    }

    /**
     * Returns decoded username.
     *
     * @return username
     */
    public String username() {
        return username;
    }

    /**
     * Returns encoded password.
     *
     * @return encoded password
     */
    public String encodedPassword() {
        return password.isEmpty() ? Normal.EMPTY : encode(password);
    }

    /**
     * Returns decoded password.
     *
     * @return password
     */
    public String password() {
        return password;
    }

    /**
     * Returns decoded path segments.
     *
     * @return path segments
     */
    public List<String> pathSegments() {
        if (Symbol.SLASH.equals(path)) {
            return List.of(Normal.EMPTY);
        }
        return List.of(path.substring(1).split(Symbol.SLASH, -1));
    }

    /**
     * Returns encoded path segments.
     *
     * @return encoded path segments
     */
    public List<String> encodedPathSegments() {
        return pathSegments().stream().map(UnoUrl::encode).toList();
    }

    /**
     * Returns query pair count.
     *
     * @return query size
     */
    public int querySize() {
        return queryParameters.size();
    }

    /**
     * Returns query parameter names in first-seen order.
     *
     * @return query names
     */
    public java.util.Set<String> queryParameterNames() {
        final java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<>();
        queryParameters.forEach(parameter -> names.add(parameter.name()));
        return java.util.Collections.unmodifiableSet(names);
    }

    /**
     * Returns values for a query name in URL order.
     *
     * @param name query name
     * @return query values
     */
    public List<String> queryParameterValues(final String name) {
        final String checkedName = validateKey(name);
        final ArrayList<String> values = new ArrayList<>();
        for (final QueryParameter parameter : queryParameters) {
            if (parameter.name().equals(checkedName)) {
                values.add(parameter.value());
            }
        }
        return List.copyOf(values);
    }

    /**
     * Returns query name by pair index.
     *
     * @param index pair index
     * @return query name
     */
    public String queryParameterName(final int index) {
        return queryParameters.get(index).name();
    }

    /**
     * Returns query value by pair index.
     *
     * @param index pair index
     * @return query value
     */
    public String queryParameterValue(final int index) {
        return queryParameters.get(index).value();
    }

    /**
     * Returns encoded fragment.
     *
     * @return encoded fragment or null
     */
    public String encodedFragment() {
        return fragment == null ? null : encode(fragment);
    }

    /**
     * Returns decoded fragment.
     *
     * @return decoded fragment or null
     */
    public String fragment() {
        return fragment;
    }

    /**
     * Returns a redacted URL for logs.
     *
     * @return redacted URL
     */
    public String redact() {
        return builder().scheme(scheme()).host(host()).port(port()).path("/...").build().encoded();
    }

    /**
     * Resolves a relative or absolute link against this URL.
     *
     * @param link link
     * @return resolved URL, or null when invalid
     */
    public UnoUrl resolve(final String link) {
        if (link == null) {
            return null;
        }
        try {
            return parse(toUri().resolve(link).toString());
        } catch (final RuntimeException e) {
            return null;
        }
    }

    /**
     * Creates a builder initialized from this URL.
     *
     * @return builder
     */
    public Builder newBuilder() {
        final Builder builder = builder().scheme(scheme()).host(host()).port(port()).path(path).username(username)
                .password(password);
        if (fragment != null) {
            builder.fragment(fragment);
        }
        queryParameters.forEach(parameter -> builder.query(parameter.name(), parameter.value()));
        return builder;
    }

    /**
     * Resolves a link and returns an initialized builder.
     *
     * @param link link
     * @return builder, or null when invalid
     */
    public Builder newBuilder(final String link) {
        final UnoUrl resolved = resolve(link);
        return resolved == null ? null : resolved.newBuilder();
    }

    /**
     * Converts this URL to a URI.
     *
     * @return URI
     */
    public URI toUri() {
        try {
            return new URI(encoded());
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Unable to create URL URI", e);
        }
    }

    /**
     * Converts this URL to a URI.
     *
     * @return URI
     */
    public URI uri() {
        return toUri();
    }

    @Override
    public String toString() {
        return encoded();
    }

    /**
     * Builds the encoded URL.
     *
     * @param address address
     * @param path    path
     * @param query   query
     * @return encoded URL
     */
    private static String buildEncoded(
            final Address address,
            final String path,
            final List<QueryParameter> query,
            final String username,
            final String password,
            final String fragment) {
        final StringBuilder builder = new StringBuilder();
        builder.append(address.scheme()).append("://");
        if (StringKit.isNotEmpty(username) || StringKit.isNotEmpty(password)) {
            builder.append(encode(username));
            if (StringKit.isNotEmpty(password)) {
                builder.append(Symbol.C_COLON).append(encode(password));
            }
            builder.append(Symbol.C_AT);
        }
        builder.append(formatHost(address.host())).append(Symbol.C_COLON).append(address.port())
                .append(encodePath(path));
        final String encodedQuery = encodeQuery(query);
        if (StringKit.isNotEmpty(encodedQuery)) {
            builder.append(Symbol.QUESTION_MARK).append(encodedQuery);
        }
        if (fragment != null) {
            builder.append(Symbol.C_HASH).append(encode(fragment));
        }
        return builder.toString();
    }

    /**
     * Creates an immutable query map.
     *
     * @param source source query
     * @return immutable query
     */
    private static Map<String, List<String>> immutableQuery(final Map<String, List<String>> source) {
        final LinkedHashMap<String, List<String>> copy = new LinkedHashMap<>();
        if (source != null) {
            for (final Map.Entry<String, List<String>> entry : source.entrySet()) {
                final String key = validateKey(entry.getKey());
                final List<String> values = entry.getValue() == null ? List.of() : entry.getValue();
                copy.put(key, Collections.unmodifiableList(new ArrayList<>(values)));
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Creates immutable query parameters.
     *
     * @param source source parameters
     * @return immutable parameters
     */
    private static List<QueryParameter> immutableParameters(final List<QueryParameter> source) {
        if (source == null) {
            return List.of();
        }
        final ArrayList<QueryParameter> copy = new ArrayList<>(source.size());
        for (final QueryParameter parameter : source) {
            if (parameter == null) {
                throw new ValidateException("Query parameter must not be null");
            }
            copy.add(
                    new QueryParameter(validateKey(parameter.name()),
                            validateComponent(parameter.value(), "Query value")));
        }
        return List.copyOf(copy);
    }

    /**
     * Converts query parameters to grouped map view.
     *
     * @param parameters parameters
     * @return grouped query
     */
    private static Map<String, List<String>> queryFromParameters(final List<QueryParameter> parameters) {
        final LinkedHashMap<String, List<String>> values = new LinkedHashMap<>();
        if (parameters != null) {
            for (final QueryParameter parameter : parameters) {
                values.computeIfAbsent(parameter.name(), ignored -> new ArrayList<>()).add(parameter.value());
            }
        }
        return values;
    }

    /**
     * Converts a grouped map into query parameters.
     *
     * @param query grouped query
     * @return query parameters
     */
    private static List<QueryParameter> parametersFromMap(final Map<String, List<String>> query) {
        final ArrayList<QueryParameter> parameters = new ArrayList<>();
        if (query != null) {
            for (final Map.Entry<String, List<String>> entry : query.entrySet()) {
                final String name = validateKey(entry.getKey());
                final List<String> values = entry.getValue() == null ? List.of() : entry.getValue();
                for (final String value : values) {
                    parameters.add(new QueryParameter(name, value));
                }
            }
        }
        return parameters;
    }

    /**
     * Encodes the query map.
     *
     * @param values query values
     * @return query string
     */
    private static String encodeQuery(final List<QueryParameter> values) {
        final StringBuilder builder = new StringBuilder();
        for (final QueryParameter parameter : values) {
            if (StringKit.isNotEmpty(builder)) {
                builder.append(Symbol.C_AND);
            }
            builder.append(encode(parameter.name())).append(Symbol.C_EQUAL).append(encode(parameter.value()));
        }
        return builder.toString();
    }

    /**
     * Encodes a path while preserving slash separators.
     *
     * @param value path
     * @return encoded path
     */
    private static String encodePath(final String value) {
        final StringBuilder builder = new StringBuilder();
        final String[] parts = value.split(Symbol.SLASH, -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                builder.append(Symbol.SLASH);
            }
            builder.append(encode(parts[i]));
        }
        return StringKit.isEmpty(builder) ? Symbol.SLASH : builder.toString();
    }

    /**
     * Encodes one URL component.
     *
     * @param value decoded value
     * @return encoded value
     */
    private static String encode(final String value) {
        return UrlEncoder.encodeComponent(value, Charset.UTF_8);
    }

    /**
     * Decodes one URL component.
     *
     * @param value encoded value
     * @return decoded value
     */
    private static String decode(final String value) {
        return UrlDecoder.decodeStrict(value, Charset.UTF_8, true);
    }

    /**
     * Formats host text for URI output.
     *
     * @param host normalized host
     * @return host text
     */
    private static String formatHost(final String host) {
        return host.indexOf(Symbol.C_COLON) >= 0 && !host.startsWith(Symbol.BRACKET_LEFT)
                ? Symbol.BRACKET_LEFT + host + Symbol.BRACKET_RIGHT
                : host;
    }

    /**
     * Normalizes a URL path.
     *
     * @param value path
     * @return normalized path
     */
    private static String normalizePath(final String value) {
        if (StringKit.isBlank(value)) {
            return Symbol.SLASH;
        }
        return value.startsWith(Symbol.SLASH) ? value : Symbol.SLASH + value;
    }

    /**
     * Parses a raw query string.
     *
     * @param rawQuery raw query
     * @return decoded query map
     */
    private static List<QueryParameter> parseQueryParameters(final String rawQuery) {
        final ArrayList<QueryParameter> values = new ArrayList<>();
        if (StringKit.isEmpty(rawQuery)) {
            return values;
        }
        int start = 0;
        while (start <= rawQuery.length()) {
            final int found = rawQuery.indexOf(Symbol.C_AND, start);
            final int end = found < 0 ? rawQuery.length() : found;
            final String pair = rawQuery.substring(start, end);
            final int separator = pair.indexOf(Symbol.C_EQUAL);
            final String rawKey = separator >= 0 ? pair.substring(0, separator) : pair;
            final String rawValue = separator >= 0 ? pair.substring(separator + 1) : Normal.EMPTY;
            final String key = decode(rawKey);
            final String value = decode(rawValue);
            validateKey(key);
            values.add(new QueryParameter(key, value));
            if (found < 0) {
                break;
            }
            start = end + 1;
        }
        return values;
    }

    /**
     * Parses the common lowercase absolute URL form without creating a {@link URI}.
     * <p>
     * This path is intentionally narrow: it accepts only scheme, host, optional port, path, and query forms that can be
     * reused exactly after normalization. Anything involving user-info, fragments, IPv6 literals, encoded path bytes,
     * or non-common schemes falls back to the full URI parser.
     * </p>
     *
     * @param source candidate URL text
     * @return parsed URL, or {@code null} when the slower parser must handle the input
     */
    private static UnoUrl fastParse(final String source) {
        final int schemeEnd = source.indexOf("://");
        if (schemeEnd <= 0 || source.indexOf(Symbol.C_HASH) >= 0) {
            return null;
        }
        final String scheme = source.substring(0, schemeEnd);
        if (!commonScheme(scheme)) {
            return null;
        }
        final int authorityStart = schemeEnd + 3;
        final int queryStart = source.indexOf(Symbol.C_QUESTION_MARK, authorityStart);
        final int pathStart = source.indexOf(Symbol.C_SLASH, authorityStart);
        final int authorityEnd = positiveMin(source.length(), queryStart, pathStart);
        if (authorityEnd <= authorityStart || source.indexOf(Symbol.C_AT, authorityStart) >= authorityEnd) {
            return null;
        }
        final String authority = source.substring(authorityStart, authorityEnd);
        if (authority.startsWith(Symbol.BRACKET_LEFT)) {
            return null;
        }
        final int colon = authority.lastIndexOf(Symbol.C_COLON);
        final String host = colon > 0 ? authority.substring(0, colon) : authority;
        if (StringKit.isEmpty(host) || !host.equals(host.toLowerCase(Locale.ROOT))) {
            return null;
        }
        final int port = colon > 0 ? parsePort(authority.substring(colon + 1)) : defaultPort(scheme);
        if (port < 0) {
            return null;
        }
        final int pathEnd = queryStart >= 0 ? queryStart : source.length();
        final String rawPath = pathStart >= 0 && pathStart < pathEnd ? source.substring(pathStart, pathEnd)
                : Symbol.SLASH;
        if (rawPath.indexOf(Symbol.C_PERCENT) >= 0 || rawPath.indexOf(Symbol.C_PLUS) >= 0) {
            return null;
        }
        final String rawQuery = queryStart >= 0 ? source.substring(queryStart + 1) : null;
        final String path = normalizePath(rawPath);
        final Address address = new Address(scheme, host, port, path);
        final String cached = colon > 0 && pathStart >= 0 && (rawQuery == null || rawQuery.indexOf(Symbol.C_PLUS) < 0)
                ? source
                : null;
        return new UnoUrl(address, path, parseQueryParameters(rawQuery), Normal.EMPTY, Normal.EMPTY, null, cached);
    }

    /**
     * Parses raw user info.
     *
     * @param rawUserInfo raw user info
     * @return username/password pair
     */
    private static String[] parseUserInfo(final String rawUserInfo) {
        if (rawUserInfo == null) {
            return new String[] { Normal.EMPTY, Normal.EMPTY };
        }
        final int separator = rawUserInfo.indexOf(Symbol.C_COLON);
        final String rawUsername = separator >= 0 ? rawUserInfo.substring(0, separator) : rawUserInfo;
        final String rawPassword = separator >= 0 ? rawUserInfo.substring(separator + 1) : Normal.EMPTY;
        return new String[] { decode(rawUsername), decode(rawPassword) };
    }

    /**
     * Returns whether a scheme is one of the client-facing protocols supported by the fast parser.
     *
     * @param scheme lowercase scheme candidate
     * @return {@code true} for HTTP, HTTPS, WebSocket, and secure WebSocket schemes
     */
    private static boolean commonScheme(final String scheme) {
        return "http".equals(scheme) || "https".equals(scheme) || "ws".equals(scheme) || "wss".equals(scheme);
    }

    /**
     * Parses an explicit TCP port without accepting signs, whitespace, or zero.
     *
     * @param value decimal port text
     * @return port in the valid user range, or {@code -1} when invalid
     */
    private static int parsePort(final String value) {
        if (StringKit.isEmpty(value)) {
            return -1;
        }
        int port = 0;
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current < Symbol.C_ZERO || current > Symbol.C_NINE) {
                return -1;
            }
            port = port * 10 + current - Symbol.C_ZERO;
            if (port > 65535) {
                return -1;
            }
        }
        return port == 0 ? -1 : port;
    }

    /**
     * Selects the earliest non-negative delimiter position while keeping a known upper bound.
     *
     * @param fallback upper bound used when no delimiter is present
     * @param first    first delimiter position
     * @param second   second delimiter position
     * @return minimum non-negative position
     */
    private static int positiveMin(final int fallback, final int first, final int second) {
        int result = fallback;
        if (first >= 0 && first < result) {
            result = first;
        }
        if (second >= 0 && second < result) {
            result = second;
        }
        return result;
    }

    /**
     * Checks whether the original URL text can be retained as the encoded representation.
     * <p>
     * Reuse is limited to fully explicit lowercase authority forms without user-info or fragments, because the encoded
     * value is later exposed as the stable wire form.
     * </p>
     *
     * @param uri    parsed URI
     * @param source original URL text
     * @return {@code true} when {@code source} is already the canonical encoded form
     */
    private static boolean reusableEncoded(final URI uri, final String source) {
        final String scheme = uri.getScheme();
        final String host = uri.getHost();
        final String path = uri.getRawPath();
        if (scheme == null || host == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null
                || uri.getPort() < 0 || StringKit.isEmpty(path)) {
            return false;
        }
        if (!scheme.equals(scheme.toLowerCase(Locale.ROOT)) || !host.equals(host.toLowerCase(Locale.ROOT))) {
            return false;
        }
        final String query = uri.getRawQuery();
        if (query != null && query.indexOf(Symbol.C_PLUS) >= 0) {
            return false;
        }
        final String prefix = scheme + "://" + formatHost(host) + Symbol.C_COLON + uri.getPort();
        return source.startsWith(prefix);
    }

    /**
     * Validates single-line query keys.
     *
     * @param key query key
     * @return valid key
     */
    private static String validateKey(final String key) {
        if (StringKit.isBlank(key) || StringKit.containsAny(key, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Query key must be non-blank and single-line");
        }
        return key;
    }

    /**
     * Validates required builder text.
     *
     * @param value text
     * @param name  field name
     * @return valid text
     */
    private static String validateText(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

    /**
     * Validates an optional URL component.
     *
     * @param value component
     * @param name  field name
     * @return validated component
     */
    private static String validateComponent(final String value, final String name) {
        if (value == null || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-null and single-line");
        }
        return value;
    }

    /**
     * Decoded query parameter pair.
     *
     * @param name  query name
     * @param value query value
     */
    private record QueryParameter(String name, String value) {
    }

    /**
     * URL builder.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Scheme candidate.
         */
        private String scheme;

        /**
         * Host candidate.
         */
        private String host;

        /**
         * Port candidate.
         */
        private int port = -1;

        /**
         * Path candidate.
         */
        private String path = Symbol.SLASH;

        /**
         * Username candidate.
         */
        private String username = Normal.EMPTY;

        /**
         * Password candidate.
         */
        private String password = Normal.EMPTY;

        /**
         * Fragment candidate.
         */
        private String fragment;

        /**
         * Query candidates in URL order.
         */
        private final List<QueryParameter> queryParameters = new ArrayList<>();

        /**
         * Query candidates.
         */
        private final LinkedHashMap<String, List<String>> query = new LinkedHashMap<>();

        /**
         * Creates a URL builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the scheme.
         *
         * @param scheme scheme
         * @return this builder
         */
        public Builder scheme(final String scheme) {
            this.scheme = validateText(scheme, "Scheme").toLowerCase(Locale.ROOT);
            return this;
        }

        /**
         * Sets the host.
         *
         * @param host host
         * @return this builder
         */
        public Builder host(final String host) {
            this.host = validateText(host, "Host").toLowerCase(Locale.ROOT);
            return this;
        }

        /**
         * Sets the decoded username.
         *
         * @param username username
         * @return this builder
         */
        public Builder username(final String username) {
            this.username = validateComponent(username, "Username");
            return this;
        }

        /**
         * Sets the encoded username.
         *
         * @param encodedUsername encoded username
         * @return this builder
         */
        public Builder encodedUsername(final String encodedUsername) {
            this.username = decode(validateComponent(encodedUsername, "Encoded username"));
            return this;
        }

        /**
         * Sets the decoded password.
         *
         * @param password password
         * @return this builder
         */
        public Builder password(final String password) {
            this.password = validateComponent(password, "Password");
            return this;
        }

        /**
         * Sets the encoded password.
         *
         * @param encodedPassword encoded password
         * @return this builder
         */
        public Builder encodedPassword(final String encodedPassword) {
            this.password = decode(validateComponent(encodedPassword, "Encoded password"));
            return this;
        }

        /**
         * Sets the port.
         *
         * @param port port
         * @return this builder
         */
        public Builder port(final int port) {
            if (port != -1 && (port < 1 || port > 65535)) {
                throw new ValidateException("Port must be -1 or between 1 and 65535");
            }
            this.port = port;
            return this;
        }

        /**
         * Sets the path.
         *
         * @param path path
         * @return this builder
         */
        public Builder path(final String path) {
            this.path = normalizePath(validateText(path, "Path"));
            return this;
        }

        /**
         * Appends a query value.
         *
         * @param key   query key
         * @param value query value
         * @return this builder
         */
        public Builder query(final String key, final String value) {
            validateKey(key);
            validateComponent(value, "Query value");
            query.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
            queryParameters.add(new QueryParameter(key, value));
            return this;
        }

        /**
         * Sets the decoded fragment.
         *
         * @param fragment fragment
         * @return this builder
         */
        public Builder fragment(final String fragment) {
            this.fragment = fragment == null ? null : validateComponent(fragment, "Fragment");
            return this;
        }

        /**
         * Sets the encoded fragment.
         *
         * @param encodedFragment encoded fragment
         * @return this builder
         */
        public Builder encodedFragment(final String encodedFragment) {
            this.fragment = encodedFragment == null ? null
                    : decode(validateComponent(encodedFragment, "Encoded fragment"));
            return this;
        }

        /**
         * Builds an immutable URL.
         *
         * @return URL
         */
        public UnoUrl build() {
            validateText(scheme, "Scheme");
            validateText(host, "Host");
            final String portText = port == -1 ? Normal.EMPTY : Symbol.C_COLON + Integer.toString(port);
            final Address base = Address.parse(scheme + "://" + formatHost(host) + portText + Symbol.SLASH);
            final String normalizedPath = normalizePath(path);
            final Address address = new Address(base.scheme(), base.host(), base.port(), normalizedPath);
            final List<QueryParameter> parameters = queryParameters.isEmpty() ? parametersFromMap(query)
                    : queryParameters;
            return new UnoUrl(address, normalizedPath, parameters, username, password, fragment, null);
        }

    }

}
