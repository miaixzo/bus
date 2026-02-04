/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.intern;

import java.lang.ref.WeakReference;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;

/**
 * An implementation of {@link Intern} that uses a thread-safe {@link WeakConcurrentMap} to store canonical objects.
 * This ensures that canonical objects can be garbage collected if they are no longer strongly referenced elsewhere.
 * <p>
 * This class should generally be used as a singleton instance to maintain a consistent pool of interned objects.
 *
 * @param <T> The type of the object to be interned.
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeakIntern<T> implements Intern<T> {

    /**
     * The cache for storing weak references to interned objects. The keys are the objects themselves, and the values
     * are weak references to the canonical objects.
     */
    private final WeakConcurrentMap<T, WeakReference<T>> cache = new WeakConcurrentMap<>();

    /**
     * Returns the canonical representation for the given object. If the object is already in the cache, its canonical
     * instance is returned. Otherwise, the object is added to the cache as a new canonical instance.
     * <p>
     * This method handles the case where a weak reference might be garbage collected immediately after creation by
     * looping until a non-null value is retrieved.
     *
     * @param sample The object for which to retrieve the canonical representation.
     * @return The canonical object instance, or {@code null} if the sample is {@code null}.
     */
    @Override
    public T intern(final T sample) {
        if (null == sample) {
            return null;
        }
        T val;
        // Loop to avoid the situation where a newly created WeakReference is immediately garbage collected.
        do {
            val = this.cache.computeIfAbsent(sample, WeakReference::new).get();
        } while (val == null);
        return val;
    }

}
