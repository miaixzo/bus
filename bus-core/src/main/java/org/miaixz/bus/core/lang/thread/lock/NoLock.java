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
