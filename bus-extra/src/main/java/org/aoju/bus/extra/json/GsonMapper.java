package org.aoju.bus.extra.json;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GsonMapper extends AbstractJsonMapper {

    @Override
    public Map toMap(String json) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<Map<Object, Object>>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    @Override
    public List toList(String json) {
        TypeToken<List<Object>> typeToken = new TypeToken<List<Object>>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    @Override
    public <T> List<T> toList(String json, Type type) {
        return gson.fromJson(json, type);
    }

    @Override
    public String toJsonString(Object object) {
        return gson.toJson(object);
    }

    @Override
    public String toJsonWithDateFormat(Object object, String dateFormatPattern) {
        gson = new GsonBuilder().setDateFormat(dateFormatPattern).create();
        return gson.toJson(object);
    }

    @Override
    public <T> T toPojo(String json, Class<T> valueType) {
        return gson.fromJson(json, valueType);
    }

    @Override
    public Map convertToMap(Object fromValue) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<Map<Object, Object>>() {
        };
        String json = gson.toJson(fromValue);
        return gson.fromJson(json, typeToken.getType());
    }

    @Override
    public <T> T convertFromMap(Map fromMap, Class<T> toValueType) {
        String json = gson.toJson(fromMap);
        return gson.fromJson(json, toValueType);
    }

}
