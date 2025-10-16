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
