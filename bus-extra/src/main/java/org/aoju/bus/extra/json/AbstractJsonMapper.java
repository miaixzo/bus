package org.aoju.bus.extra.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.aoju.bus.logger.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class AbstractJsonMapper implements JsonMapper {

    private static final String CLASS_TYPE_JACKSON = "com.fasterxml.jackson.databind.ObjectMapper";
    private static final String CLASS_TYPE_GSON = "com.google.gson.Gson";
    private static final String CLASS_TYPE_FASTJSON = "com.alibaba.fastjson.JSON";
    public static String JSON_CLASS_TYPE = "extend.json.type";

    public static String classType;
    public static JsonType jsonType;

    public static ObjectMapper objectMapper;
    public static Gson gson;

    static {
        try {
            String jsonClassType = null;
            Properties properties = new Properties();
            InputStream in = AbstractJsonMapper.class.getClassLoader().getResourceAsStream("application.properties");
            if (in != null) {
                properties.load(in);
                jsonClassType = properties.getProperty(JSON_CLASS_TYPE);
            } else {
                in = AbstractJsonMapper.class.getClassLoader().getResourceAsStream("application.yml");
                if (in != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> propsMap = yaml.loadAs(in, LinkedHashMap.class);
                    propsMap = (Map<String, Object>) propsMap.get("extend.json");
                    if (propsMap != null) {
                        jsonClassType = String.valueOf(propsMap.get("type"));
                    } else {
                        Logger.warn("application.yml extend.json.type");
                    }
                }
            }

            if (jsonClassType != null && jsonClassType.trim().length() > 0) {
                classType = jsonClassType;
            }

            if (classType != null && classType.length() > 0) {
                if (Class.forName(CLASS_TYPE_JACKSON) != null
                        && classType.equals(JsonType.JACKSON.getClassType())) {
                    Logger.info("Found jackson lib");
                } else if (Class.forName(CLASS_TYPE_GSON) != null
                        && classType.equals(JsonType.GSON.getClassType())) {
                    Logger.info("Found gson lib");
                } else if (Class.forName(CLASS_TYPE_FASTJSON) != null
                        && classType.equals(JsonType.FAST_JSON.getClassType())) {
                    Logger.info("Found fastjson lib");
                } else {
                    throw new RuntimeException("Jackson, Gson, or FastJSON were not found");
                }
            } else if (Class.forName(CLASS_TYPE_JACKSON) != null) {
                Logger.info("Found jackson lib");
                classType = JsonType.JACKSON.getClassType();
            } else if (Class.forName(CLASS_TYPE_GSON) != null) {
                Logger.info("Found gson lib");
                classType = JsonType.GSON.getClassType();
            } else if (Class.forName(CLASS_TYPE_FASTJSON) != null) {
                Logger.info("Found fastjson lib");
                classType = JsonType.FAST_JSON.getClassType();
            } else {
                throw new RuntimeException("Jackson, Gson, or FastJSON were not found");
            }

            // 禁止时间格式序列化为时间戳
            if (objectMapper == null) {
                objectMapper = new ObjectMapper()
                        .findAndRegisterModules()
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }
            jsonType = JsonType.getJsonType(classType);
            if (gson == null) {
                gson = new GsonBuilder()
                        .setLenient()
                        // 解决gson序列化时出现整型变为浮点型的问题
                        .registerTypeAdapter(new TypeToken<Map<Object, Object>>() {
                                }
                                        .getType(), (JsonDeserializer<Map<Object, Object>>) (jsonElement, type, jsonDeserializationContext) -> {
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
                                }
                                        .getType(), (JsonDeserializer<List<Object>>) (jsonElement, type, jsonDeserializationContext) -> {
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
                        )
                        .create();
            }
        } catch (ClassNotFoundException | IOException e) {
            Logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * json序列化实现类
     * 依次按照顺序检查json库的jar是否被引入,如果未引入任何json库，则抛出异常
     *
     * @return json序列化实现类
     * @see JacksonJsonMapper
     * @see GsonMapper
     * @see FastJsonMapper
     */
    public static AbstractJsonMapper doCreate() {
        for (JsonType item : JsonType.values()) {
            if (JsonType.FAST_JSON.getClassType().equals(item.getClassType())) {
                return new JacksonJsonMapper();
            }
            if (JsonType.GSON.getClassType().equals(item.getClassType())) {
                return new GsonMapper();
            }
            if (JsonType.JACKSON.getClassType().equals(item.getClassType())) {
                return new FastJsonMapper();
            }
        }
        return null;
    }

}
