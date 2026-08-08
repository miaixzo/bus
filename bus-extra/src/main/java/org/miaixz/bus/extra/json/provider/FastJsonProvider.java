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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;

import org.miaixz.bus.extra.json.JsonPropertyFilter;
import org.miaixz.bus.extra.json.JsonWriteOptions;

/**
 * A {@link org.miaixz.bus.extra.json.JsonProvider} implementation based on Alibaba's Fastjson2 library. This class
 * provides JSON serialization and deserialization functionalities using Fastjson2.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FastJsonProvider extends AbstractJsonProvider {

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
     * Default writer features for Fastjson serialization, including field-based serialization and writing null values.
     */
    private static final JSONWriter.Feature[] WRITER_FEATURES = { JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty,
            JSONWriter.Feature.BrowserCompatible, JSONWriter.Feature.WriteNulls };

    /**
     * Writer features used when null-valued properties must be omitted.
     */
    private static final JSONWriter.Feature[] NON_NULL_WRITER_FEATURES = { JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.BrowserCompatible };

    /**
     * Default filters for Fastjson serialization, which convert null, empty, or blank string values to null.
     */
    private static final Filter[] FILTERS = { (ValueFilter) (object, name, value) -> value };

    /**
     * Constructs a new {@code FastJsonProvider} instance.
     */
    public FastJsonProvider() {
        // No initialization required.
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
        List<Filter> filters = new ArrayList<>(List.of(FILTERS));
        filters.add((PropertyFilter) resolved.propertyFilter()::accept);
        JSONWriter.Feature[] features = resolved.writeNulls() ? WRITER_FEATURES : NON_NULL_WRITER_FEATURES;
        String json = resolved.dateFormat() == null
                ? JSON.toJSONString(object, filters.toArray(Filter[]::new), features)
                : JSON.toJSONString(object, resolved.dateFormat(), filters.toArray(Filter[]::new), features);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Description inherited from parent class or interface.
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
     */
    @Override
    public <T> T toPojo(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T toPojo(Map map, Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json) {
        return JSON.parseObject(json, LinkedList.class);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <K, V> Map<K, V> toMap(String json) {
        return JSON.parseObject(json, Map.class);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <K, V> Map<K, V> toMap(Object object) {
        return toMap(JSON.toJSONString(object));
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T getValue(String json, String field) {
        return (T) JSON.parseObject(json).get(field);
    }

    /**
     * Description inherited from parent class or interface.
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
