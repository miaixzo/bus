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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.json.JsonPropertyFilter;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.extra.json.JsonWriteOptions;

/**
 * Provides Bus JSON operations through Fastjson2 while confining its mutable containers to this provider.
 * <p>
 * Provider-neutral reads enable arbitrary-precision numeric features and reject single-quoted strings. The legacy
 * object API retains the established Fastjson2 field-based writer options for compatibility.
 * </p>
 *
 * @author Kimi Liu
 */
public class FastJsonProvider extends AbstractJsonProvider {

    /**
     * Reader features that retain arbitrary-precision numbers and disallow non-standard single-quoted strings.
     */
    private static final JSONReader.Feature[] VALUE_READER_FEATURES = { JSONReader.Feature.UseBigDecimalForFloats,
            JSONReader.Feature.UseBigDecimalForDoubles, JSONReader.Feature.UseBigIntegerForInts,
            JSONReader.Feature.DisableSingleQuote };

    /**
     * Writer feature that retains explicit JSON null members in provider-neutral object values.
     */
    private static final JSONWriter.Feature[] VALUE_WRITER_FEATURES = { JSONWriter.Feature.WriteMapNullValue };

    /**
     * Returns the canonical configuration name of this provider.
     *
     * @return {@code fastjson}
     */
    @Override
    public String name() {
        return "fastjson";
    }

    /**
     * Parses one complete JSON document into Fastjson2 containers and converts them to provider-neutral values.
     *
     * @param document complete non-empty JSON document
     * @return immutable provider-neutral JSON value
     * @throws InternalException if Fastjson2 rejects the syntax or exposes a value outside the RFC 8259 data model
     */
    @Override
    protected JsonValue decodeValue(final String document) {
        try {
            return fromFastJsonTree(JSON.parse(document, VALUE_READER_FEATURES));
        } catch (JSONException | NumberFormatException cause) {
            throw new InternalException("Fastjson2 cannot parse the RFC 8259 document", cause);
        }
    }

    /**
     * Converts the provider-neutral JSON model into Fastjson2 containers and serializes one complete document.
     *
     * @param value immutable provider-neutral JSON value
     * @return complete JSON document
     * @throws InternalException if Fastjson2 cannot serialize the mapped tree
     */
    @Override
    protected String encodeValue(final JsonValue value) {
        try {
            return JSON.toJSONString(toFastJsonTree(value), VALUE_WRITER_FEATURES);
        } catch (JSONException | IllegalArgumentException cause) {
            throw new InternalException("Fastjson2 cannot serialize the provider-neutral JSON value", cause);
        }
    }

    /**
     * Recursively converts Fastjson2 containers and scalar values without exposing them through the public contract.
     *
     * @param tree Fastjson2 container or scalar representing one JSON value
     * @return immutable provider-neutral JSON value
     * @throws InternalException if the parsed value is not part of the RFC 8259 data model
     */
    private JsonValue fromFastJsonTree(final Object tree) {
        if (tree == null) {
            return JsonValue.NullValue.instance();
        }
        if (tree instanceof JSONObject object) {
            final Map<String, JsonValue> values = new LinkedHashMap<>(object.size());
            object.forEach((name, member) -> values.put(name, fromFastJsonTree(member)));
            return new JsonValue.ObjectValue(values);
        }
        if (tree instanceof JSONArray array) {
            final List<JsonValue> values = new ArrayList<>(array.size());
            array.forEach(element -> values.add(fromFastJsonTree(element)));
            return new JsonValue.ArrayValue(values);
        }
        if (tree instanceof String string) {
            return new JsonValue.StringValue(string);
        }
        if (tree instanceof Boolean bool) {
            return new JsonValue.BooleanValue(bool);
        }
        if (tree instanceof Number number) {
            final BigDecimal decimal = number instanceof BigDecimal bigDecimal ? bigDecimal
                    : new BigDecimal(number.toString());
            return new JsonValue.NumberValue(decimal);
        }
        throw new InternalException("Fastjson2 tree contains a non-JSON value: " + tree.getClass().getName());
    }

    /**
     * Recursively maps immutable provider-neutral values into Fastjson2 containers and scalar values.
     *
     * @param value immutable provider-neutral JSON value
     * @return Fastjson2-compatible tree value owned by this provider
     * @throws InternalException if an unknown JsonValue implementation reaches the provider boundary
     */
    private Object toFastJsonTree(final JsonValue value) {
        if (value instanceof JsonValue.ObjectValue objectValue) {
            final JSONObject object = new JSONObject(objectValue.values().size());
            objectValue.values().forEach((name, member) -> object.put(name, toFastJsonTree(member)));
            return object;
        }
        if (value instanceof JsonValue.ArrayValue arrayValue) {
            final JSONArray array = new JSONArray(arrayValue.values().size());
            arrayValue.values().forEach(element -> array.add(toFastJsonTree(element)));
            return array;
        }
        if (value instanceof JsonValue.StringValue stringValue) {
            return stringValue.value();
        }
        if (value instanceof JsonValue.NumberValue numberValue) {
            return numberValue.value();
        }
        if (value instanceof JsonValue.BooleanValue booleanValue) {
            return booleanValue.value();
        }
        if (value instanceof JsonValue.NullValue) {
            return null;
        }
        throw new InternalException("Unsupported provider-neutral JSON value: " + value.getClass().getName());
    }

    /**
     * Field-based writer features used when null-valued properties must remain in legacy object output.
     */
    private static final JSONWriter.Feature[] WRITER_FEATURES = { JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty,
            JSONWriter.Feature.BrowserCompatible, JSONWriter.Feature.WriteNulls };

    /**
     * Field-based writer features used when null-valued properties must be omitted from legacy object output.
     */
    private static final JSONWriter.Feature[] NON_NULL_WRITER_FEATURES = { JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.BrowserCompatible };

