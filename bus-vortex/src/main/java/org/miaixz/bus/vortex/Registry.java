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
package org.miaixz.bus.vortex;

import java.util.Collection;

/**
 * A generic contract for an in-memory registry that stores and manages objects of type {@code T}.
 * <p>
 * This interface defines the basic CRUD (Create, Read, Update, Delete) operations for a thread-safe, key-value based
 * registry. Concrete implementations, like {@link org.miaixz.bus.vortex.registry.AbstractRegistry}, provide the
 * underlying storage mechanism.
 *
 * @param <T> The type of objects to be stored in the registry.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Registry<T> {

    /**
     * A hook for subclasses to perform initialization logic, typically called after the registry is constructed or
     * refreshed.
     */
    default void init() {

    }

    /**
     * Registers an item in the registry using a key derived from the item itself by the registry's key generation
     * strategy.
     *
     * @param item The item to register.
     */
    void register(T item);

    /**
     * Registers an item in the registry with a specific key.
     *
     * @param key  The unique key to associate with the item.
     * @param item The item to register.
     */
    void register(String key, T item);

    /**
     * Removes an item from the registry using its key.
     *
     * @param key The key of the item to remove.
     */
    void destroy(String key);

    /**
     * Removes an item from the registry using a key derived from the item itself.
     *
     * @param item The item to remove.
     */
    void destroy(T item);

    /**
     * Updates an item in the registry using a key derived from the item itself. If the item does not exist, it will be
     * registered.
     *
     * @param item The item to update.
     */
    void update(T item);

    /**
     * Updates an item in the registry for a specific key. If the key does not exist, a new entry will be created.
     *
     * @param key  The key of the item to update.
     * @param item The new item.
     */
    void update(String key, T item);

    /**
     * Clears all items from the registry and re-initializes it by calling {@link #init()}.
     */
    void refresh();

    /**
     * Retrieves an item from the registry by its key.
     *
     * @param key The key of the item to retrieve.
     * @return The item associated with the key, or {@code null} if not found.
     */
    T get(String key);

    /**
     * Retrieves a collection of all items currently in the registry.
     *
     * @return An unmodifiable {@link Collection} of all registered items.
     */
    Collection<T> getAll();

}
