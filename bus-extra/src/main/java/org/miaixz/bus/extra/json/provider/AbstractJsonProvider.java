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
package org.miaixz.bus.extra.json.provider;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Enforces the provider-independent boundary for immutable RFC 8259 JSON values.
 * <p>
 * This base class validates caller input, performs strict UTF-8 decoding, rejects empty documents, and prevents a
 * concrete engine from returning Java {@code null}. It deliberately delegates JSON syntax handling and tree mapping to
 * each provider and therefore contains no provider selection or global factory state. Instances are safe to share when
 * the concrete provider's encoder and decoder are safe to share.
 * </p>
 *
 * @author Kimi Liu
 */
public abstract class AbstractJsonProvider implements JsonProvider {

    /**
     * Creates the provider-independent JSON boundary for a concrete engine implementation.
     */
    protected AbstractJsonProvider() {
        // No initialization required.
    }

    /**
     * Converts one provider-neutral JSON object through the single strict record-binding contract.
     * <p>
     * The final method prevents a concrete JSON engine from weakening component-derived schema validation or adding a
     * provider-specific property list.
     * </p>
     *
     * @param value JSON object whose members must match the target record
     * @param type  public record class to instantiate
     * @param <T>   target record type
     * @return non-null decoded record
     */
    @Override
    public final <T extends Record> T toRecord(final JsonValue.ObjectValue value, final Class<T> type) {
        return JsonProvider.super.toRecord(value, type);
    }

    /**
     * Converts one public record through the single strict provider-neutral object-binding contract.
     * <p>
     * The final method prevents a concrete JSON engine from bypassing the schema associated with the record's actual
     * runtime class.
     * </p>
     *
     * @param record public record to encode
     * @param <T>    source record type
     * @return immutable provider-neutral JSON object
     */
    @Override
    public final <T extends Record> JsonValue.ObjectValue toObject(final T record) {
        return JsonProvider.super.toObject(record);
    }

    /**
     * Serializes an immutable JSON value through the concrete engine and encodes the document as UTF-8.
     *
     * @param value provider-neutral JSON value
     * @return newly allocated UTF-8 JSON document
     * @throws IllegalArgumentException if the value is {@code null}
     * @throws InternalException        if the concrete engine returns no document
     */
    @Override
    public final byte[] writeValue(final JsonValue value) {
        Assert.notNull(value, "JSON value must not be null");
        final String document = encodeValue(value);
        if (document == null) {
            throw new InternalException("JSON provider returned no serialized document: " + type());
        }
        return document.getBytes(Charset.UTF_8);
    }

    /**
     * Strictly decodes one UTF-8 document and delegates syntax and tree conversion to the concrete engine.
     *
     * @param json complete UTF-8 JSON document
     * @return immutable provider-neutral JSON value, including {@link JsonValue.NullValue} for JSON {@code null}
     * @throws IllegalArgumentException if the input byte array is {@code null}
     * @throws InternalException        if the bytes are not valid UTF-8, the document is empty, or the engine returns
     *                                  no value
     */
    @Override
    public final JsonValue readValue(final byte[] json) {
        return readValue(json, Integer.MAX_VALUE, false);
    }

    /**
     * Strictly decodes one UTF-8 document after applying provider-independent depth and duplicate-name guards.
     *
     * @param json                 complete UTF-8 JSON document
     * @param maximumDepth         positive maximum object/array nesting depth
     * @param rejectDuplicateNames whether duplicate object member names are rejected
     * @return immutable provider-neutral JSON value
     * @throws IllegalArgumentException if the byte array is {@code null} or the depth is not positive
     * @throws InternalException        if UTF-8, structural guard, engine parsing, or result validation fails
     */
    @Override
    public final JsonValue readValue(final byte[] json, final int maximumDepth, final boolean rejectDuplicateNames) {
        if (maximumDepth <= 0) {
            throw new IllegalArgumentException("JSON maximum depth must be positive");
        }
        final String document = document(json);
        new DocumentGuard(document, maximumDepth, rejectDuplicateNames).validate();
        final JsonValue value = decodeValue(document);
        if (value == null) {
            throw new InternalException("JSON provider returned no provider-neutral value: " + type());
        }
        return value;
    }

    /**
     * Extracts one top-level member's original value bytes through the shared structural guard.
     * <p>
     * The complete document is strictly decoded, structurally guarded, and parsed by the concrete provider before the
     * exact original value slice is returned. No provider tree type or reserialized representation crosses this
     * boundary.
     * </p>
     *
     * @param json                 complete UTF-8 JSON object
     * @param member               exact decoded top-level member name
     * @param maximumDepth         positive maximum object/array nesting depth
     * @param rejectDuplicateNames whether duplicate object member names are rejected
     * @return newly allocated original member-value bytes
     * @throws IllegalArgumentException if an argument is invalid
     * @throws InternalException        if UTF-8, structure, syntax, root type, or member selection is invalid
     */
    @Override
    public final byte[] extractValue(
            final byte[] json,
            final String member,
            final int maximumDepth,
            final boolean rejectDuplicateNames) {
        Assert.notBlank(member, "JSON member name must not be blank");
        if (maximumDepth <= 0) {
            throw new IllegalArgumentException("JSON maximum depth must be positive");
        }
        final String document = document(json);
        final String value = new DocumentGuard(document, maximumDepth, rejectDuplicateNames).extract(member);
        if (!(decodeValue(document) instanceof JsonValue.ObjectValue)) {
            throw new InternalException("JSON raw member extraction requires an object root");
        }
        return value.getBytes(Charset.UTF_8);
    }

