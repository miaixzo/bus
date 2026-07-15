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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Thread-safe registry ledger contract.
 *
 * @param <T> value type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Ledger<T> {

    /**
     * Creates a concurrent registry ledger.
     *
     * @param <T> value type
     * @return registry ledger
     */
    static <T> Ledger<T> create() {
        return new DefaultLedger<>();
    }

    /**
     * Stores a binding.
     *
     * @param binding binding
     */
    void put(Binding<T> binding);

    /**
     * Reads a value.
     *
     * @param key key
     * @return value or null
     */
    T get(String key);

    /**
     * Reads a binding.
     *
     * @param key key
     * @return binding or null
     */
    Binding<T> binding(String key);

    /**
     * Removes a value.
     *
     * @param key key
     * @return removed value or null
     */
    T remove(String key);

    /**
     * Returns an immutable ledger snapshot.
     *
     * @return snapshot
     */
    Map<String, Binding<T>> snapshot();

    /**
     * Returns the current size.
     *
     * @return size
     */
    int size();

}

/**
 * Default concurrent registry ledger implementation.
 *
 * @param <T> value type
 * @author Kimi Liu
 * @since Java 21+
 */
final class DefaultLedger<T> implements Ledger<T> {

    /**
     * Stored bindings.
     */
    private final ConcurrentHashMap<String, Binding<T>> bindings = new ConcurrentHashMap<>();

    /**
     * Stores or replaces a binding by key.
     *
     * @param binding binding
     */
    @Override
    public void put(final Binding<T> binding) {
        final Binding<T> checked = require(binding, "Binding");
        bindings.put(checked.key(), checked);
    }

    /**
     * Returns the bound value for a key.
     *
     * @param key binding key
     * @return value, or null when absent
     */
    @Override
    public T get(final String key) {
        final Binding<T> binding = binding(key);
        return binding == null ? null : binding.value();
    }

    /**
     * Returns the full binding for a key.
     *
     * @param key binding key
     * @return binding, or null when absent
     */
    @Override
    public Binding<T> binding(final String key) {
        return bindings.get(validateKey(key));
    }

    /**
     * Removes a binding by key and returns its value.
     *
     * @param key binding key
     * @return removed value, or null when absent
     */
    @Override
    public T remove(final String key) {
        final Binding<T> removed = bindings.remove(validateKey(key));
        return removed == null ? null : removed.value();
    }

    /**
     * Returns an immutable snapshot of all bindings.
     *
     * @return binding snapshot
     */
    @Override
    public Map<String, Binding<T>> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(bindings));
    }

    /**
     * Returns the number of stored bindings.
     *
     * @return binding count
     */
    @Override
    public int size() {
        return bindings.size();
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
