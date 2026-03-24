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
package org.miaixz.bus.core.cache.provider;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;

/**
 * An iterator for the values in an {@link AbstractCache}.
 *
 * @param <V> The type of the iterated object.
 * @author Kimi Liu
 * @since Java 21+
 */
public class CacheValuesIterator<V> implements Iterator<V>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852231336892L;

    /**
     * The underlying cache object iterator.
     */
    private final CacheObjectIterator<?, V> cacheObjIter;

    /**
     * Constructs a new values iterator.
     *
     * @param iterator The original {@link CacheObjectIterator}.
     */
    CacheValuesIterator(final CacheObjectIterator<?, V> iterator) {
        this.cacheObjIter = iterator;
    }

    /**
     * Checks if there is another value in the cache.
     *
     * @return {@code true} if there is a next value, otherwise {@code false}.
     */
    @Override
    public boolean hasNext() {
        return this.cacheObjIter.hasNext();
    }

    /**
     * Returns the next value in the cache.
     *
     * @return The next value.
     */
    @Override
    public V next() {
        return cacheObjIter.next().getValue();
    }

    /**
     * This operation is not supported and will throw an {@link UnsupportedOperationException}.
     */
    @Override
    public void remove() {
        cacheObjIter.remove();
    }

}
