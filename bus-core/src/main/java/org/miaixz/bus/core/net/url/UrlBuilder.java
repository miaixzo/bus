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
package org.miaixz.bus.core.net.url;

import java.io.Serial;
import java.net.*;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;

/**
 * A builder for creating URLs in the format:
 * 
 * <pre>
 * [scheme:]scheme-specific-part[#fragment]
 * [scheme:][//authority][path][?query][#fragment]
 * [scheme:][//host:port][path][?query][#fragment]
 * </pre>
 *
 * @author Kimi Liu
 * @see <a href="https://en.wikipedia.org/wiki/Uniform_Resource_Identifier">Uniform Resource Identifier</a>
 * @since Java 17+
 */
public final class UrlBuilder implements Builder<String> {

    @Serial
    private static final long serialVersionUID = 2852231355809L;

    /**
     * The URL scheme (e.g., "http").
     */
    private String scheme;
    /**
     * The host (e.g., "127.0.0.1").
     */
    private String host;
    /**
     * The port number (-1 for default).
     */
    private int port = -1;
    /**
     * The path (e.g., "/aa/bb/cc").
     */
    private UrlPath path;
    /**
     * The query string (e.g., "a=1&amp;b=2").
     */
    private UrlQuery query;
    /**
     * The fragment identifier (the part after "#").
     */
    private String fragment;
    /**
     * The character set for URL encoding and decoding.
     */
    private java.nio.charset.Charset charset;

    /**
     * Constructs a new {@link UrlBuilder} with default settings (UTF-8 charset).
     */
    public UrlBuilder() {
        this.charset = Charset.UTF_8;
    }

    /**
     * Constructs a new {@link UrlBuilder} with the specified components.
     *
     * @param scheme   The URL scheme (e.g., "http").
     * @param host     The host (e.g., "127.0.0.1").
     * @param port     The port number (-1 for default).
     * @param path     The URL path.
     * @param query    The URL query.
     * @param fragment The fragment identifier.
     * @param charset  The character set for encoding and decoding ({@code null} for no encoding).
     */
    public UrlBuilder(final String scheme, final String host, final int port, final UrlPath path, final UrlQuery query,
            final String fragment, final java.nio.charset.Charset charset) {
        this.charset = charset;
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.setFragment(fragment);
    }

