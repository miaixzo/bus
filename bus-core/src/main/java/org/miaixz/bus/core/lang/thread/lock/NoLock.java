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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * A no-operation implementation of the {@link Lock} interface. This class provides a "lock" that does not actually
 * perform any locking or synchronization. It can be useful in scenarios where a {@link Lock} interface is required but
 * no actual locking behavior is desired, for example, as a placeholder or in single-threaded contexts.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NoLock implements Lock {

    /**
     * A singleton instance of {@code NoLock}.
     */
    public static NoLock INSTANCE = new NoLock();

    /**
     * This method performs no operation.
     */
    @Override
    public void lock() {
    }

    /**
     * This method performs no operation.
     */
    @Override
    public void lockInterruptibly() {
    }

    /**
     * This method always returns {@code true}, indicating that the lock is always acquired immediately.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean tryLock() {
        return true;
    }

    /**
     * This method always returns {@code true}, indicating that the lock is always acquired immediately, regardless of
     * the timeout.
     *
     * @param time The maximum time to wait for the lock (ignored).
     * @param unit The time unit of the {@code time} argument (ignored).
     * @return Always {@code true}.
     */
    @Override
    public boolean tryLock(final long time, final TimeUnit unit) {
        return true;
    }

    /**
     * This method performs no operation.
     */
    @Override
    public void unlock() {
    }

    /**
     * This method is not supported and always throws an {@link UnsupportedOperationException}.
     *
     * @return (Never returns) A new {@link Condition} instance.
     * @throws UnsupportedOperationException always.
     */
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("NoLock`s newCondition method is unsupported");
    }

}
