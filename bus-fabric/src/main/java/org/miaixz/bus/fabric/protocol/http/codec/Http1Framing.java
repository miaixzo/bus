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

import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.protocol.http.HttpHeaders;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * Stateless HTTP/1 message-framing decisions and request-smuggling validation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class Http1Framing {

    /**
     * Body wire representation.
     */
    enum Kind {

        /**
         * Message semantics prohibit a body.
         */
        NONE,

        /**
         * Content-Length bounds the body.
         */
        FIXED,

        /**
         * Transfer-Encoding chunked bounds the body.
         */
        CHUNKED,

        /**
         * Connection closure terminates the body.
         */
        UNKNOWN

    }

    /**
     * Immutable body-framing decision.
     *
     * @param kind   body wire representation
     * @param length fixed length, zero for no body, or -1 otherwise
     */
    record Decision(Kind kind, long length) {
    }

    /**
     * Determines response framing from request method, status, and headers.
     *
     * @param request originating request
     * @param code    response status
     * @param headers response headers
     * @return validated framing decision
     */
    static Decision response(final HttpRequest request, final int code, final Headers headers) {
        require(request, "Request");
        validate(headers);
        if (request.method() == Http.Method.HEAD
                || request.method() == Http.Method.CONNECT && code >= Http.Status.OK
                        && code < Http.Status.MULTIPLE_CHOICES
                || code == Http.Status.NO_CONTENT || code == Http.Status.NOT_MODIFIED
                || code >= Http.Status.CONTINUE && code < Http.Status.OK) {
            return new Decision(Kind.NONE, Normal._0);
        }
        if (chunked(headers)) {
            return new Decision(Kind.CHUNKED, Normal.__1);
        }
        final long length = declaredLength(headers);
        return length >= Normal._0 ? new Decision(Kind.FIXED, length) : new Decision(Kind.UNKNOWN, Normal.__1);
    }

    /**
     * Validates that Content-Length and Transfer-Encoding cannot conflict.
     *
     * @param headers headers
     */
    static void validate(final Headers headers) {
        require(headers, "Headers");
        if (headers.contains(Http.Header.CONTENT_LENGTH) && headers.contains(Http.Header.TRANSFER_ENCODING)) {
            throw new ProtocolException("HTTP/1 cannot combine Content-Length and Transfer-Encoding");
        }
        declaredLength(headers);
    }

    /**
     * Returns a canonical header snapshot for equivalent repeated lengths.
     *
     * @param headers parsed headers
     * @return canonical headers
     */
    static Headers normalize(final Headers headers) {
        validate(headers);
        return headers.values(Http.Header.CONTENT_LENGTH).size() <= Normal._1 ? headers
                : headers.with(Http.Header.CONTENT_LENGTH, Long.toString(declaredLength(headers)));
    }

    /**
     * Returns a validated Content-Length.
     *
     * @param headers headers
     * @return length or -1
     */
    static long declaredLength(final Headers headers) {
        final List<String> values = headers.values(Http.Header.CONTENT_LENGTH);
        if (values.isEmpty()) {
            return Normal.__1;
        }
        long normalized = Normal.__1;
        for (final String value : values) {
            final long current = parseLength(value);
            if (normalized == Normal.__1) {
                normalized = current;
            } else if (normalized != current) {
                throw new ProtocolException("Conflicting Content-Length values");
            }
        }
        return normalized;
    }

    /**
     * Returns whether chunked transfer coding is present.
     *
     * @param headers headers
     * @return true when chunked
     */
    static boolean chunked(final Headers headers) {
        for (final String value : HttpHeaders.values(headers, Http.Header.TRANSFER_ENCODING)) {
            if (Http.Header.TRANSFER_CODING_CHUNKED.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether Connection includes close.
     *
     * @param headers headers
     * @return true when close is present
     */
    static boolean connectionClose(final Headers headers) {
        for (final String value : HttpHeaders.values(headers, Http.Header.CONNECTION)) {
            if (Http.Header.CONNECTION_CLOSE.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a non-negative decimal length without allocation.
     *
     * @param value decimal value
     * @return length
     */
    private static long parseLength(final String value) {
        if (value.isEmpty()) {
            throw new ProtocolException("Invalid Content-Length");
        }
        long length = Normal._0;
        for (int index = Normal._0; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (current < '0' || current > '9') {
                throw new ProtocolException("Invalid Content-Length");
            }
            final int digit = current - '0';
            if (length > (Long.MAX_VALUE - digit) / Normal._10) {
                throw new ProtocolException("Invalid Content-Length");
            }
            length = length * Normal._10 + digit;
        }
        return length;
    }

    /**
     * Validates a required reference.
     *
     * @param value value
     * @param name  diagnostic name
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Prevents instantiation.
     */
    private Http1Framing() {
        // No initialization required.
    }

}
