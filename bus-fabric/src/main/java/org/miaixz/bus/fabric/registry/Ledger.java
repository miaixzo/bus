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
 * @param <T> type of value stored in each binding
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Ledger<T> {

    /**
     * Creates a concurrent registry ledger.
     *
     * @param <T> type of value stored in each binding
     * @return empty thread-safe in-memory ledger
     */
    static <T> Ledger<T> create() {
        return new DefaultLedger<>();
    }

    /**
     * Stores a binding.
     *
     * @param binding non-null binding to store or replace by key
     * @throws ValidateException if {@code binding} is {@code null}
     */
    void put(Binding<T> binding);

    /**
     * Reads a value.
     *
     * @param key non-blank, single-line binding key
     * @return bound value, or {@code null} when the key is absent
     * @throws ValidateException if the key is blank or contains a line break
     */
    T get(String key);

    /**
     * Reads a binding.
     *
     * @param key non-blank, single-line binding key
     * @return complete binding, or {@code null} when the key is absent
     * @throws ValidateException if the key is blank or contains a line break
     */
    Binding<T> binding(String key);

    /**
     * Removes a value.
     *
     * @param key non-blank, single-line binding key
     * @return value from the atomically removed binding, or {@code null} when absent
     * @throws ValidateException if the key is blank or contains a line break
     */
    T remove(String key);

    /**
     * Returns an immutable ledger snapshot.
     *
     * @return immutable copy of mappings observed while the snapshot is created
     */
    Map<String, Binding<T>> snapshot();

    /**
     * Returns the current size.
     *
     * @return current number of stored bindings
     */
    int size();

}

/**
 * Default concurrent registry ledger implementation.
 *
 * @param <T> type of value stored in each binding
 * @author Kimi Liu
 * @since Java 21+
 */
final class DefaultLedger<T> implements Ledger<T> {

    /**
     * Concurrent bindings indexed by validated key.
     */
    private final ConcurrentHashMap<String, Binding<T>> bindings = new ConcurrentHashMap<>();

    /**
     * Creates an empty concurrent ledger.
     */
    DefaultLedger() {
    }

    /**
     * Stores or replaces a binding by key.
     *
     * @param binding non-null validated binding that replaces any entry with the same key
     */
    @Override
    public void put(final Binding<T> binding) {
        final Binding<T> checked = require(binding, "Binding");
        bindings.put(checked.key(), checked);
    }

    /**
     * Returns the bound value for a key.
     *
     * @param key non-blank, single-line binding key to trim before lookup
     * @return bound value, or null when the trimmed key is absent
     */
    @Override
    public T get(final String key) {
        final Binding<T> binding = binding(key);
        return binding == null ? null : binding.value();
    }

    /**
     * Returns the full binding for a key.
     *
     * @param key non-blank, single-line binding key to trim before lookup
     * @return complete binding, or null when the trimmed key is absent
     */
    @Override
    public Binding<T> binding(final String key) {
        return bindings.get(validateKey(key));
    }

    /**
     * Removes a binding by key and returns its value.
     *
     * @param key non-blank, single-line binding key to trim before removal
     * @return value from the atomically removed binding, or null when absent
     */
    @Override
    public T remove(final String key) {
        final Binding<T> removed = bindings.remove(validateKey(key));
        return removed == null ? null : removed.value();
    }

    /**
     * Returns an immutable copy of bindings observed during concurrent-map traversal.
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
     * @param key binding key to validate and normalize
     * @return trimmed, non-blank, single-line key
     * @throws ValidateException if the key is blank or contains a line break
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
     * @param value reference to validate
     * @param name  logical reference name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
