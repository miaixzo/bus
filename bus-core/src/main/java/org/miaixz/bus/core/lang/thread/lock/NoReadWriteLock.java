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
package org.miaixz.bus.core.lang.thread.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A no-operation implementation of the {@link ReadWriteLock} interface. This class provides a "read-write lock" that
 * does not actually perform any locking or synchronization. Both {@link #readLock()} and {@link #writeLock()} methods
 * return a singleton {@link NoLock} instance, effectively making all read and write operations non-blocking.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NoReadWriteLock implements ReadWriteLock {

    /**
     * Returns a no-operation read lock.
     *
     * @return A singleton instance of {@link NoLock}.
     */
    @Override
    public Lock readLock() {
        return NoLock.INSTANCE;
    }

    /**
     * Returns a no-operation write lock.
     *
     * @return A singleton instance of {@link NoLock}.
     */
    @Override
    public Lock writeLock() {
        return NoLock.INSTANCE;
    }

}
