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
package org.miaixz.bus.auth.codec;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlQuery;

/**
 * Strictly encodes and decodes an RFC 3986 URI query parameter sequence.
 * <p>
 * Encoding reuses the Bus strict query percent codec. Decoding accepts only ASCII query text plus percent-encoded
 * UTF-8, rejects malformed escapes and UTF-8, preserves ordered duplicate names and empty values, and treats plus as
 * the literal plus character rather than the HTML form representation of space.
 * </p>
 *
 * @author Kimi Liu
 */
public final class QueryCodec implements DualCodec<List<NameValue>, String> {

    /**
     * Creates a stateless strict query codec.
     */
    public QueryCodec() {
        // No initialization required.
    }

    /**
     * Decodes one non-empty ampersand-delimited query sequence.
     *
     * @param encoded complete query component
     * @param start   inclusive sequence offset
     * @param end     exclusive sequence offset
     * @return decoded name/value parameter
     */
    private static NameValue decodeParameter(final String encoded, final int start, final int end) {
        int separator = end;
        for (int index = start; index < end; index++) {
            if (encoded.charAt(index) == Symbol.C_EQUAL) {
                separator = index;
                break;
            }
        }
        final String name = decodeComponent(encoded, start, separator);
        final String value = separator == end ? Normal.EMPTY : decodeComponent(encoded, separator + 1, end);
        return new NameValue(name, value);
    }

    /**
     * Decodes one strict percent-encoded query component while preserving literal plus.
     *
     * @param encoded complete query component
     * @param start   inclusive component offset
     * @param end     exclusive component offset
     * @return decoded Unicode component
     * @throws ValidateException if raw syntax, percent encoding, or UTF-8 is malformed
     */
    private static String decodeComponent(final String encoded, final int start, final int end) {
        final ByteArrayOutputStream decoded = new ByteArrayOutputStream(end - start);
        for (int index = start; index < end; index++) {
            final char value = encoded.charAt(index);
            if (value == Symbol.C_PERCENT) {
                if (index + 2 >= end) {
                    throw new ValidateException("Malformed query percent encoding");
                }
                final int high = hexadecimal(encoded.charAt(index + 1));
                final int low = hexadecimal(encoded.charAt(index + 2));
                if (high < 0 || low < 0) {
                    throw new ValidateException("Malformed query percent encoding");
                }
                decoded.write(high << 4 | low);
                index += 2;
            } else {
                if (value > 0x7f) {
                    throw new ValidateException("Query component must percent-encode non-ASCII text");
                }
                decoded.write(value);
            }
        }
        try {
            return Charset.newDecoder(Charset.UTF_8, CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(decoded.toByteArray())).toString();
        } catch (CharacterCodingException cause) {
            throw new ValidateException("Malformed UTF-8 query component", cause);
        }
    }

    /**
     * Converts one ASCII hexadecimal character to its numeric value.
     *
     * @param value candidate ASCII character
     * @return value from zero through fifteen, or {@code -1} when invalid
     */
    private static int hexadecimal(final char value) {
        if (value >= Symbol.C_ZERO && value <= Symbol.C_NINE) {
            return value - Symbol.C_ZERO;
        }
        if (value >= Symbol.C_UPPER_A && value <= Symbol.C_UPPER_F) {
            return value - Symbol.C_UPPER_A + 10;
        }
        if (value >= Symbol.C_LOWER_A && value <= Symbol.C_LOWER_F) {
            return value - Symbol.C_LOWER_A + 10;
        }
        return -1;
    }

    /**
     * Encodes ordered decoded parameters to an RFC 3986 query component without a leading question mark.
     *
     * @param data ordered parameters with duplicates retained
     * @return strict ASCII query component
     * @throws IllegalArgumentException if the list or an entry is {@code null}
     */
    @Override
    public String encode(final List<NameValue> data) {
        Assert.notNull(data, "Query parameters must not be null");
        final UrlQuery query = UrlQuery.of(UrlQuery.EncodeMode.STRICT);
        for (NameValue parameter : data) {
            final NameValue value = Assert.notNull(parameter, "Query parameter must not be null");
            query.add(value.name(), value.value());
        }
        return query.build(Charset.UTF_8);
    }

    /**
     * Decodes one RFC 3986 query component without interpreting protocol parameter names.
     *
     * @param encoded ASCII query component without a fragment
     * @return immutable ordered decoded parameters
     * @throws ValidateException        if raw non-ASCII text, a fragment delimiter, percent encoding, or UTF-8 is
     *                                  malformed
     * @throws IllegalArgumentException if the query component is {@code null}
     */
    @Override
    public List<NameValue> decode(final String encoded) {
        Assert.notNull(encoded, "Encoded query component must not be null");
        Assert.isFalse(
                encoded.indexOf(Symbol.C_HASH) >= 0,
                "Encoded query component must not contain a fragment delimiter");
        final List<NameValue> parameters = new ArrayList<>();
        int start = 0;
        for (int index = 0; index <= encoded.length(); index++) {
            if (index == encoded.length() || encoded.charAt(index) == Symbol.C_AND) {
                if (index > start) {
                    parameters.add(decodeParameter(encoded, start, index));
                }
                start = index + 1;
            }
        }
        return List.copyOf(parameters);
    }

}
