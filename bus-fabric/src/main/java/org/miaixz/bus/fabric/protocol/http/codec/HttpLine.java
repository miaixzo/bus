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
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
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
     * @param request request supplying the method and precomputed request target
     * @return HTTP/1.1 request line without its CRLF terminator
     * @throws ValidateException if the request or method is invalid
     * @throws ProtocolException if the method contains a non-token character
     */
    public static String request(final HttpRequest request) {
        final HttpRequest current = Assert
                .notNull(request, () -> new ValidateException("HTTP request must not be null"));
        final String method = token(current.methodText(), "HTTP method");
        return method + Symbol.SPACE + current.requestTarget() + Symbol.SPACE + Protocol.HTTP_1_1;
    }

    /**
     * Parses a status line into a status code.
     *
     * @param line complete HTTP status line without CRLF
     * @return three-digit status code in the inclusive range {@code 100..999}
     * @throws ValidateException if the line is blank, multiline, or lacks a valid HTTP version prefix
     * @throws ProtocolException if the status-code field is malformed or out of range
     */
    public static int status(final String line) {
        final String value = singleLine(line, "HTTP status line");
        final int first = value.indexOf(Symbol.SPACE);
        if (first <= Normal._0 || !value.substring(Normal._0, first).startsWith("HTTP/")) {
            throw new ValidateException("Invalid HTTP status line");
        }
        final int codeStart = first + Normal._1;
        if (value.length() < codeStart + Normal._3
                || value.length() > codeStart + Normal._3 && value.charAt(codeStart + Normal._3) != Symbol.C_SPACE) {
            throw new ProtocolException("Invalid HTTP status code");
        }
        final char hundreds = value.charAt(codeStart);
        final char tens = value.charAt(codeStart + Normal._1);
        final char units = value.charAt(codeStart + Normal._2);
        if (hundreds < '0' || hundreds > '9' || tens < '0' || tens > '9' || units < '0' || units > '9') {
            throw new ProtocolException("Invalid HTTP status code");
        }
        final int code = (hundreds - '0') * 100 + (tens - '0') * Normal._10 + units - '0';
        if (code < Http.Status.CONTINUE || code >= Normal.KILO) {
            throw new ProtocolException("HTTP status code out of range");
        }
        return code;
    }

    /**
     * Formats a header line without the trailing CRLF.
     *
     * @param name  header name
     * @param value header value
     * @return validated {@code name: value} line without its CRLF terminator
     * @throws ValidateException if the name or value is invalid
     * @throws ProtocolException if the name contains a non-token character
     */
    public static String header(final String name, final String value) {
        return token(name, "HTTP header name") + Symbol.COLON + Symbol.SPACE + headerValue(value);
    }

    /**
     * Writes a request line directly to a target buffer without an intermediate encoded byte array.
     *
     * @param request request supplying the method and precomputed request target
     * @param target  destination buffer positioned for writing
     * @return number of ASCII bytes appended, excluding CRLF
     * @throws BufferOverflowException if the complete line does not fit
     * @throws ProtocolException       if line content is not valid ASCII HTTP syntax
     * @throws ValidateException       if the request, method, or target buffer is invalid
     */
    public static int writeRequest(final HttpRequest request, final ByteBuffer target) {
        final HttpRequest current = Assert
                .notNull(request, () -> new ValidateException("HTTP request must not be null"));
        final String method = token(current.methodText(), "HTTP method");
        final String requestTarget = current.requestTarget();
        final String protocol = Protocol.HTTP_1_1.name;
        final int required = method.length() + requestTarget.length() + protocol.length() + Normal._2;
        requireRemaining(target, required);
        writeAscii(method, target);
        target.put((byte) Symbol.C_SPACE);
        writeAscii(requestTarget, target);
        target.put((byte) Symbol.C_SPACE);
        writeAscii(protocol, target);
        return required;
    }

    /**
     * Writes a header line directly to a target buffer without an intermediate encoded byte array.
     *
     * @param name   header name
     * @param value  header value
     * @param target destination buffer positioned for writing
     * @return number of ASCII bytes appended, excluding CRLF
     * @throws BufferOverflowException if the complete line does not fit
     * @throws ProtocolException       if the name contains a non-token or non-ASCII character
     * @throws ValidateException       if the name, value, or target buffer is invalid
     */
    public static int writeHeader(final String name, final String value, final ByteBuffer target) {
        final String checkedName = token(name, "HTTP header name");
        final String checkedValue = headerValue(value);
        final int required = checkedName.length() + checkedValue.length() + Normal._2;
        requireRemaining(target, required);
        writeAscii(checkedName, target);
        target.put((byte) Symbol.C_COLON);
        target.put((byte) Symbol.C_SPACE);
        writeAscii(checkedValue, target);
        return required;
    }

    /**
     * Creates an origin-form request target.
     *
     * @param uri request URI whose raw path and query are used
     * @return origin-form target containing a path and optional query
     * @throws ValidateException if {@code uri} is {@code null}
     * @throws ProtocolException if the raw path or query contains a line break
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
     * @param value candidate HTTP token
     * @param name  logical token name included in validation errors
     * @return validated non-blank, single-line RFC token
     * @throws ValidateException if the token is blank or contains a line break
     * @throws ProtocolException if any character is not permitted in an HTTP token
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
     * @param value header field content to validate
     * @return unchanged non-null, single-line header content
     * @throws ValidateException if the content is {@code null} or contains a line break
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
     * @param value text to validate
     * @param name  logical value name included in the validation error
     * @return unchanged non-blank, single-line text
     * @throws ValidateException if the text is blank or contains a line break
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
     * @return {@code true} when the character belongs to the RFC HTTP token alphabet
     */
    private static boolean tchar(final char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == Symbol.C_NOT
                || c == Symbol.C_HASH || c == Symbol.C_DOLLAR || c == Symbol.C_PERCENT || c == Symbol.C_AND
                || c == Symbol.C_SINGLE_QUOTE || c == Symbol.C_STAR || c == Symbol.C_PLUS || c == Symbol.C_MINUS
                || c == Symbol.C_DOT || c == Symbol.C_CARET || c == Symbol.C_UNDERLINE || c == '`' || c == Symbol.C_OR
                || c == Symbol.C_TILDE;
    }

    /**
     * Writes validated ASCII characters directly to the destination.
     *
     * @param value  validated line text to encode
     * @param target destination byte buffer
     */
    private static void writeAscii(final String value, final ByteBuffer target) {
        for (int i = Normal._0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c > 0x7f) {
                throw new ProtocolException("HTTP line contains non-ASCII characters");
            }
            target.put((byte) c);
        }
    }

    /**
     * Ensures the complete planned direct write fits before any bytes are appended.
     *
     * @param target   destination byte buffer
     * @param required bytes required by the atomic write
     * @throws ValidateException       if {@code target} is {@code null}
     * @throws BufferOverflowException if fewer than {@code required} bytes remain
     */
    private static void requireRemaining(final ByteBuffer target, final int required) {
        Assert.notNull(target, () -> new ValidateException("HTTP line target must not be null"));
        if (target.remaining() < required) {
            throw new BufferOverflowException();
        }
    }

    /**
     * Bounded incremental CRLF line parser. A parser instance is reusable and retains only the unfinished line.
     */
    public static final class Parser {

        /**
         * Fixed-capacity storage for the current line, excluding its CRLF terminator.
         */
        private final byte[] line;

        /**
         * Number of line bytes currently retained in {@link #line}.
         */
        private int length;

        /**
         * Whether the previous consumed byte was a carriage return awaiting line feed.
         */
        private boolean carriageReturn;

        /**
         * Creates a parser with a strict maximum line size.
         *
         * @param maxLineBytes maximum bytes excluding CRLF
         * @throws ValidateException if {@code maxLineBytes} is not positive
         */
        public Parser(final int maxLineBytes) {
            if (maxLineBytes <= Normal._0) {
                throw new ValidateException("HTTP maximum line bytes must be positive");
            }
            this.line = new byte[maxLineBytes];
        }

        /**
         * Consumes available bytes and returns one completed line, or {@code null} when more bytes are required.
         *
         * @param source source buffer consumed from its current position
         * @return completed ASCII line without CRLF, or {@code null} when the terminator is incomplete
         * @throws ValidateException if {@code source} is {@code null}
         * @throws ProtocolException if framing, ASCII content, or the configured line limit is invalid
         */
        public String accept(final ByteBuffer source) {
            Assert.notNull(source, () -> new ValidateException("HTTP line source must not be null"));
            while (source.hasRemaining()) {
                final int value = source.get() & 0xff;
                if (carriageReturn) {
                    if (value != Symbol.C_LF) {
                        reset();
                        throw new ProtocolException("HTTP line contains a bare carriage return");
                    }
                    final String completed = new String(line, Normal._0, length, StandardCharsets.US_ASCII);
                    reset();
                    return completed;
                }
                if (value == Symbol.C_CR) {
                    carriageReturn = true;
                } else {
                    if (value == Symbol.C_LF || value > 0x7f) {
                        reset();
                        throw new ProtocolException("HTTP line contains invalid ASCII framing");
                    }
                    if (length == line.length) {
                        reset();
                        throw new ProtocolException("HTTP line exceeds configured maximum");
                    }
                    line[length++] = (byte) value;
                }
            }
            return null;
        }

        /**
         * Resets unfinished parsing state.
         */
        public void reset() {
            length = Normal._0;
            carriageReturn = false;
        }
    }

}
