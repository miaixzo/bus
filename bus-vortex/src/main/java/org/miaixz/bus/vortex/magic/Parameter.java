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
package org.miaixz.bus.vortex.magic;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.vortex.Holder;

/**
 * Controlled parameter container that centralizes sanitization while preserving the familiar {@link Map} API.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Parameter extends AbstractMap<String, Object> {

    /**
     * Internal mutable storage for request parameters.
     */
    private final Map<String, Object> values = new LinkedHashMap<>();

    /**
     * Whether this container accepts only string values.
     */
    private final boolean stringOnly;

    /**
     * Cached unmodifiable string view for string-only containers.
     */
    private final Map<String, String> stringView;

    /**
     * Cached unmodifiable object view.
     */
    private final Map<String, Object> objectView;

    /**
     * Creates a parameter container that accepts general object values.
     */
    public Parameter() {
        this(false);
    }

    /**
     * Creates a parameter container with optional string-only storage semantics.
     *
     * @param stringOnly whether only string values should be retained
     */
    public Parameter(boolean stringOnly) {
        this.stringOnly = stringOnly;
        this.objectView = Collections.unmodifiableMap(this);
        this.stringView = stringOnly ? Collections.unmodifiableMap((Map<String, String>) (Map<?, ?>) this.values)
                : null;
    }

    /**
     * Adds a single value after applying the shared sanitization rules.
     *
     * @param key   The parameter key.
     * @param value The parameter value.
     */
    @Override
    public Object put(String key, Object value) {
        Object previous = this.values.get(key);
        if (key == null) {
            return previous;
        }
        Object sanitized = sanitizeValue(value);
        if (sanitized == null) {
            this.values.remove(key);
            return previous;
        }
        writeSanitized(key, sanitized);
        return previous;
    }

    /**
     * Adds all values from the given source map after applying the shared sanitization rules.
     *
     * @param source The source parameter map.
     */
    @Override
    public void putAll(Map<? extends String, ?> source) {
        ingest(source);
    }

    /**
     * Replaces the current contents with the sanitized values from the given source map.
     *
     * @param source The source parameter map.
     */
    public void replaceAll(Map<?, ?> source) {
        clear();
        ingest(source);
    }

    /**
     * Returns a read-only live view of the stored parameters.
     *
     * @return An unmodifiable parameter map view.
     */
    public Map<String, Object> asMap() {
        return this.objectView;
    }

    /**
     * Returns a read-only live string view for string-only containers.
     *
     * @return An unmodifiable string map view.
     */
    public Map<String, String> asStringMap() {
        if (!this.stringOnly) {
            return sanitizeQueryParameters(this.values);
        }
        return this.stringView;
    }

    /**
     * Returns a stored value without additional sanitization.
     *
     * @param key parameter key
     * @return stored value or {@code null}
     */
    @Override
    public Object get(Object key) {
        return this.values.get(key);
    }

    /**
     * Checks whether the backing store contains the given key.
     *
     * @param key parameter key
     * @return {@code true} when the key is present
     */
    @Override
    public boolean containsKey(Object key) {
        return this.values.containsKey(key);
    }

    /**
     * Removes a stored value by key.
     *
     * @param key parameter key
     * @return removed value or {@code null}
     */
    @Override
    public Object remove(Object key) {
        return this.values.remove(key);
    }

    /**
     * Clears all stored values from the container.
     */
    @Override
    public void clear() {
        this.values.clear();
    }

    /**
     * Adds values from untyped sources without allocating an intermediate sanitized map.
     *
     * @param source The source parameter map.
     */
    public void ingest(Map<?, ?> source) {
        if (source == null || source.isEmpty()) {
            return;
        }
        source.forEach((key, value) -> {
            if (key == null) {
                return;
            }
            String stringKey = String.valueOf(key);
            Object sanitized = sanitizeValue(value);
            if (sanitized == null) {
                this.values.remove(stringKey);
                return;
            }
            writeSanitized(stringKey, sanitized);
        });
    }

    /**
     * Returns a live entry-set view backed by the sanitized parameter store.
     *
     * @return mutable entry-set view
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new AbstractSet<>() {

            @Override
            public Iterator<Entry<String, Object>> iterator() {
                Iterator<String> iterator = values.keySet().iterator();
                return new Iterator<>() {

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Entry<String, Object> next() {
                        String key = iterator.next();
                        return new SimpleEntry<>(key, values.get(key)) {

                            @Override
                            public Object setValue(Object value) {
                                return Parameter.this.put(key, value);
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return values.size();
            }
        };
    }

    /**
     * Sanitizes a business parameter map.
     *
     * @param source The original parameter map.
     * @return A sanitized copy of the input map.
     */
    public static Map<String, Object> sanitizeParameters(Map<?, ?> source) {
        Parameter sanitized = new Parameter();
        sanitized.ingest(source);
        return new LinkedHashMap<>(sanitized.values);
    }

    /**
     * Sanitizes a query-parameter map and keeps only string values that remain valid after normalization.
     *
     * @param source The original query-parameter map.
     * @return A sanitized string-only copy.
     */
    public static Map<String, String> sanitizeQueryParameters(Map<?, ?> source) {
        Parameter sanitized = new Parameter(true);
        sanitized.ingest(source);
        return new LinkedHashMap<>(sanitized.asStringMap());
    }

    /**
     * Sanitizes a single value.
     *
     * @param value The original value.
     * @return The sanitized value, or {@code null} when the value should be removed.
     */
    public static Object sanitizeValue(Object value) {
        if (!Holder.isSanitizeNullLikeParameters()) {
            return value;
        }
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence sequence) {
            String text = sequence.toString();
            String normalized = text.trim();
            if ("null".equalsIgnoreCase(normalized) || "undefined".equalsIgnoreCase(normalized)) {
                return null;
            }
            return text;
        }
        if (value instanceof Map<?, ?> map) {
            return sanitizeParameters(map);
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> sanitized = new ArrayList<>();
            for (Object item : iterable) {
                Object candidate = sanitizeValue(item);
                if (candidate != null) {
                    sanitized.add(candidate);
                }
            }
            return sanitized;
        }
        if (value.getClass().isArray()) {
            List<Object> sanitized = new ArrayList<>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object candidate = sanitizeValue(Array.get(value, i));
                if (candidate != null) {
                    sanitized.add(candidate);
                }
            }
            return sanitized;
        }
        return value;
    }

    /**
     * Writes a value that has already passed sanitization into the backing map. For string-only containers, non-string
     * values are ignored to keep the stored representation aligned with the exposed string view.
     *
     * @param key       The parameter key.
     * @param sanitized The sanitized value to store.
     */
    private void writeSanitized(String key, Object sanitized) {
        if (sanitized == null) {
            return;
        }
        if (this.stringOnly) {
            if (sanitized instanceof String stringValue) {
                this.values.put(key, stringValue);
            }
            return;
        }
        this.values.put(key, sanitized);
    }

}