    /**
     * Identity value filter retained as the base of the legacy per-call filter chain.
     */
    private static final Filter[] FILTERS = { (ValueFilter) (object, name, value) -> value };

    /**
     * Creates a stateless provider backed by Fastjson2's static reader and writer facilities.
     */
    public FastJsonProvider() {
        // No initialization required.
    }

    /**
     * Serializes an object with the default Fastjson2 writer features.
     *
     * @param object object accepted by Fastjson2
     * @return JSON document produced by Fastjson2
     * @throws JSONException if Fastjson2 cannot serialize the object
     */
    @Override
    public String toJsonString(Object object) {
        return new String(write(object, JsonWriteOptions.defaults()), Charset.UTF_8);
    }

    /**
     * Serializes an object with the requested Fastjson2 date format while retaining null-valued properties.
     *
     * @param object object accepted by Fastjson2
     * @param format Fastjson2 date format
     * @return JSON document produced by Fastjson2
     * @throws JSONException if Fastjson2 rejects the format or cannot serialize the object
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
     * @throws JSONException if Fastjson2 rejects an option or cannot serialize the value
     */
    @Override
    public byte[] write(Object object, JsonWriteOptions options) {
        JsonWriteOptions resolved = options == null ? JsonWriteOptions.defaults() : options;
        List<Filter> filters = new ArrayList<>(List.of(FILTERS));
        filters.add((PropertyFilter) resolved.propertyFilter()::accept);
        JSONWriter.Feature[] features = resolved.writeNulls() ? WRITER_FEATURES : NON_NULL_WRITER_FEATURES;
        String json = resolved.dateFormat() == null
                ? JSON.toJSONString(object, filters.toArray(Filter[]::new), features)
                : JSON.toJSONString(object, resolved.dateFormat(), filters.toArray(Filter[]::new), features);
        return json.getBytes(Charset.UTF_8);
    }

    /**
     * Deserializes a JSON object into a concrete Java class through Fastjson2.
     *
     * @param <T>   requested Java value type
     * @param json  JSON object document
     * @param clazz target Java class
     * @return value produced by Fastjson2
     * @throws JSONException if Fastjson2 cannot parse or bind the document
     */
    @Override
    public <T> T toPojo(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    /**
     * Deserializes JSON into the supplied Java type, including parameterized generic types.
     *
     * @param <T>  target value type
     * @param json JSON document
     * @param type target Java type
     * @return deserialized value
     * @throws JSONException if Fastjson2 cannot parse or bind the document
     */
    @Override
    public <T> T toPojo(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    /**
     * Converts map entries into a target Java object through a Fastjson2 JSON round trip.
     *
     * @param <T>   target value type
     * @param map   source map
     * @param clazz target Java class
     * @return converted Java value
     * @throws JSONException if Fastjson2 cannot serialize or bind the map
     */
    @Override
    public <T> T toPojo(Map map, Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }

    /**
     * Deserializes a JSON array into a linked list using Fastjson2's natural value mappings.
     *
     * @param <T>  inferred list element type
     * @param json JSON array document
     * @return linked list populated in wire order
     * @throws JSONException if Fastjson2 cannot parse the array
     */
    @Override
    public <T> List<T> toList(String json) {
        return JSON.parseObject(json, LinkedList.class);
    }

    /**
     * Deserializes a JSON array whose element class is explicitly supplied.
     *
     * @param <T>   list element type
     * @param json  JSON array document
     * @param clazz element Java class
     * @return list populated in wire order
     * @throws JSONException if Fastjson2 cannot parse or bind an element
     */
    @Override
    public <T> List<T> toList(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    /**
     * Deserializes a JSON array using the caller's reflective type token.
     *
     * @param <T>  list element type
     * @param json JSON array document
     * @param type reflective list type consumed by Fastjson2
     * @return list populated in wire order
     * @throws JSONException if Fastjson2 cannot parse or bind the array
     */
    @Override
    public <T> List<T> toList(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    /**
     * Deserializes a JSON object into a map using Fastjson2's natural mappings.
     *
     * @param <K>  inferred map key type
     * @param <V>  inferred map value type
     * @param json JSON object document
     * @return map of the object's properties
     * @throws JSONException if Fastjson2 cannot parse the object
     */
    @Override
    public <K, V> Map<K, V> toMap(String json) {
        return JSON.parseObject(json, Map.class);
    }

    /**
     * Converts an object into a map through a Fastjson2 JSON round trip.
     *
     * @param <K>    inferred map key type
     * @param <V>    inferred map value type
     * @param object source object
     * @return map of Fastjson2-visible properties
     * @throws JSONException if Fastjson2 cannot serialize or bind the object
     */
    @Override
    public <K, V> Map<K, V> toMap(Object object) {
        return toMap(JSON.toJSONString(object));
    }

    /**
     * Reads one top-level member from a JSON object through Fastjson2's object container.
     *
     * @param <T>   caller-selected return type
     * @param json  JSON object document
     * @param field top-level member name
     * @return mapped value for the member, or {@code null} when absent
     * @throws JSONException if Fastjson2 cannot parse the object
     */
    @Override
    public <T> T getValue(String json, String field) {
        return (T) JSON.parseObject(json).get(field);
    }

    /**
     * Determines whether Fastjson2 accepts the supplied text as either an object or an array.
     *
     * @param json candidate JSON object or array document
     * @return {@code true} when one container parser accepts the document; otherwise {@code false}
     */
    @Override
    public boolean isJson(String json) {
        try {
            JSON.parseObject(json);
        } catch (RuntimeException ex) {
            try {
                JSON.parseArray(json);
            } catch (RuntimeException ex1) {
                return false;
            }
        }
        return true;
    }

}
