/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.extra.json.provider;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Gson 解析器
 *
 * @author Kimi Liu
 * @version 6.1.1
 * @since JDK 1.8+
 */
public class GsonProvider extends AbstractJsonProvider {

    public static Gson gson;

    /**
     * 构造
     */
    public GsonProvider() {
        gson = new GsonBuilder()
                .setLenient()
                // 解决gson序列化时出现整型变为浮点型的问题
                .registerTypeAdapter(new TypeToken<Map<Object, Object>>() {
                        }.getType(), (JsonDeserializer<Map<Object, Object>>) (jsonElement, type, jsonDeserializationContext) -> {
                            Map<Object, Object> map = new LinkedHashMap<>();
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                Object obj = entry.getValue();
                                if (obj instanceof JsonPrimitive) {
                                    map.put(entry.getKey(), ((JsonPrimitive) obj).getAsString());
                                } else {
                                    map.put(entry.getKey(), obj);
                                }
                            }
                            return map;
                        }
                )
                .registerTypeAdapter(new TypeToken<List<Object>>() {
                        }.getType(), (JsonDeserializer<List<Object>>) (jsonElement, type, jsonDeserializationContext) -> {
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
                        }
                ).create();
    }

    @Override
    public String toJsonString(Object object) {
        return gson.toJson(object);
    }

    @Override
    public String toJsonString(Object object, String format) {
        gson = new GsonBuilder().setDateFormat(format).create();
        return gson.toJson(object);
    }

    @Override
    public <T> T toPojo(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    @Override
    public <T> T toPojo(Map map, Class<T> clazz) {
        return gson.fromJson(gson.toJson(map), clazz);
    }

    @Override
    public List toList(String json) {
        TypeToken<List<Object>> typeToken = new TypeToken<List<Object>>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    @Override
    public <T> List<T> toList(String json, Class<T> clazz) {
        return gson.fromJson(json, (Type) clazz);
    }

    @Override
    public <T> List<T> toList(String json, Type type) {
        return gson.fromJson(json, type);
    }

    @Override
    public Map toMap(String json) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<Map<Object, Object>>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    @Override
    public Map toMap(Object object) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<Map<Object, Object>>() {
        };
        return gson.fromJson(gson.toJson(object), typeToken.getType());
    }

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
