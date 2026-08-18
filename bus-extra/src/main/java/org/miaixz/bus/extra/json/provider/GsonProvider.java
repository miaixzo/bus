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
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.json.JsonPropertyFilter;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.extra.json.JsonWriteOptions;

/**
 * Provides Bus JSON operations through Gson while confining {@link JsonElement} instances to this provider.
 * <p>
 * A caller may inject a preconfigured Gson engine. Per-call write options derive a new engine and therefore do not
 * mutate the shared instance. Provider-neutral JSON values are parsed with RFC 8259 strictness independently of the
 * compatibility behavior retained by the legacy object-mapping methods.
 * </p>
 *
 * @author Kimi Liu
 */
public class GsonProvider extends AbstractJsonProvider {

    /**
     * Application-supplied or default Gson engine shared by provider operations.
     */
    private final Gson gson;

    /**
     * Derived Gson engine that preserves explicit JSON null object members for provider-neutral value serialization.
     */
    private final Gson valueGson;

    /**
     * Creates a provider with Bus-compatible map and mixed-array adapters.
     */
    public GsonProvider() {
        this(createDefaultGson());
    }

    /**
     * Creates a provider backed by an application-configured Gson instance.
     *
     * @param gson non-null Gson engine whose registered adapters remain available to legacy object mapping
     * @throws NullPointerException if the Gson engine is {@code null}
     */
    public GsonProvider(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
        this.valueGson = gson.newBuilder().serializeNulls().create();
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
     * Parses one complete RFC 8259 document with Gson's strict reader and converts its tree recursively.
     *
     * @param document complete non-empty JSON document
     * @return immutable provider-neutral JSON value
     * @throws InternalException if Gson rejects the syntax, trailing data remains, or a value cannot be mapped exactly
     */
    @Override
    protected JsonValue decodeValue(final String document) {
        try (JsonReader reader = new JsonReader(new StringReader(document))) {
            reader.setStrictness(Strictness.STRICT);
            final JsonElement element = JsonParser.parseReader(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonSyntaxException("JSON document contains trailing data");
            }
            return fromGsonElement(element);
        } catch (IOException | JsonParseException | IllegalStateException | NumberFormatException cause) {
            throw new InternalException("Gson cannot parse the RFC 8259 document", cause);
        }
    }

    /**
     * Converts the provider-neutral JSON model to a Gson tree and serializes one complete document.
     *
     * @param value immutable provider-neutral JSON value
     * @return complete JSON document
     * @throws InternalException if Gson cannot serialize the mapped tree
     */
    @Override
    protected String encodeValue(final JsonValue value) {
        try {
            return valueGson.toJson(toGsonElement(value));
        } catch (JsonParseException | IllegalStateException cause) {
            throw new InternalException("Gson cannot serialize the provider-neutral JSON value", cause);
        }
    }

    /**
     * Recursively maps a Gson tree into immutable provider-neutral values.
     *
     * @param element Gson tree element representing one JSON value
     * @return immutable provider-neutral JSON value
     * @throws InternalException if Gson exposes an unsupported primitive or tree element
     */
    private JsonValue fromGsonElement(final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return JsonValue.NullValue.instance();
        }
        if (element.isJsonObject()) {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> property : element.getAsJsonObject().entrySet()) {
                values.put(property.getKey(), fromGsonElement(property.getValue()));
            }
            return new JsonValue.ObjectValue(values);
        }
        if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final List<JsonValue> values = new ArrayList<>(array.size());
            for (JsonElement item : array) {
                values.add(fromGsonElement(item));
            }
            return new JsonValue.ArrayValue(values);
        }
        if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new JsonValue.StringValue(primitive.getAsString());
            }
            if (primitive.isBoolean()) {
                return new JsonValue.BooleanValue(primitive.getAsBoolean());
            }
            if (primitive.isNumber()) {
                return new JsonValue.NumberValue(primitive.getAsBigDecimal());
            }
            throw new InternalException("Gson tree contains an unsupported primitive");
        }
        throw new InternalException("Gson tree contains a non-JSON element: " + element.getClass().getName());
    }

    /**
     * Recursively maps immutable provider-neutral values into Gson tree elements.
     *
     * @param value immutable provider-neutral JSON value
     * @return Gson tree element owned by this provider
     * @throws InternalException if an unknown JsonValue implementation reaches the provider boundary
     */
    private JsonElement toGsonElement(final JsonValue value) {
        if (value instanceof JsonValue.ObjectValue objectValue) {
            final JsonObject object = new JsonObject();
            objectValue.values().forEach((name, member) -> object.add(name, toGsonElement(member)));
            return object;
        }
        if (value instanceof JsonValue.ArrayValue arrayValue) {
            final JsonArray array = new JsonArray(arrayValue.values().size());
            arrayValue.values().forEach(element -> array.add(toGsonElement(element)));
            return array;
        }
        if (value instanceof JsonValue.StringValue stringValue) {
            return new JsonPrimitive(stringValue.value());
        }
        if (value instanceof JsonValue.NumberValue numberValue) {
            return new JsonPrimitive(numberValue.value());
        }
        if (value instanceof JsonValue.BooleanValue booleanValue) {
            return new JsonPrimitive(booleanValue.value());
        }
        if (value instanceof JsonValue.NullValue) {
            return JsonNull.INSTANCE;
        }
        throw new InternalException("Unsupported provider-neutral JSON value: " + value.getClass().getName());
    }

    /**
     * Serializes an object with Gson while omitting null-valued properties.
     *
     * @param object object accepted by Gson
     * @return JSON document produced by Gson
     * @throws JsonParseException if a registered adapter cannot serialize the object
     */
    @Override
    public String toJsonString(Object object) {
        return new String(write(object, new JsonWriteOptions(null, false, JsonPropertyFilter.always())), Charset.UTF_8);
    }

    /**
     * Serializes an object with the requested date format while omitting null-valued properties.
     *
     * @param object object accepted by Gson
     * @param format date format pattern accepted by {@link GsonBuilder#setDateFormat(String)}
     * @return JSON document produced by Gson
     * @throws IllegalArgumentException if the date format is invalid
     * @throws JsonParseException       if a registered adapter cannot serialize the object
     */
    @Override
    public String toJsonString(Object object, String format) {
        return new String(write(object, new JsonWriteOptions(format, false, JsonPropertyFilter.always())),
                Charset.UTF_8);
    }

    /**
     * Serializes a value with the shared date, null, and property-filtering options.
     *
     * @param object  value to serialize
     * @param options framework-independent serialization options
     * @return UTF-8 JSON bytes
     * @throws IllegalArgumentException if a requested date format is invalid
     * @throws JsonParseException       if a registered adapter cannot serialize the value
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
        return builder.create().toJson(object).getBytes(Charset.UTF_8);
    }

    /**
     * Deserializes a JSON document into a concrete Java class through the configured Gson adapters.
     *
     * @param <T>   requested Java value type
     * @param json  JSON document
     * @param clazz target Java class
     * @return value produced by Gson
     * @throws JsonParseException if Gson cannot parse or bind the document
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
     * @throws JsonParseException if Gson cannot parse or bind the document
     */
    @Override
    public <T> T toPojo(String json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * Converts map entries into a target Java object through Gson's configured adapters.
     *
     * @param <T>   target value type
     * @param map   source map
     * @param clazz target Java class
     * @return converted Java value
     * @throws JsonParseException if Gson cannot serialize or bind the map
     */
    @Override
    public <T> T toPojo(Map map, Class<T> clazz) {
        return gson.fromJson(gson.toJson(map), clazz);
    }

    /**
     * Deserializes a JSON array using the default mixed-value list adapter.
     *
     * @param <T>  inferred list element type
     * @param json JSON array document
     * @return list populated in wire order
     * @throws JsonParseException if Gson cannot parse the array
     */
    @Override
    public <T> List<T> toList(String json) {
        TypeToken<List<Object>> typeToken = new TypeToken<>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    /**
     * Deserializes a JSON array whose element class is explicitly supplied.
     *
     * @param <T>   list element type
     * @param json  JSON array document
     * @param clazz element Java class
     * @return list populated in wire order
     * @throws JsonParseException if Gson cannot parse or bind an element
     */
    @Override
    public <T> List<T> toList(String json, Class<T> clazz) {
        return gson.fromJson(json, TypeToken.getParameterized(List.class, clazz).getType());
    }

    /**
     * Deserializes a JSON array using the caller's reflective type token.
     *
     * @param <T>  list element type
     * @param json JSON array document
     * @param type reflective list type consumed by Gson
     * @return list populated in wire order
     * @throws JsonParseException if Gson cannot parse or bind the array
     */
    @Override
    public <T> List<T> toList(String json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * Deserializes a JSON object with the default ordered map adapter.
     *
     * @param <K>  inferred map key type
     * @param <V>  inferred map value type
     * @param json JSON object document
     * @return insertion-ordered map of the object's properties
     * @throws JsonParseException if Gson cannot parse the object
     */
    @Override
    public <K, V> Map<K, V> toMap(String json) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    /**
     * Converts an object into a map through a Gson JSON round trip.
     *
     * @param <K>    inferred map key type
     * @param <V>    inferred map value type
     * @param object source object
     * @return map produced by the default ordered map adapter
     * @throws JsonParseException if Gson cannot serialize or bind the object
     */
    @Override
    public <K, V> Map<K, V> toMap(Object object) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<>() {
        };
        return gson.fromJson(gson.toJson(object), typeToken.getType());
    }

    /**
     * Reads one top-level member from a JSON object through Gson's tree model.
     *
     * @param <T>   caller-selected return type
     * @param json  JSON object document
     * @param field top-level member name
     * @return Gson tree element for the member, or {@code null} when absent
     * @throws JsonParseException    if Gson cannot parse the document
     * @throws IllegalStateException if the document's root is not an object
     */
    @Override
    public <T> T getValue(String json, String field) {
        return (T) JsonParser.parseString(json).getAsJsonObject().get(field);
    }

    /**
     * Determines whether Gson's compatibility parser accepts the supplied text.
     *
     * @param json candidate JSON document
     * @return {@code true} when Gson parses the document; otherwise {@code false}
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
