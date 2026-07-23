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
 * Immutable registry binding with a non-null option snapshot.
 *
 * @param key     trimmed, non-blank, single-line binding key
 * @param value   non-null bound value
 * @param options immutable binding options; {@code null} is normalized to {@link Options#empty()}
 * @param <T>     bound value type
 * @author Kimi Liu
 * @since Java 21+
 */
public record Binding<T>(String key, T value, Options options) {

    /**
     * Creates and validates a binding.
     *
     * @param key     non-blank, single-line binding key
     * @param value   non-null value associated with the key
     * @param options option snapshot, or {@code null} to use an empty snapshot
     * @throws ValidateException if {@code key} is blank or multi-line, or {@code value} is {@code null}
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
     * @param key   non-blank, single-line binding key
     * @param value non-null value associated with the key
     * @param <T>   bound value type
     * @return binding with an empty option snapshot
     * @throws ValidateException if {@code key} is blank or multi-line, or {@code value} is {@code null}
     */
    public static <T> Binding<T> of(final String key, final T value) {
        return new Binding<>(key, value, Options.empty());
    }

    /**
     * Returns a copy whose option snapshot contains a replaced or added entry.
     *
     * @param key   non-blank, single-line option key
     * @param value non-null option value
     * @return new binding retaining this binding key and value with the updated options
     * @throws ValidateException if {@code key} is blank or multi-line, or {@code value} is {@code null}
     */
    public Binding<T> with(final String key, final Object value) {
        return new Binding<>(this.key, this.value, options.with(validateKey(key), require(value, "Option value")));
    }

    /**
     * Returns the binding key.
     *
     * @return trimmed, non-blank, single-line registry key
     */
    @Override
    public String key() {
        return key;
    }

    /**
     * Returns the binding value.
     *
     * @return non-null value associated with this binding
     */
    @Override
    public T value() {
        return value;
    }

    /**
     * Returns the binding options.
     *
     * @return non-null immutable option snapshot
     */
    @Override
    public Options options() {
        return options;
    }

    /**
     * Validates binding keys.
     *
     * @param key candidate binding or option key
     * @return trimmed, non-blank, single-line key
     * @throws ValidateException if {@code key} is blank or contains a carriage return or line feed
     */
    private static String validateKey(final String key) {
        if (StringKit.isBlank(key) || StringKit.containsAny(key, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Registry key must be non-blank and single-line");
        }
        return key.trim();
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
