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
package org.miaixz.bus.core.net.url;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CharKit;

/**
 * Provides a percent-encoding implementation for URLs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlEncoder {

    private static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;

    /**
     * Encodes a URL using the default UTF-8 charset. Non-ASCII characters are converted to their hexadecimal
     * representation, prefixed with '%'. The following characters are not encoded:
     * 
     * <pre>
     * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
     * </pre>
     *
     * @param url The URL to encode.
     * @return The encoded URL.
     * @throws InternalException if the encoding is not supported.
     */
    public static String encodeAll(final String url) {
        return encodeAll(url, DEFAULT_CHARSET);
    }

    /**
     * Encodes a URL using the specified charset. Non-ASCII characters are converted to their hexadecimal
     * representation, prefixed with '%'. The following characters are not encoded:
     * 
     * <pre>
     * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
     * </pre>
     *
     * @param url     The URL to encode.
     * @param charset The charset to use for encoding; if null, no encoding is performed.
     * @return The encoded URL.
     * @throws InternalException if the encoding is not supported.
     */
    public static String encodeAll(final String url, final java.nio.charset.Charset charset) throws InternalException {
        return RFC3986.UNRESERVED.encode(url, charset);
    }

    /**
     * Encodes a URL for a query string using the default UTF-8 charset. This method is intended for automatically
     * encoding the request body in POST requests, and it escapes most special characters.
     *
     * @param url The URL to encode.
     * @return The encoded URL.
     */
    public static String encodeQuery(final String url) {
        return encodeQuery(url, DEFAULT_CHARSET);
    }

    /**
     * Encodes a string for a URL query. Non-ASCII characters are converted to their hexadecimal representation,
     * prefixed with '%'. This method is intended for automatically encoding the request body in POST requests, and it
     * escapes most special characters.
     *
     * @param url     The string to encode.
     * @param charset The charset to use for encoding.
     * @return The encoded string.
     */
    public static String encodeQuery(final String url, final java.nio.charset.Charset charset) {
        return RFC3986.QUERY.encode(url, charset);
    }

    /**
     * Encodes only the whitespace characters in a URL string to "%20".
     *
     * @param urlStr The URL string.
     * @return The encoded string.
     */
    public static String encodeBlank(final CharSequence urlStr) {
        if (urlStr == null) {
            return null;
        }

        final int len = urlStr.length();
        final StringBuilder sb = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = urlStr.charAt(i);
            if (CharKit.isBlankChar(c)) {
                sb.append("%20");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
