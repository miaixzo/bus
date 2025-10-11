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
package org.miaixz.bus.core.xyz;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.net.url.UrlQuery;

/**
 * URL (Uniform Resource Locator) related utility class.
 *
 * <p>
 * A Uniform Resource Locator describes the specific location of a resource on a particular server. URL composition:
 * 
 * <pre>
 *   protocol://hostname[:port]/path/[:parameters][?query]#Fragment
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlKit {

    /**
     * Converts a {@link URI} to a {@link URL}.
     *
     * @param uri The {@link URI} to convert.
     * @return The converted {@link URL} object.
     * @throws InternalException If the URI format is invalid, wrapping a {@link MalformedURLException}.
     * @see URI#toURL()
     */
    public static URL url(final URI uri) throws InternalException {
        if (null == uri) {
            return null;
        }
        try {
            return uri.toURL();
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a {@link URL} object from a string representation of a URL address.
     *
     * @param url The URL string.
     * @return The {@link URL} object.
     */
    public static URL url(final String url) {
        return url(url, null);
    }

    /**
     * Creates a {@link URL} object from a string representation of a URL address with a specified
     * {@link URLStreamHandler}.
     *
     * @param url     The URL string.
     * @param handler The {@link URLStreamHandler} to use.
     * @return The {@link URL} object.
     * @throws InternalException If a {@link MalformedURLException} occurs, or if a GraalVM-specific protocol access
     *                           error occurs.
     */
    public static URL url(String url, final URLStreamHandler handler) {
        if (null == url) {
            return null;
        }

        // Compatible with Spring's ClassPath path
        if (url.startsWith(Normal.CLASSPATH)) {
            url = url.substring(Normal.CLASSPATH.length());
            return ClassKit.getClassLoader().getResource(url);
        }

        try {
            return new URL(null, url, handler);
        } catch (final MalformedURLException e) {
            if (e.getMessage().contains("Accessing an URL protocol that was not enabled")) {
                // Graalvm packaging requires manual specification of parameters to enable protocols:
                // --enable-url-protocols=http
                // --enable-url-protocols=https
                throw new InternalException(e);
            }

            // Try file path
            try {
                return new File(url).toURI().toURL();
            } catch (final MalformedURLException ex2) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * Retrieves a string protocol URL, similar to {@code string:///xxxxx}.
     *
     * @param content The main content.
     * @return The {@link URI} object.
     */
    public static URI getStringURI(final CharSequence content) {
        if (null == content) {
            return null;
        }
        return URI.create(StringKit.addPrefixIfNot(content, "string:///"));
    }

    /**
     * Converts a URL string to a {@link URL} object, performing necessary validation for HTTP/HTTPS protocols.
     *
     * @param urlStr The URL string.
     * @return The {@link URL} object.
     * @throws InternalException If a {@link MalformedURLException} occurs.
     */
    public static URL toUrlForHttp(final String urlStr) {
        return toUrlForHttp(urlStr, null);
    }

    /**
     * Converts a URL string to a {@link URL} object with a specified {@link URLStreamHandler}, performing necessary
     * validation for HTTP/HTTPS protocols.
     *
     * @param urlStr  The URL string.
     * @param handler The {@link URLStreamHandler} to use.
     * @return The {@link URL} object.
     * @throws InternalException If a {@link MalformedURLException} occurs.
     */
    public static URL toUrlForHttp(String urlStr, final URLStreamHandler handler) {
        Assert.notBlank(urlStr, "Url is blank !");
        // Encode whitespace characters to prevent request exceptions caused by spaces
        urlStr = UrlEncoder.encodeBlank(urlStr);
        try {
            return new URL(null, urlStr, handler);
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves a {@link URL} from the classpath.
     *
     * @param pathBaseClassLoader The relative path (relative to classes).
     * @return The {@link URL}.
     * @see ResourceKit#getResourceUrl(String)
     */
    public static URL getURL(final String pathBaseClassLoader) {
        return ResourceKit.getResourceUrl(pathBaseClassLoader);
    }

    /**
     * Retrieves a {@link URL} relative to a given class.
     *
     * @param path  The path relative to the specified class.
     * @param clazz The specified class.
     * @return The {@link URL}.
     * @see ResourceKit#getResourceUrl(String, Class)
     */
    public static URL getURL(final String path, final Class<?> clazz) {
        return ResourceKit.getResourceUrl(path, clazz);
    }

    /**
     * Retrieves a {@link URL} for a given {@link File} object, commonly used for absolute paths.
     *
     * @param file The {@link File} object corresponding to the URL.
     * @return The {@link URL}.
     * @throws InternalException If a {@link MalformedURLException} occurs during URL conversion.
     */
    public static URL getURL(final File file) {
        Assert.notNull(file, "File is null !");
        try {
            return file.toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new InternalException(e, "Error occurred when get URL!");
        }
    }

    /**
     * Retrieves a new {@link URL} relative to a given base URL. Inspired by:
     * {@code org.springframework.core.io.UrlResource#createRelativeURL}
     *
     * @param url          The base {@link URL}.
     * @param relativePath The relative path.
     * @return The child path {@link URL} relative to the base URL.
     * @throws InternalException If a {@link MalformedURLException} occurs.
     */
    public static URL getURL(final URL url, String relativePath) throws InternalException {
        // # is valid in file paths but invalid in URLs, so escape it here
        relativePath = StringKit.replace(StringKit.removePrefix(relativePath, Symbol.SLASH), Symbol.HASH, "%23");
        try {
            return new URL(url, relativePath);
        } catch (final MalformedURLException e) {
            throw new InternalException(e, "Error occurred when get URL!");
        }
    }

    /**
     * Retrieves an array of {@link URL}s for given {@link File} objects, commonly used for absolute paths.
     *
     * @param files An array of {@link File} objects corresponding to the URLs.
     * @return An array of {@link URL}s.
     * @throws InternalException If a {@link MalformedURLException} occurs during URL conversion.
     */
    public static URL[] getURLs(final File... files) {
        final URL[] urls = new URL[files.length];
        try {
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURI().toURL();
            }
        } catch (final MalformedURLException e) {
            throw new InternalException(e, "Error occurred when get URL!");
        }

        return urls;
    }

    /**
     * Extracts the domain part from a {@link URL}, retaining only the protocol and host, with other parts set to
     * {@code null}.
     *
     * @param url The {@link URL}.
     * @return The domain {@link URI}.
     * @throws InternalException If a {@link URISyntaxException} occurs.
     */
    public static URI getHost(final URL url) {
        if (null == url) {
            return null;
        }

        try {
            return new URI(url.getProtocol(), url.getHost(), null, null);
        } catch (final URISyntaxException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Completes a relative path to an absolute URL.
     *
     * @param baseUrl      The base URL.
     * @param relativePath The relative URL.
     * @return The absolute URL string.
     * @throws InternalException If a {@link MalformedURLException} occurs.
     */
    public static String completeUrl(String baseUrl, final String relativePath) {
        baseUrl = normalize(baseUrl, false);
        if (StringKit.isBlank(baseUrl)) {
            return null;
        }

        try {
            final URL absoluteUrl = new URL(baseUrl);
            final URL parseUrl = new URL(absoluteUrl, relativePath);
            return parseUrl.toString();
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves the path part from a URI string.
     *
     * @param uriStr The URI path string.
     * @return The path string.
     * @throws InternalException Wraps a {@link URISyntaxException}.
     */
    public static String getPath(final String uriStr) {
        return toURI(uriStr).getPath();
    }

    /**
     * Retrieves the decoded path from a {@link URL} object. For local paths, the {@code getPath} method of {@link URL}
     * objects may encode Chinese characters or spaces, leading to incorrect path readings. This method converts the URL
     * to a URI to get the path, solving the encoding issue.
     *
     * @param url The {@link URL}.
     * @return The decoded path string.
     */
    public static String getDecodedPath(final URL url) {
        if (null == url) {
            return null;
        }

        String path = null;
        try {
            // The getPath method of URL objects has issues with Chinese characters or spaces.
            path = toURI(url).getPath();
        } catch (final InternalException e) {
            // ignore
        }
        return (null != path) ? path : url.getPath();
    }

    /**
     * Converts a {@link URL} to a {@link URI}.
     *
     * @param url The {@link URL}.
     * @return The {@link URI}.
     * @throws InternalException Wraps a {@link URISyntaxException}.
     */
    public static URI toURI(final URL url) throws InternalException {
        return toURI(url, false);
    }

    /**
     * Converts a {@link URL} to a {@link URI}, with an option to encode special characters in parameters.
     *
     * @param url      The {@link URL}.
     * @param isEncode Whether to encode special characters in parameters (default UTF-8 encoding).
     * @return The {@link URI}.
     * @throws InternalException Wraps a {@link URISyntaxException}.
     */
    public static URI toURI(final URL url, final boolean isEncode) throws InternalException {
        if (null == url) {
            return null;
        }

        return toURI(url.toString(), isEncode);
    }

    /**
     * Converts a string path to a {@link URI}.
     *
     * @param location The string path.
     * @return The {@link URI}.
     * @throws InternalException Wraps a {@link URISyntaxException}.
     */
    public static URI toURI(final String location) throws InternalException {
        return toURI(location, false);
    }

    /**
     * Converts a string path to a {@link URI}, with an option to encode special characters in parameters.
     *
     * @param location The string path.
     * @param isEncode Whether to encode special characters in parameters (default UTF-8 encoding).
     * @return The {@link URI}.
     * @throws InternalException Wraps a {@link URISyntaxException}.
     */
    public static URI toURI(String location, final boolean isEncode) throws InternalException {
        if (isEncode) {
            location = RFC3986.PATH.encode(location, Charset.UTF_8);
        }
        try {
            return new URI(StringKit.trim(location));
        } catch (final URISyntaxException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves an {@link InputStream} from a {@link URL}.
     *
     * @param url The {@link URL}.
     * @return The {@link InputStream}.
     * @throws InternalException If an {@link IOException} occurs.
     */
    public static InputStream getStream(final URL url) {
        Assert.notNull(url, "URL must be not null");
        try {
            return url.openStream();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves a {@link BufferedReader} from a {@link URL} with a specified character set.
     *
     * @param url     The {@link URL}.
     * @param charset The character set.
     * @return The {@link BufferedReader}.
     */
    public static BufferedReader getReader(final URL url, final java.nio.charset.Charset charset) {
        return IoKit.toReader(getStream(url), charset);
    }

    /**
     * Normalizes a URL string, including:
     * <ol>
     * <li>Automatically adding "http://" prefix if missing.</li>
     * <li>Removing leading backslashes or forward slashes.</li>
     * <li>Replacing backslashes with forward slashes.</li>
     * </ol>
     *
     * @param url The URL string.
     * @return The normalized URL string.
     */
    public static String normalize(final String url) {
        return normalize(url, false);
    }

    /**
     * Normalizes a URL string, including:
     * <ol>
     * <li>Automatically adding "http://" prefix if missing.</li>
     * <li>Removing leading backslashes or forward slashes.</li>
     * <li>Replacing backslashes with forward slashes.</li>
     * </ol>
     * Optionally encodes Chinese characters and special characters in the path part (excluding http:, / and domain).
     *
     * @param url          The URL string.
     * @param isEncodePath Whether to escape Chinese characters and special characters in the path part of the URL.
     * @return The normalized URL string.
     */
    public static String normalize(final String url, final boolean isEncodePath) {
        return normalize(url, isEncodePath, false);
    }

    /**
     * Normalizes a URL string, including:
     * <ol>
     * <li>Automatically adding "http://" prefix if missing.</li>
     * <li>Removing leading backslashes or forward slashes.</li>
     * <li>Replacing backslashes with forward slashes.</li>
     * <li>If {@code replaceSlash} is true, multiple consecutive slashes in the body are replaced with a single
     * slash.</li>
     * </ol>
     *
     * @param url          The URL string.
     * @param isEncodePath Whether to escape Chinese characters and special characters in the path part of the URL.
     * @param replaceSlash Whether to replace multiple consecutive slashes in the URL body with a single slash.
     * @return The normalized URL string.
     */
    public static String normalize(final String url, final boolean isEncodePath, final boolean replaceSlash) {
        if (StringKit.isBlank(url)) {
            return url;
        }
        final int sepIndex = url.indexOf("://");
        final String protocol;
        String body;
        if (sepIndex > 0) {
            protocol = StringKit.subPre(url, sepIndex + 3);
            body = StringKit.subSuf(url, sepIndex + 3);
        } else {
            protocol = "http://";
            body = url;
        }

        final int paramsSepIndex = StringKit.indexOf(body, Symbol.C_QUESTION_MARK);
        String params = null;
        if (paramsSepIndex > 0) {
            params = StringKit.subSuf(body, paramsSepIndex);
            body = StringKit.subPre(body, paramsSepIndex);
        }

        if (StringKit.isNotEmpty(body)) {
            // Remove leading backslashes or forward slashes
            body = body.replaceAll("^[\\\\/]+", Normal.EMPTY);
            // Replace backslashes with forward slashes
            body = body.replace("\\", "/");
            if (replaceSlash) {
                // Double slashes are allowed in URLs and are not replaced by default
                body = body.replaceAll("//+", "/");
            }
        }

        final int pathSepIndex = StringKit.indexOf(body, '/');
        String domain = body;
        String path = null;
        if (pathSepIndex > 0) {
            domain = StringKit.subPre(body, pathSepIndex);
            path = StringKit.subSuf(body, pathSepIndex);
        }
        if (isEncodePath) {
            path = RFC3986.PATH.encode(path, Charset.UTF_8);
        }
        return protocol + domain + StringKit.toStringOrEmpty(path) + StringKit.toStringOrEmpty(params);
    }

    /**
     * Converts a Map of form data into a URL query string format. If a key in {@code paramMap} is empty ({@code null}
     * or ""), it will be ignored. If a value is {@code null}, it will be treated as an empty string (""). Keys and
     * values are automatically URL encoded.
     *
     * <pre>
     * key1=v1&amp;key2=&amp;key3=v3
     * </pre>
     *
     * @param paramMap The form data map.
     * @param charset  The character set for encoding. If {@code null}, no encoding is performed.
     * @return The URL query string.
     */
    public static String buildQuery(final Map<String, ?> paramMap, final java.nio.charset.Charset charset) {
        return UrlQuery.of(paramMap).build(charset);
    }

    /**
     * Encapsulates the Data URI Scheme, with data in Base64 format. The Data URI scheme allows embedding small data
     * directly into web pages inline, avoiding external file loading. Commonly used for embedding images in web pages.
     * Data URI format specification:
     * 
     * <pre>
     *     data:[&lt;mime type&gt;][;charset=&lt;charset&gt;][;&lt;encoding&gt;],&lt;encoded data&gt;
     * </pre>
     *
     * @param mimeType Optional ({@code null} if none), the data type (e.g., image/png, text/plain).
     * @param data     The encoded data.
     * @return The Data URI string.
     */
    public static String getDataUriBase64(final String mimeType, final String data) {
        return getDataUri(mimeType, null, "base64", data);
    }

    /**
     * Encapsulates the Data URI Scheme. The Data URI scheme allows embedding small data directly into web pages inline,
     * avoiding external file loading. Commonly used for embedding images in web pages. Data URI format specification:
     * 
     * <pre>
     *     data:[&lt;mime type&gt;][;charset=&lt;charset&gt;][;&lt;encoding&gt;],&lt;encoded data&gt;
     * </pre>
     *
     * @param mimeType Optional ({@code null} if none), the data type (e.g., image/png, text/plain).
     * @param encoding The data encoding method (e.g., US-ASCII, BASE64).
     * @param data     The encoded data.
     * @return The Data URI string.
     */
    public static String getDataUri(final String mimeType, final String encoding, final String data) {
        return getDataUri(mimeType, null, encoding, data);
    }

    /**
     * Encapsulates the Data URI Scheme. The Data URI scheme allows embedding small data directly into web pages inline,
     * avoiding external file loading. Commonly used for embedding images in web pages. Data URI format specification:
     * 
     * <pre>
     *     data:[&lt;mime type&gt;][;charset=&lt;charset&gt;][;&lt;encoding&gt;],&lt;encoded data&gt;
     * </pre>
     *
     * @param mimeType Optional ({@code null} if none), the data type (e.g., image/png, text/plain).
     * @param charset  Optional ({@code null} if none), the character set encoding of the source text.
     * @param encoding The data encoding method (e.g., US-ASCII, BASE64).
     * @param data     The encoded data.
     * @return The Data URI string.
     */
    public static String getDataUri(
            final String mimeType,
            final java.nio.charset.Charset charset,
            final String encoding,
            final String data) {
        final StringBuilder builder = StringKit.builder("data:");
        if (StringKit.isNotBlank(mimeType)) {
            builder.append(mimeType);
        }
        if (null != charset) {
            builder.append(";charset=").append(charset.name());
        }
        if (StringKit.isNotBlank(encoding)) {
            builder.append(Symbol.C_SEMICOLON).append(encoding);
        }
        builder.append(Symbol.C_COMMA).append(data);

        return builder.toString();
    }

    /**
     * Retrieves the length of the data corresponding to a URL.
     * <ul>
     * <li>If the URL points to a file, it converts to a file and gets the file length.</li>
     * <li>Otherwise, it retrieves the {@link URLConnection#getContentLengthLong()}.</li>
     * </ul>
     *
     * @param url The {@link URL}.
     * @return The length of the data.
     * @throws InternalException If an I/O error occurs or the file does not exist/has zero size.
     */
    public static long size(final URL url) {
        if (Normal.isFileOrVfsURL(url)) {
            // If the resource exists as an independent file, try to get the file length
            final File file = FileKit.file(url);
            final long length = file.length();
            if (length == 0L && !file.exists()) {
                throw new InternalException("File not exist or size is zero!");
            }
            return length;
        } else {
            // If the resource is in a jar package or from the network, use network request to get length
            // From Spring's AbstractFileResolvingResource
            URLConnection conn = null;
            try {
                conn = url.openConnection();
                useCachesIfNecessary(conn);
                if (conn instanceof HttpURLConnection) {
                    final HttpURLConnection httpCon = (HttpURLConnection) conn;
                    httpCon.setRequestMethod("HEAD");
                }
                return conn.getContentLengthLong();
            } catch (final IOException e) {
                throw new InternalException(e);
            } finally {
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }
            }
        }
    }

    /**
     * Sets {@code useCaches} to {@code true} for {@link URLConnection} if it's a JNLP connection.
     *
     * @param con The {@link URLConnection}.
     */
    public static void useCachesIfNecessary(final URLConnection con) {
        con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
    }

    /**
     * Converts a Map of form data into a URL query string format, automatically URL encoding keys and values.
     *
     * @param paramMap The form data map.
     * @return The URL query string.
     */
    public static String toQuery(final Map<String, ?> paramMap) {
        return toQuery(paramMap, Charset.UTF_8);
    }

    /**
     * Converts a Map of form data into a URL query string format. If a key in {@code paramMap} is empty ({@code null}
     * or ""), it will be ignored. If a value is {@code null}, it will be treated as an empty string (""). Keys and
     * values are automatically URL encoded. This method is used for constructing the query part of a URL and is not
     * suitable for form data in POST requests.
     *
     * <pre>
     * key1=v1&amp;key2=&amp;key3=v3
     * </pre>
     *
     * @param paramMap The form data map.
     * @param charset  The character set for encoding. If {@code null}, no encoding is performed for key-value pairs.
     * @return The URL query string.
     */
    public static String toQuery(final Map<String, ?> paramMap, final java.nio.charset.Charset charset) {
        return toQuery(paramMap, charset, null);
    }

    /**
     * Converts a Map of form data into a URL query string format. If a key in {@code paramMap} is empty ({@code null}
     * or ""), it will be ignored. If a value is {@code null}, it will be treated as an empty string (""). Keys and
     * values are automatically URL encoded.
     *
     * <pre>
     * key1=v1&amp;key2=&amp;key3=v3
     * </pre>
     *
     * @param paramMap   The form data map.
     * @param charset    The character set for encoding. If {@code null}, no encoding is performed for key-value pairs.
     * @param encodeMode The encoding mode.
     * @return The URL query string.
     */
    public static String toQuery(
            final Map<String, ?> paramMap,
            final java.nio.charset.Charset charset,
            final UrlQuery.EncodeMode encodeMode) {
        return UrlQuery.of(paramMap, encodeMode).build(charset);
    }

    /**
     * Encodes URL parameters, encoding only the keys and values.
     * <p>
     * Note: This method is suitable for standardizing an entire URL, not for individually encoding parameter values.
     *
     * @param query   The URL and parameters, can include the URL itself or just parameters.
     * @param charset The character set for encoding.
     * @return The encoded URL and parameters.
     */
    public static String encodeQuery(final String query, final java.nio.charset.Charset charset) {
        if (StringKit.isBlank(query)) {
            return Normal.EMPTY;
        }

        String urlPart = null; // URL part, excluding the question mark
        String paramPart; // Parameter part
        final int pathEndPos = query.indexOf(Symbol.C_QUESTION_MARK);
        if (pathEndPos > -1) {
            // URL + parameters
            urlPart = StringKit.subPre(query, pathEndPos);
            paramPart = StringKit.subSuf(query, pathEndPos + 1);
            if (StringKit.isBlank(paramPart)) {
                // No parameters, return URL
                return urlPart;
            }
        } else if (!StringKit.contains(query, Symbol.C_EQUAL)) {
            // URL without parameters
            return query;
        } else {
            // Parameters without URL
            paramPart = query;
        }

        paramPart = normalizeQuery(paramPart, charset);

        return StringKit.isBlank(urlPart) ? paramPart : urlPart + "?" + paramPart;
    }

    /**
     * Normalizes the parameter string, which is the part after the '?' in a URL.
     * <p>
     * Note: This method is suitable for standardizing an entire URL, not for individually encoding parameter values.
     *
     * @param query   The parameter string.
     * @param charset The character set for encoding.
     * @return The normalized parameter string.
     */
    public static String normalizeQuery(final String query, final java.nio.charset.Charset charset) {
        if (StringKit.isEmpty(query)) {
            return query;
        }
        final StringBuilder builder = new StringBuilder(query.length() + 16);
        final int len = query.length();
        String name = null;
        int pos = 0; // Start position of unprocessed characters
        char c; // Current character
        int i; // Current character position
        for (i = 0; i < len; i++) {
            c = query.charAt(i);
            if (c == Symbol.C_EQUAL) { // Key-value pair delimiter
                if (null == name) {
                    // Only when 'name' is undefined before '=' is it treated as a key-value delimiter, otherwise as a
                    // normal character
                    name = (pos == i) ? Normal.EMPTY : query.substring(pos, i);
                    pos = i + 1;
                }
            } else if (c == Symbol.C_AND) { // Parameter pair delimiter
                if (null == name) {
                    // For strings like &a& with no parameter value, we set the value of 'a' to ""
                    if (pos != i) {
                        name = query.substring(pos, i);
                        builder.append(RFC3986.QUERY_PARAM_NAME.encode(name, charset)).append(Symbol.C_EQUAL);
                    }
                } else {
                    builder.append(RFC3986.QUERY_PARAM_NAME.encode(name, charset)).append(Symbol.C_EQUAL)
                            .append(RFC3986.QUERY_PARAM_VALUE.encode(query.substring(pos, i), charset))
                            .append(Symbol.C_AND);
                }
                name = null;
                pos = i + 1;
            }
        }

        // End processing
        if (null != name) {
            builder.append(UrlEncoder.encodeQuery(name, charset)).append(Symbol.C_EQUAL);
        }
        if (pos != i) {
            if (null == name && pos > 0) {
                builder.append(Symbol.C_EQUAL);
            }
            builder.append(UrlEncoder.encodeQuery(query.substring(pos, i), charset));
        }

        // Remove trailing '&'
        final int lastIndex = builder.length() - 1;
        if (lastIndex >= 0 && Symbol.C_AND == builder.charAt(lastIndex)) {
            builder.deleteCharAt(lastIndex);
        }
        return builder.toString();
    }

    /**
     * Parses URL parameters into a Map (also applicable for key-value pair parameters in POST requests).
     *
     * @param query   The parameter string (or path with parameters).
     * @param charset The character set.
     * @return The parameter Map.
     */
    public static Map<String, String> decodeQuery(final String query, final java.nio.charset.Charset charset) {
        final Map<CharSequence, CharSequence> queryMap = UrlQuery.of(query, charset).getQueryMap();
        if (MapKit.isEmpty(queryMap)) {
            return MapKit.empty();
        }
        return Convert.toMap(String.class, String.class, queryMap);
    }

    /**
     * Parses URL parameters into a Map where values are lists of strings (also applicable for key-value pair parameters
     * in POST requests).
     *
     * @param query   The parameter string (or path with parameters).
     * @param charset The character set.
     * @return The parameter Map with list values.
     */
    public static Map<String, List<String>> decodeQueryList(
            final String query,
            final java.nio.charset.Charset charset) {
        final Map<CharSequence, CharSequence> queryMap = UrlQuery.of(query, charset).getQueryMap();
        if (MapKit.isEmpty(queryMap)) {
            return MapKit.empty();
        }

        final Map<String, List<String>> map = new LinkedHashMap<>();
        queryMap.forEach((key, value) -> {
            if (null != key) {
                final List<String> values = map.computeIfAbsent(key.toString(), k -> new ArrayList<>(1));
                // Usually a single parameter
                values.add(StringKit.toStringOrNull(value));
            }
        });
        return map;
    }

}
