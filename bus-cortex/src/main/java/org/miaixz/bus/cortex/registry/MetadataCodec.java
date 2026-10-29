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
package org.miaixz.bus.cortex.registry;

import java.util.*;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.EnumKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.logger.Logger;

/**
 * Encodes and decodes metadata payloads as JSON.
 * <p>
 * This class deliberately knows only JSON shapes, not subtype-specific metadata fields.
 * </p>
 *
 * @author Kimi Liu
 */
public class MetadataCodec {

    /**
     * Root metadata object reserved for cortex-specific fields.
     */
    public static final String ROOT = "_cortex";

    /**
     * Metadata key storing the effective asset type.
     */
    public static final String TYPE = "type";

    /**
     * Maximum supported nesting depth when accepting raw metadata JSON.
     */
    private static final int MAXIMUM_DEPTH = 64;

    /**
     * Keeps registry metadata encoding and decoding on the static API.
     */
    public MetadataCodec() {
        // No initialization required.
    }

    /**
     * Parses one complete metadata JSON object into a detached mutable root map. Java {@code null} and blank text
     * initialize an empty object; JSON null, arrays, scalars, duplicate names and malformed documents are rejected.
     *
     * @param metadata raw metadata JSON
     * @return mutable metadata root
     * @throws IllegalArgumentException if the nonblank payload is not a valid bounded JSON object
     */
    public static Map<String, Object> root(String metadata) {
        if (StringKit.isBlank(metadata)) {
            return new LinkedHashMap<>();
        }
        JsonValue parsed = JsonKit.readValue(metadata.getBytes(Charset.UTF_8), MAXIMUM_DEPTH, true);
        if (!(parsed instanceof JsonValue.ObjectValue object)) {
            throw new IllegalArgumentException("Asset metadata must be a JSON object");
        }
        Map<String, Object> root = new LinkedHashMap<>();
        object.values().forEach((key, value) -> root.put(key, JsonKit.toJavaValue(value)));
        return root;
    }

    /**
     * Merges a subtype-owned field projection into the existing metadata object. Only the fixed field set supplied by
     * subtype code may change; null or absent owned values remove those fields. Unknown members, including nested
     * objects and arrays, retain their JSON values. The ownership set must never come from a request payload.
     *
     * @param metadata    existing raw metadata, or blank text to initialize an object
     * @param ownedFields immutable field vocabulary declared by the subtype
     * @param values      complete projection of the subtype-owned fields
     * @return merged metadata JSON
     * @throws IllegalArgumentException if the raw object or the ownership projection is invalid
     */
    public static String merge(String metadata, Set<String> ownedFields, Map<String, Object> values) {
        if (ownedFields == null || values == null || ownedFields.stream().anyMatch(Objects::isNull)
                || !ownedFields.containsAll(values.keySet())) {
            throw new IllegalArgumentException("Metadata projection must contain only subtype-owned fields");
        }
        Map<String, Object> root = root(metadata);
        for (String field : ownedFields) {
            put(root, field, values.get(field));
        }
        return json(root);
    }

    /**
     * Converts a metadata root map to JSON.
     *
     * @param root metadata root
     * @return JSON string
     */
    public static String json(Map<String, Object> root) {
        return JsonKit.toJsonString(root == null ? new LinkedHashMap<>() : root);
    }

    /**
     * Returns or creates one mutable nested object.
     *
     * @param root metadata object
     * @param key  nested key
     * @return mutable nested object
     */
    public static Map<String, Object> nested(Map<String, Object> root, String key) {
        Map<String, Object> nested = object(root == null ? null : root.get(key));
        return nested == null ? new LinkedHashMap<>() : nested;
    }

    /**
     * Converts one metadata object into a mutable string-keyed map.
     *
     * @param value metadata object
     * @return mutable map or {@code null}
     */
    public static Map<String, Object> object(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(entry.getKey().toString(), entry.getValue());
                }
            }
            return result;
        }
        try {
            Map<String, Object> result = JsonKit.toMap(JsonKit.toJsonString(value));
            return result == null ? null : new LinkedHashMap<>(result);
        } catch (Exception ignore) {
            Logger.debug(
                    false,
                    "Cortex",
                    "Cortex operation skipped: provider={}, recoverable={}, exception={}",
                    "MetadataCodec",
                    true,
                    ignore.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Writes or removes one metadata value.
     *
     * @param target metadata target map
     * @param key    metadata key
     * @param value  metadata value
     */
    public static void put(Map<String, Object> target, String key, Object value) {
        if (target == null || key == null) {
            return;
        }
        if (value == null) {
            target.remove(key);
            return;
        }
        target.put(key, value);
    }

    /**
     * Converts one metadata value to a string.
     *
     * @param value metadata value
     * @return string value or {@code null}
     */
    public static String string(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * Converts one metadata value to a {@link Long}.
     *
     * @param value metadata value
     * @return long value or {@code null}
     */
    public static Long longObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignore) {
            Logger.debug(
                    false,
                    "Cortex",
                    "Cortex operation skipped: provider={}, recoverable={}, exception={}",
                    "MetadataCodec",
                    true,
                    ignore.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Converts one metadata value to an {@link Integer}.
     *
     * @param value metadata value
     * @return integer value or {@code null}
     */
    public static Integer intObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ignore) {
            Logger.debug(
                    false,
                    "Cortex",
                    "Cortex operation skipped: provider={}, recoverable={}, exception={}",
                    "MetadataCodec",
                    true,
                    ignore.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Converts one metadata value into the requested enum constant.
     *
     * @param value metadata value
     * @param <E>   enum type
     * @param type  target enum type
     * @return enum constant or {@code null}
     */
    public static <E extends Enum<E>> E enumValue(Object value, Class<E> type) {
        String text = string(value);
        if (StringKit.isBlank(text)) {
            return null;
        }
        return EnumKit.fromStringQuietly(Objects.requireNonNull(type), StringKit.toUpperCase(text));
    }

    /**
     * Converts one metadata list payload into a list of strings.
     *
     * @param value metadata list payload
     * @return list of strings or {@code null}
     */
    public static List<String> strings(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Iterable<?> iterable) {
            return CollKit.map(iterable, MetadataCodec::scalarString);
        }
        try {
            List<Object> parsed = JsonKit.toList(JsonKit.toJsonString(value), Object.class);
            if (parsed == null) {
                return null;
            }
            return CollKit.map(parsed, MetadataCodec::scalarString);
        } catch (Exception ignore) {
            Logger.debug(
                    false,
                    "Cortex",
                    "Cortex operation skipped: provider={}, recoverable={}, exception={}",
                    "MetadataCodec",
                    true,
                    ignore.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Converts one scalar metadata element into a string.
     *
     * @param value metadata element
     * @return string value or {@code null}
     */
    public static String scalarString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        try {
            return JsonKit.toPojo(JsonKit.toJsonString(value), String.class);
        } catch (Exception ignore) {
            Logger.debug(
                    false,
                    "Cortex",
                    "Cortex operation skipped: provider={}, recoverable={}, exception={}",
                    "MetadataCodec",
                    true,
                    ignore.getClass().getSimpleName());
            return value.toString();
        }
    }

}
