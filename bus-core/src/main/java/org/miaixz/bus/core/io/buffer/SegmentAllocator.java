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
package org.miaixz.bus.core.io.buffer;

import org.miaixz.bus.core.lang.Normal;

/**
 * Allocates reusable {@link Segment} instances through striped per-thread buckets.
 * <p>
 * The allocator keeps a byte limit per bucket and stores released segments in thread-selected buckets. This avoids a
 * single global monitor on the read/write hot path while keeping retained memory bounded.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SegmentAllocator {

    /**
     * The maximum number of bytes retained by each bucket.
     */
    public static final long MAX_SIZE = Normal._64 * Normal._1024;

    /**
     * The number of striped buckets used by the allocator.
     */
    private static final int BUCKET_COUNT = bucketCount();

    /**
     * The per-bucket locks.
     */
    private static final Object[] LOCKS = locks();

    /**
     * The per-bucket stack heads.
     */
    private static final Segment[] BUCKETS = new Segment[BUCKET_COUNT];

    /**
     * Private constructor to prevent instantiation.
     */
    private SegmentAllocator() {
        // No initialization required.
    }

    /**
     * Allocates a segment from the current thread's bucket or creates a new segment when the bucket is empty.
     *
     * @return a reusable {@link Segment}
     */
    public static Segment allocate() {
        int bucket = bucketIndex();
        synchronized (LOCKS[bucket]) {
            Segment head = BUCKETS[bucket];
            if (head == null) {
                final Segment created = new Segment();
                created.poolBucket = bucket;
                return created;
            }
            Segment next = head.next;
            BUCKETS[bucket] = next;
            head.next = null;
            head.prev = null;
            head.pos = 0;
            head.limit = 0;
            head.cached = false;
            return head;
        }
    }

    /**
     * Releases an unshared, detached segment into the current thread's bucket.
     *
     * @param segment the segment to release
     * @throws IllegalArgumentException if the segment is null, still linked, or already cached.
     */
    public static void release(Segment segment) {
        if (segment == null) {
            throw new IllegalArgumentException("segment == null");
        }
        if (segment.next != null || segment.prev != null) {
            throw new IllegalArgumentException("segment is still linked");
        }
        if (segment.shared) {
            return;
        }
        if (segment.cached) {
            throw new IllegalArgumentException("segment is already cached");
        }

        int bucket = segment.poolBucket >= 0 ? segment.poolBucket : bucketIndex();
        synchronized (LOCKS[bucket]) {
            Segment head = BUCKETS[bucket];
            int bucketByteCount = head != null ? head.limit : 0;
            if (bucketByteCount + Segment.SIZE > MAX_SIZE) {
                segment.next = null;
                segment.pos = 0;
                segment.limit = 0;
                return;
            }
            segment.next = head;
            segment.pos = 0;
            segment.limit = bucketByteCount + Segment.SIZE;
            segment.cached = true;
            BUCKETS[bucket] = segment;
        }
    }

    /**
     * Returns the current retained byte count.
     *
     * @return the retained byte count
     */
    public static long byteCount() {
        long byteCount = 0L;
        for (int i = 0; i < BUCKET_COUNT; i++) {
            synchronized (LOCKS[i]) {
                Segment head = BUCKETS[i];
                if (head != null) {
                    byteCount += head.limit;
                }
            }
        }
        return byteCount;
    }

    /**
     * Returns the bucket index for the current thread.
     *
     * @return the current thread's bucket index
     */
    private static int bucketIndex() {
        return (int) (Thread.currentThread().threadId() & (BUCKET_COUNT - 1));
    }

    /**
     * Returns a power-of-two bucket count based on the available processors.
     *
     * @return the bucket count
     */
    private static int bucketCount() {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        return Math.max(1, Integer.highestOneBit((cpuCount * 2) - 1));
    }

    /**
     * Creates one lock per bucket.
     *
     * @return the bucket locks
     */
    private static Object[] locks() {
        Object[] locks = new Object[BUCKET_COUNT];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }
        return locks;
    }

}
