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

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Serializes and deserializes JSON values, which automatically identifies the underlying JSON provider via SPI. This
 * class acts as a facade, providing static methods for common JSON serialization and deserialization tasks.
 *
 * @author Kimi Liu
 */
public class JsonKit {

    /**
     * Constructs a new JsonKit instance.
     */
    public JsonKit() {
        // No initialization required.
    }

    /**
     * Retrieves the globally available singleton JSON provider instance.
     *
     * @return The singleton {@link JsonProvider} instance.
     */
    public static JsonProvider getProvider() {
        return JsonFactory.get();
    }

    /**
     * Serializes one implementation-neutral JSON value as a complete UTF-8 RFC 8259 document.
     * <p>
     * The selected provider must preserve member order, array order, JSON null, booleans, strings, and arbitrary
     * decimal precision without adding a provider-specific type envelope.
     * </p>
     *
     * @param value immutable JSON value
     * @return newly allocated UTF-8 JSON document
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    public static byte[] writeValue(final JsonValue value) {
        return getProvider().writeValue(value);
    }

    /**
     * Parses one complete UTF-8 RFC 8259 document into the implementation-neutral JSON value model.
     * <p>
     * The selected provider rejects malformed input and trailing non-whitespace content, preserves arbitrary decimal
     * precision, and represents JSON {@code null} with {@link JsonValue.NullValue}.
     * </p>
     *
     * @param json complete UTF-8 JSON document
     * @return immutable JSON value
     * @throws IllegalArgumentException if {@code json} is {@code null}
     */
    public static JsonValue readValue(final byte[] json) {
        return getProvider().readValue(json);
    }

    /**
     * Strictly parses one complete UTF-8 RFC 8259 document with explicit structural security limits.
     *
     * @param json                 complete UTF-8 JSON document
     * @param maximumDepth         positive maximum object or array nesting depth
     * @param rejectDuplicateNames whether duplicate object member names must be rejected
     * @return immutable JSON value
     * @throws IllegalArgumentException if {@code json} is {@code null} or {@code maximumDepth} is not positive
     */
    public static JsonValue readValue(final byte[] json, final int maximumDepth, final boolean rejectDuplicateNames) {
        return getProvider().readValue(json, maximumDepth, rejectDuplicateNames);
    }

    /**
     * Extracts the original UTF-8 bytes of one top-level object member without reserializing its value.
     * <p>
     * This operation preserves the exact signed representation required by security protocols while still validating
     * the complete document, root type, requested member, nesting depth, and duplicate-name policy.
     * </p>
     *
     * @param json                 complete UTF-8 JSON object
     * @param member               exact decoded top-level member name
     * @param maximumDepth         positive maximum object or array nesting depth
     * @param rejectDuplicateNames whether duplicate object member names must be rejected
     * @return newly allocated original member-value bytes
     * @throws IllegalArgumentException if an argument is invalid
     */
    public static byte[] extractValue(
            final byte[] json,
            final String member,
            final int maximumDepth,
            final boolean rejectDuplicateNames) {
        return getProvider().extractValue(json, member, maximumDepth, rejectDuplicateNames);
    }

    /**
     * Converts an implementation-neutral JSON object into a public record after exact member-vocabulary validation.
     *
     * @param value JSON object whose members must match the record components
     * @param type  public record class to instantiate
     * @param <T>   target record type
     * @return non-null decoded record
     * @throws IllegalArgumentException if an argument is invalid
     */
    public static <T extends Record> T toRecord(final JsonValue.ObjectValue value, final Class<T> type) {
        return getProvider().toRecord(value, type);
    }

    /**
     * Converts one public record into an implementation-neutral JSON object after exact member-vocabulary validation.
     *
     * @param record public record to encode
     * @param <T>    source record type
     * @return immutable JSON object
     * @throws IllegalArgumentException if {@code record} is invalid
     */
    public static <T extends Record> JsonValue.ObjectValue toObject(final T record) {
        return getProvider().toObject(record);
    }

