/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serial;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

public class ApplicationSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852235962768L;

    private Long id;
    private Date createdAt;
    private Date updatedAt;
    private Map<String, Object> settings = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    @JsonIgnore
    public Object getSetting(Setting setting) {

        if (setting == null) {
            return (null);
        }

        String name = setting.toString();
        return (settings.get(name));
    }

    @JsonIgnore
    public Object getSetting(String setting) {

        if (setting == null) {
            return (null);
        }

        return (settings.get(setting));
    }

    public Object addSetting(String setting, Object value) {

        Setting appSetting = Setting.forValue(setting);
        if (appSetting != null) {
            return (addSetting(appSetting, value));
        }

        settings.put(setting, value);
        return (value);
    }

    public Object addSetting(Setting setting, Object value) {

        if (value instanceof JsonNode) {
            value = jsonNodeToValue((JsonNode) value, setting);
        }

        setting.validate(value);
        settings.put(setting.toString(), value);
        return (value);
    }

    public Object removeSetting(Setting setting) {
        return settings.remove(setting.toString());
    }

    public Object removeSetting(String setting) {
        return settings.remove(setting);
    }

    public void clearSettings() {
        settings.clear();
    }

    private Object jsonNodeToValue(JsonNode node, Setting setting) {

        Object value = node;
        if (node instanceof NullNode) {
            value = null;
        } else if (node instanceof TextNode) {
            value = node.asText();
        } else if (node instanceof BooleanNode) {
            value = node.asBoolean();
        } else if (node instanceof IntNode) {
            value = node.asInt();
        } else if (node instanceof FloatNode) {
            value = (float) node.asDouble();
        } else if (node instanceof DoubleNode) {
            value = (float) node.asDouble();
        } else if (node instanceof ArrayNode) {
            if (node.isEmpty()) {
                value = setting.emptyArrayValue();
            } else {
                List<Object> values = new ArrayList<>(node.size());
                node.forEach(element -> values.add(jsonNodeToValue(element, setting)));
                Class<?> type = values.get(0).getClass();
                value = Array.newInstance(type, values.size());
                for (int i = 0; i < values.size(); i++) {
                    Array.set(value, i, type.cast(values.get(i)));
                }
            }
        } else if (node instanceof ObjectNode) {
            ObjectMapper mapper = new ObjectMapper();
            value = mapper.convertValue(node, HashMap.class);
        }

        return (value);
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
