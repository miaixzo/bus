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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.json.JsonPropertyFilter;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.extra.json.JsonWriteOptions;
import org.miaixz.bus.logger.Logger;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

/**
 * Provides Bus JSON operations through Jackson while keeping Jackson tree types behind the provider boundary.
 * <p>
 * The provider reuses one immutable mapper configuration and creates derived mappers for per-call write options. The
 * application may inject a mapper with its own modules; no mapper or {@link JsonNode} is exposed by the
 * provider-neutral value API.
 * </p>
 *
 * @author Kimi Liu
 */
public class JacksonProvider extends AbstractJsonProvider {

    /**
     * Identifier used to attach the shared property filter to all serialized objects.
     */
    private static final String FILTER_ID = "BusJsonPropertyFilter";

    /**
     * Application-supplied or default Jackson mapper shared by provider operations.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates a provider with a Jackson mapper that discovers modules available on the runtime class path.
     */
    public JacksonProvider() {
        this(JsonMapper.builder().findAndAddModules().build());
    }

    /**
     * Creates a provider backed by an application-configured Jackson mapper.
     *
     * @param objectMapper non-null Jackson mapper whose modules and features remain available to legacy object mapping
     * @throws NullPointerException if the mapper is {@code null}
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
     * Parses one complete JSON document into a Jackson tree and converts it to the provider-neutral value model.
     * Jackson is instructed to reject trailing tokens and to retain arbitrary-precision integer and decimal values.
     *
     * @param document complete non-empty JSON document
     * @return immutable provider-neutral JSON value
     * @throws InternalException if Jackson rejects the document or exposes a non-JSON tree node
     */
    @Override
    protected JsonValue decodeValue(final String document) {
        try {
            final JsonNode node = objectMapper.reader(
                    DeserializationFeature.FAIL_ON_TRAILING_TOKENS,
                    DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,
                    DeserializationFeature.USE_BIG_INTEGER_FOR_INTS).readTree(document);
            return fromJacksonNode(node);
        } catch (JacksonException | IllegalArgumentException cause) {
            throw new InternalException("Jackson cannot parse the RFC 8259 document", cause);
        }
    }

    /**
     * Converts the provider-neutral value to a Jackson tree before serializing one complete JSON document.
     *
     * @param value immutable provider-neutral JSON value
     * @return complete JSON document
     * @throws InternalException if Jackson cannot serialize the mapped tree
     */
    @Override
    protected String encodeValue(final JsonValue value) {
        try {
            return objectMapper.writeValueAsString(toJacksonNode(value));
        } catch (JacksonException | IllegalArgumentException cause) {
            throw new InternalException("Jackson cannot serialize the provider-neutral JSON value", cause);
        }
    }

    /**
     * Recursively converts a Jackson tree node without exposing it through the public JSON contract.
     *
     * @param node Jackson tree node representing one RFC 8259 value
     * @return immutable provider-neutral JSON value
     * @throws InternalException if the node is absent or represents a Jackson-only value kind
     */
    private JsonValue fromJacksonNode(final JsonNode node) {
        if (node == null || node.isMissingNode()) {
            throw new InternalException("Jackson tree does not contain an RFC 8259 value");
        }
        if (node.isObject()) {
            final Map<String, JsonValue> values = new LinkedHashMap<>(node.size());
            for (Map.Entry<String, JsonNode> property : node.properties()) {
                values.put(property.getKey(), fromJacksonNode(property.getValue()));
            }
            return new JsonValue.ObjectValue(values);
        }
        if (node.isArray()) {
            final List<JsonValue> values = new ArrayList<>(node.size());
            for (JsonNode element : node) {
                values.add(fromJacksonNode(element));
            }
            return new JsonValue.ArrayValue(values);
        }
        if (node.isString()) {
            return new JsonValue.StringValue(node.stringValue());
        }
        if (node.isNumber()) {
            final BigDecimal number = node.decimalValue();
            if (number == null) {
                throw new InternalException("Jackson numeric node has no exact decimal value");
            }
            return new JsonValue.NumberValue(number);
        }
        if (node.isBoolean()) {
            return new JsonValue.BooleanValue(node.booleanValue());
        }
        if (node.isNull()) {
            return JsonValue.NullValue.instance();
        }
        throw new InternalException("Jackson tree contains a non-JSON node: " + node.getNodeType());
    }

    /**
     * Recursively converts the provider-neutral model into nodes created by the configured Jackson mapper.
     *
     * @param value immutable provider-neutral JSON value
     * @return Jackson tree node owned by this provider
     * @throws InternalException if an unknown JsonValue implementation reaches the provider boundary
     */
    private JsonNode toJacksonNode(final JsonValue value) {
        final JsonNodeFactory factory = objectMapper.getNodeFactory();
        if (value instanceof JsonValue.ObjectValue objectValue) {
            final ObjectNode objectNode = factory.objectNode();
            objectValue.values().forEach((name, member) -> objectNode.set(name, toJacksonNode(member)));
            return objectNode;
        }
        if (value instanceof JsonValue.ArrayValue arrayValue) {
            final ArrayNode arrayNode = factory.arrayNode(arrayValue.values().size());
            arrayValue.values().forEach(element -> arrayNode.add(toJacksonNode(element)));
            return arrayNode;
        }
        if (value instanceof JsonValue.StringValue stringValue) {
            return factory.stringNode(stringValue.value());
        }
        if (value instanceof JsonValue.NumberValue numberValue) {
            return factory.numberNode(numberValue.value());
        }
        if (value instanceof JsonValue.BooleanValue booleanValue) {
            return factory.booleanNode(booleanValue.value());
        }
        if (value instanceof JsonValue.NullValue) {
            return factory.nullNode();
        }
        throw new InternalException("Unsupported provider-neutral JSON value: " + value.getClass().getName());
    }

