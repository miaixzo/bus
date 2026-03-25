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
package org.miaixz.bus.core;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Manages configuration properties by providing methods to load, retrieve, and group them. This class supports a
 * singleton pattern via {@link #INSTANCE} for global access, but also allows for the creation of separate instances.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Context extends Keys {

    /**
     * The global, statically initialized unique instance, ensuring a singleton pattern for easy access.
     */
    public static final Context INSTANCE = new Context();

    /**
     * The underlying {@link Properties} object that stores the configuration key-value pairs.
     */
    public final Properties delegate = new Properties();

    /**
     * Default constructor for creating a new, empty context.
     */
    public Context() {
        // No-op
    }

    /**
     * Constructs a new context and initializes it with the given properties.
     *
     * @param properties The initial set of properties to load.
     */
    public Context(Properties properties) {
        this.delegate.putAll(properties);
    }

    /**
     * Static factory method to create a new {@code Context} instance from a {@link Properties} object.
     *
     * @param properties The properties to initialize the new context with.
     * @return A new {@code Context} instance.
     */
    public static Context newInstance(Properties properties) {
        return new Context(properties);
    }

    /**
     * Retrieves a set of all property keys contained in this context.
     *
     * @return A {@link Set} of all property keys.
     */
    public Set<String> keys() {
        return delegate.stringPropertyNames();
    }

    /**
     * Merges all properties from the given {@link Properties} object into the current context. Existing properties with
     * the same key will be overwritten.
     *
     * @param properties The properties to merge into this context.
     */
    public void putAll(Properties properties) {
        this.delegate.putAll(properties);
    }

    /**
     * Retrieves the property value for the specified key.
     *
     * @param key The key of the property to retrieve.
     * @return The property value as a {@code String}, or {@code null} if the key is not found.
     */
    public String getProperty(String key) {
        return delegate.getProperty(key);
    }

    /**
     * Retrieves the property value for the specified key, returning a default value if the key is not found.
     *
     * @param key          The key of the property to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The property value, or the default value if the key is not present.
     */
    public String getProperty(String key, String defaultValue) {
        return this.delegate.getProperty(key, defaultValue);
    }

    /**
     * Retrieves the property value for the specified key and converts it to an integer.
     *
     * @param key          The key of the property to retrieve.
     * @param defaultValue The default value to return if the key is not found or the value cannot be parsed as an
     *                     integer.
     * @return The integer property value, or the default value on failure.
     */
    public int getProperty(String key, int defaultValue) {
        return Convert.toInt(getProperty(key), defaultValue);
    }

    /**
     * Retrieves the property value for the specified key and converts it to a boolean.
     *
     * @param key          The key of the property to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The boolean property value, or the default value if the key is not present.
     */
    public boolean getProperty(String key, boolean defaultValue) {
        final String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return BooleanKit.toBoolean(value);
    }

    /**
     * Groups properties based on a shared prefix. This method is designed for a specific hierarchical structure where a
     * top-level key points to a group name, and other keys are prefixed with that group name.
     *
     * @param group The common prefix used for grouping properties.
     * @return A {@link Map} where each key is a group identifier and the value is a {@link Properties} object
     *         containing all properties belonging to that group. Returns an empty map if no matching properties are
     *         found.
     */
    public Map<String, Properties> group(String group) {
        final Set<String> keys = keys();
        // Filter for keys that start with the specified group prefix.
        Set<String> inner = keys.stream().filter(i -> i.startsWith(group)).collect(Collectors.toSet());
        if (CollKit.isEmpty(inner)) {
            return Collections.emptyMap();
        }
        Map<String, Properties> map = MapKit.newHashMap();
        inner.forEach(i -> {
            Properties p = new Properties();
            // Extract the sub-key part that follows the group prefix.
            String key = i.substring(group.length()) + Symbol.COLON;
            int keyIndex = key.length();
            // Filter and store properties that start with the derived sub-key.
            keys.stream().filter(j -> j.startsWith(key))
                    .forEach(j -> p.setProperty(j.substring(keyIndex), delegate.getProperty(j)));
            map.put(delegate.getProperty(i), p);
        });

        return map;
    }

    /**
     * Executes a consumer if the property for the given key is not blank. This provides a fluent way to handle
     * property-dependent logic.
     *
     * @param key      The property key to check.
     * @param consumer The {@link Consumer} to execute with the property value if it is not blank.
     * @return The current {@code Context} instance for chaining.
     */
    public Context whenNotBlank(String key, Consumer<String> consumer) {
        String value = delegate.getProperty(key);
        if (StringKit.isNotBlank(value)) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * Applies a function and executes a consumer if the property for the given key is not blank. This allows for
     * transforming the property value before it is consumed.
     *
     * @param key      The property key to check.
     * @param function A {@link Function} to transform the property value.
     * @param consumer The {@link Consumer} to execute with the transformed value.
     * @param <T>      The type of the transformed value.
     * @return The current {@code Context} instance for chaining.
     */
    public <T> Context whenNotBlank(String key, Function<String, T> function, Consumer<T> consumer) {
        String value = delegate.getProperty(key);
        if (StringKit.isNotBlank(value)) {
            consumer.accept(function.apply(value));
        }
        return this;
    }

}
