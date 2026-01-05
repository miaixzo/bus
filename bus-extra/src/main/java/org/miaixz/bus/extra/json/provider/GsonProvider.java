/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.json.provider;

import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * A {@link org.miaixz.bus.extra.json.JsonProvider} implementation based on Google's Gson library. This class provides
 * JSON serialization and deserialization functionalities using Gson.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GsonProvider extends AbstractJsonProvider {

    /**
     * The underlying Gson instance used for JSON operations. It is configured with custom type adapters.
     */
    public static Gson gson;

    /**
     * Constructs a new {@code GsonProvider} instance. Initializes a {@link Gson} instance with custom type adapters to
     * handle potential issues, such as integers being converted to floating-point numbers during deserialization.
     */
    public GsonProvider() {
        gson = new GsonBuilder()
                // Custom deserializer for Map to prevent integers from being parsed as doubles.
                .registerTypeAdapter(new TypeToken<Map<Object, Object>>() {
                }.getType(),
                        // Custom JSON deserializer for Map objects. This deserializer prevents Gson from converting
                        // integer values to doubles during JSON deserialization by ensuring all primitive values are
                        // converted to strings.
                        (JsonDeserializer<Map<Object, Object>>) (jsonElement, type, jsonDeserializationContext) -> {
                            Map<Object, Object> map = new LinkedHashMap<>();
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                Object object = entry.getValue();
                                if (object instanceof JsonPrimitive) {
                                    map.put(entry.getKey(), ((JsonPrimitive) object).getAsString());
                                } else {
                                    map.put(entry.getKey(), object);
                                }
                            }
                            return map;
                        })
                // Custom deserializer for List to handle mixed-type arrays.
                .registerTypeAdapter(new TypeToken<List<Object>>() {
                }.getType(),
                        // Custom JSON deserializer for List objects. This deserializer handles mixed-type arrays by
                        // processing JSON objects and primitives appropriately, extracting all entries from nested JSON
                        // objects.
                        (JsonDeserializer<List<Object>>) (jsonElement, type, jsonDeserializationContext) -> {
                            List<Object> list = new LinkedList<>();
                            JsonArray jsonArray = jsonElement.getAsJsonArray();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                if (jsonArray.get(i).isJsonObject()) {
                                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                                    list.addAll(entrySet);
                                } else if (jsonArray.get(i).isJsonPrimitive()) {
                                    list.add(jsonArray.get(i));
                                }
                            }
                            return list;
                        })
                .create();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String toJsonString(Object object) {
        return gson.toJson(object);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String toJsonString(Object object, String format) {
        // Note: This creates a new Gson instance on each call, which can be inefficient.
        gson = new GsonBuilder().setDateFormat(format).create();
        return gson.toJson(object);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T toPojo(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T toPojo(Map map, Class<T> clazz) {
        return gson.fromJson(gson.toJson(map), clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json) {
        TypeToken<List<Object>> typeToken = new TypeToken<>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json, Class<T> clazz) {
        return gson.fromJson(json, (Type) clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <K, V> Map<K, V> toMap(String json) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <K, V> Map<K, V> toMap(Object object) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<>() {
        };
        return gson.fromJson(gson.toJson(object), typeToken.getType());
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T getValue(String json, String field) {
        return (T) JsonParser.parseString(json).getAsJsonObject().get(field);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean isJson(String json) {
        try {
            gson.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

}
