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
package org.miaixz.bus.extra.json;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * Represents a provider-neutral JSON value as defined by RFC 8259.
 * <p>
 * Every permitted implementation is immutable and serializable. Object member order and array element order are
 * retained so that conversions between JSON providers do not introduce avoidable structural changes. This model does
 * not expose objects from Jackson, Gson, Fastjson2, or any other JSON implementation.
 * </p>
 *
 * @author Kimi Liu
 */
public interface JsonValue extends Serializable {

    /**
     * Represents a JSON object while preserving the insertion order of its members.
     *
     * @param values member names and provider-neutral values; neither keys nor values may be {@code null}
     * @author Kimi Liu
     */
    record ObjectValue(Map<String, JsonValue> values) implements JsonValue {

        /**
         * Creates an immutable JSON object after validating and copying every member reference.
         *
         * @param values member names and values in their source order
         * @throws IllegalArgumentException if the map, a member name, or a member value is {@code null}
         */
        public ObjectValue {
            Assert.notNull(values, "JSON object members must not be null");
            final Map<String, JsonValue> copy = new LinkedHashMap<>(values.size());
            values.forEach(
                    (name, value) -> copy.put(
                            Assert.notNull(name, "JSON object member name must not be null"),
                            Assert.notNull(value, "JSON object member value must not be null")));
            values = Collections.unmodifiableMap(copy);
        }
    }

    /**
     * Represents an ordered JSON array.
     *
     * @param values array elements in wire order; elements may use {@link NullValue} but may not be Java {@code null}
     * @author Kimi Liu
     */
    record ArrayValue(List<JsonValue> values) implements JsonValue {

        /**
         * Creates an immutable JSON array after validating and copying every element reference.
         *
         * @param values array elements in wire order
         * @throws IllegalArgumentException if the list or any element is {@code null}
         */
        public ArrayValue {
            Assert.notNull(values, "JSON array elements must not be null");
            final List<JsonValue> copy = new ArrayList<>(values.size());
            for (JsonValue value : values) {
                copy.add(Assert.notNull(value, "JSON array element must not be null"));
            }
            values = Collections.unmodifiableList(copy);
        }
    }

    /**
     * Represents a JSON string without applying provider-specific escaping outside serialization.
     *
     * @param value decoded Unicode string value
     * @author Kimi Liu
     */
    record StringValue(String value) implements JsonValue {

        /**
         * Creates a JSON string value.
         *
         * @param value decoded Unicode string value
         * @throws IllegalArgumentException if the value is {@code null}
         */
        public StringValue {
            Assert.notNull(value, "JSON string value must not be null");
        }
    }

    /**
     * Represents an RFC 8259 JSON number with arbitrary decimal precision.
     *
     * @param value exact decimal value supplied by the JSON provider
     * @author Kimi Liu
     */
    record NumberValue(BigDecimal value) implements JsonValue {

        /**
         * Creates an exact JSON number value without narrowing it to a binary floating-point representation.
         *
         * @param value exact decimal value
         * @throws IllegalArgumentException if the value is {@code null}
         */
        public NumberValue {
            Assert.notNull(value, "JSON number value must not be null");
        }
    }

    /**
     * Represents an RFC 8259 JSON boolean.
     *
     * @param value boolean value
     * @author Kimi Liu
     */
    record BooleanValue(boolean value) implements JsonValue {

    }

    /**
     * Represents the single RFC 8259 JSON {@code null} value.
     * <p>
     * {@link #instance()} provides the shared value, and serialization replacement canonicalizes deserialized values.
     * </p>
     *
     * @author Kimi Liu
     */
    class NullValue implements JsonValue {

        /**
         * Serialization identifier for the singleton value.
         */
        @Serial
        private static final long serialVersionUID = 2852291720260L;

        /**
         * Shared immutable JSON null instance.
         */
        private static final NullValue INSTANCE = new NullValue();

        /**
         * Prevents creation of additional JSON null values.
         */
        public NullValue() {
            // No initialization required.
        }

        /**
         * Returns the shared JSON null value.
         *
         * @return singleton JSON null instance
         */
        public static NullValue instance() {
            return INSTANCE;
        }

        /**
         * Restores singleton identity after Java deserialization.
         *
         * @return shared JSON null instance
         */
        @Serial
        private Object readResolve() {
            return INSTANCE;
        }

        /**
         * Returns the RFC 8259 literal represented by this value without exposing provider state.
         *
         * @return the literal {@code null}
         */
        @Override
        public String toString() {
            return Normal.NULL;
        }
    }

}
