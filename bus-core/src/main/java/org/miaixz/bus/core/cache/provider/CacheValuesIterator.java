/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

/**
 * An iterator for the values in an {@link AbstractCache}.
 *
 * @param <V> The type of the iterated object.
 * @author Kimi Liu
 * @since Java 17+
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
