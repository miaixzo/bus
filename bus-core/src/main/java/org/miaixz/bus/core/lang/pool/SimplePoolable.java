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
package org.miaixz.bus.core.lang.pool;

/**
 * A simple implementation of {@link Poolable} that wraps a raw object. This class holds the original object and tracks
 * its last return time to the pool.
 *
 * @param <T> the type of the raw object being pooled
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimplePoolable<T> implements Poolable<T> {

    /**
     * The raw object wrapped by this poolable instance.
     */
    private final T raw;
    /**
     * The timestamp in milliseconds when this object was last returned to the pool.
     */
    private long lastReturn;

    /**
     * Constructs a new {@code SimplePoolable} instance with the given raw object. The {@code lastReturn} time is
     * initialized to the current system time.
     *
     * @param raw the raw object to be wrapped and made poolable
     */
    public SimplePoolable(final T raw) {
        this.raw = raw;
        this.lastReturn = System.currentTimeMillis();
    }

    /**
     * Retrieves the raw object wrapped by this poolable instance.
     *
     * @return the raw object
     */
    @Override
    public T getRaw() {
        return this.raw;
    }

    /**
     * Retrieves the timestamp of when this object was last returned to the pool.
     *
     * @return the timestamp in milliseconds when the object was last returned
     */
    @Override
    public long getLastReturn() {
        return lastReturn;
    }

    /**
     * Sets the timestamp of when this object was last returned to the pool. This method should be called when the
     * object is successfully returned to the pool.
     *
     * @param lastReturn the timestamp in milliseconds representing the last return time
     */
    @Override
    public void setLastReturn(final long lastReturn) {
        this.lastReturn = lastReturn;
    }

}
