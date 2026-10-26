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
package org.miaixz.bus.core.lang.intern;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;

/**
 * An implementation of {@link Intern} that uses a thread-safe {@link WeakConcurrentMap} to store canonical objects.
 * This ensures that canonical objects can be garbage collected if they are no longer strongly referenced elsewhere.
 * <p>
 * This class should generally be used as a singleton instance to maintain a consistent pool of interned objects.
 *
 * @param <T> The type of the object to be interned.
 * @author Kimi Liu
 */
public class WeakIntern<T> implements Intern<T> {

    /**
     * Canonical objects held weakly as both keys and values, without a separately collectible reference wrapper.
     */
    private final WeakConcurrentMap<T, T> cache = new WeakConcurrentMap<>();

    /**
     * Constructs a new WeakIntern instance.
     */
    public WeakIntern() {
        // No initialization required.
    }

    /**
     * Returns the canonical representation for the given object. If the object is already in the cache, its canonical
     * instance is returned. Otherwise, the object is added to the cache as a new canonical instance.
     * <p>
     * The lookup retains its result strongly until this call returns; an external strong reference keeps that same
     * canonical object available to subsequent equal samples.
     *
     * @param sample The object for which to retrieve the canonical representation.
     * @return The canonical object instance, or {@code null} if the sample is {@code null}.
     */
    @Override
    public T intern(final T sample) {
        if (null == sample) {
            return null;
        }
        return this.cache.computeIfAbsent(sample, key -> key);
    }

}
