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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.fabric.Headers;

/**
 * Strict, allocation-conscious HTTP/1 metadata parser.
 * <p>
 * Network buffering remains owned by {@link Http1Codec}; this class owns the syntax and validation rules so parsing
 * does not depend on response, connection-pool, or body lifecycle code.
 *
 * @author Kimi Liu
 */
final class Http1Parser {

    /**
     * Prevents instantiation.
     */
    private Http1Parser() {
        // No initialization required.
    }

    /**
     * Parses and validates an HTTP status code.
     *
     * @param line complete HTTP status line
     * @return numeric status code
     */
    static int status(final String line) {
        return HttpLine.status(line);
    }

    /**
     * Extracts the optional reason phrase from a validated status line.
     *
     * @param line complete HTTP status line
     * @return reason phrase, or the empty string
     */
    static String reason(final String line) {
        final int first = line.indexOf(Symbol.SPACE);
        final int second = first < Normal._0 ? Normal.__1 : line.indexOf(Symbol.SPACE, first + Normal._1);
        return second < Normal._0 ? Normal.EMPTY : line.substring(second + Normal._1);
    }

    /**
     * Adds one materialized header line after validating the parsed spans.
     *
     * @param builder    destination header builder
     * @param line       complete header line
     * @param colon      colon offset
     * @param valueStart first value offset
     * @param valueEnd   exclusive value end
     */
    static void addHeader(
            final Headers.Builder builder,
            final String line,
            final int colon,
            final int valueStart,
            final int valueEnd) {
        if (colon <= Normal._0 || valueStart < colon + Normal._1 || valueEnd < valueStart || valueEnd > line.length()) {
            throw new ProtocolException("Invalid HTTP response header");
        }
        try {
            builder.add(line.substring(Normal._0, colon), line.substring(valueStart, valueEnd));
        } catch (final RuntimeException e) {
            throw new ProtocolException("Invalid HTTP response header", e);
        }
    }

    /**
     * Validates framing fields after all header lines have been parsed.
     *
     * @param headers parsed headers
     * @return canonical framing-safe headers
     */
    static Headers headers(final Headers headers) {
        return Http1Framing.normalize(headers);
    }

}
