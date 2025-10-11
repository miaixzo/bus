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

/**
 * Generic registry interface for managing and operating key-value pair data (e.g., routes, rate limiting
 * configurations).
 *
 * @param <T> The type of value stored in the registry.
 * @author Justubborn
 * @since Java 17+
 */
public interface Registry<T> {

    /**
     * Initializes the registry, loading initial data or configurations.
     */
    void init();

    /**
     * Adds a key-value pair to the registry.
     *
     * @param key The unique identifier key.
     * @param reg The object to be registered.
     * @return {@code true} if the addition was successful, {@code false} otherwise.
     */
    boolean add(String key, T reg);

    /**
     * Removes a record with the specified key from the registry.
     *
     * @param key The unique identifier key.
     * @return {@code true} if the removal was successful, {@code false} otherwise.
     */
    boolean remove(String key);

    /**
     * Modifies a key-value pair in the registry.
     *
     * @param key The unique identifier key.
     * @param reg The new value.
     * @return {@code true} if the modification was successful, {@code false} otherwise.
     */
    boolean amend(String key, T reg);

    /**
     * Refreshes the registry, reloading data or clearing and reinitializing.
     */
    void refresh();

    /**
     * Retrieves the value corresponding to the specified key.
     *
     * @param id The unique identifier key.
     * @return The corresponding value, or {@code null} if not found.
     */
    T get(String id);

}
