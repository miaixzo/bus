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

import org.miaixz.bus.core.lang.Wrapper;

/**
 * Represents a poolable object that can be managed by an object pool. This interface provides methods to track the idle
 * time and last return time of the object.
 *
 * @param <T> the type of the object being pooled
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Poolable<T> extends Wrapper<T> {

    /**
     * Retrieves the idle time of the object in milliseconds. Idle time is defined as the duration the object has been
     * in the pool since its last return.
     *
     * @return the idle time in milliseconds
     */
    default long getIdle() {
        return System.currentTimeMillis() - getLastReturn();
    }

    /**
     * Retrieves the timestamp of when the object was last returned to the pool.
     *
     * @return the timestamp in milliseconds when the object was last returned
     */
    long getLastReturn();

    /**
     * Sets the timestamp of when the object was last returned to the pool. This method should be called when the object
     * is successfully returned to the pool.
     *
     * @param lastReturn the timestamp in milliseconds representing the last return time
     */
    void setLastReturn(final long lastReturn);

}
