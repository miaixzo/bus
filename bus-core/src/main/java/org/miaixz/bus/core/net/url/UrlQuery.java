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

import java.util.Iterator;
import java.util.Map;

import org.miaixz.bus.core.center.map.TableMap;
import org.miaixz.bus.core.codec.PercentCodec;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;

/**
 * Represents the query part of a URL, which is a collection of key-value pairs. For example:
 * 
 * <pre>
 *   key1=v1&amp;key2=&amp;key3=v3
 * </pre>
 * 
 * This class provides methods for parsing and building query strings. When parsing, you can specify a charset to decode
 * the content. When building, you can specify a charset to encode the key-value pairs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlQuery {

    /**
     * A {@link PercentCodec} that encodes all characters except for unreserved characters ("-", "_", ".", "*"). This is
     * similar to {@link java.net.URLEncoder}.
     */
    public static final PercentCodec ALL = PercentCodec.Builder.of(RFC3986.UNRESERVED).removeSafe(Symbol.C_TILDE)
            .addSafe(Symbol.C_STAR).setEncodeSpaceAsPlus(true).build();

    private final TableMap<CharSequence, CharSequence> query;
    /**
     * The encoding mode for the query parameters.
     */
    private EncodeMode encodeMode;

    /**
     * Constructs a new {@link UrlQuery} instance.
     *
     * @param queryMap   The initial map of query parameters.
     * @param encodeMode The encoding mode for the query parameters.
     */
    public UrlQuery(final Map<? extends CharSequence, ?> queryMap, final EncodeMode encodeMode) {
        if (MapKit.isNotEmpty(queryMap)) {
            query = new TableMap<>(queryMap.size());
            addAll(queryMap);
        } else {
            query = new TableMap<>(Normal._16);
        }
        this.encodeMode = ObjectKit.defaultIfNull(encodeMode, EncodeMode.NORMAL);
    }

    /**
     * Creates a new {@link UrlQuery} instance from a query string.
     *
     * @param query   The query string.
     * @param charset The charset for decoding the query string; if {@code null}, no decoding is performed.
     * @return A new {@link UrlQuery} instance.
     */
    public static UrlQuery of(final String query, final java.nio.charset.Charset charset) {
        return of(query, charset, true);
    }

    /**
     * Creates a new {@link UrlQuery} instance from a query string.
     *
     * @param query          The query string.
     * @param charset        The charset for decoding the query string; if {@code null}, no decoding is performed.
     * @param autoRemovePath If {@code true}, automatically removes the path part before the first '?'.
     * @return A new {@link UrlQuery} instance.
     */
    public static UrlQuery of(
            final String query,
            final java.nio.charset.Charset charset,
            final boolean autoRemovePath) {
        return of(query, charset, autoRemovePath, null);
    }

    /**
     * Creates a new {@link UrlQuery} instance from a query string.
     *
     * @param query          The query string.
     * @param charset        The charset for decoding the query string; if {@code null}, no decoding is performed.
     * @param autoRemovePath If {@code true}, automatically removes the path part before the first '?'.
     * @param encodeMode     The encoding mode for the query parameters.
     * @return A new {@link UrlQuery} instance.
     */
    public static UrlQuery of(
            final String query,
            final java.nio.charset.Charset charset,
            final boolean autoRemovePath,
            final EncodeMode encodeMode) {
        return of(encodeMode).parse(query, charset, autoRemovePath);
    }

    /**
     * Creates a new, empty {@link UrlQuery} instance.
     *
     * @return A new {@link UrlQuery} instance.
     */
    public static UrlQuery of() {
        return of(EncodeMode.NORMAL);
    }

    /**
     * Creates a new, empty {@link UrlQuery} instance with a specified encoding mode.
     *
     * @param encodeMode The encoding mode for the query parameters.
     * @return A new {@link UrlQuery} instance.
     */
    public static UrlQuery of(final EncodeMode encodeMode) {
        return new UrlQuery(null, encodeMode);
    }

    /**
     * Creates a new {@link UrlQuery} instance from a map of query parameters.
     *
     * @param queryMap The initial map of query parameters.
     * @return A new {@link UrlQuery} instance.
     */
    public static UrlQuery of(final Map<? extends CharSequence, ?> queryMap) {
        return of(queryMap, null);
    }

    /**
     * Creates a new {@link UrlQuery} instance from a map of query parameters.
     *
     * @param queryMap   The initial map of query parameters.
     * @param encodeMode The encoding mode for the query parameters.
     * @return A new {@link UrlQuery} instance.
     */
    public static UrlQuery of(final Map<? extends CharSequence, ?> queryMap, final EncodeMode encodeMode) {
        return new UrlQuery(queryMap, encodeMode);
    }

    /**
     * Converts an object to its string representation for use in a URL query.
     *
     * @param value The object to convert.
     * @return The string representation of the object.
     */
    private static String toString(final Object value) {
        final String result;
        if (value instanceof Iterable) {
            result = CollKit.join((Iterable<?>) value, Symbol.COMMA);
        } else if (value instanceof Iterator) {
            result = IteratorKit.join((Iterator<?>) value, Symbol.COMMA);
        } else {
            result = Convert.toString(value);
        }
        return result;
    }

    /**
     * Sets the encoding mode for the query parameters.
     *
     * @param encodeMode The encoding mode.
     * @return This {@link UrlQuery} instance for method chaining.
     */
    public UrlQuery setEncodeMode(final EncodeMode encodeMode) {
        this.encodeMode = encodeMode;
        return this;
    }

    /**
     * Adds a key-value pair to the query.
     *
     * @param key   The key.
     * @param value The value. Collections and arrays are converted to comma-separated strings.
     * @return This {@link UrlQuery} instance for method chaining.
     */
    public UrlQuery add(final CharSequence key, final Object value) {
        this.query.put(key, toString(value));
        return this;
    }

    /**
     * Adds all key-value pairs from a map to the query.
     *
     * @param queryMap The map of query parameters to add.
     * @return This {@link UrlQuery} instance for method chaining.
     */
    public UrlQuery addAll(final Map<? extends CharSequence, ?> queryMap) {
        if (MapKit.isNotEmpty(queryMap)) {
            queryMap.forEach(this::add);
        }
        return this;
    }

    /**
     * Removes a key and all its corresponding values from the query.
     *
     * @param key The key to remove.
     * @return This {@link UrlQuery} instance for method chaining.
     */
    public UrlQuery remove(final CharSequence key) {
        this.query.remove(key);
        return this;
    }

    /**
     * Parses a query string.
     *
     * @param query   The query string, e.g., {@code key1=v1&key2=&key3=v3}.
     * @param charset The charset for decoding; if {@code null}, no decoding is performed.
     * @return This {@link UrlQuery} instance for method chaining.
     */
    public UrlQuery parse(final String query, final java.nio.charset.Charset charset) {
        return parse(query, charset, true);
    }

    /**
     * Parses a query string.
     *
     * @param query          The query string, e.g., {@code key1=v1&key2=&key3=v3}.
     * @param charset        The charset for decoding; if {@code null}, no decoding is performed.
     * @param autoRemovePath If {@code true}, automatically removes the path part before the first '?'.
     * @return This {@link UrlQuery} instance for method chaining.
     */
    public UrlQuery parse(String query, final java.nio.charset.Charset charset, final boolean autoRemovePath) {
        if (StringKit.isBlank(query)) {
            return this;
        }

        if (autoRemovePath) {
            final int pathEndPos = query.indexOf(Symbol.C_QUESTION_MARK);
            if (pathEndPos > -1) {
                query = StringKit.subSuf(query, pathEndPos + 1);
                if (StringKit.isBlank(query)) {
                    return this;
                }
            } else if (StringKit.startWith(query, "http://") || StringKit.startWith(query, "https://")) {
                return this;
            }
        }

        return doParse(query, charset);
    }

    /**
     * Returns the map of query parameters.
     *
     * @return A read-only map of query parameters.
     */
    public Map<CharSequence, CharSequence> getQueryMap() {
        return MapKit.view(this.query);
    }

    /**
     * Retrieves the value for a given key.
     *
     * @param key The key.
     * @return The corresponding value, or {@code null} if the key is not found.
     */
    public CharSequence get(final CharSequence key) {
        if (MapKit.isEmpty(this.query)) {
            return null;
        }
        return this.query.get(key);
    }

    /**
     * Builds the URL query string, converting key-value pairs to the format {@code key1=v1&key2=v2&key3=v3}. Rules for
     * {@code null} values:
     * <ul>
     * <li>If a key is {@code null}, the key-value pair is ignored.</li>
     * <li>If a value is {@code null}, only the key is included (e.g., {@code key1&key2=v2}).</li>
     * </ul>
     *
     * @param charset The charset for encoding; if {@code null}, no encoding is performed.
     * @return The URL query string.
     */
    public String build(final java.nio.charset.Charset charset) {
        switch (this.encodeMode) {
            case FORM_URL_ENCODED:
                return build(ALL, ALL, charset);

            case STRICT:
                return build(RFC3986.QUERY_PARAM_NAME_STRICT, RFC3986.QUERY_PARAM_VALUE_STRICT, charset);

            default:
                return build(RFC3986.QUERY_PARAM_NAME, RFC3986.QUERY_PARAM_VALUE, charset);
        }
    }

    /**
     * Builds the URL query string, converting key-value pairs to the format {@code key1=v1&key2=v2&key3=v3}. Rules for
     * {@code null} values:
     * <ul>
     * <li>If a key is {@code null}, the key-value pair is ignored.</li>
     * <li>If a value is {@code null}, only the key is included (e.g., {@code key1&key2=v2}).</li>
     * </ul>
     *
     * @param keyCoder   The encoder for the keys.
     * @param valueCoder The encoder for the values.
     * @param charset    The charset for encoding; if {@code null}, no encoding is performed.
     * @return The URL query string.
     */
    public String build(
            final PercentCodec keyCoder,
            final PercentCodec valueCoder,
            final java.nio.charset.Charset charset) {
        if (MapKit.isEmpty(this.query)) {
            return Normal.EMPTY;
        }

        final StringBuilder sb = new StringBuilder();
        CharSequence name;
        CharSequence value;
        for (final Map.Entry<CharSequence, CharSequence> entry : this.query) {
            name = entry.getKey();
            if (null != name) {
                if (!sb.isEmpty()) {
                    sb.append(Symbol.AND);
                }
                sb.append(keyCoder.encode(name, charset));
                value = entry.getValue();
                if (null != value) {
                    sb.append("=").append(valueCoder.encode(value, charset));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Parses a URL query string according to the rules at https://url.spec.whatwg.org/#urlencoded-parsing.
     *
     * @param query   The query string, e.g., {@code key1=v1&key2=&key3=v3}.
     * @param charset The charset for decoding; if {@code null}, no decoding is performed.
     * @return This {@link UrlQuery} instance for method chaining.
     */
    private UrlQuery doParse(final String query, final java.nio.charset.Charset charset) {
        final int len = query.length();
        String name = null;
        int pos = 0; // Start position of the unprocessed string
        int i; // End position of the unprocessed string
        char c; // Current character
        for (i = 0; i < len; i++) {
            c = query.charAt(i);
            switch (c) {
                case Symbol.C_EQUAL: // Delimiter between key and value
                    if (null == name) {
                        name = query.substring(pos, i);
                        pos = i + 1;
                    }
                    break;

                case Symbol.C_AND: // Delimiter between key-value pairs
                    addParam(name, query.substring(pos, i), charset);
                    name = null;
                    if (i + 4 < len && "amp;".equals(query.substring(i + 1, i + 5))) {
                        // Unescape "&amp;" to "&"
                        i += 4;
                    }
                    pos = i + 1;
                    break;
            }
        }

        // Process the last part
        addParam(name, query.substring(pos, i), charset);

        return this;
    }

    /**
     * Adds a key-value pair to the map.
     * 
     * <pre>
     *     1. If key and value are not null (e.g., "a=1" or "=1"), put them directly.
     *     2. If key is not null and value is null (e.g., "a="), the value is treated as an empty string.
     *     3. If key is null and value is not null (e.g., "1"), the value is treated as the key.
     *     4. If both key and value are null (e.g., &&), they are ignored.
     * </pre>
     *
     * @param key     The key; if null, the value is used as the key.
     * @param value   The value; if null and the key is not null, it's treated as an empty string.
     * @param charset The charset for decoding.
     */
    private void addParam(final String key, final String value, final java.nio.charset.Charset charset) {
        final boolean isFormUrlEncoded = EncodeMode.FORM_URL_ENCODED == this.encodeMode;
        if (null != key) {
            final String actualKey = UrlDecoder.decode(key, charset, isFormUrlEncoded);
            this.query.put(actualKey, StringKit.toStringOrEmpty(UrlDecoder.decode(value, charset, isFormUrlEncoded)));
        } else if (null != value) {
            this.query.put(UrlDecoder.decode(value, charset, isFormUrlEncoded), null);
        }
    }

    /**
     * Generates a query string for display purposes (e.g., {@code aaa=111&bbb=222}). This method does not encode any
     * special characters.
     *
     * @return The query string.
     */
    @Override
    public String toString() {
        return build(null);
    }

    /**
     * Defines the encoding mode for query parameters, which determines how names and values are encoded.
     */
    public enum EncodeMode {
        /**
         * Normal (loose) mode, where some delimiters are not escaped.
         */
        NORMAL,
        /**
         * The x-www-form-urlencoded mode, where spaces are encoded as '+', and '~' and '*' are escaped.
         */
        FORM_URL_ENCODED,
        /**
         * Strict mode, where all characters except for unreserved characters are escaped.
         */
        STRICT
    }

}
