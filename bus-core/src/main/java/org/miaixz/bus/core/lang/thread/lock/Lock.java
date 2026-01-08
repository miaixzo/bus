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

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Utility class for creating and managing various types of locks and synchronization primitives. This class provides
 * factory methods for standard Java locks as well as custom segment locks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Lock {

    /**
     * A singleton instance of {@link NoLock} representing a no-operation lock.
     */
    private static final NoLock NO_LOCK = new NoLock();

    /**
     * Creates a new {@link StampedLock} instance. {@link StampedLock} is a capability-based lock with three modes for
     * controlling read/write access.
     *
     * @return A new {@link StampedLock}.
     */
    public static StampedLock createStampLock() {
        return new StampedLock();
    }

    /**
     * Creates a new {@link ReentrantReadWriteLock} instance. This lock maintains a pair of associated
     * {@link java.util.concurrent.locks.Lock}s, one for read-only operations and one for writing. The {@code fair}
     * parameter determines if the lock grants access to the longest-waiting thread.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy; {@code false} for a non-fair policy.
     * @return A new {@link ReentrantReadWriteLock}.
     */
    public static ReentrantReadWriteLock createReadWriteLock(final boolean fair) {
        return new ReentrantReadWriteLock(fair);
    }

    // region ----- SegmentLock
    /**
     * Creates a segment lock with strong references, using {@link java.util.concurrent.locks.ReentrantLock} for each
     * segment. Segment locks divide a resource into segments, each protected by its own lock, reducing contention.
     *
     * @param segments The number of segments, must be greater than 0.
     * @return A {@link SegmentLock} instance managing {@link java.util.concurrent.locks.Lock}s.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static SegmentLock<java.util.concurrent.locks.Lock> createSegmentLock(final int segments) {
        return SegmentLock.lock(segments);
    }

    /**
     * Creates a segment read-write lock with strong references, using {@link ReentrantReadWriteLock} for each segment.
     *
     * @param segments The number of segments, must be greater than 0.
     * @return A {@link SegmentLock} instance managing {@link ReadWriteLock}s.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static SegmentLock<ReadWriteLock> createSegmentReadWriteLock(final int segments) {
        return SegmentLock.readWriteLock(segments);
    }

    /**
     * Creates a segment semaphore with strong references.
     *
     * @param segments The number of segments, must be greater than 0.
     * @param permits  The number of permits available for each semaphore segment.
     * @return A {@link SegmentLock} instance managing {@link Semaphore}s.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static SegmentLock<Semaphore> createSegmentSemaphore(final int segments, final int permits) {
        return SegmentLock.semaphore(segments, permits);
    }

    /**
     * Creates a segment lock with weak references, using {@link java.util.concurrent.locks.ReentrantLock} for each
     * segment. Locks are lazily initialized and can be garbage collected if not in use.
     *
     * @param segments The number of segments, must be greater than 0.
     * @return A {@link SegmentLock} instance managing weakly-referenced {@link java.util.concurrent.locks.Lock}s.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static SegmentLock<java.util.concurrent.locks.Lock> createLazySegmentLock(final int segments) {
        return SegmentLock.lazyWeakLock(segments);
    }

    /**
     * Retrieves a segment lock (strong reference) based on a given key. The key is used to determine which segment's
     * lock to return.
     *
     * @param segments The total number of segments, must be greater than 0.
     * @param key      The key used to map to a specific segment.
     * @return The {@link java.util.concurrent.locks.Lock} instance corresponding to the key's segment.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static java.util.concurrent.locks.Lock getSegmentLock(final int segments, final Object key) {
        return SegmentLock.lock(segments).get(key);
    }

    /**
     * Retrieves a segment read lock (strong reference) based on a given key. The key is used to determine which
     * segment's read lock to return.
     *
     * @param segments The total number of segments, must be greater than 0.
     * @param key      The key used to map to a specific segment.
     * @return The read {@link java.util.concurrent.locks.Lock} instance corresponding to the key's segment.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static java.util.concurrent.locks.Lock getSegmentReadLock(final int segments, final Object key) {
        return SegmentLock.readWriteLock(segments).get(key).readLock();
    }

    /**
     * Retrieves a segment write lock (strong reference) based on a given key. The key is used to determine which
     * segment's write lock to return.
     *
     * @param segments The total number of segments, must be greater than 0.
     * @param key      The key used to map to a specific segment.
     * @return The write {@link java.util.concurrent.locks.Lock} instance corresponding to the key's segment.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static java.util.concurrent.locks.Lock getSegmentWriteLock(final int segments, final Object key) {
        return SegmentLock.readWriteLock(segments).get(key).writeLock();
    }

    /**
     * Retrieves a segment semaphore (strong reference) based on a given key. The key is used to determine which
     * segment's semaphore to return.
     *
     * @param segments The total number of segments, must be greater than 0.
     * @param permits  The number of permits available for each semaphore segment.
     * @param key      The key used to map to a specific segment.
     * @return The {@link Semaphore} instance corresponding to the key's segment.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static Semaphore getSegmentSemaphore(final int segments, final int permits, final Object key) {
        return SegmentLock.semaphore(segments, permits).get(key);
    }

    /**
     * Retrieves a weakly-referenced segment lock (lazy-loaded) based on a given key. The key is used to determine which
     * segment's lock to return.
     *
     * @param segments The total number of segments, must be greater than 0.
     * @param key      The key used to map to a specific segment.
     * @return The {@link java.util.concurrent.locks.Lock} instance corresponding to the key's segment.
     * @throws IllegalArgumentException if {@code segments} is not greater than 0.
     */
    public static java.util.concurrent.locks.Lock getLazySegmentLock(final int segments, final Object key) {
        return SegmentLock.lazyWeakLock(segments).get(key);
    }

    /**
     * Returns a singleton instance of {@link NoLock}, which is a no-operation lock. This can be used when a lock
     * interface is required but no actual locking is desired.
     *
     * @return The singleton {@link NoLock} instance.
     */
    public static NoLock getNoLock() {
        return NO_LOCK;
    }

}
