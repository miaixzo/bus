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
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Defines the contract for a JSON service provider. This interface specifies a set of common methods for JSON
 * serialization and deserialization, allowing for different underlying JSON libraries (e.g., Jackson, Gson, Fastjson)
 * to be used interchangeably.
 *
 * @author Kimi Liu
 */
public interface JsonProvider extends Provider<String> {

    /**
     * Returns the stable, non-blank provider name used by configuration, discovery and diagnostics. Implementations
     * should override this default with a short canonical name when they participate in named selection.
     *
     * @return fully qualified implementation class name by default
     */
    @Override
    default String type() {
        return getClass().getName();
    }

    /**
     * Converts an object into its JSON string representation.
     *
     * @param object The object to be serialized.
     * @return The JSON string representation of the object.
     */
    String toJsonString(Object object);

    /**
     * Converts an object into its JSON string representation, with a specified date format.
     *
     * @param object The object to be serialized.
     * @param format The date format string to use for date/time objects, e.g., "yyyy-MM-dd HH:mm:ss".
     * @return The JSON string representation of the object.
     */
    String toJsonString(Object object, String format);

    /**
     * Parses a JSON string into an object of the specified class.
     *
     * @param <T>   The type of the target object.
     * @param json  The JSON string to be deserialized.
     * @param clazz The class of the target object.
     * @return The deserialized object.
     */
    <T> T toPojo(String json, Class<T> clazz);

    /**
     * Parses JSON into the specified Java type, preserving generic type information when supported.
     *
     * @param <T>  target value type
     * @param json JSON document
     * @param type target Java type
     * @return deserialized value
     */
    default <T> T toPojo(String json, Type type) {
        if (type instanceof Class<?> clazz) {
            return (T) toPojo(json, clazz);
        }
        throw new InternalException("JSON provider does not support generic type: " + type() + ", type=" + type);
    }

    /**
     * Serializes a value to UTF-8 JSON bytes.
     *
     * @param object value to serialize
     * @return UTF-8 JSON bytes
     */
    default byte[] write(Object object) {
        return toJsonString(object).getBytes(Charset.UTF_8);
    }

    /**
     * Serializes a provider-neutral JSON value as an RFC 8259 document encoded with UTF-8.
     * <p>
     * Implementations must preserve object member order, array order, JSON null, booleans, strings, and arbitrary
     * decimal precision. The returned bytes must not contain a provider-specific type envelope.
     * </p>
     *
     * @param value provider-neutral JSON value
     * @return newly allocated UTF-8 JSON document
     * @throws IllegalArgumentException if the value is {@code null}
     * @throws InternalException        if the provider cannot serialize the value
     */
    byte[] writeValue(JsonValue value);

    /**
     * Serializes a value to UTF-8 JSON bytes using framework-independent options. Third-party providers inherit a
     * compatibility implementation supporting date formatting, but must override this method before accepting null or
     * property-filtering behavior that differs from their native defaults.
     *
     * @param object  value to serialize
     * @param options serialization options
     * @return UTF-8 JSON bytes
     * @throws InternalException if the provider does not support requested filtering behavior
     */
    default byte[] write(Object object, JsonWriteOptions options) {
        JsonWriteOptions resolved = options == null ? JsonWriteOptions.defaults() : options;
        if (!resolved.writeNulls() || resolved.hasPropertyFilter()) {
            throw new InternalException("JSON provider does not support write options: " + type());
        }
        String json = resolved.dateFormat() == null ? toJsonString(object)
                : toJsonString(object, resolved.dateFormat());
        return json.getBytes(Charset.UTF_8);
    }

    /**
     * Deserializes UTF-8 JSON bytes into the specified Java type.
     *
     * @param <T>  target value type
     * @param json UTF-8 JSON bytes
     * @param type target Java type
     * @return deserialized value
     */
    default <T> T read(byte[] json, Type type) {
        return toPojo(new String(json, Charset.UTF_8), type);
    }

    /**
     * Parses one complete RFC 8259 document from UTF-8 bytes into the provider-neutral JSON value model.
     * <p>
     * Implementations must reject malformed input and trailing non-whitespace content. JSON {@code null} is returned as
     * {@link JsonValue.NullValue}, never as Java {@code null}; JSON numbers retain arbitrary decimal precision.
     * </p>
     *
     * @param json complete UTF-8 JSON document
     * @return immutable provider-neutral JSON value
     * @throws IllegalArgumentException if the input byte array is {@code null}
     * @throws InternalException        if the input is empty, malformed, not valid UTF-8, or cannot be mapped
     *                                  losslessly
     */
    JsonValue readValue(byte[] json);

