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
package org.miaixz.bus.fabric.registry;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Options;

/**
 * Immutable binding with optional options.
 *
 * @param key     binding key
 * @param value   binding value
 * @param options binding options
 * @param <T>     value type
 * @author Kimi Liu
 * @since Java 21+
 */
public record Binding<T>(String key, T value, Options options) {

    /**
     * Creates and validates a binding.
     *
     * @param key     binding key
     * @param value   binding value
     * @param options binding options
     */
    public Binding {
        key = validateKey(key);
        value = require(value, "Registry value");
        if (options == null) {
            options = Options.empty();
        }
    }

    /**
     * Creates a binding without options.
     *
     * @param key   binding key
     * @param value binding value
     * @param <T>   value type
     * @return binding
     */
    public static <T> Binding<T> of(final String key, final T value) {
        return new Binding<>(key, value, Options.empty());
    }

    /**
     * Returns a copy with a replaced option.
     *
     * @param key   option key
     * @param value option value
     * @return copied binding
     */
    public Binding<T> with(final String key, final Object value) {
        return new Binding<>(this.key, this.value, options.with(validateKey(key), require(value, "Option value")));
    }

    /**
     * Returns the binding key.
     *
     * @return binding key
     */
    @Override
    public String key() {
        return key;
    }

    /**
     * Returns the binding value.
     *
     * @return binding value
     */
    @Override
    public T value() {
        return value;
    }

    /**
     * Returns the binding options.
     *
     * @return binding options
     */
    @Override
    public Options options() {
        return options;
    }

    /**
     * Validates binding keys.
     *
     * @param key key
     * @return valid key
     */
    private static String validateKey(final String key) {
        if (StringKit.isBlank(key) || StringKit.containsAny(key, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Registry key must be non-blank and single-line");
        }
        return key.trim();
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
