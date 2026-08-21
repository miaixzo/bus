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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Strictly encodes and decodes one RFC 9110 authentication Header field value.
 * <p>
 * The codec handles one scheme followed by token68 or auth-param. It does not accept a Header field name, split a list
 * of challenges, infer an OAuth method, or log credential content. Encoding always quotes auth-param values and escapes
 * quote and backslash, yielding an unambiguous field value suitable for a Fabric Headers entry selected by the caller.
 * </p>
 *
 * @author Kimi Liu
 */
public final class HeaderCodec implements DualCodec<HeaderValue, String> {

    /**
     * Creates a stateless authentication Header value codec.
     */
    public HeaderCodec() {
        // No initialization required.
    }

    /**
     * Escapes one decoded auth-param value as an RFC quoted-string payload.
     *
     * @param value decoded auth-param value
     * @return escaped quoted-string payload without surrounding quotes
     */
    private static String quote(final String value) {
        final StringBuilder encoded = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            validateFieldCharacter(character);
            if (character == Symbol.C_DOUBLE_QUOTES || character == Symbol.C_BACKSLASH) {
                encoded.append(Symbol.C_BACKSLASH);
            }
            encoded.append(character);
        }
        return encoded.toString();
    }

    /**
     * Returns the first non-OWS offset at or after a supplied position.
     *
     * @param value  complete field value
     * @param offset starting offset
     * @param end    exclusive field-value end
     * @return first non-OWS offset or {@code end}
     */
    private static int leadingOws(final String value, final int offset, final int end) {
        int index = offset;
        while (index < end && ows(value.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * Returns the exclusive field-value end after removing trailing OWS.
     *
     * @param value complete field value
     * @return exclusive offset of the last non-OWS character
     */
    private static int trailingOws(final String value) {
        int end = value.length();
        while (end > 0 && ows(value.charAt(end - 1))) {
            end--;
        }
        return end;
    }

    /**
     * Tests HTTP optional whitespace.
     *
     * @param value candidate character
     * @return {@code true} for space or horizontal tab
     */
    private static boolean ows(final char value) {
        return value == Symbol.C_SPACE || value == Symbol.C_TAB;
    }

    /**
     * Tests one RFC 9110 tchar character.
     *
     * @param value candidate character
     * @return {@code true} when the character is allowed in a token
     */
    private static boolean tokenCharacter(final char value) {
        return alphaNumeric(value) || "!#$%&'*+-.^_`|~".indexOf(value) >= 0;
    }

    /**
     * Tests one RFC 9110 token68 value.
     *
     * @param value candidate credentials text
     * @return {@code true} when the entire value satisfies token68 grammar
     */
    private static boolean token68(final String value) {
        if (value.isEmpty()) {
            return false;
        }
        boolean padding = false;
        boolean content = false;
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character == Symbol.C_EQUAL) {
                padding = true;
            } else {
                if (padding || !alphaNumeric(character) && "-._~+/".indexOf(character) < 0) {
                    return false;
                }
                content = true;
            }
        }
        return content;
    }

    /**
     * Tests whether one character is an ASCII letter or digit.
     *
     * @param value candidate character
     * @return {@code true} for an ASCII letter or digit
     */
    private static boolean alphaNumeric(final char value) {
        return value >= Symbol.C_LOWER_A && value <= Symbol.C_LOWER_Z
                || value >= Symbol.C_UPPER_A && value <= Symbol.C_UPPER_Z
                || value >= Symbol.C_ZERO && value <= Symbol.C_NINE;
    }

    /**
     * Rejects characters that cannot occur in an HTTP field-value quoted pair.
     *
     * @param value candidate decoded field character
     * @throws ValidateException if the character is a prohibited control or non-Latin-1 value
     */
    private static void validateFieldCharacter(final char value) {
        if (value > 0xff || value < 0x20 && value != Symbol.C_TAB || value == 0x7f) {
            throw new ValidateException("Authentication Header contains a prohibited field character");
        }
    }

    /**
     * Rejects characters forbidden directly inside an RFC quoted-string.
     *
     * @param value candidate decoded quoted-string character
     * @throws ValidateException if the character must be escaped or is prohibited in a field value
     */
    private static void validateQuotedCharacter(final char value) {
        validateFieldCharacter(value);
        if (value == Symbol.C_BACKSLASH) {
            throw new ValidateException("Authentication Header backslash must form a quoted pair");
        }
    }

    /**
     * Encodes one validated authentication Header value.
     *
     * @param data decoded Header value
     * @return RFC 9110 field value without a Header name
     * @throws IllegalArgumentException if the value is {@code null} or contains a character forbidden in a field value
     */
    @Override
    public String encode(final HeaderValue data) {
        Assert.notNull(data, "Authentication Header value must not be null");
        final StringBuilder encoded = new StringBuilder(data.scheme());
        if (!data.token68().isEmpty()) {
            encoded.append(Symbol.C_SPACE).append(data.token68().getOrNull());
            return encoded.toString();
        }
        for (int index = 0; index < data.parameters().size(); index++) {
            encoded.append(index == 0 ? Symbol.SPACE : ", ");
            final NameValue parameter = data.parameters().get(index);
            encoded.append(parameter.name()).append("=\"").append(quote(parameter.value()))
                    .append(Symbol.C_DOUBLE_QUOTES);
        }
        return encoded.toString();
    }

    /**
     * Decodes one RFC 9110 authentication Header field value.
     *
     * @param encoded field value without the Header name
     * @return immutable decoded scheme and credentials
     * @throws ValidateException        if token, token68, quoted-string, delimiter, or control-character syntax is
     *                                  invalid
     * @throws IllegalArgumentException if the field value is {@code null}
     */
    @Override
    public HeaderValue decode(final String encoded) {
        Assert.notNull(encoded, "Encoded authentication Header value must not be null");
        final int end = trailingOws(encoded);
        int index = leadingOws(encoded, 0, end);
        final int schemeStart = index;
        while (index < end && tokenCharacter(encoded.charAt(index))) {
            index++;
        }
        if (index == schemeStart) {
            throw new ValidateException("Authentication Header scheme is missing or invalid");
        }
        final String scheme = encoded.substring(schemeStart, index);
        if (index == end) {
            return new HeaderValue(scheme, Optional.empty(), List.of());
        }
        if (!ows(encoded.charAt(index))) {
            throw new ValidateException("Authentication Header scheme must be followed by whitespace");
        }
        index = leadingOws(encoded, index, end);
        if (index == end) {
            return new HeaderValue(scheme, Optional.empty(), List.of());
        }
        final String credentials = encoded.substring(index, end);
        if (token68(credentials)) {
            return new HeaderValue(scheme, Optional.of(credentials), List.of());
        }
        final List<NameValue> parameters = new ArrayList<>();
        while (index < end) {
            index = leadingOws(encoded, index, end);
            final int nameStart = index;
            while (index < end && tokenCharacter(encoded.charAt(index))) {
                index++;
            }
            if (index == nameStart) {
                throw new ValidateException("Authentication Header parameter name is invalid");
            }
            final String name = encoded.substring(nameStart, index);
            index = leadingOws(encoded, index, end);
            if (index >= end || encoded.charAt(index) != Symbol.C_EQUAL) {
                throw new ValidateException("Authentication Header parameter requires an equals sign");
            }
            index = leadingOws(encoded, index + 1, end);
            final String value;
            if (index < end && encoded.charAt(index) == Symbol.C_DOUBLE_QUOTES) {
                final StringBuilder decoded = new StringBuilder();
                index++;
                boolean closed = false;
                while (index < end) {
                    final char character = encoded.charAt(index++);
                    if (character == Symbol.C_DOUBLE_QUOTES) {
                        closed = true;
                        break;
                    }
                    if (character == Symbol.C_BACKSLASH) {
                        if (index >= end) {
                            throw new ValidateException("Authentication Header quoted pair is incomplete");
                        }
                        final char escaped = encoded.charAt(index++);
                        validateFieldCharacter(escaped);
                        decoded.append(escaped);
                    } else {
                        validateQuotedCharacter(character);
                        decoded.append(character);
                    }
                }
                if (!closed) {
                    throw new ValidateException("Authentication Header quoted string is not closed");
                }
                value = decoded.toString();
            } else {
                final int valueStart = index;
                while (index < end && tokenCharacter(encoded.charAt(index))) {
                    index++;
                }
                if (index == valueStart) {
                    throw new ValidateException("Authentication Header parameter value is invalid");
                }
                value = encoded.substring(valueStart, index);
            }
            parameters.add(new NameValue(name, value));
            index = leadingOws(encoded, index, end);
            if (index == end) {
                break;
            }
            if (encoded.charAt(index) != Symbol.C_COMMA) {
                throw new ValidateException("Authentication Header parameters require a comma delimiter");
            }
            index = leadingOws(encoded, index + 1, end);
            if (index == end) {
                throw new ValidateException("Authentication Header has a trailing comma");
            }
        }
        return new HeaderValue(scheme, Optional.empty(), parameters);
    }

}
