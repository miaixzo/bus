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

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.json.JsonPropertyFilter;
import org.miaixz.bus.extra.json.JsonWriteOptions;
import org.miaixz.bus.logger.Logger;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

/**
 * A {@link org.miaixz.bus.extra.json.JsonProvider} implementation based on the Jackson library. This class provides
 * JSON serialization and deserialization functionalities using Jackson's {@link ObjectMapper}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JacksonProvider extends AbstractJsonProvider {

    /**
     * Identifier used to attach the shared property filter to all serialized objects.
     */
    private static final String FILTER_ID = "busJsonPropertyFilter";

    /**
     * The underlying Jackson {@link ObjectMapper} used for JSON operations.
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code JacksonProvider} instance. Initializes an {@link ObjectMapper} with module auto-detection
     * enabled.
     */
    public JacksonProvider() {
        this(JsonMapper.builder().findAndAddModules().build());
    }

    /**
     * Constructs a provider backed by an application-configured Jackson mapper.
     *
     * @param objectMapper Jackson mapper
     */
    public JacksonProvider(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    /**
     * Returns the canonical configuration name of this provider.
     *
     * @return {@code jackson}
     */
    @Override
    public String name() {
        return "jackson";
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String toJsonString(Object object) {
        return new String(write(object, JsonWriteOptions.defaults()), StandardCharsets.UTF_8);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String toJsonString(Object object, String format) {
        return new String(write(object, new JsonWriteOptions(format, true, JsonPropertyFilter.always())),
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
        SimpleFilterProvider filters = new SimpleFilterProvider()
                .addFilter(FILTER_ID, new SharedPropertyFilter(resolved));
        ObjectMapper mapper = objectMapper.rebuild().filterProvider(filters).addMixIn(Object.class, FilterMixIn.class)
                .changeDefaultPropertyInclusion(
                        value -> JsonInclude.Value.construct(
                                resolved.writeNulls() ? JsonInclude.Include.ALWAYS : JsonInclude.Include.NON_NULL,
                                resolved.writeNulls() ? JsonInclude.Include.ALWAYS : JsonInclude.Include.NON_NULL))
                .build();
        try {
            String json = resolved.dateFormat() == null ? mapper.writeValueAsString(object)
                    : mapper.writer(new SimpleDateFormat(resolved.dateFormat())).writeValueAsString(object);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (JacksonException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T toPojo(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JacksonException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JSON operation failed: provider={}, exception={}",
                    "JacksonProvider",
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
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
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructType(type));
        } catch (JacksonException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T toPojo(Map fromMap, Class<T> clazz) {
        return objectMapper.convertValue(fromMap, clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json) {
        try {
            return objectMapper.readValue(json, LinkedList.class);
        } catch (JacksonException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JSON operation failed: provider={}, exception={}",
                    "JacksonProvider",
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json, Class<T> clazz) {
        try {
            return objectMapper
                    .readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JacksonException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JSON operation failed: provider={}, exception={}",
                    "JacksonProvider",
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json, Type type) {
        TypeReference<List<T>> typeReference = new TypeReference<>() {

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public Type getType() {
                return type;
            }
        };
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JacksonException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JSON operation failed: provider={}, exception={}",
                    "JacksonProvider",
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <K, V> Map<K, V> toMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JacksonException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JSON operation failed: provider={}, exception={}",
                    "JacksonProvider",
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <K, V> Map<K, V> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T getValue(String json, String field) {
        try {
            return (T) objectMapper.readTree(json).get(field);
        } catch (JacksonException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JSON operation failed: provider={}, exception={}",
                    "JacksonProvider",
                    e.getClass().getSimpleName());
            throw new InternalException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean isJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JacksonException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "JSON operation failed: provider={}, exception={}",
                    "JacksonProvider",
                    e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Applies the shared Jackson property filter to every object through a mix-in.
     */
    @JsonFilter(FILTER_ID)
    private interface FilterMixIn {
        // Carries the JsonFilter annotation for the global Object mix-in.
    }

    /**
     * Jackson filter bridge for framework-independent null and property-filtering options.
     */
    private static final class SharedPropertyFilter extends SimpleBeanPropertyFilter {

        /**
         * Serialization options applied to each property.
         */
        private final JsonWriteOptions options;

        /**
         * Creates a Jackson property filter bridge.
         *
         * @param options framework-independent serialization options
         */
        private SharedPropertyFilter(JsonWriteOptions options) {
            this.options = options;
        }

        /**
         * Writes a property only when it satisfies the shared null and property-filtering rules.
         *
         * @param source   owning object
         * @param output   target JSON generator
         * @param context  Jackson serialization context
         * @param property Jackson property writer
         * @throws Exception if property access or JSON output fails
         */
        @Override
        public void serializeAsProperty(
                Object source,
                tools.jackson.core.JsonGenerator output,
                SerializationContext context,
                PropertyWriter property) throws Exception {
            if (!(property instanceof BeanPropertyWriter beanProperty)) {
                property.serializeAsProperty(source, output, context);
                return;
            }
            Object value = beanProperty.get(source);
            if ((options.writeNulls() || value != null)
                    && options.propertyFilter().accept(source, beanProperty.getName(), value)) {
                property.serializeAsProperty(source, output, context);
            } else if (!output.canOmitProperties()) {
                output.writeName(property.getName());
                output.writeNull();
            }
        }
    }

}
