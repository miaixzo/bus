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
package org.miaixz.bus.core.lang.pool;

import java.io.Closeable;
import java.io.Serializable;

/**
 * Interface for an object pool, providing core functionalities for managing pooled objects. This includes borrowing,
 * returning, and freeing objects.
 * <ul>
 * <li>{@link #borrowObject()} for borrowing an object from the pool.</li>
 * <li>{@link #returnObject(Object)} for returning an object to the pool.</li>
 * <li>{@link #free(Object)} for explicitly destroying an object.</li>
 * </ul>
 * Object maintenance within the pool is controlled by {@link PoolConfig#getMaxIdle()}. The rules are as follows:
 * <ul>
 * <li>If many objects are borrowed, the pool will continuously expand until it reaches
 * {@link PoolConfig#getMaxSize()}.</li>
 * <li>If a pooled object remains idle beyond {@link PoolConfig#getMaxIdle()}, it will be destroyed.</li>
 * <li>In practical use, the number of objects in the pool might be less than {@link PoolConfig#getMinSize()}.</li>
 * </ul>
 *
 * @param <T> the type of objects managed by this pool
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ObjectPool<T> extends Closeable, Serializable {

    /**
     * Borrows an object from the pool. The process involves:
     * <ol>
     * <li>Retrieving an object from the pool.</li>
     * <li>Checking the object's validity.</li>
     * <li>If no valid object is available, the pool expands and creates a new object.</li>
     * <li>Continues to retrieve an object.</li>
     * </ol>
     *
     * @return the borrowed object
     */
    T borrowObject();

    /**
     * Returns an object to the pool. The process involves:
     * <ol>
     * <li>Checking the object's validity.</li>
     * <li>If the object is invalid, it is destroyed.</li>
     * <li>If the object is valid, it is returned to the pool.</li>
     * </ol>
     *
     * @param object the object to be returned
     * @return this object pool instance
     */
    ObjectPool<T> returnObject(final T object);

    /**
     * Frees (destroys) an object. This method should be called if an object is found to be corrupted or unusable during
     * its active use.
     *
     * @param object the object to be freed/destroyed
     * @return this object pool instance
     */
    ObjectPool<T> free(final T object);

    /**
     * Retrieves the total number of objects held by the pool, including both idle and actively used objects.
     *
     * @return the total count of objects in the pool
     */
    int getTotal();

    /**
     * Retrieves the number of idle objects currently in the pool.
     *
     * @return the count of idle objects, or -1 if this information is not available
     */
    int getIdleCount();

    /**
     * Retrieves the number of objects currently borrowed and in active use.
     *
     * @return the count of actively used objects, or -1 if this information is not available
     */
    int getActiveCount();

}
