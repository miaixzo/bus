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
package org.miaixz.bus.mapper.parsing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.ObjectKit;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A base class for property mapping, providing storage and manipulation of properties.
 *
 * @param <T> The subtype, for supporting fluent-style chaining.
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class PropertyMeta<T extends PropertyMeta> {

    /**
     * Additional properties for extension.
     */
    protected Map<String, String> props;

    /**
     * Gets a property value.
     *
     * @param key The property name.
     * @return The property value, or `null` if it does not exist.
     */
    public String getProp(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        String val = props != null ? props.get(key) : null;
        // If the configured value does not exist, get it from the global configuration.
        if (val == null) {
            val = Context.INSTANCE.getProperty(key);
        }
        return val;
    }

    /**
     * Gets a property value, with support for a default value.
     *
     * @param key          The property name.
     * @param defaultValue The default value.
     * @return The property value, or the default value if it does not exist.
     */
    public String getProp(String key, String defaultValue) {
        return ObjectKit.defaultIfNull(getProp(key), defaultValue);
    }

    /**
     * Gets an integer property value.
     *
     * @param prop The property name.
     * @return The integer property value, or `null` if it does not exist or cannot be parsed.
     */
    public Integer getInt(String prop) {
        return Convert.toInt(getProp(prop));
    }

    /**
     * Gets an integer property value, with support for a default value.
     *
     * @param key          The property name.
     * @param defaultValue The default value.
     * @return The integer property value, or the default value if it does not exist or cannot be parsed.
     */
    public Integer getInt(String key, Integer defaultValue) {
        return Convert.toInt(getInt(key), defaultValue);
    }

    /**
     * Gets a boolean property value.
     *
     * @param key The property name.
     * @return The boolean property value, or `null` if it does not exist.
     */
    public Boolean getBoolean(String key) {
        return BooleanKit.toBoolean(getProp(key));
    }

    /**
     * Gets a boolean property value, with support for a default value.
     *
     * @param key          The property name.
     * @param defaultValue The default value.
     * @return The boolean property value, or the default value if it does not exist.
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        final String value = getProp(key);
        if (value == null) {
            return defaultValue;
        }
        return BooleanKit.toBoolean(value);
    }

    /**
     * Sets a property value.
     *
     * @param prop  The property name.
     * @param value The property value.
     * @return The current instance for fluent-style chaining.
     */
    public T put(String prop, String value) {
        if (this.props == null) {
            synchronized (this) {
                if (this.props == null) {
                    this.props = new ConcurrentHashMap<>();
                }
            }
        }
        this.props.put(prop, value);
        return (T) this;
    }

    /**
     * Sets multiple property values, appending them to the existing property set.
     *
     * @param props The map of properties.
     * @return The current instance for fluent-style chaining.
     */
    public T put(Map<String, String> props) {
        if (props != null && !props.isEmpty()) {
            for (Map.Entry<String, String> entry : props.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
        return (T) this;
    }

    /**
     * Removes a specified property.
     *
     * @param prop The property name.
     * @return The removed property value, or `null` if it did not exist.
     */
    public String remove(String prop) {
        if (props != null) {
            String value = getProp(prop);
            props.remove(prop);
            return value;
        }
        return null;
    }

}