    /**
     * Creates a new {@link UrlBuilder} from an existing {@link UrlBuilder}.
     *
     * @param builder The {@link UrlBuilder} to copy.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder of(final UrlBuilder builder) {
        return of(
                builder.getScheme(),
                builder.getHost(),
                builder.getPort(),
                builder.getPaths(),
                builder.getQuerys(),
                builder.getFragment(),
                builder.getCharset());
    }

    /**
     * Creates a new {@link UrlBuilder} from a {@link URI}.
     *
     * @param uri     The {@link URI}.
     * @param charset The character set for encoding and decoding.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder of(final URI uri, final java.nio.charset.Charset charset) {
        return of(
                uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath(),
                uri.getRawQuery(),
                uri.getFragment(),
                charset);
    }

    /**
     * Creates a new {@link UrlBuilder} from a URL string, assuming the HTTP protocol if no protocol is specified. This
     * method does not perform any URL encoding.
     *
     * @param httpUrl The URL string.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder ofHttpWithoutEncode(final String httpUrl) {
        return ofHttp(httpUrl, null);
    }

    /**
     * Creates a new {@link UrlBuilder} from a URL string, assuming the HTTP protocol if no protocol is specified. The
     * default encoding is UTF-8.
     *
     * @param httpUrl The URL string.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder ofHttp(final String httpUrl) {
        return ofHttp(httpUrl, Charset.UTF_8);
    }

    /**
     * Creates a new {@link UrlBuilder} from a URL string, assuming the HTTP protocol if no protocol is specified.
     * <ul>
     * <li>If the URL is not encoded, set {@code charset} to {@code null} to prevent decoding and encoding.</li>
     * <li>If the URL is already encoded (or partially encoded), set {@code charset} to decode and re-encode during the
     * build.</li>
     * <li>If the URL is not encoded and contains ambiguous characters, set {@code charset} to {@code null} and then
     * call {@link #setCharset(java.nio.charset.Charset)} to encode the URL during the build.</li>
     * </ul>
     *
     * @param httpUrl The URL string.
     * @param charset The character set for encoding and decoding; if {@code null}, no decoding is performed.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder ofHttp(String httpUrl, final java.nio.charset.Charset charset) {
        Assert.notBlank(httpUrl, "Url must be not blank!");

        httpUrl = StringKit.trimPrefix(httpUrl);
        if (!StringKit.startWithAnyIgnoreCase(httpUrl, "http://", "https://")) {
            httpUrl = "http://" + httpUrl;
        }

        return of(UrlKit.toUrlForHttp(httpUrl), charset);
    }

    /**
     * Creates a new {@link UrlBuilder} from a URL string with default UTF-8 encoding. If the URL does not have a
     * network protocol, it will attempt to use the file protocol.
     *
     * @param url The URL string.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder of(final String url) {
        return of(url, Charset.UTF_8);
    }

    /**
     * Creates a new {@link UrlBuilder} from a URL string.
     * <ul>
     * <li>If the URL is not encoded, set {@code charset} to {@code null} to prevent decoding and encoding.</li>
     * <li>If the URL is already encoded (or partially encoded), set {@code charset} to decode and re-encode during the
     * build.</li>
     * <li>If the URL is not encoded and contains ambiguous characters, set {@code charset} to {@code null} and then
     * call {@link #setCharset(java.nio.charset.Charset)} to encode the URL during the build.</li>
     * </ul>
     *
     * @param url     The URL string.
     * @param charset The character set for encoding and decoding.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder of(final String url, final java.nio.charset.Charset charset) {
        Assert.notBlank(url, "Url must be not blank!");
        return of(UrlKit.url(StringKit.trim(url)), charset);
    }

    /**
     * Creates a new {@link UrlBuilder} from a {@link URL}.
     *
     * @param url     The {@link URL}.
     * @param charset The character set for encoding and decoding ({@code null} for no decoding).
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder of(final URL url, final java.nio.charset.Charset charset) {
        return of(
                url.getProtocol(),
                url.getHost(),
                url.getPort(),
                url.getPath(),
                url.getQuery(),
                url.getRef(),
                charset);
    }

    /**
     * Creates a new {@link UrlBuilder} with the specified components.
     *
     * @param scheme   The URL scheme (e.g., "http").
     * @param host     The host (e.g., "127.0.0.1").
     * @param port     The port number (-1 for default).
     * @param path     The URL path string.
     * @param query    The URL query string.
     * @param fragment The fragment identifier.
     * @param charset  The character set for encoding and decoding.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder of(
            final String scheme,
            final String host,
            final int port,
            final String path,
            final String query,
            final String fragment,
            final java.nio.charset.Charset charset) {
        return of(scheme, host, port, UrlPath.of(path, charset), UrlQuery.of(query, charset, false), fragment, charset);
    }

    /**
     * Creates a new {@link UrlBuilder} with the specified components.
     *
     * @param scheme   The URL scheme (e.g., "http").
     * @param host     The host (e.g., "127.0.0.1").
     * @param port     The port number (-1 for default).
     * @param path     The {@link UrlPath}.
     * @param query    The {@link UrlQuery}.
     * @param fragment The fragment identifier.
     * @param charset  The character set for encoding and decoding.
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder of(
            final String scheme,
            final String host,
            final int port,
            final UrlPath path,
            final UrlQuery query,
            final String fragment,
            final java.nio.charset.Charset charset) {
        return new UrlBuilder(scheme, host, port, path, query, fragment, charset);
    }

    /**
     * Creates a new, empty {@link UrlBuilder}.
     *
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder of() {
        return new UrlBuilder();
    }

    /**
     * Returns the URL scheme (e.g., "http").
     *
     * @return The URL scheme.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the URL scheme (e.g., "http").
     *
     * @param scheme The URL scheme.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder setScheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Returns the URL scheme, defaulting to "http" if not specified.
     *
     * @return The URL scheme.
     */
    public String getSchemeWithDefault() {
        return StringKit.defaultIfEmpty(this.scheme, Protocol.HTTP.name);
    }

    /**
     * Returns the host (e.g., "127.0.0.1").
     *
     * @return The host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host (e.g., "127.0.0.1").
     *
     * @param host The host.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder setHost(final String host) {
        this.host = host;
        return this;
    }

    /**
     * Returns the port number.
     *
     * @return The port number, or -1 if not set.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number.
     *
     * @param port The port number (-1 for default).
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder setPort(final int port) {
        this.port = port;
        return this;
    }

    /**
     * Returns the port number, or the default port for the protocol if not set.
     *
     * @return The port number.
     */
    public int getPortWithDefault() {
        int port = getPort();
        if (port <= 0) {
            port = toURL().getDefaultPort();
        }
        return port;
    }

    /**
     * Returns the authority part of the URL (host:port).
     *
     * @return The authority string.
     */
    public String getAuthority() {
        return (port < 0) ? host : host + Symbol.COLON + port;
    }

    /**
     * Returns the {@link UrlPath} object.
     *
     * @return The {@link UrlPath}.
     */
    public UrlPath getPath() {
        return path;
    }

    /**
     * Sets the URL path, replacing any existing path.
     *
     * @param path The {@link UrlPath}.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder setPath(final UrlPath path) {
        this.path = path;
        return this;
    }

    /**
     * Returns the path string.
     *
     * @return The path string (e.g., "/aa/bb/cc").
     */
    public String getPaths() {
        return null == this.path ? Symbol.SLASH : this.path.build(charset);
    }

