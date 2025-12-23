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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.ValueFilter;

/**
 * A {@link org.miaixz.bus.extra.json.JsonProvider} implementation based on Alibaba's Fastjson2 library. This class
 * provides JSON serialization and deserialization functionalities using Fastjson2.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastJsonProvider extends AbstractJsonProvider {

    /**
     * Default writer features for Fastjson serialization, including field-based serialization and writing null values.
     */
    private static final JSONWriter.Feature[] WRITER_FEATURES = { JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty,
            JSONWriter.Feature.BrowserCompatible, JSONWriter.Feature.WriteNulls };

    /**
     * Default filters for Fastjson serialization, which convert null, empty, or blank string values to null.
     */
    private static final Filter[] FILTERS = { (ValueFilter) (object, name, value) -> value };

    /**
     * Constructs a new {@code FastJsonProvider} instance.
     */
    public FastJsonProvider() {

    }

    @Override
    public String toJsonString(Object object) {
        return JSON.toJSONString(object, FILTERS, WRITER_FEATURES);
    }

    @Override
    public String toJsonString(Object object, String format) {
        return JSON.toJSONString(object, format, FILTERS, WRITER_FEATURES);
    }

    @Override
    public <T> T toPojo(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    @Override
    public <T> T toPojo(Map map, Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }

    @Override
    public <T> List<T> toList(String json) {
        return JSON.parseObject(json, LinkedList.class);
    }

    @Override
    public <T> List<T> toList(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    @Override
    public <T> List<T> toList(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    @Override
    public <K, V> Map<K, V> toMap(String json) {
        return JSON.parseObject(json, Map.class);
    }

    @Override
    public <K, V> Map<K, V> toMap(Object object) {
        return toMap(JSON.toJSONString(object));
    }

    @Override
    public <T> T getValue(String json, String field) {
        return (T) JSON.parseObject(json).get(field);
    }

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
