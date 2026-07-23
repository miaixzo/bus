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
package org.miaixz.bus.fabric.protocol.http.http2;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable HTTP/2 header field.
 *
 * @param name   non-empty lowercase ASCII field name, optionally beginning with a colon
 * @param value  non-null field value without NUL, carriage-return, or line-feed characters
 * @param pseudo whether {@code name} begins with a colon; derived by the canonical constructor
 * @author Kimi Liu
 * @since Java 21+
 */
public record Http2Header(String name, String value, boolean pseudo) {

    /**
     * Creates a validated header field.
     *
     * @param name   non-empty lowercase ASCII field name, optionally beginning with a colon
     * @param value  non-null field value without prohibited control characters
     * @param pseudo ignored input; replaced with whether {@code name} begins with a colon
     */
    public Http2Header {
        validateName(name);
        validateValue(value);
        pseudo = name.startsWith(Symbol.COLON);
    }

    /**
     * Creates a header.
     *
     * @param name  lowercase ASCII field name to validate
     * @param value field value to validate
     * @return validated immutable field with its pseudo-header flag derived from the name
     */
    public static Http2Header of(final String name, final String value) {
        return new Http2Header(name, value, false);
    }

    /**
     * Returns whether HPACK must avoid indexing this security-sensitive field.
     *
     * @return {@code true} for authorization and cookie fields
     */
    public boolean sensitive() {
        return switch (name) {
            case "authorization", "proxy-authorization", "cookie", "set-cookie" -> true;
            default -> false;
        };
    }

    /**
     * Returns the RFC 7541 entry size without allocating encoded byte arrays.
     *
     * @return 32-byte overhead plus name and UTF-8 value lengths, saturated at {@link Integer#MAX_VALUE}
     */
    public int hpackSize() {
        final long size = 32L + name.length() + utf8Length(value);
        return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
    }

    /**
     * Validates a lowercase ASCII HTTP/2 field name.
     *
     * @param value field name to validate
     */
    private static void validateName(final String value) {
        if (value == null || value.isEmpty()) {
            throw new ValidateException("HTTP/2 header name must be non-empty lowercase ASCII");
        }
        int index = value.charAt(0) == Symbol.C_COLON ? 1 : 0;
        if (index == value.length()) {
            throw new ValidateException("HTTP/2 pseudo-header name must not be empty");
        }
        for (; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (!token(current)) {
                throw new ValidateException("HTTP/2 header name must be lowercase ASCII token text");
            }
        }
    }

    /**
     * Validates an HTTP/2 field value; the empty value is legal.
     *
     * @param value field value to validate
     */
    private static void validateValue(final String value) {
        if (value == null) {
            throw new ValidateException("HTTP/2 header value must not be null");
        }
        for (int index = 0; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (current == 0 || current == Symbol.C_CR || current == Symbol.C_LF) {
                throw new ValidateException("HTTP/2 header value contains prohibited characters");
            }
        }
    }

    /**
     * Returns whether a character is an allowed lowercase RFC token.
     *
     * @param value character to classify
     * @return {@code true} when the character is valid in a lower-case field name
     */
    private static boolean token(final char value) {
        if ((value >= 'a' && value <= 'z') || (value >= '0' && value <= '9')) {
            return true;
        }
        return switch (value) {
            case '!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|', '~' -> true;
            default -> false;
        };
    }

    /**
     * Counts UTF-8 bytes without creating a temporary byte array.
     *
     * @param value text whose encoded length is required
     * @return UTF-8 byte length, saturated at {@link Integer#MAX_VALUE}
     */
    private static int utf8Length(final String value) {
        long length = 0;
        for (int index = 0; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (current < 0x80) {
                length++;
            } else if (current < 0x800) {
                length += 2;
            } else if (Character.isHighSurrogate(current) && index + 1 < value.length()
                    && Character.isLowSurrogate(value.charAt(index + 1))) {
                length += 4;
                index++;
            } else {
                length += 3;
            }
            if (length >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) length;
    }

}
