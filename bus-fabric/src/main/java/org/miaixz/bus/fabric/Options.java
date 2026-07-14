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
package org.miaixz.bus.fabric;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable option snapshot with copy-on-write update methods.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Options {

    /**
     * Option key for the maximum bytes allowed when materializing a payload.
     */
    public static final String MATERIALIZE_MAX_BYTES = "materialize.maxBytes";

    /**
     * Default maximum bytes allowed when materializing a payload.
     */
    public static final long DEFAULT_MATERIALIZE_MAX_BYTES = Normal._64 * Normal.MEBI;

    /**
     * Immutable backing values.
     */
    private final Map<String, Object> values;

    /**
     * Creates an immutable option snapshot.
     *
     * @param values backing values
     */
    private Options(final Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    /**
     * Returns the empty options singleton.
     *
     * @return empty options
     */
    public static Options empty() {
        return Instances.get(Options.class.getName() + ".empty", () -> new Options(Map.of()));
    }

    /**
     * Creates options containing one key.
     *
     * @param key   option key
     * @param value option value
     * @return options
     */
    public static Options of(final String key, final Object value) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(validateKey(key), mask(value));
        return new Options(map);
    }

    /**
     * Creates options from a map snapshot.
     *
     * @param values source values
     * @return options
     */
    public static Options from(final Map<String, ?> values) {
        if (values == null) {
            throw new ValidateException("Options map must not be null");
        }
        if (values.isEmpty()) {
            return empty();
        }
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (final Map.Entry<String, ?> entry : values.entrySet()) {
            map.put(validateKey(entry.getKey()), mask(entry.getValue()));
        }
        return new Options(map);
    }

    /**
     * Reads an option using a target type.
     *
     * @param key  option key
     * @param type target type
     * @param <T>  target type
     * @return typed value or null
     */
    public <T> T get(final String key, final Class<T> type) {
        if (type == null) {
            throw new ValidateException("Option type must not be null");
        }
        final Object value = get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ValidateException("Option value type mismatch for " + key);
        }
        return type.cast(value);
    }

    /**
     * Reads an option value.
     *
     * @param key option key
     * @return value or null
     */
    public Object get(final String key) {
        return unmask(values.get(validateKey(key)));
    }

    /**
     * Checks whether an option key exists.
     *
     * @param key option key
     * @return true when present
     */
    public boolean contains(final String key) {
        return values.containsKey(validateKey(key));
    }

    /**
     * Returns options with a replaced value.
     *
     * @param key   option key
     * @param value option value
     * @return updated options
     */
    public Options with(final String key, final Object value) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>(values);
        map.put(validateKey(key), mask(value));
        return new Options(map);
    }

    /**
     * Returns options without a key.
     *
     * @param key option key
     * @return updated options
     */
    public Options without(final String key) {
        final String validKey = validateKey(key);
        if (!values.containsKey(validKey)) {
            return this;
        }
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>(values);
        map.remove(validKey);
        return map.isEmpty() ? empty() : new Options(map);
    }

    /**
     * Returns an immutable map snapshot.
     *
     * @return option map
     */
    public Map<String, Object> asMap() {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            map.put(entry.getKey(), unmask(entry.getValue()));
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the maximum bytes allowed when materializing a payload.
     *
     * @return materialize byte threshold
     */
    public long materializeMaxBytes() {
        final Object value = get(MATERIALIZE_MAX_BYTES);
        if (value == null) {
            return DEFAULT_MATERIALIZE_MAX_BYTES;
        }
        if (!(value instanceof Number number)) {
            throw new ValidateException("Materialize max bytes must be numeric");
        }
        return validateMaterializeMaxBytes(number.longValue());
    }

    /**
     * Returns options with a replacement payload materialize threshold.
     *
     * @param bytes materialize byte threshold
     * @return updated options
     */
    public Options materializeMaxBytes(final long bytes) {
        return with(MATERIALIZE_MAX_BYTES, validateMaterializeMaxBytes(bytes));
    }

    /**
     * Converts null values to the sentinel.
     *
     * @param value source value
     * @return stored value
     */
    private static Object mask(final Object value) {
        return value == null ? nullValue() : value;
    }

    /**
     * Restores a sentinel value to null.
     *
     * @param value stored value
     * @return public value
     */
    private static Object unmask(final Object value) {
        return value == nullValue() ? null : value;
    }

    /**
     * Validates a materialize threshold.
     *
     * @param bytes threshold bytes
     * @return validated threshold
     */
    private static long validateMaterializeMaxBytes(final long bytes) {
        if (bytes <= 0) {
            throw new ValidateException("Materialize max bytes must be positive");
        }
        return bytes;
    }

    /**
     * Returns the shared sentinel used to preserve explicit null values.
     *
     * @return null sentinel
     */
    private static Object nullValue() {
        return Instances.get(Options.class.getName() + ".null", Object::new);
    }

    /**
     * Validates an option key.
     *
     * @param key option key
     * @return validated key
     */
    private static String validateKey(final String key) {
        if (StringKit.isBlank(key) || StringKit.containsAny(key, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Option key must be non-blank and single-line");
        }
        return key;
    }

}