    /**
     * Parses one complete RFC 8259 document with an explicit nesting limit and duplicate-member policy.
     * <p>
     * The default implementation validates tree depth after parsing. Implementations that can prove duplicate member
     * names must override this method; requesting duplicate rejection from an implementation that has not done so fails
     * closed.
     * </p>
     *
     * @param json                 complete UTF-8 JSON document
     * @param maximumDepth         positive maximum object/array nesting depth
     * @param rejectDuplicateNames whether duplicate object member names must be rejected
     * @return immutable provider-neutral JSON value
     * @throws IllegalArgumentException if the bytes are {@code null} or the depth is not positive
     * @throws InternalException        if duplicate rejection is requested but unsupported
     * @throws ValidateException        if the parsed value exceeds the maximum depth
     */
    default JsonValue readValue(final byte[] json, final int maximumDepth, final boolean rejectDuplicateNames) {
        if (maximumDepth <= 0) {
            throw new IllegalArgumentException("JSON maximum depth must be positive");
        }
        if (rejectDuplicateNames) {
            throw new InternalException("JSON provider does not support duplicate member rejection: " + type());
        }
        final JsonValue value = readValue(json);
        validateDepth(value, 0, maximumDepth);
        return value;
    }

    /**
     * Extracts the original UTF-8 bytes of one top-level object member value without reserialization.
     * <p>
     * Implementations must validate the complete RFC 8259 document, require an object root, compare the decoded member
     * name exactly, enforce the requested nesting and duplicate-name policy, and return a newly allocated byte array.
     * The returned bytes begin at the member value's first token and end at its last token, excluding surrounding
     * whitespace. This operation does not interpret JSON Pointer or JSONPath expressions.
     * </p>
     *
     * @param json                 complete UTF-8 JSON object
     * @param member               exact decoded top-level member name
     * @param maximumDepth         positive maximum object/array nesting depth
     * @param rejectDuplicateNames whether duplicate object member names must be rejected
     * @return newly allocated original member-value bytes
     * @throws IllegalArgumentException if an argument is invalid
     * @throws InternalException        if strict extraction is unsupported or the document/member is invalid
     */
    default byte[] extractValue(
            final byte[] json,
            final String member,
            final int maximumDepth,
            final boolean rejectDuplicateNames) {
        if (json == null) {
            throw new IllegalArgumentException("JSON document must not be null");
        }
        if (member == null || member.isBlank()) {
            throw new IllegalArgumentException("JSON member name must not be blank");
        }
        if (maximumDepth <= 0) {
            throw new IllegalArgumentException("JSON maximum depth must be positive");
        }
        throw new InternalException("JSON provider does not support raw member extraction: " + type());
    }

    /**
     * Recursively checks provider-neutral JSON container depth.
     *
     * @param value        current JSON value
     * @param depth        current container depth
     * @param maximumDepth maximum permitted container depth
     * @throws ValidateException if the maximum depth is exceeded
     */
    private static void validateDepth(final JsonValue value, final int depth, final int maximumDepth) {
        final int nested = value instanceof JsonValue.ObjectValue || value instanceof JsonValue.ArrayValue ? depth + 1
                : depth;
        if (nested > maximumDepth) {
            throw new ValidateException("JSON document exceeds the maximum nesting depth");
        }
        if (value instanceof JsonValue.ObjectValue object) {
            object.values().values().forEach(item -> validateDepth(item, nested, maximumDepth));
        } else if (value instanceof JsonValue.ArrayValue array) {
            array.values().forEach(item -> validateDepth(item, nested, maximumDepth));
        }
    }

    /**
     * Deserializes UTF-8 JSON bytes after validating the requested Java type against framework-independent options.
     *
     * @param <T>     target value type
     * @param json    UTF-8 JSON bytes
     * @param type    target Java type
     * @param options deserialization options
     * @return deserialized value
     * @throws IllegalArgumentException if the requested type is rejected
     */
    default <T> T read(byte[] json, Type type, JsonReadOptions options) {
        JsonReadOptions resolved = options == null ? JsonReadOptions.defaults() : options;
        resolved.validate(type);
        return read(json, type);
    }

