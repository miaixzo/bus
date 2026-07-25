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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.lang.exception.ProtocolException;

/**
 * Unique HTTP/2 flow-control synchronization boundary and window arithmetic.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class Http2FlowController {

    /**
     * Existing single flow lock.
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Wakeup condition for credit, capacity, and terminal transitions.
     */
    private final Condition changed = lock.newCondition();

    /**
     * Acquires the flow synchronization boundary.
     */
    void lock() {
        lock.lock();
    }

    /**
     * Releases the flow synchronization boundary.
     */
    void unlock() {
        lock.unlock();
    }

    /**
     * Wakes all writers or stream creators awaiting a state change.
     */
    void signalAll() {
        changed.signalAll();
    }

    /**
     * Waits until signalled.
     *
     * @throws InterruptedException when interrupted
     */
    void await() throws InterruptedException {
        changed.await();
    }

    /**
     * Waits up to a remaining deadline.
     *
     * @param nanos remaining nanoseconds
     * @return remaining time
     * @throws InterruptedException when interrupted
     */
    long awaitNanos(final long nanos) throws InterruptedException {
        return changed.awaitNanos(nanos);
    }

    /**
     * Adds a positive WINDOW_UPDATE delta.
     *
     * @param current current window
     * @param delta   positive delta
     * @return updated window
     */
    static long add(final long current, final long delta) {
        if (delta <= 0L || current < 0L || current > Integer.MAX_VALUE - delta) {
            throw new ProtocolException("HTTP/2 flow-control window overflow");
        }
        return current + delta;
    }

    /**
     * Applies a signed SETTINGS_INITIAL_WINDOW_SIZE delta.
     *
     * @param current current stream window
     * @param delta   signed delta
     * @return adjusted window
     */
    static long adjust(final long current, final long delta) {
        final long adjusted;
        try {
            adjusted = Math.addExact(current, delta);
        } catch (final ArithmeticException e) {
            throw new ProtocolException("HTTP/2 flow-control window overflow", e);
        }
        if (adjusted > Integer.MAX_VALUE || adjusted < -Integer.MAX_VALUE) {
            throw new ProtocolException("HTTP/2 flow-control window overflow");
        }
        return adjusted;
    }

}