    /**
     * Converts one provider-supported Java value into the implementation-neutral JSON value model.
     * <p>
     * Conversion deliberately crosses the existing provider serialization boundary so maps, records, collections,
     * arrays, enums, temporal values, numbers, booleans, strings, and JSON null retain the same semantics as every
     * other {@code JsonKit} operation. No provider-specific tree type escapes this method.
     * </p>
     *
     * @param value provider-supported Java value, including {@code null}
     * @return immutable implementation-neutral JSON value
     * @throws ValidateException if the selected provider returns no JSON document
     */
    public static JsonValue toValue(final Object value) {
        final String json = toJsonString(value);
        if (json == null) {
            throw new ValidateException("JSON provider returned no serialized value");
        }
        return readValue(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converts a string-keyed Java map into an implementation-neutral JSON object.
     *
     * @param values Java map whose keys become exact JSON member names
     * @return immutable implementation-neutral JSON object
     * @throws IllegalArgumentException if the map, a key, or a converted member is invalid
     * @throws ValidateException        if the provider does not produce a JSON object
     */
    public static JsonValue.ObjectValue toObject(final Map<String, ?> values) {
        Assert.notNull(values, "JSON object source map must not be null");
        for (String name : values.keySet()) {
            Assert.notNull(name, "JSON object member name must not be null");
        }
        final JsonValue converted = toValue(values);
        if (!(converted instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("JSON source map must produce an object value");
        }
        return object;
    }

    /**
     * Converts an object into its JSON string representation.
     *
     * @param object The object to be converted.
     * @return The JSON string representation of the object.
     */
    public static String toJsonString(Object object) {
        return getProvider().toJsonString(object);
    }

    /**
     * Converts an object into its JSON string representation, with a specified date format.
     *
     * @param object The object to be converted.
     * @param format The date format string, e.g., "yyyy-MM-dd HH:mm:ss".
     * @return The JSON string representation of the object.
     */
    public static String toJsonString(Object object, String format) {
        return getProvider().toJsonString(object, format);
    }

    /**
     * Parses a JSON string into an object of the specified class.
     *
     * @param <T>   The type of the target object.
     * @param json  The JSON string to be parsed.
     * @param clazz The class of the target object.
     * @return The parsed object.
     */
    public static <T> T toPojo(String json, Class<T> clazz) {
        return getProvider().toPojo(json, clazz);
    }

    /**
     * Converts a {@link Map} into an object of the specified class.
     *
     * @param <T>   The type of the target object.
     * @param map   The source map.
     * @param clazz The class of the target object.
     * @return The object converted from the map.
     */
    public static <T> T toPojo(Map map, Class<T> clazz) {
        return getProvider().toPojo(map, clazz);
    }

    /**
     * Parses a JSON string into a {@link List}.
     *
     * @param json The JSON string to be parsed.
     * @return The resulting {@link List}.
     */
    public static List toList(String json) {
        return getProvider().toList(json);
    }

    /**
     * Parses a JSON string into a {@link List} of a specific generic type.
     *
     * @param <T>  The generic type of the elements in the list.
     * @param json The JSON string to be parsed.
     * @param type The {@link Type} representing the list's generic type.
     * @return The resulting {@link List}.
     */
    public static <T> List<T> toList(String json, final Type type) {
        return getProvider().toList(json, type);
    }

    /**
     * Parses a JSON string into a {@link List} of objects of the specified class.
     *
     * @param <T>   The type of the elements in the list.
     * @param json  The JSON string to be parsed.
     * @param clazz The class of the elements in the list.
     * @return The resulting {@link List}.
     */
    public static <T> List<T> toList(String json, final Class<T> clazz) {
        return getProvider().toList(json, clazz);
    }

    /**
     * Parses a JSON string into a {@link Map}.
     *
     * @param json The JSON string to be parsed.
     * @return The resulting {@link Map}.
     */
    public static Map toMap(String json) {
        return getProvider().toMap(json);
    }

    /**
     * Converts an object into a {@link Map}.
     *
     * @param object The object to be converted.
     * @return The resulting {@link Map}.
     */
    public static Map toMap(Object object) {
        return getProvider().toMap(object);
    }

    /**
     * Extracts the value of a specific field from a JSON string.
     *
     * @param <T>   The type of the value to be returned.
     * @param json  The JSON string to be parsed.
     * @param field The name of the field whose value is to be extracted.
     * @return The value of the specified field.
     */
    public static <T> T getValue(String json, String field) {
        return getProvider().getValue(json, field);
    }

    /**
     * Checks if a given string is a valid JSON string.
     *
     * @param json The string to be checked.
     * @return {@code true} if the string is a valid JSON, {@code false} otherwise.
     */
    public static boolean isJson(String json) {
        return getProvider().isJson(json);
    }

}
