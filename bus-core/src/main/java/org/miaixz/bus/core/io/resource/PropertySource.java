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
