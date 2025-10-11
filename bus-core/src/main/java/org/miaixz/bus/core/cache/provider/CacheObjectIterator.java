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
 * @since Java 17+
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
