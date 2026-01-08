/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.json;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;

/**
 * Defines the contract for a JSON service provider. This interface specifies a set of common methods for JSON
 * serialization and deserialization, allowing for different underlying JSON libraries (e.g., Jackson, Gson, Fastjson)
 * to be used interchangeably.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface JsonProvider extends Provider {

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

    /**
     * Returns the provider type.
     *
     * @return the provider type identifier
     */
    @Override
    default Object type() {
        return EnumValue.Povider.JSON;
    }

}
