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
 * Strictly encodes and decodes UTF-8 {@code application/x-www-form-urlencoded} parameters.
 * <p>
 * Encoding reuses the Bus form-mode percent codec, including the required plus representation for space. Decoding
 * rejects malformed percent escapes and malformed UTF-8, preserves list order and duplicate names, preserves empty
 * values, and ignores empty ampersand-separated sequences as required by the URL-encoded parser.
 * </p>
 *
 * @author Kimi Liu
 */
public class FormCodec implements DualCodec<List<NameValue>, byte[]> {

    /**
     * Creates a stateless form codec.
     */
    public FormCodec() {
        // No initialization required.
    }

    /**
     * Decodes one non-empty ampersand-delimited form sequence.
     *
     * @param bytes complete form body bytes
     * @param start inclusive sequence offset
     * @param end   exclusive sequence offset
     * @return decoded name/value parameter
     */
    private static NameValue decodeParameter(final byte[] bytes, final int start, final int end) {
        int separator = end;
        for (int index = start; index < end; index++) {
            if (bytes[index] == Symbol.C_EQUAL) {
                separator = index;
                break;
            }
        }
        final String name = decodeComponent(bytes, start, separator);
        final String value = separator == end ? Normal.EMPTY : decodeComponent(bytes, separator + 1, end);
        return new NameValue(name, value);
    }

    /**
     * Decodes one form component with plus-to-space and strict percent/UTF-8 handling.
     *
     * @param bytes complete form body bytes
     * @param start inclusive component offset
     * @param end   exclusive component offset
     * @return decoded Unicode component
     * @throws ValidateException if percent encoding or UTF-8 is malformed
     */
    private static String decodeComponent(final byte[] bytes, final int start, final int end) {
        final ByteArrayOutputStream decoded = new ByteArrayOutputStream(end - start);
        for (int index = start; index < end; index++) {
            final int value = bytes[index] & 0xff;
            if (value == Symbol.C_PLUS) {
                decoded.write(Symbol.C_SPACE);
            } else if (value == Symbol.C_PERCENT) {
                if (index + 2 >= end) {
                    throw new ValidateException("Malformed form percent encoding");
                }
                final int high = hexadecimal(bytes[index + 1]);
                final int low = hexadecimal(bytes[index + 2]);
                if (high < 0 || low < 0) {
                    throw new ValidateException("Malformed form percent encoding");
                }
                decoded.write(high << 4 | low);
                index += 2;
            } else {
                decoded.write(value);
            }
        }
        try {
            return Charset.newDecoder(Charset.UTF_8, CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(decoded.toByteArray())).toString();
        } catch (CharacterCodingException cause) {
            throw new ValidateException("Malformed UTF-8 form component", cause);
        }
    }

    /**
     * Converts one ASCII hexadecimal byte to its numeric value.
     *
     * @param value candidate ASCII byte
     * @return value from zero through fifteen, or {@code -1} when invalid
     */
    private static int hexadecimal(final byte value) {
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
     * Encodes ordered decoded parameters to a UTF-8 form body.
     *
     * @param data ordered parameters with duplicates retained
     * @return newly allocated UTF-8 form bytes
     * @throws IllegalArgumentException if the list or an entry is {@code null}
     */
    @Override
    public byte[] encode(final List<NameValue> data) {
        Assert.notNull(data, "Form parameters must not be null");
        final UrlQuery query = UrlQuery.of(UrlQuery.EncodeMode.FORM_URL_ENCODED);
        for (NameValue parameter : data) {
            final NameValue value = Assert.notNull(parameter, "Form parameter must not be null");
            query.add(value.name(), value.value());
        }
        return query.build(Charset.UTF_8).getBytes(Charset.UTF_8);
    }

    /**
     * Decodes a UTF-8 form body to ordered decoded parameters.
     *
     * @param encoded raw form body bytes
     * @return immutable ordered parameters with duplicates retained
     * @throws ValidateException        if percent encoding or UTF-8 is malformed
     * @throws IllegalArgumentException if the byte array is {@code null}
     */
    @Override
    public List<NameValue> decode(final byte[] encoded) {
        Assert.notNull(encoded, "Encoded form body must not be null");
        final List<NameValue> parameters = new ArrayList<>();
        int start = 0;
        for (int index = 0; index <= encoded.length; index++) {
            if (index == encoded.length || encoded[index] == Symbol.C_AND) {
                if (index > start) {
                    parameters.add(decodeParameter(encoded, start, index));
                }
                start = index + 1;
            }
        }
        return List.copyOf(parameters);
    }

}
