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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.miaixz.bus.extra.json.JsonPropertyFilter;
import org.miaixz.bus.extra.json.JsonWriteOptions;

/**
 * A {@link org.miaixz.bus.extra.json.JsonProvider} implementation based on Google's Gson library. This class provides
 * JSON serialization and deserialization functionalities using Gson.
 *
 * @author Kimi Liu
 */
public class GsonProvider extends AbstractJsonProvider {

    /**
     * The underlying Gson instance used for JSON operations. It is configured with custom type adapters.
     */
    private final Gson gson;

    /**
     * Constructs a new {@code GsonProvider} instance. Initializes a {@link Gson} instance with custom type adapters to
     * handle potential issues, such as integers being converted to floating-point numbers during deserialization.
     */
    public GsonProvider() {
        this(createDefaultGson());
    }

    /**
     * Constructs a provider backed by an application-configured Gson instance.
     *
     * @param gson Gson instance
     */
    public GsonProvider(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    /**
     * Creates the standalone default Gson engine used when the application does not provide a configured Gson bean.
     *
     * @return Gson engine with Bus-compatible map and list adapters
     */
    private static Gson createDefaultGson() {
        return new GsonBuilder()
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
     * Returns the canonical configuration name of this provider.
     *
     * @return {@code gson}
     */
    @Override
    public String name() {
        return "gson";
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String toJsonString(Object object) {
        return new String(write(object, new JsonWriteOptions(null, false, JsonPropertyFilter.always())),
                StandardCharsets.UTF_8);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String toJsonString(Object object, String format) {
        return new String(write(object, new JsonWriteOptions(format, false, JsonPropertyFilter.always())),
                StandardCharsets.UTF_8);
    }

    /**
     * Serializes a value with the shared date, null, and property-filtering options.
     *
     * @param object  value to serialize
     * @param options framework-independent serialization options
     * @return UTF-8 JSON bytes
     */
    @Override
    public byte[] write(Object object, JsonWriteOptions options) {
        JsonWriteOptions resolved = options == null ? JsonWriteOptions.defaults() : options;
        GsonBuilder builder = gson.newBuilder().registerTypeAdapterFactory(new FilteringTypeAdapterFactory(resolved));
        if (resolved.writeNulls()) {
            builder.serializeNulls();
        }
        if (resolved.dateFormat() != null) {
            builder.setDateFormat(resolved.dateFormat());
        }
        return builder.create().toJson(object).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T toPojo(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * Deserializes JSON into the supplied Java type, including parameterized generic types.
     *
     * @param <T>  target value type
     * @param json JSON document
     * @param type target Java type
     * @return deserialized value
     */
    @Override
    public <T> T toPojo(String json, Type type) {
        return gson.fromJson(json, type);
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
        return gson.fromJson(json, TypeToken.getParameterized(List.class, clazz).getType());
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

    /**
     * Wraps Gson serialization adapters so shared null and property-filtering options are applied without replacing
     * application-configured adapters.
     */
    private static final class FilteringTypeAdapterFactory implements TypeAdapterFactory {

        /**
         * Serialization options applied by every adapter created through this factory.
         */
        private final JsonWriteOptions options;

        /**
         * Creates a filtering adapter factory.
         *
         * @param options framework-independent serialization options
         */
        private FilteringTypeAdapterFactory(JsonWriteOptions options) {
            this.options = options;
        }

        /**
         * Wraps Gson's next adapter and filters its JSON object representation before output.
         *
         * @param <T>  adapted value type
         * @param gson Gson engine creating the adapter
         * @param type adapted type token
         * @return filtering adapter delegating reads to Gson's original adapter
         */
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            return new TypeAdapter<>() {

                /**
                 * Writes a filtered JSON tree produced by the delegated application adapter.
                 *
                 * @param output target JSON writer
                 * @param value  value to serialize
                 * @throws IOException if the JSON writer fails
                 */
                @Override
                public void write(JsonWriter output, T value) throws IOException {
                    JsonElement tree = delegate.toJsonTree(value);
                    filterObject(value, tree, options);
                    gson.toJson(tree, output);
                }

                /**
                 * Reads with the delegated application adapter because write options must not alter deserialization.
                 *
                 * @param input source JSON reader
                 * @return deserialized value
                 * @throws IOException if the JSON reader fails
                 */
                @Override
                public T read(JsonReader input) throws IOException {
                    return delegate.read(input);
                }
            };
        }
    }

    /**
     * Filters the direct properties of a Gson JSON object using values read from the original source object. Nested
     * objects are handled by their own wrapped adapters.
     *
     * @param source  original source value
     * @param tree    serialized JSON tree
     * @param options serialization options
     */
    private static void filterObject(Object source, JsonElement tree, JsonWriteOptions options) {
        if (source == null || !tree.isJsonObject()) {
            return;
        }
        Iterator<Map.Entry<String, JsonElement>> entries = tree.getAsJsonObject().entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, JsonElement> entry = entries.next();
            Object propertyValue = readFieldValue(source, entry.getKey());
            if ((!options.writeNulls() && entry.getValue().isJsonNull())
                    || !options.propertyFilter().accept(source, entry.getKey(), propertyValue)) {
                entries.remove();
            }
        }
    }

    /**
     * Reads a field value for filtering while keeping custom adapters and synthetic JSON properties permissive.
     *
     * @param source source object
     * @param name   serialized property name
     * @return field value, or {@code null} when the field is absent or inaccessible
     */
    private static Object readFieldValue(Object source, String name) {
        Class<?> type = source.getClass();
        while (type != null && type != Object.class) {
            try {
                Field field = type.getDeclaredField(name);
                if (field.trySetAccessible()) {
                    return field.get(source);
                }
                return null;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException | RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

}
