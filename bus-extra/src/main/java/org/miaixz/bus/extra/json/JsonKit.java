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
import java.util.List;
import java.util.Map;

/**
 * A utility class for JSON operations, which automatically identifies the underlying JSON provider via SPI. This class
 * acts as a facade, providing static methods for common JSON serialization and deserialization tasks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JsonKit {

    /**
     * Retrieves the globally available singleton JSON provider instance.
     *
     * @return The singleton {@link JsonProvider} instance.
     */
    public static JsonProvider getProvider() {
        return JsonFactory.get();
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
