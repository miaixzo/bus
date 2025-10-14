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
package org.miaixz.bus.vortex.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.vortex.Registry;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract registry class, providing generic registry functionality for managing and storing key-value pair data.
 *
 * @param <T> The type of value stored in the registry.
 * @author Justubborn
 * @since Java 17+
 */
public abstract class AbstractRegistry<T> implements Registry<T>, InitializingBean {

    /**
     * A thread-safe cache, used to store key-value pair data.
     */
    private final Map<String, T> cache = new ConcurrentHashMap<>();

    /**
     * The key generation strategy.
     */
    protected RegistryKey<T> registryKey;

    /**
     * Sets the key generation strategy.
     *
     * @param registryKey The key generation strategy.
     */
    protected void setKeyGenerator(RegistryKey<T> registryKey) {
        this.registryKey = registryKey;
    }

    /**
     * Adds a key-value pair to the registry.
     *
     * @param key The key.
     * @param reg The value to register.
     * @return {@code true} if the key did not previously exist and the addition was successful, {@code false}
     *         otherwise.
     */
    @Override
    public boolean add(String key, T reg) {
        if (null != cache.get(key)) {
            return false;
        }
        cache.put(key, reg);
        return true;
    }

    /**
     * Adds an object to the registry, using the key generation strategy to generate the key.
     *
     * @param item The object to add.
     * @return {@code true} if the addition was successful, {@code false} otherwise.
     * @throws IllegalStateException if the key generator is not set.
     */
    public boolean add(T item) {
        if (registryKey == null) {
            throw new IllegalStateException("Key generator not set");
        }
        return add(registryKey.keys(item), item);
    }

    /**
     * Removes a record with the specified key from the registry.
     *
     * @param id The key.
     * @return {@code true} if the removal was successful, {@code false} otherwise.
     */
    public boolean remove(String id) {
        return null != this.cache.remove(id);
    }

    /**
     * Modifies a key-value pair in the registry by first removing and then adding it.
     *
     * @param key The key.
     * @param reg The new value.
     * @return {@code true} if the modification was successful, {@code false} otherwise.
     */
    public boolean amend(String key, T reg) {
        cache.remove(key);
        return add(key, reg);
    }

    /**
     * Updates an object in the registry, using the key generation strategy to generate the key.
     *
     * @param item The object to update.
     * @return {@code true} if the update was successful, {@code false} otherwise.
     * @throws IllegalStateException if the key generator is not set.
     */
    public boolean amend(T item) {
        if (registryKey == null) {
            throw new IllegalStateException("Key generator not set");
        }
        return amend(registryKey.keys(item), item);
    }

    /**
     * Refreshes the registry by clearing the cache and reinitializing it.
     */
    public void refresh() {
        cache.clear();
        init();
    }

    /**
     * Retrieves the value corresponding to the specified key.
     *
     * @param key The key.
     * @return The corresponding value, or {@code null} if not found.
     */
    public T get(String key) {
        return cache.get(key);
    }

    /**
     * Spring initialization callback, invoked after bean properties are set, triggering a registry refresh.
     */
    @Override
    public void afterPropertiesSet() {
        refresh();
    }

    /**
     * Interface for key generation strategy.
     *
     * @param <T> The type of object.
     */
    @FunctionalInterface
    public interface RegistryKey<T> {

        /**
         * Generates a key based on the given object.
         *
         * @param item The object.
         * @return The generated key.
         */
        String keys(T item);
    }

}
