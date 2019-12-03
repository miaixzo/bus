/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.registry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.office.magic.Formats;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public class JsonFormatRegistry extends SimpleFormatRegistry {

    public JsonFormatRegistry(InputStream input) throws IOException {
        readJsonArray(IoUtils.toString(input, Charset.DEFAULT_UTF_8));
    }

    public JsonFormatRegistry(String source) {
        readJsonArray(source);
    }

    private void readJsonArray(String source) {
        JSONArray array = JSONArray.parseArray(source);
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonFormat = array.getJSONObject(i);
            Formats format = new Formats();
            format.setName(jsonFormat.getString("name"));
            format.setExtension(jsonFormat.getString("extension"));
            format.setMediaType(jsonFormat.getString("mediaType"));
            if (jsonFormat.containsKey("inputFamily")) {
                format.setInputFamily(Formats.Type.valueOf(jsonFormat.getString("inputFamily")));
            }
            if (jsonFormat.containsKey("loadProperties")) {
                format.setLoadProperties(toJavaMap(jsonFormat.getJSONObject("loadProperties")));
            }
            if (jsonFormat.containsKey("storePropertiesByFamily")) {
                JSONObject jsonStorePropertiesByFamily = jsonFormat.getJSONObject("storePropertiesByFamily");
                for (Map.Entry<String, ?> entry : jsonStorePropertiesByFamily.entrySet()) {
                    Map<String, ?> storeProperties = toJavaMap(jsonStorePropertiesByFamily.getJSONObject(entry.getKey()));
                    format.setStoreProperties(Formats.Type.valueOf(entry.getKey()), storeProperties);
                }
            }
            addFormat(format);
        }
    }

    private Map<String, ?> toJavaMap(JSONObject jsonMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Object value = jsonMap.get(entry.getKey());
            if (value instanceof JSONObject) {
                map.put(entry.getKey(), toJavaMap((JSONObject) value));
            } else {
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }

}