    /**
     * Serializes an object using the provider's default framework-independent write options.
     *
     * @param object object accepted by the configured Jackson mapper
     * @return JSON document produced by Jackson
     * @throws InternalException if Jackson cannot serialize the object
     */
    @Override
    public String toJsonString(Object object) {
        return new String(write(object, JsonWriteOptions.defaults()), Charset.UTF_8);
    }

    /**
     * Serializes an object while applying the supplied date format and retaining null-valued properties.
     *
     * @param object object accepted by the configured Jackson mapper
     * @param format date format understood by {@link SimpleDateFormat}
     * @return JSON document produced by Jackson
     * @throws InternalException if the format is invalid or Jackson cannot serialize the object
     */
    @Override
    public String toJsonString(Object object, String format) {
        return new String(write(object, new JsonWriteOptions(format, true, JsonPropertyFilter.always())),
                Charset.UTF_8);
    }

    /**
     * Serializes a value with the shared date, null, and property-filtering options.
     *
     * @param object  value to serialize
     * @param options framework-independent serialization options
     * @return UTF-8 JSON bytes
     * @throws InternalException if Jackson cannot serialize the value
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
            return json.getBytes(Charset.UTF_8);
        } catch (JacksonException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Deserializes a JSON document into a concrete Java class using the configured mapper.
     *
     * @param <T>       requested Java value type
     * @param json      JSON document
     * @param valueType target Java class
     * @return value produced by Jackson
     * @throws InternalException if Jackson cannot parse or bind the document
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
            throw new InternalException(e);
        }
    }

    /**
     * Deserializes JSON into the supplied Java type, including parameterized generic types.
     *
     * @param <T>  target value type
     * @param json JSON document
     * @param type target Java type
     * @return deserialized value
     * @throws InternalException if Jackson cannot parse or bind the document
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
     * Converts map entries into a target Java object through Jackson's configured conversion rules.
     *
     * @param <T>     target value type
     * @param fromMap source map
     * @param clazz   target Java class
     * @return converted Java value
     * @throws IllegalArgumentException if Jackson cannot convert the map
     */
    @Override
    public <T> T toPojo(Map fromMap, Class<T> clazz) {
        return objectMapper.convertValue(fromMap, clazz);
    }

    /**
     * Deserializes a JSON array into a linked list of values inferred by Jackson.
     *
     * @param <T>  inferred list element type
     * @param json JSON array document
     * @return linked list populated in wire order
     * @throws InternalException if Jackson cannot parse or bind the array
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
            throw new InternalException(e);
        }
    }

    /**
     * Deserializes a JSON array into a list whose element class is explicitly supplied.
     *
     * @param <T>   list element type
     * @param json  JSON array document
     * @param clazz element Java class
     * @return list populated in wire order
     * @throws InternalException if Jackson cannot parse or bind an element
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
            throw new InternalException(e);
        }
    }

    /**
     * Deserializes a JSON array using the caller's reflective type token.
     *
     * @param <T>  list element type
     * @param json JSON array document
     * @param type reflective list type consumed by Jackson
     * @return list populated in wire order
     * @throws InternalException if Jackson cannot parse or bind the array
     */
    @Override
    public <T> List<T> toList(String json, Type type) {
        TypeReference<List<T>> typeReference = new TypeReference<>() {

            /**
             * Returns the exact reflective type captured from the caller for Jackson binding.
             *
             * @return caller-supplied reflective type
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
            throw new InternalException(e);
        }
    }

    /**
     * Deserializes a JSON object into a map using Jackson's natural key and value mappings.
     *
     * @param <K>  inferred map key type
     * @param <V>  inferred map value type
     * @param json JSON object document
     * @return map populated with the document's properties
     * @throws InternalException if Jackson cannot parse or bind the object
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
            throw new InternalException(e);
        }
    }

    /**
     * Converts an object into a map using Jackson's configured property model.
     *
     * @param <K>    inferred map key type
     * @param <V>    inferred map value type
     * @param object source object
     * @return map containing Jackson-visible properties
     * @throws IllegalArgumentException if Jackson cannot convert the object
     */
    @Override
    public <K, V> Map<K, V> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    /**
     * Reads one top-level field from a JSON object through Jackson's tree model.
     *
     * @param <T>   caller-selected return type
     * @param json  JSON object document
     * @param field top-level property name
     * @return Jackson tree value for the property, or {@code null} when absent
     * @throws InternalException if Jackson cannot parse the document
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
     * Determines whether Jackson accepts the supplied text as JSON.
     *
     * @param json candidate JSON document
     * @return {@code true} when Jackson parses a tree; otherwise {@code false}
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
