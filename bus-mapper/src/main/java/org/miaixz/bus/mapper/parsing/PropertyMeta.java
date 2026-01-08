/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * @since Java 17+
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
