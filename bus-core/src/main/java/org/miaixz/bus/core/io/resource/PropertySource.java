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
package org.miaixz.bus.core.io.resource;

import java.util.Properties;

import org.miaixz.bus.core.Binder;

/**
 * Interface for property sources, providing methods to access and manage properties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface PropertySource {

    /**
     * Retrieves the collection of properties.
     *
     * @return A {@link Properties} object containing all properties.
     */
    Properties props();

    /**
     * Retrieves the property value associated with the given key. If the value contains placeholders (e.g., ${key}),
     * they will be resolved.
     *
     * @param key The key of the property to retrieve.
     * @return The resolved property value, or {@code null} if the key is not found.
     */
    default String getProperty(String key) {
        String value = props().getProperty(key);
        if (null == value) {
            return null;
        }
        return getPlaceholderProperty(value);
    }

    /**
     * Retrieves the property value associated with the given key, returning a default value if the key is not found. If
     * the value contains placeholders (e.g., ${key}), they will be resolved.
     *
     * @param key          The key of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found.
     * @return The resolved property value, or the {@code defaultValue} if the key is not found.
     */
    default String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Resolves placeholders within a given string using the properties in this source. Placeholders are expected in the
     * format {@code ${key}}.
     *
     * @param placeholder The string potentially containing placeholders.
     * @return The string with all placeholders resolved.
     */
    default String getPlaceholderProperty(String placeholder) {
        return Binder.DEFAULT_HELPER.replacePlaceholders(placeholder, props());
    }

    /**
     * Checks if any property in this source starts with the given prefix.
     *
     * @param prefix The prefix to check for.
     * @return {@code true} if at least one property key starts with the prefix, {@code false} otherwise.
     */
    default boolean containPrefix(String prefix) {
        Properties properties = props();
        for (Object key : properties.keySet()) {
            if (key.toString().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
