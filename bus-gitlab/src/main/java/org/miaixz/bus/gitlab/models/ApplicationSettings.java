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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.support.JacksonJson;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.*;

/**
 * The application settings class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ApplicationSettings implements Serializable {

    /**
     * Constructs a new {@code ApplicationSettings} instance.
     */
    public ApplicationSettings() {
        // No initialization required.
    }

    @Serial
    private static final long serialVersionUID = 2852235962768L;

    private Long id;
    private Date createdAt;
    private Date updatedAt;
    private Map<String, Object> settings = new HashMap<>();

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the updated at.
     *
     * @return the result
     */

    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the updated at.
     *
     * @param updatedAt the updated at value
     */

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the settings.
     *
     * @return the result
     */

    public Map<String, Object> getSettings() {
        return settings;
    }

    /**
     * Sets the settings.
     *
     * @param settings the settings value
     */

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    /**
     * Returns the setting.
     *
     * @param setting the setting value
     * @return the result
     */

    @JsonIgnore
    public Object getSetting(Setting setting) {

        if (setting == null) {
            return (null);
        }

        String name = setting.toString();
        return (settings.get(name));
    }

    /**
     * Returns the setting.
     *
     * @param setting the setting value
     * @return the result
     */

    @JsonIgnore
    public Object getSetting(String setting) {

        if (setting == null) {
            return (null);
        }

        return (settings.get(setting));
    }

    /**
     * Adds the setting.
     *
     * @param setting the setting value
     * @param value   the value value
     * @return the result
     */
    public Object addSetting(String setting, Object value) {

        Setting appSetting = Setting.forValue(setting);
        if (appSetting != null) {
            return (addSetting(appSetting, value));
        }

        settings.put(setting, value);
        return (value);
    }

    /**
     * Adds the setting.
     *
     * @param setting the setting value
     * @param value   the value value
     * @return the result
     */
    public Object addSetting(Setting setting, Object value) {

        if (value instanceof JsonNode) {
            value = jsonNodeToValue((JsonNode) value, setting);
        }

        setting.validate(value);
        settings.put(setting.toString(), value);
        return (value);
    }

    /**
     * Removes the setting.
     *
     * @param setting the setting value
     * @return the result
     */

    public Object removeSetting(Setting setting) {
        return settings.remove(setting.toString());
    }

    /**
     * Removes the setting.
     *
     * @param setting the setting value
     * @return the result
     */

    public Object removeSetting(String setting) {
        return settings.remove(setting);
    }

    /**
     * Clears the settings.
     */

    public void clearSettings() {
        settings.clear();
    }

    private Object jsonNodeToValue(JsonNode node, Setting setting) {

        Object value = node;
        if (node instanceof NullNode) {
            value = null;
        } else if (node instanceof StringNode) {
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

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