    /**
     * Converts one provider-neutral JSON object into a public record after validating its exact component-derived
     * member vocabulary.
     * <p>
     * The record class must be publicly accessible to this provider module. Private Vendor wire records deliberately
     * fail this boundary and must decode inside their owning adapter without reflective access overrides.
     * </p>
     *
     * @param value JSON object whose members must match the target record components
     * @param type  public record class to instantiate
     * @param <T>   target record type
     * @return non-null record decoded by this provider
     * @throws IllegalArgumentException if either argument is null or the class is not a public accessible record
     * @throws ValidateException        if the object contains a member not declared by the record
     * @throws InternalException        if this provider cannot encode or decode the validated value
     */
    default <T extends Record> T toRecord(final JsonValue.ObjectValue value, final Class<T> type) {
        final JsonValue.ObjectValue object = Assert.notNull(value, "JSON record source must not be null");
        final Class<T> recordType = Assert.notNull(type, "JSON record type must not be null");
        Assert.isTrue(recordType.isRecord(), "JSON record type must declare a record: {}", recordType.getName());
        Assert.isTrue(Modifier.isPublic(recordType.getModifiers()), "JSON record type must be public: {}",
                recordType.getName());
        final Module owner = recordType.getModule();
        Assert.isTrue(!owner.isNamed() || owner.isExported(recordType.getPackageName(), JsonProvider.class.getModule()),
                "JSON record package must be exported to the provider module: {}", recordType.getName());
        JsonRecordVerifier.of(recordType).validate(object);
        return Assert.notNull(read(writeValue(object), recordType), "JSON provider returned a null record");
    }

    /**
     * Converts one public record into a provider-neutral JSON object and verifies that the resulting member vocabulary
     * exactly belongs to the record components.
     * <p>
     * The conversion rejects private records instead of opening modules or overriding Java access checks. This keeps
     * platform-private wire models inside their owning adapters.
     * </p>
     *
     * @param record public record to encode
     * @param <T>    source record type
     * @return immutable provider-neutral JSON object
     * @throws IllegalArgumentException if the value is null or its class is not a public accessible record
     * @throws ValidateException        if the encoded value is not an object or contains an undeclared member
     * @throws InternalException        if this provider cannot encode or parse the record
     */
    @SuppressWarnings("unchecked")
    default <T extends Record> JsonValue.ObjectValue toObject(final T record) {
        final T value = Assert.notNull(record, "JSON record value must not be null");
        final Class<T> recordType = (Class<T>) value.getClass();
        Assert.isTrue(Modifier.isPublic(recordType.getModifiers()), "JSON record type must be public: {}",
                recordType.getName());
        final Module owner = recordType.getModule();
        Assert.isTrue(!owner.isNamed() || owner.isExported(recordType.getPackageName(), JsonProvider.class.getModule()),
                "JSON record package must be exported to the provider module: {}", recordType.getName());
        final JsonValue encoded = readValue(write(value));
        if (!(encoded instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("JSON record must encode as an object: " + recordType.getName());
        }
        return JsonRecordVerifier.of(recordType).validate(object);
    }

    /**
     * Converts a {@link Map} into a plain old Java object (POJO) of the specified class.
     *
     * @param <T>   The type of the target POJO.
     * @param map   The source map.
     * @param clazz The class of the target POJO.
     * @return The POJO converted from the map.
     */
    <T> T toPojo(Map map, Class<T> clazz);

    /**
     * Parses a JSON string into a {@link List}.
     *
     * @param <T>  The generic type of the elements in the list.
     * @param json The JSON string to be deserialized.
     * @return The resulting {@link List}.
     */
    <T> List<T> toList(String json);

    /**
     * Parses a JSON string into a {@link List} of objects of the specified class.
     *
     * @param <T>   The type of the elements in the list.
     * @param json  The JSON string to be deserialized.
     * @param clazz The class of the elements in the list.
     * @return The resulting {@link List}.
     */
    <T> List<T> toList(String json, Class<T> clazz);

    /**
     * Parses a JSON string into a {@link List} of a specific generic type.
     *
     * @param <T>  The generic type of the elements in the list.
     * @param json The JSON string to be deserialized.
     * @param type The {@link Type} representing the list's generic type.
     * @return The resulting {@link List}.
     */
    <T> List<T> toList(String json, final Type type);

    /**
     * Parses a JSON string into a {@link Map}.
     *
     * @param <K>  The type of the keys in the map.
     * @param <V>  The type of the values in the map.
     * @param json The JSON string to be deserialized.
     * @return The resulting {@link Map}.
     */
    <K, V> Map<K, V> toMap(String json);

    /**
     * Converts an object into a {@link Map}.
     *
     * @param <K>    The type of the keys in the map.
     * @param <V>    The type of the values in the map.
     * @param object The object to be converted.
     * @return The resulting {@link Map}.
     */
    <K, V> Map<K, V> toMap(Object object);

    /**
     * Extracts the value of a specific field from a JSON string.
     *
     * @param <T>   The type of the value to be returned.
     * @param json  The JSON string to be parsed.
     * @param field The name of the field whose value is to be extracted.
     * @return The value of the specified field.
     */
    <T> T getValue(String json, String field);

    /**
     * Checks if a given string is a valid, well-formed JSON string.
     *
     * @param json The string to be checked.
     * @return {@code true} if the string is a valid JSON, {@code false} otherwise.
     */
    boolean isJson(String json);

}
