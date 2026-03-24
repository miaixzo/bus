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
package org.miaixz.bus.core.instance;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * Interface for instantiating objects. 1. Classes that use this interface must have a no-argument constructor. 2. This
 * class is currently in the testing phase.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Instance {

    /**
     * Gets the singleton object of a class. 1. The thread safety of the object needs to be guaranteed. 2. Only objects
     * returned in the same group will be singletons; otherwise, a new instance is returned.
     *
     * @param <T>       The generic type.
     * @param clazz     The class type.
     * @param groupName The group name.
     * @return The instantiated object.
     */
    <T> T singleton(final Class<T> clazz, final String groupName);

    /**
     * Gets the singleton object of a class. 1. The thread safety of the object needs to be guaranteed.
     *
     * @param <T>   The generic type.
     * @param clazz The class type.
     * @return The instantiated object.
     */
    <T> T singleton(final Class<T> clazz);

    /**
     * Gets a unique instantiated object within each thread. Note: Scenarios where memory leaks may occur: (1) As long
     * as the thread object is garbage collected, there will be no memory leak. However, during the time between setting
     * the ThreadLocal to null and the thread ending, it will not be collected, which is what we consider a memory leak.
     * The most critical situation is when the thread object is not collected, which leads to a real memory leak. For
     * example, when using a thread pool, threads are not destroyed when they finish but are reused, which can lead to
     * memory leaks. Reference: https://www.cnblogs.com/onlywujun/p/3524675.html
     *
     * @param <T>   The generic type.
     * @param clazz The class type.
     * @return The instantiated object.
     * @see java.lang.ref.WeakReference
     */
    <T> T threadLocal(final Class<T> clazz);

    /**
     * Gets a new instance of the object every time.
     *
     * @param <T>   The generic type.
     * @param clazz The class type.
     * @return The instantiated object.
     */
    <T> T multiple(final Class<T> clazz);

    /**
     * Gets a thread-safe object. 1. It checks if the current class has the {@link ThreadSafe} annotation. If it does,
     * it creates a singleton object directly. If not, it creates a new instance.
     *
     * @param <T>   The generic type.
     * @param clazz The class type.
     * @return The instantiated object.
     */
    <T> T threadSafe(final Class<T> clazz);

}
