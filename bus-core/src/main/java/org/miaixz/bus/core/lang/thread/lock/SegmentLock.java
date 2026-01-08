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

import java.io.Serial;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * Utility class for segmenting locks, supporting segmented implementations of {@link Lock}, {@link Semaphore}, and
 * {@link ReadWriteLock}.
 * <p>
 * By dividing a resource into multiple segments, each protected by its own lock, different operations can concurrently
 * access different segments, thereby avoiding contention on a single lock. Equal keys are guaranteed to map to the same
 * segment lock (e.g., if key1.equals(key2), then get(key1) and get(key2) will return the same object). However,
 * different keys might map to the same segment due to hash collisions; the fewer the segments, the higher the
 * probability of collisions.
 *
 * <p>
 * This class supports two types of implementations:
 * <ul>
 * <li>Strong references: All segments are initialized upon creation, ensuring stable memory usage.</li>
 * <li>Weak references: Segments are lazily loaded upon first use and can be garbage collected if not in use. This is
 * suitable for scenarios with a large number of segments but infrequent usage.</li>
 * </ul>
 *
 * @param <L> The type of the lock or synchronization primitive managed by the segments (e.g., {@link Lock},
 *            {@link Semaphore}, {@link ReadWriteLock}).
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SegmentLock<L> {

    /**
     * When the number of segments exceeds this threshold, a {@link ConcurrentMap} is used instead of a large array to
     * save memory (applicable in lazy-loading scenarios).
     */
    private static final int LARGE_LAZY_CUTOFF = 1024;
    /**
     * A bitmask representing all bits set, used for maximum integer value.
     */
    private static final int ALL_SET = ~0;

    /**
     * Creates a segment lock with strong references, where all segments are initialized upon creation.
     *
     * @param stripes  The number of segments.
     * @param supplier A {@link Supplier} that provides new instances of the lock type {@code L}.
     * @param <L>      The type of the lock or synchronization primitive.
     * @return A {@link SegmentLock} instance with strong references.
     */
    public static <L> SegmentLock<L> custom(final int stripes, final Supplier<L> supplier) {
        return new CompactSegmentLock<>(stripes, supplier);
    }

    /**
     * Creates a segment of reentrant locks with strong references.
     *
     * @param stripes The number of segments.
     * @return A {@link SegmentLock} instance managing {@link java.util.concurrent.locks.Lock}s.
     */
    public static SegmentLock<java.util.concurrent.locks.Lock> lock(final int stripes) {
        return custom(stripes, PaddedLock::new);
    }

    /**
     * Creates a segment of reentrant locks with weak references, using lazy loading. Locks are created only when
     * accessed and can be garbage collected if not strongly referenced elsewhere.
     *
     * @param stripes The number of segments.
     * @return A {@link SegmentLock} instance managing weakly-referenced {@link java.util.concurrent.locks.Lock}s.
     */
    public static SegmentLock<java.util.concurrent.locks.Lock> lazyWeakLock(final int stripes) {
        return lazyWeakCustom(stripes, () -> new ReentrantLock(false));
    }

    /**
     * Creates a segment lock with weak references and lazy loading, using a custom supplier for the lock type.
     *
     * @param stripes  The number of segments.
     * @param supplier A {@link Supplier} that provides new instances of the lock type {@code L}.
     * @param <L>      The type of the lock or synchronization primitive.
     * @return A {@link SegmentLock} instance with weak references and lazy loading.
     */
    private static <L> SegmentLock<L> lazyWeakCustom(final int stripes, final Supplier<L> supplier) {
        return stripes < LARGE_LAZY_CUTOFF ? new SmallLazySegmentLock<>(stripes, supplier)
                : new LargeLazySegmentLock<>(stripes, supplier);
    }

    /**
     * Creates a segment of semaphores with strong references.
     *
     * @param stripes The number of segments.
     * @param permits The number of permits available for each semaphore segment.
     * @return A {@link SegmentLock} instance managing {@link Semaphore}s.
     */
    public static SegmentLock<Semaphore> semaphore(final int stripes, final int permits) {
        return custom(stripes, () -> new PaddedSemaphore(permits));
    }

    /**
     * Creates a segment of semaphores with weak references and lazy loading.
     *
     * @param stripes The number of segments.
     * @param permits The number of permits available for each semaphore segment.
     * @return A {@link SegmentLock} instance managing weakly-referenced {@link Semaphore}s.
     */
    public static SegmentLock<Semaphore> lazyWeakSemaphore(final int stripes, final int permits) {
        return lazyWeakCustom(stripes, () -> new Semaphore(permits, false));
    }

    /**
     * Creates a segment of read-write locks with strong references.
     *
     * @param stripes The number of segments.
     * @return A {@link SegmentLock} instance managing {@link ReadWriteLock}s.
     */
    public static SegmentLock<ReadWriteLock> readWriteLock(final int stripes) {
        return custom(stripes, ReentrantReadWriteLock::new);
    }

    /**
     * Creates a segment of read-write locks with weak references and lazy loading.
     *
     * @param stripes The number of segments.
     * @return A {@link SegmentLock} instance managing weakly-referenced {@link ReadWriteLock}s.
     */
    public static SegmentLock<ReadWriteLock> lazyWeakReadWriteLock(final int stripes) {
        return lazyWeakCustom(stripes, WeakSafeReadWriteLock::new);
    }

    /**
     * Calculates the smallest power of two that is greater than or equal to the given integer.
     *
     * @param x The integer value.
     * @return The smallest power of two greater than or equal to {@code x}.
     */
    private static int ceilToPowerOfTwo(final int x) {
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }

    /**
     * Spreads the bits of an integer hash code to improve distribution, reducing hash collisions.
     *
     * @param hashCode The original hash code.
     * @return The smeared hash code.
     */
    private static int smear(int hashCode) {
        hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
        return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
    }

    /**
     * Retrieves the lock segment corresponding to the given key. Ensures that identical keys return the same lock
     * object.
     *
     * @param key The non-null key used to map to a segment.
     * @return The corresponding lock segment.
     */
    public abstract L get(Object key);

    /**
     * Retrieves the lock segment at the specified index. The index must be within the range [0, size()).
     *
     * @param index The index of the lock segment.
     * @return The lock segment at the specified index.
     */
    public abstract L getAt(int index);

    /**
     * Calculates the segment index for a given key.
     *
     * @param key The non-null key.
     * @return The segment index.
     */
    abstract int indexFor(Object key);

    /**
     * Returns the total number of segments in this lock.
     *
     * @return The number of segments.
     */
    public abstract int size();

    /**
     * Retrieves a list of lock segments corresponding to a batch of keys. The returned list is sorted by index to help
     * prevent deadlocks when acquiring multiple locks.
     *
     * @param keys A non-empty collection of keys.
     * @return An unmodifiable list of lock segments (may contain duplicates if multiple keys map to the same segment).
     */
    public Iterable<L> bulkGet(final Iterable<?> keys) {
        final List<Object> result = (List<Object>) ListKit.of(keys);
        if (CollKit.isEmpty(result)) {
            return Collections.emptyList();
        }
        final int[] stripes = new int[result.size()];
        for (int i = 0; i < result.size(); i++) {
            stripes[i] = indexFor(result.get(i));
        }
        Arrays.sort(stripes);
        int previousStripe = stripes[0];
        result.set(0, getAt(previousStripe));
        for (int i = 1; i < result.size(); i++) {
            final int currentStripe = stripes[i];
            if (currentStripe == previousStripe) {
                result.set(i, result.get(i - 1));
            } else {
                result.set(i, getAt(currentStripe));
                previousStripe = currentStripe;
            }
        }
        final List<L> asStripes = (List<L>) result;
        return Collections.unmodifiableList(asStripes);
    }

    /**
     * A weak-reference safe {@link ReadWriteLock} implementation that ensures the read and write locks maintain a
     * strong reference to themselves to prevent premature garbage collection.
     */
    private static final class WeakSafeReadWriteLock implements ReadWriteLock {

        private final ReadWriteLock delegate;

        /**
         * Constructs a new {@code WeakSafeReadWriteLock}.
         */
        WeakSafeReadWriteLock() {
            this.delegate = new ReentrantReadWriteLock();
        }

        /**
         * Returns the lock used for reading.
         *
         * @return The read lock.
         */
        @Override
        public java.util.concurrent.locks.Lock readLock() {
            return new WeakSafeLock(delegate.readLock(), this);
        }

        /**
         * Returns the lock used for writing.
         *
         * @return The write lock.
         */
        @Override
        public java.util.concurrent.locks.Lock writeLock() {
            return new WeakSafeLock(delegate.writeLock(), this);
        }
    }

    /**
     * A weak-reference safe {@link Lock} wrapper that maintains a strong reference to its enclosing
     * {@link WeakSafeReadWriteLock} to prevent premature garbage collection.
     */
    private static final class WeakSafeLock implements java.util.concurrent.locks.Lock {

        private final java.util.concurrent.locks.Lock delegate;
        private final WeakSafeReadWriteLock strongReference;

        /**
         * Constructs a new {@code WeakSafeLock}.
         *
         * @param delegate        The underlying {@link Lock} to delegate calls to.
         * @param strongReference A strong reference to the enclosing {@link WeakSafeReadWriteLock}.
         */
        WeakSafeLock(final Lock delegate, final WeakSafeReadWriteLock strongReference) {
            this.delegate = delegate;
            this.strongReference = strongReference;
        }

        /**
         * Acquires the lock.
         */
        @Override
        public void lock() {
            delegate.lock();
        }

        /**
         * Acquires the lock unless the current thread is interrupted.
         *
         * @throws InterruptedException if the current thread is interrupted while acquiring the lock.
         */
        @Override
        public void lockInterruptibly() throws InterruptedException {
            delegate.lockInterruptibly();
        }

        /**
         * Acquires the lock only if it is free at the time of invocation.
         *
         * @return {@code true} if the lock was acquired and {@code false} otherwise.
         */
        @Override
        public boolean tryLock() {
            return delegate.tryLock();
        }

        /**
         * Acquires the lock if it is free within the given waiting time and the current thread has not been
         * interrupted.
         *
         * @param time The maximum time to wait for the lock.
         * @param unit The time unit of the {@code time} argument.
         * @return {@code true} if the lock was acquired and {@code false} if the waiting time elapsed before the lock
         *         was acquired.
         * @throws InterruptedException if the current thread is interrupted while acquiring the lock.
         */
        @Override
        public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
            return delegate.tryLock(time, unit);
        }

        /**
         * Releases the lock.
         */
        @Override
        public void unlock() {
            delegate.unlock();
        }

        /**
         * Returns a new {@link Condition} instance that is bound to this {@code Lock} instance.
         *
         * @return A new {@link Condition} instance.
         */
        @Override
        public Condition newCondition() {
            return new WeakSafeCondition(delegate.newCondition(), strongReference);
        }
    }

    /**
     * A weak-reference safe {@link Condition} wrapper that maintains a strong reference to its enclosing
     * {@link WeakSafeReadWriteLock} to prevent premature garbage collection.
     */
    private static final class WeakSafeCondition implements Condition {

        private final Condition delegate;

        /**
         * A strong reference to the enclosing {@link WeakSafeReadWriteLock} to prevent garbage collection.
         */
        private final WeakSafeReadWriteLock strongReference;

        /**
         * Constructs a new {@code WeakSafeCondition}.
         *
         * @param delegate        The underlying {@link Condition} to delegate calls to.
         * @param strongReference A strong reference to the enclosing {@link WeakSafeReadWriteLock}.
         */
        WeakSafeCondition(final Condition delegate, final WeakSafeReadWriteLock strongReference) {
            this.delegate = delegate;
            this.strongReference = strongReference;
        }

        /**
         * Causes the current thread to wait until it is signalled or interrupted.
         *
         * @throws InterruptedException if the current thread is interrupted.
         */
        @Override
        public void await() throws InterruptedException {
            delegate.await();
        }

        /**
         * Causes the current thread to wait until it is signalled. The lock associated with this {@code Condition} is
         * atomically released and the current thread becomes disabled for thread scheduling purposes and lies dormant
         * until one of two things happens: The lock is reacquired by the current thread, and the current thread is
         * interrupted.
         */
        @Override
        public void awaitUninterruptibly() {
            delegate.awaitUninterruptibly();
        }

        /**
         * Causes the current thread to wait until it is signalled or interrupted, or the specified waiting time
         * elapses.
         *
         * @param nanosTimeout The maximum time to wait, in nanoseconds.
         * @return The remaining nanoseconds from the timeout, or a value less than or equal to zero if the timeout
         *         occurred.
         * @throws InterruptedException if the current thread is interrupted.
         */
        @Override
        public long awaitNanos(final long nanosTimeout) throws InterruptedException {
            return delegate.awaitNanos(nanosTimeout);
        }

        /**
         * Causes the current thread to wait until it is signalled or interrupted, or the specified waiting time
         * elapses.
         *
         * @param time The maximum time to wait.
         * @param unit The time unit of the {@code time} argument.
         * @return {@code false} if the waiting time elapsed before the condition was signalled, else {@code true}.
         * @throws InterruptedException if the current thread is interrupted.
         */
        @Override
        public boolean await(final long time, final TimeUnit unit) throws InterruptedException {
            return delegate.await(time, unit);
        }

        /**
         * Causes the current thread to wait until it is signalled or interrupted, or the specified deadline passes.
         *
         * @param deadline The absolute time by which to wait.
         * @return {@code false} if the deadline has elapsed upon return, else {@code true}.
         * @throws InterruptedException if the current thread is interrupted.
         */
        @Override
        public boolean awaitUntil(final Date deadline) throws InterruptedException {
            return delegate.awaitUntil(deadline);
        }

        /**
         * Wakes up one waiting thread.
         */
        @Override
        public void signal() {
            delegate.signal();
        }

        /**
         * Wakes up all waiting threads.
         */
        @Override
        public void signalAll() {
            delegate.signalAll();
        }
    }

    /**
     * Abstract base class for segment locks, ensuring that the number of segments is a power of two.
     *
     * @param <L> The type of the lock or synchronization primitive.
     */
    private abstract static class PowerOfTwoSegmentLock<L> extends SegmentLock<L> {

        /**
         * The bitmask used to determine the segment index from a hash code.
         */
        final int mask;

        /**
         * Constructs a {@code PowerOfTwoSegmentLock} with the specified number of stripes.
         *
         * @param stripes The number of segments. Must be positive.
         * @throws IllegalArgumentException if {@code stripes} is not positive.
         */
        PowerOfTwoSegmentLock(final int stripes) {
            Assert.isTrue(stripes > 0, "Segment count must be positive");
            this.mask = stripes > Integer.MAX_VALUE / 2 ? ALL_SET : ceilToPowerOfTwo(stripes) - 1;
        }

        /**
         * Calculates the segment index for a given key by smearing its hash code and applying the mask.
         *
         * @param key The non-null key.
         * @return The segment index.
         */
        @Override
        final int indexFor(final Object key) {
            final int hash = smear(key.hashCode());
            return hash & mask;
        }

        /**
         * Retrieves the lock segment corresponding to the given key.
         *
         * @param key The non-null key.
         * @return The corresponding lock segment.
         */
        @Override
        public final L get(final Object key) {
            return getAt(indexFor(key));
        }
    }

    /**
     * A strong-reference implementation of {@code SegmentLock} that uses a fixed-size array to store segments. All
     * segments are initialized upon construction.
     *
     * @param <L> The type of the lock or synchronization primitive.
     */
    private static class CompactSegmentLock<L> extends PowerOfTwoSegmentLock<L> {

        private final Object[] array;

        /**
         * Constructs a new {@code CompactSegmentLock}.
         *
         * @param stripes  The number of segments.
         * @param supplier A {@link Supplier} that provides new instances of the lock type {@code L}.
         */
        CompactSegmentLock(final int stripes, final Supplier<L> supplier) {
            super(stripes);
            Assert.isTrue(stripes <= Integer.MAX_VALUE / 2, "Segment count must be <= 2^30");
            this.array = new Object[mask + 1];
            for (int i = 0; i < array.length; i++) {
                array[i] = supplier.get();
            }
        }

        /**
         * Retrieves the lock segment at the specified index.
         *
         * @param index The index of the lock segment.
         * @return The lock segment at the specified index.
         * @throws IllegalArgumentException if the index is out of bounds.
         */
        @Override
        public L getAt(final int index) {
            if (index < 0 || index >= array.length) {
                throw new IllegalArgumentException("Index " + index + " out of bounds for size " + array.length);
            }
            return (L) array[index];
        }

        /**
         * Returns the total number of segments.
         *
         * @return The number of segments.
         */
        @Override
        public int size() {
            return array.length;
        }
    }

    /**
     * A small-scale weak-reference implementation of {@code SegmentLock} that uses an {@link AtomicReferenceArray} to
     * store segments. Segments are lazily loaded and can be garbage collected if not in use.
     *
     * @param <L> The type of the lock or synchronization primitive.
     */
    private static class SmallLazySegmentLock<L> extends PowerOfTwoSegmentLock<L> {

        final AtomicReferenceArray<ArrayReference<? extends L>> locks;
        final Supplier<L> supplier;
        final int size;
        final ReferenceQueue<L> queue = new ReferenceQueue<>();

        /**
         * Constructs a new {@code SmallLazySegmentLock}.
         *
         * @param stripes  The number of segments.
         * @param supplier A {@link Supplier} that provides new instances of the lock type {@code L}.
         */
        SmallLazySegmentLock(final int stripes, final Supplier<L> supplier) {
            super(stripes);
            this.size = (mask == ALL_SET) ? Integer.MAX_VALUE : mask + 1;
            this.locks = new AtomicReferenceArray<>(size);
            this.supplier = supplier;
        }

        /**
         * Retrieves the lock segment at the specified index. If the segment does not exist or has been garbage
         * collected, a new one is created and stored weakly.
         *
         * @param index The index of the lock segment.
         * @return The lock segment at the specified index.
         * @throws IllegalArgumentException if the index is out of bounds (unless size is {@link Integer#MAX_VALUE}).
         */
        @Override
        public L getAt(final int index) {
            if (size != Integer.MAX_VALUE) {
                Assert.isTrue(index >= 0 && index < size, "Index out of bounds");
            }
            ArrayReference<? extends L> existingRef = locks.get(index);
            L existing = existingRef == null ? null : existingRef.get();
            if (existing != null) {
                return existing;
            }
            final L created = supplier.get();
            final ArrayReference<L> newRef = new ArrayReference<>(created, index, queue);
            while (!locks.compareAndSet(index, existingRef, newRef)) {
                existingRef = locks.get(index);
                existing = existingRef == null ? null : existingRef.get();
                if (existing != null) {
                    return existing;
                }
            }
            drainQueue();
            return created;
        }

        /**
         * Drains the {@link ReferenceQueue}, removing any garbage-collected weak references from the {@code locks}
         * array.
         */
        private void drainQueue() {
            Reference<? extends L> ref;
            while ((ref = queue.poll()) != null) {
                final ArrayReference<? extends L> arrayRef = (ArrayReference<? extends L>) ref;
                locks.compareAndSet(arrayRef.index, arrayRef, null);
            }
        }

        /**
         * Returns the total number of segments.
         *
         * @return The number of segments.
         */
        @Override
        public int size() {
            return size;
        }

        /**
         * A weak reference that also stores the index of the element in the {@link AtomicReferenceArray}.
         *
         * @param <L> The type of the referent.
         */
        private static final class ArrayReference<L> extends WeakReference<L> {

            final int index;

            /**
             * Constructs a new {@code ArrayReference}.
             *
             * @param referent The object to which this weak reference refers.
             * @param index    The index of the referent in the array.
             * @param queue    The queue with which the reference is registered.
             */
            ArrayReference(final L referent, final int index, final ReferenceQueue<L> queue) {
                super(referent, queue);
                this.index = index;
            }
        }
    }

    /**
     * A large-scale weak-reference implementation of {@code SegmentLock} that uses a {@link ConcurrentMap} to store
     * segments. Segments are lazily loaded and can be garbage collected if not in use.
     *
     * @param <L> The type of the lock or synchronization primitive.
     */
    private static class LargeLazySegmentLock<L> extends PowerOfTwoSegmentLock<L> {

        final ConcurrentMap<Integer, L> locks;
        final Supplier<L> supplier;
        final int size;

        /**
         * Constructs a new {@code LargeLazySegmentLock}.
         *
         * @param stripes  The number of segments.
         * @param supplier A {@link Supplier} that provides new instances of the lock type {@code L}.
         */
        LargeLazySegmentLock(final int stripes, final Supplier<L> supplier) {
            super(stripes);
            this.size = (mask == ALL_SET) ? Integer.MAX_VALUE : mask + 1;
            this.locks = new ConcurrentHashMap<>();
            this.supplier = supplier;
        }

        /**
         * Retrieves the lock segment at the specified index. If the segment does not exist, a new one is created and
         * stored in the map.
         *
         * @param index The index of the lock segment.
         * @return The lock segment at the specified index.
         * @throws IllegalArgumentException if the index is out of bounds (unless size is {@link Integer#MAX_VALUE}).
         */
        @Override
        public L getAt(final int index) {
            if (size != Integer.MAX_VALUE) {
                Assert.isTrue(index >= 0 && index < size, "Index out of bounds");
            }
            L existing = locks.get(index);
            if (existing != null) {
                return existing;
            }
            final L created = supplier.get();
            existing = locks.putIfAbsent(index, created);
            return existing != null ? existing : created;
        }

        /**
         * Returns the total number of segments.
         *
         * @return The number of segments.
         */
        @Override
        public int size() {
            return size;
        }
    }

    /**
     * 填充锁，避免缓存行干扰。
     */
    private static class PaddedLock extends ReentrantLock {

        @Serial
        private static final long serialVersionUID = 2852280575965L;

        long unused1;
        long unused2;
        long unused3;

        /**
         * Constructs a new {@code PaddedLock} with a non-fair locking policy.
         */
        PaddedLock() {
            super(false);
        }
    }

    /**
     * A {@link Semaphore} subclass that includes padding to avoid false sharing in highly contended scenarios. This can
     * improve performance in some multi-threaded applications.
     */
    private static class PaddedSemaphore extends Semaphore {

        @Serial
        private static final long serialVersionUID = 2852280626061L;

        /**
         * Constructs a new {@code PaddedSemaphore} with the given number of permits and a non-fair policy.
         *
         * @param permits The initial number of permits available.
         */
        PaddedSemaphore(final int permits) {
            super(permits, false);
        }
    }

}
