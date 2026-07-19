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
package org.miaixz.bus.fabric.protocol.http.codec;

import java.net.URI;
import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * HTTP/1 line formatting and validation helpers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpLine {

    /**
     * Keeps HTTP start-line parsing on the static API.
     */
    private HttpLine() {
        // No initialization required.
    }

    /**
     * Formats a request line without the trailing CRLF.
     *
     * @param request request
     * @return request line
     */
    public static String request(final HttpRequest request) {
        final HttpRequest current = Assert
                .notNull(request, () -> new ValidateException("HTTP request must not be null"));
        final String method = token(current.method().value(), "HTTP method").toUpperCase(Locale.ROOT);
        return method + Symbol.SPACE + target(current.url().toUri()) + Symbol.SPACE + Protocol.HTTP_1_1;
    }

    /**
     * Parses a status line into a status code.
     *
     * @param line status line
     * @return status code
     */
    public static int status(final String line) {
        final String value = singleLine(line, "HTTP status line");
        final int first = value.indexOf(Symbol.SPACE);
        if (first <= Normal._0 || !value.substring(Normal._0, first).startsWith("HTTP/")) {
            throw new ValidateException("Invalid HTTP status line");
        }
        final int second = value.indexOf(Symbol.SPACE, first + Normal._1);
        final String codeText = second < Normal._0 ? value.substring(first + Normal._1)
                : value.substring(first + Normal._1, second);
        if (codeText.length() != Normal._3) {
            throw new ProtocolException("Invalid HTTP status code");
        }
        try {
            final int code = Integer.parseInt(codeText);
            if (code < HTTP.HTTP_CONTINUE || code >= Normal.KILO) {
                throw new ProtocolException("HTTP status code out of range");
            }
            return code;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid HTTP status code", e);
        }
    }

    /**
     * Formats a header line without the trailing CRLF.
     *
     * @param name  header name
     * @param value header value
     * @return header line
     */
    public static String header(final String name, final String value) {
        return token(name, "HTTP header name") + Symbol.COLON + Symbol.SPACE + headerValue(value);
    }

    /**
     * Creates an origin-form request target.
     *
     * @param uri URI
     * @return target
     */
    private static String target(final URI uri) {
        final URI current = Assert.notNull(uri, () -> new ValidateException("HTTP request URI must not be null"));
        final String path = StringKit.isBlank(current.getRawPath()) ? Symbol.SLASH : current.getRawPath();
        final String query = current.getRawQuery();
        if (StringKit.containsAny(path, Symbol.C_CR, Symbol.C_LF)
                || StringKit.containsAny(query, Symbol.C_CR, Symbol.C_LF)) {
            throw new ProtocolException("Invalid HTTP request target");
        }
        return StringKit.isBlank(query) ? path : path + Symbol.QUESTION_MARK + query;
    }

    /**
     * Validates a token value.
     *
     * @param value value
     * @param name  name
     * @return token
     */
    private static String token(final String value, final String name) {
        final String token = singleLine(value, name);
        for (int i = Normal._0; i < token.length(); i++) {
            final char c = token.charAt(i);
            if (!tchar(c)) {
                throw new ProtocolException(name + " contains invalid token characters");
            }
        }
        return token;
    }

    /**
     * Validates a header value.
     *
     * @param value value
     * @return value
     */
    private static String headerValue(final String value) {
        Assert.isFalse(
                value == null || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("HTTP header value must be non-null and single-line"));
        return value;
    }

    /**
     * Validates a non-blank single-line value.
     *
     * @param value value
     * @param name  name
     * @return value
     */
    private static String singleLine(final String value, final String name) {
        Assert.isFalse(
                StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException(name + " must be non-blank and single-line"));
        return value;
    }

    /**
     * Returns whether a character is an RFC token character.
     *
     * @param c character
     * @return true when valid
     */
    private static boolean tchar(final char c) {
        return Character.isLetterOrDigit(c) || c == Symbol.C_NOT || c == Symbol.C_HASH || c == Symbol.C_DOLLAR
                || c == Symbol.C_PERCENT || c == Symbol.C_AND || c == Symbol.C_SINGLE_QUOTE || c == Symbol.C_STAR
                || c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_DOT || c == Symbol.C_CARET
                || c == Symbol.C_UNDERLINE || c == '`' || c == Symbol.C_OR || c == Symbol.C_TILDE;
    }

}
