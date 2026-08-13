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
package org.miaixz.bus.auth.codec.json;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Performs a bounded strict scan of one raw UTF-8 JSON document before delegating object construction to exactly one
 * injected {@link JsonProvider} invocation. The scanner validates the complete JSON grammar, duplicate object names,
 * Unicode surrogate pairing, trailing content, finite number syntax, document size, and nesting depth without
 * constructing a JSON tree.
 * <p>
 * <strong>Bus dependencies:</strong> {@link JsonProvider} performs the sole object mapping, while {@link Symbol} and
 * {@link Normal} supply framework punctuation and numeric constants, and {@link ErrorCode} supplies the framework JSON
 * and request-size errors.
 *
 * @author Kimi Liu
 */
public final class StrictJsonReader {

    /**
     * Product-supplied JSON mapper that is never owned or closed by this reader.
     */
    private final JsonProvider provider;

    /**
     * Maximum accepted UTF-8 document length.
     */
    private final int maximumBytes;

    /**
     * Maximum accepted object or array nesting depth.
     */
    private final int maximumDepth;

    /**
     * Creates an immutable strict reader with explicit closed input bounds.
     *
     * @param provider     product-supplied JSON provider
     * @param maximumBytes maximum accepted UTF-8 document length in bytes
     * @param maximumDepth maximum accepted object or array nesting depth
     * @throws ValidateException if the provider is null or either bound is not positive
     */
    public StrictJsonReader(final JsonProvider provider, final int maximumBytes, final int maximumDepth) {
        this.provider = Assert.notNull(provider, () -> new ValidateException("JSON provider must not be null"));
        if (maximumBytes <= Normal._0) {
            throw new ValidateException("Maximum JSON bytes must be positive");
        }
        if (maximumDepth <= Normal._0) {
            throw new ValidateException("Maximum JSON depth must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Decodes one complete document with malformed and unmappable UTF-8 reporting enabled.
     *
     * @param document bounded document bytes
     * @return decoded JSON text
     */
    private static String decode(final byte[] document) {
        try {
            return Charset.newDecoder(Charset.UTF_8, CodingErrorAction.REPORT).decode(ByteBuffer.wrap(document))
                    .toString();
        } catch (final CharacterCodingException failure) {
            throw new ProtocolException(ErrorCode._100302.getKey(), ErrorCode._100302.getValue(), failure);
        }
    }

    /**
     * Strictly scans and maps one complete UTF-8 JSON document.
     *
     * @param json raw UTF-8 JSON bytes
     * @param type requested Java target type
     * @param <T>  target value type
     * @return mapped value returned by the injected provider
     */
    public <T> T read(final byte[] json, final Type type) {
        final byte[] document = Arrays
                .copyOf(Assert.notNull(json, () -> new ValidateException("JSON bytes must not be null")), json.length);
        final Type target = Assert.notNull(type, () -> new ValidateException("JSON target type must not be null"));
        if (document.length > maximumBytes) {
            throw new ProtocolException(ErrorCode._100530);
        }
        final String text = decode(document);
        new Scanner(text, maximumDepth).scan();
        try {
            return provider.read(document, target);
        } catch (final ProtocolException failure) {
            throw failure;
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ErrorCode._100302.getKey(), ErrorCode._100302.getValue(), failure);
        }
    }

    /**
     * Stateful grammar scanner scoped to one immutable decoded document.
     *
     * @author Kimi Liu
     */
    private static final class Scanner {

        /**
         * Immutable decoded JSON document.
         */
        private final String source;

        /**
         * Maximum object or array nesting depth.
         */
        private final int maximumDepth;

        /**
         * Current UTF-16 character offset.
         */
        private int index;

        /**
         * Creates a scanner for one bounded document.
         *
         * @param source       decoded JSON document
         * @param maximumDepth maximum nesting depth
         */
        private Scanner(final String source, final int maximumDepth) {
            this.source = source;
            this.maximumDepth = maximumDepth;
        }

        /**
         * Returns whether one character is a decimal digit.
         *
         * @param value candidate character
         * @return whether the value is between zero and nine
         */
        private static boolean digit(final char value) {
            return value >= Symbol.C_ZERO && value <= '9';
        }

        /**
         * Returns whether one character is a non-zero decimal digit.
         *
         * @param value candidate character
         * @return whether the value is between one and nine
         */
        private static boolean nonZeroDigit(final char value) {
            return value >= Symbol.C_ONE && value <= '9';
        }

        /**
         * Appends one character when decoded output is requested.
         *
         * @param target optional decoded output
         * @param value  decoded character
         */
        private static void append(final StringBuilder target, final char value) {
            if (target != null) {
                target.append(value);
            }
        }

        /**
         * Appends one surrogate pair when decoded output is requested.
         *
         * @param target optional decoded output
         * @param high   high surrogate
         * @param low    low surrogate
         */
        private static void append(final StringBuilder target, final char high, final char low) {
            if (target != null) {
                target.append(high).append(low);
            }
        }

        /**
         * Requires one scanner condition.
         *
         * @param condition required condition
         */
        private static void require(final boolean condition) {
            if (!condition) {
                reject();
            }
        }

        /**
         * Rejects the current document with the stable Bus invalid-JSON error.
         *
         */
        private static void reject() {
            throw new ProtocolException(ErrorCode._100302);
        }

        /**
         * Validates exactly one JSON value followed only by JSON whitespace.
         *
         */
        private void scan() {
            whitespace();
            value(Normal._0);
            whitespace();
            require(index == source.length());
        }

        /**
         * Scans one JSON value at the supplied container depth.
         *
         * @param depth current container depth
         */
        private void value(final int depth) {
            require(index < source.length());
            final char current = source.charAt(index);
            if (current == Symbol.C_BRACE_LEFT) {
                object(depth + Normal._1);
            } else if (current == Symbol.C_BRACKET_LEFT) {
                array(depth + Normal._1);
            } else if (current == Symbol.C_DOUBLE_QUOTES) {
                string(false);
            } else if (current == 't') {
                literal("true");
            } else if (current == 'f') {
                literal("false");
            } else if (current == 'n') {
                literal("null");
            } else if (current == Symbol.C_MINUS || digit(current)) {
                number();
            } else {
                reject();
            }
        }

        /**
         * Scans one object and rejects duplicate decoded member names.
         *
         * @param depth object nesting depth
         */
        private void object(final int depth) {
            require(depth <= maximumDepth);
            expect(Symbol.C_BRACE_LEFT);
            whitespace();
            if (consume(Symbol.C_BRACE_RIGHT)) {
                return;
            }
            final Set<String> names = new HashSet<>();
            while (true) {
                require(index < source.length() && source.charAt(index) == Symbol.C_DOUBLE_QUOTES);
                final String name = string(true);
                require(names.add(name));
                whitespace();
                expect(Symbol.C_COLON);
                whitespace();
                value(depth);
                whitespace();
                if (consume(Symbol.C_BRACE_RIGHT)) {
                    return;
                }
                expect(Symbol.C_COMMA);
                whitespace();
            }
        }

        /**
         * Scans one array.
         *
         * @param depth array nesting depth
         */
        private void array(final int depth) {
            require(depth <= maximumDepth);
            expect(Symbol.C_BRACKET_LEFT);
            whitespace();
            if (consume(Symbol.C_BRACKET_RIGHT)) {
                return;
            }
            while (true) {
                value(depth);
                whitespace();
                if (consume(Symbol.C_BRACKET_RIGHT)) {
                    return;
                }
                expect(Symbol.C_COMMA);
                whitespace();
            }
        }

        /**
         * Scans one JSON string and optionally captures its decoded value for object-name comparison.
         *
         * @param capture whether to build the decoded string
         * @return decoded value when captured, otherwise the empty string
         */
        private String string(final boolean capture) {
            expect(Symbol.C_DOUBLE_QUOTES);
            final StringBuilder decoded = capture ? new StringBuilder() : null;
            while (index < source.length()) {
                final char current = source.charAt(index++);
                if (current == Symbol.C_DOUBLE_QUOTES) {
                    return decoded == null ? Normal.EMPTY : decoded.toString();
                }
                require(current >= Symbol.C_SPACE);
                if (current == Symbol.C_BACKSLASH) {
                    escape(decoded);
                } else if (Character.isHighSurrogate(current)) {
                    require(index < source.length());
                    final char low = source.charAt(index++);
                    require(Character.isLowSurrogate(low));
                    append(decoded, current, low);
                } else {
                    require(!Character.isLowSurrogate(current));
                    append(decoded, current);
                }
            }
            reject();
            return Normal.EMPTY;
        }

        /**
         * Scans and appends one JSON escape sequence.
         *
         * @param decoded optional decoded value target
         */
        private void escape(final StringBuilder decoded) {
            require(index < source.length());
            final char escaped = source.charAt(index++);
            switch (escaped) {
                case '"', '\\', '/' -> append(decoded, escaped);
                case 'b' -> append(decoded, '\b');
                case 'f' -> append(decoded, '\f');
                case 'n' -> append(decoded, Symbol.C_LF);
                case 'r' -> append(decoded, Symbol.C_CR);
                case 't' -> append(decoded, Symbol.C_TAB);
                case 'u' -> unicode(decoded);
                default -> reject();
            }
        }

        /**
         * Scans one escaped Unicode code unit and enforces surrogate pairing.
         *
         * @param decoded optional decoded value target
         */
        private void unicode(final StringBuilder decoded) {
            final char first = hexadecimal();
            if (Character.isHighSurrogate(first)) {
                require(
                        index + 1 < source.length() && source.charAt(index) == Symbol.C_BACKSLASH
                                && source.charAt(index + Normal._1) == 'u');
                index += 2;
                final char second = hexadecimal();
                require(Character.isLowSurrogate(second));
                append(decoded, first, second);
            } else {
                require(!Character.isLowSurrogate(first));
                append(decoded, first);
            }
        }

        /**
         * Decodes exactly four hexadecimal characters into one UTF-16 code unit.
         *
         * @return decoded code unit
         */
        private char hexadecimal() {
            require(index + 4 <= source.length());
            int value = Normal._0;
            for (int count = Normal._0; count < 4; count++) {
                final int digit = Character.digit(source.charAt(index++), 16);
                require(digit >= Normal._0);
                value = value * 16 + digit;
            }
            return (char) value;
        }

        /**
         * Scans one strict finite JSON number.
         *
         */
        private void number() {
            consume(Symbol.C_MINUS);
            require(index < source.length());
            if (consume(Symbol.C_ZERO)) {
                require(index >= source.length() || !digit(source.charAt(index)));
            } else {
                require(nonZeroDigit(source.charAt(index)));
                digits();
            }
            if (consume(Symbol.C_DOT)) {
                require(index < source.length() && digit(source.charAt(index)));
                digits();
            }
            if (consume('e') || consume('E')) {
                if (!consume(Symbol.C_PLUS)) {
                    consume(Symbol.C_MINUS);
                }
                require(index < source.length() && digit(source.charAt(index)));
                digits();
            }
        }

        /**
         * Consumes all consecutive decimal digits at the current offset.
         */
        private void digits() {
            while (index < source.length() && digit(source.charAt(index))) {
                index++;
            }
        }

        /**
         * Scans one exact JSON literal.
         *
         * @param expected literal text
         */
        private void literal(final String expected) {
            require(source.regionMatches(index, expected, Normal._0, expected.length()));
            index += expected.length();
        }

        /**
         * Consumes JSON whitespace at the current offset.
         */
        private void whitespace() {
            while (index < source.length()) {
                final char current = source.charAt(index);
                if (current != Symbol.C_SPACE && current != Symbol.C_TAB && current != Symbol.C_CR
                        && current != Symbol.C_LF) {
                    return;
                }
                index++;
            }
        }

        /**
         * Consumes one expected character when present.
         *
         * @param expected expected character
         * @return whether the character was consumed
         */
        private boolean consume(final char expected) {
            if (index < source.length() && source.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        /**
         * Requires and consumes one character.
         *
         * @param expected required character
         */
        private void expect(final char expected) {
            require(consume(expected));
        }
    }

}
