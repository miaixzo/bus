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
import java.util.NoSuchElementException;

/**
 * An iterator for {@link CacheObject} that automatically skips expired entries.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 21+
 */
public class CacheObjectIterator<K, V> implements Iterator<CacheObject<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852231183370L;

    private final Iterator<CacheObject<K, V>> iterator;
    private CacheObject<K, V> nextValue;

    /**
     * Constructs a new iterator that wraps the original cache object iterator.
     *
     * @param iterator The original {@link Iterator}.
     */
    CacheObjectIterator(final Iterator<CacheObject<K, V>> iterator) {
        this.iterator = iterator;
        // Prime the first non-expired value.
        nextValue();
    }

    /**
     * Checks if there is another non-expired object in the cache.
     *
     * @return {@code true} if there is a next value, otherwise {@code false}.
     */
    @Override
    public boolean hasNext() {
        return nextValue != null;
    }

    /**
     * Returns the next non-expired object.
     *
     * @return The next {@link CacheObject}.
     * @throws NoSuchElementException if there are no more elements.
     */
    @Override
    public CacheObject<K, V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final CacheObject<K, V> cachedObject = nextValue;
        // Move to the next non-expired value.
        nextValue();
        return cachedObject;
    }

    /**
     * This operation is not supported and will throw an {@link UnsupportedOperationException}. The cache iterator is
     * read-only.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cache values Iterator does not support modification.");
    }

    /**
     * Advances to the next non-expired object in the underlying iterator. If no more non-expired objects are found,
     * {@code nextValue} is set to {@code null}.
     */
    private void nextValue() {
        while (iterator.hasNext()) {
            nextValue = iterator.next();
            if (!nextValue.isExpired()) {
                return;
            }
        }
        nextValue = null;
    }

}
