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
