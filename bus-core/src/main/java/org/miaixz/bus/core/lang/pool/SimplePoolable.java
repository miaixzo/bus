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