    /**
     * Strictly decodes one non-empty UTF-8 JSON document.
     *
     * @param json complete JSON bytes
     * @return decoded document retaining its original lexical representation
     * @throws IllegalArgumentException if {@code json} is {@code null}
     * @throws InternalException        if the bytes are invalid UTF-8 or contain only whitespace
     */
    private static String document(final byte[] json) {
        Assert.notNull(json, "JSON document must not be null");
        final String document;
        try {
            document = Charset.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(json)).toString();
        } catch (CharacterCodingException cause) {
            throw new InternalException("JSON document is not valid UTF-8", cause);
        }
        if (document.isBlank()) {
            throw new InternalException("JSON document must contain one RFC 8259 value");
        }
        return document;
    }

    /**
     * Performs provider-independent structural depth and duplicate-member validation before engine parsing.
     * <p>
     * This guard recognizes RFC 8259 strings, containers, and value boundaries only. The selected JSON engine remains
     * the authority for complete number, literal, whitespace, and trailing-content syntax.
     * </p>
     *
     * @author Kimi Liu
     */
    private static final class DocumentGuard {

        /**
         * Complete decoded JSON document.
         */
        private final String document;

        /**
         * Maximum permitted object/array nesting depth.
         */
        private final int maximumDepth;

        /**
         * Whether duplicate member names are rejected.
         */
        private final boolean rejectDuplicateNames;

        /**
         * Optional exact top-level member selected for raw-value extraction.
         */
        private String selectedMember;

        /**
         * Inclusive UTF-16 start of the selected member value.
         */
        private int selectedStart = -1;

        /**
         * Exclusive UTF-16 end of the selected member value.
         */
        private int selectedEnd = -1;

        /**
         * Current UTF-16 character index.
         */
        private int index;

        /**
         * Creates one single-use structural guard.
         *
         * @param document             complete decoded JSON document
         * @param maximumDepth         positive maximum nesting depth
         * @param rejectDuplicateNames duplicate-member policy
         */
        private DocumentGuard(final String document, final int maximumDepth, final boolean rejectDuplicateNames) {
            this.document = document;
            this.maximumDepth = maximumDepth;
            this.rejectDuplicateNames = rejectDuplicateNames;
        }

        /**
         * Validates one complete structural value and leaves full syntax validation to the JSON engine.
         *
         * @throws InternalException if nesting, duplicate names, or structural boundaries are invalid
         */
        private void validate() {
            whitespace();
            value(0);
            whitespace();
            if (index != document.length()) {
                throw invalid("JSON document contains trailing structural content");
            }
        }

        /**
         * Validates the complete object and returns one exact top-level member value slice.
         *
         * @param member exact decoded member name
         * @return original lexical member value without surrounding whitespace
         * @throws InternalException if the root is not an object or the member is absent or duplicated
         */
        private String extract(final String member) {
            selectedMember = member;
            whitespace();
            if (index >= document.length() || document.charAt(index) != '{') {
                throw invalid("JSON raw member extraction requires an object root");
            }
            value(0);
            whitespace();
            if (index != document.length()) {
                throw invalid("JSON document contains trailing structural content");
            }
            if (selectedStart < 0) {
                throw invalid("JSON top-level member is absent");
            }
            return document.substring(selectedStart, selectedEnd);
        }

        /**
         * Scans one JSON value at the current position.
         *
         * @param depth current containing-object/array depth
         */
        private void value(final int depth) {
            whitespace();
            if (index >= document.length()) {
                throw invalid("JSON document ends before a value");
            }
            final char token = document.charAt(index);
            if (token == '{') {
                object(depth + 1);
            } else if (token == '[') {
                array(depth + 1);
            } else if (token == '"') {
                string();
            } else {
                scalar();
            }
        }

        /**
         * Scans one object and detects duplicate decoded member names.
         *
         * @param depth object nesting depth
         */
        private void object(final int depth) {
            requireDepth(depth);
            index++;
            whitespace();
            final Set<String> names = rejectDuplicateNames ? new HashSet<>() : null;
            if (consume('}')) {
                return;
            }
            while (true) {
                whitespace();
                if (index >= document.length() || document.charAt(index) != '"') {
                    throw invalid("JSON object member name must be a string");
                }
                final String name = string();
                if (names != null && !names.add(name)) {
                    throw invalid("JSON object contains a duplicate member name");
                }
                whitespace();
                require(':');
                whitespace();
                final int valueStart = index;
                value(depth);
                final int valueEnd = index;
                if (depth == 1 && name.equals(selectedMember)) {
                    if (selectedStart >= 0) {
                        throw invalid("JSON selected top-level member is duplicated");
                    }
                    selectedStart = valueStart;
                    selectedEnd = valueEnd;
                }
                whitespace();
                if (consume('}')) {
                    return;
                }
                require(',');
            }
        }

        /**
         * Scans one array while enforcing container depth.
         *
         * @param depth array nesting depth
         */
        private void array(final int depth) {
            requireDepth(depth);
            index++;
            whitespace();
            if (consume(']')) {
                return;
            }
            while (true) {
                value(depth);
                whitespace();
                if (consume(']')) {
                    return;
                }
                require(',');
            }
        }

        /**
         * Decodes one JSON string sufficiently to compare member-name identity.
         *
         * @return decoded Java string
         */
        private String string() {
            require('"');
            final StringBuilder value = new StringBuilder();
            while (index < document.length()) {
                final char character = document.charAt(index++);
                if (character == '"') {
                    return value.toString();
                }
                if (character < 0x20) {
                    throw invalid("JSON string contains an unescaped control character");
                }
                if (character != '\\') {
                    value.append(character);
                    continue;
                }
                if (index >= document.length()) {
                    throw invalid("JSON string ends after an escape marker");
                }
                final char escaped = document.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> value.append(escaped);
                    case 'b' -> value.append('\b');
                    case 'f' -> value.append('\f');
                    case 'n' -> value.append('\n');
                    case 'r' -> value.append('\r');
                    case 't' -> value.append('\t');
                    case 'u' -> value.append(unicode());
                    default -> throw invalid("JSON string contains an invalid escape");
                }
            }
            throw invalid("JSON string is not terminated");
        }

        /**
         * Decodes exactly four hexadecimal digits after a Unicode escape marker.
         *
         * @return decoded UTF-16 code unit
         */
        private char unicode() {
            if (index + 4 > document.length()) {
                throw invalid("JSON Unicode escape is incomplete");
            }
            int value = 0;
            for (int offset = 0; offset < 4; offset++) {
                final int digit = Character.digit(document.charAt(index++), 16);
                if (digit < 0) {
                    throw invalid("JSON Unicode escape contains a non-hexadecimal digit");
                }
                value = value * 16 + digit;
            }
            return (char) value;
        }

        /**
         * Skips a scalar token through its structural delimiter.
         */
        private void scalar() {
            final int start = index;
            while (index < document.length()) {
                final char character = document.charAt(index);
                if (character == ',' || character == ']' || character == '}' || Character.isWhitespace(character)) {
                    break;
                }
                index++;
            }
            if (index == start) {
                throw invalid("JSON scalar value is empty");
            }
        }

        /**
         * Skips RFC 8259 whitespace characters.
         */
        private void whitespace() {
            while (index < document.length()) {
                final char character = document.charAt(index);
                if (character != ' ' && character != '\t' && character != '\r' && character != '\n') {
                    return;
                }
                index++;
            }
        }

        /**
         * Consumes one expected delimiter when present.
         *
         * @param expected expected delimiter
         * @return whether the delimiter was present
         */
        private boolean consume(final char expected) {
            if (index < document.length() && document.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        /**
         * Requires and consumes one structural delimiter.
         *
         * @param expected required delimiter
         */
        private void require(final char expected) {
            if (!consume(expected)) {
                throw invalid("JSON structural delimiter is missing");
            }
        }

        /**
         * Requires a container depth within the configured limit.
         *
         * @param depth current container depth
         */
        private void requireDepth(final int depth) {
            if (depth > maximumDepth) {
                throw invalid("JSON document exceeds the maximum nesting depth");
            }
        }

        /**
         * Creates a provider-neutral structural parsing failure at the current position.
         *
         * @param message safe failure description
         * @return JSON structural failure
         */
        private InternalException invalid(final String message) {
            return new InternalException(message + " at character " + index);
        }
    }

    /**
     * Converts a complete JSON document into the provider-neutral immutable value model.
     *
     * @param document non-empty Java string decoded strictly from UTF-8
     * @return provider-neutral JSON value; never Java {@code null}
     * @throws InternalException if syntax is invalid, trailing content exists, or conversion would lose information
     */
    protected abstract JsonValue decodeValue(String document);

    /**
     * Converts a provider-neutral JSON value into one complete RFC 8259 document.
     *
     * @param value non-null immutable JSON value
     * @return serialized JSON document; never {@code null}
     * @throws InternalException if the concrete engine cannot represent or serialize the value without loss
     */
    protected abstract String encodeValue(JsonValue value);

}
