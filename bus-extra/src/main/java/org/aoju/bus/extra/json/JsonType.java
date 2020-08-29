package org.aoju.bus.extra.json;

public enum JsonType {

    FAST_JSON("fastjson"),
    GSON("gson"),
    JACKSON("jackson");
    private String classType;

    JsonType() {
    }

    JsonType(String classType) {
        this.classType = classType;
    }

    public static JsonType getJsonType(String jsonName) {
        for (JsonType item : JsonType.values()) {
            if (FAST_JSON.classType.equals(jsonName)) {
                return FAST_JSON;
            }
            if (GSON.classType.equals(jsonName)) {
                return GSON;
            }
            if (JACKSON.classType.equals(jsonName)) {
                return JACKSON;
            }
        }
        return null;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

}
