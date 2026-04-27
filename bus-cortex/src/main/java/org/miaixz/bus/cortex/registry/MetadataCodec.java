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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Small JSON helper for metadata payloads.
 * <p>
 * This class deliberately knows only JSON shapes, not subtype-specific metadata fields.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MetadataCodec {

    /**
     * Root metadata object reserved for cortex-specific fields.
     */
    public static final String ROOT = "_cortex";
    /**
     * Metadata key storing the effective asset type.
     */
    public static final String TYPE = "type";
    /**
     * Legacy metadata key storing the effective asset type.
     */
    public static final String LEGACY_SPECIES = "species";

    /**
     * Creates the metadata codec utility holder.
     */
    private MetadataCodec() {
    }

    /**
     * Parses one metadata JSON string into a mutable root map.
     *
     * @param metadata raw metadata JSON
     * @return mutable metadata root
     */
    public static Map<String, Object> root(String metadata) {
        if (StringKit.isEmpty(metadata)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> root = JsonKit.toMap(metadata);
            return root == null ? new LinkedHashMap<>() : new LinkedHashMap<>(root);
        } catch (Exception ignore) {
            return new LinkedHashMap<>();
        }
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
            return null;
        }
    }

    /**
     * Converts one metadata value into the requested enum constant.
     *
     * @param value metadata value
     * @param type  target enum type
     * @param <E>   enum type
     * @return enum constant or {@code null}
     */
    public static <E extends Enum<E>> E enumValue(Object value, Class<E> type) {
        String text = string(value);
        if (StringKit.isBlank(text)) {
            return null;
        }
        try {
            return Enum.valueOf(type, text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignore) {
            return null;
        }
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
            List<String> result = new ArrayList<>();
            for (Object item : iterable) {
                String text = scalarString(item);
                if (text != null) {
                    result.add(text);
                }
            }
            return result;
        }
        try {
            List<Object> parsed = JsonKit.toList(JsonKit.toJsonString(value), Object.class);
            if (parsed == null) {
                return null;
            }
            List<String> result = new ArrayList<>(parsed.size());
            for (Object item : parsed) {
                String text = scalarString(item);
                if (text != null) {
                    result.add(text);
                }
            }
            return result;
        } catch (Exception ignore) {
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
            return value.toString();
        }
    }

}