    /**
     * Appends a path to the existing path.
     *
     * @param path The path to append (e.g., "aaa/bbb/ccc").
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder addPath(final CharSequence path) {
        UrlPath.of(path, this.charset).getSegments().forEach(this::addPathSegment);
        return this;
    }

    /**
     * Sets whether to add a trailing slash to the path.
     *
     * @param withEngTag {@code true} to add a trailing slash.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder setWithEndTag(final boolean withEngTag) {
        if (null == this.path) {
            this.path = UrlPath.of();
        }
        this.path.setWithEndTag(withEngTag);
        return this;
    }

    /**
     * Adds a path segment. Slashes ("/") in the segment will be encoded as "%2F".
     *
     * @param segment The path segment.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder addPathSegment(final CharSequence segment) {
        if (StringKit.isEmpty(segment)) {
            return this;
        }
        if (null == this.path) {
            this.path = new UrlPath();
        }
        this.path.add(segment);
        return this;
    }

    /**
     * Returns the {@link UrlQuery} object.
     *
     * @return The {@link UrlQuery}, which may be {@code null}.
     */
    public UrlQuery getQuery() {
        return query;
    }

    /**
     * Sets the URL query, replacing any existing query.
     *
     * @param query The {@link UrlQuery}.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder setQuery(final UrlQuery query) {
        this.query = query;
        return this;
    }

    /**
     * Returns the query string.
     *
     * @return The query string (e.g., "a=1&amp;b=2").
     */
    public String getQuerys() {
        return null == this.query ? null : this.query.build(this.charset);
    }

    /**
     * Adds a query parameter. Duplicate keys are allowed.
     *
     * @param key   The parameter key.
     * @param value The parameter value.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder addQuery(final String key, final Object value) {
        if (StringKit.isEmpty(key)) {
            return this;
        }
        if (this.query == null) {
            this.query = UrlQuery.of();
        }
        this.query.add(key, value);
        return this;
    }

    /**
     * Returns the fragment identifier (the part after "#").
     *
     * @return The fragment identifier.
     */
    public String getFragment() {
        return fragment;
    }

    /**
     * Sets the fragment identifier (the part after "#").
     *
     * @param fragment The fragment identifier.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder setFragment(final String fragment) {
        if (StringKit.isEmpty(fragment)) {
            this.fragment = null;
        }
        this.fragment = StringKit.removePrefix(fragment, Symbol.HASH);
        return this;
    }

    /**
     * Returns the encoded fragment identifier.
     *
     * @return The encoded fragment identifier.
     */
    public String getFragmentEncoded() {
        return RFC3986.FRAGMENT.encode(this.fragment, this.charset);
    }

    /**
     * Returns the character set for URL encoding and decoding.
     *
     * @return The character set.
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * Sets the character set for URL encoding and decoding.
     *
     * @param charset The character set.
     * @return This {@link UrlBuilder} for method chaining.
     */
    public UrlBuilder setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Builds the URL string.
     *
     * @return The URL string.
     */
    @Override
    public String build() {
        return toURL().toString();
    }

    /**
     * Converts this builder to a {@link URL} object.
     *
     * @return The {@link URL}.
     */
    public URL toURL() {
        return toURL(null);
    }

    /**
     * Converts this builder to a {@link URL} object.
     *
     * @param handler The {@link URLStreamHandler} ({@code null} for default).
     * @return The {@link URL}.
     */
    public URL toURL(final URLStreamHandler handler) {
        final StringBuilder fileBuilder = new StringBuilder();

        fileBuilder.append(getPaths());

        final String query = getQuerys();
        if (StringKit.isNotBlank(query)) {
            fileBuilder.append(Symbol.C_QUESTION_MARK).append(query);
        }

        if (StringKit.isNotBlank(this.fragment)) {
            fileBuilder.append(Symbol.C_HASH).append(getFragmentEncoded());
        }

        try {
            // Create URL string with port logic
            String urlStr = getSchemeWithDefault() + "://" + host;
            if (port > 0 && !isDefaultPort(getSchemeWithDefault(), port)) {
                urlStr += ":" + port;
            }
            urlStr += fileBuilder.toString();
            return URI.create(urlStr).toURL();
        } catch (final MalformedURLException | IllegalArgumentException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Check if the given port is the default port for the scheme.
     */
    private boolean isDefaultPort(String scheme, int portNum) {
        return switch (scheme.toLowerCase()) {
            case "http" -> portNum == 80;
            case "https" -> portNum == 443;
            case "ftp" -> portNum == 21;
            case "sftp" -> portNum == 22;
            default -> false;
        };
    }

    /**
     * Converts this builder to a {@link URI} object.
     *
     * @return The {@link URI}.
     */
    public URI toURI() {
        try {
            return toURL().toURI();
        } catch (final URISyntaxException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return build();
    }

}
