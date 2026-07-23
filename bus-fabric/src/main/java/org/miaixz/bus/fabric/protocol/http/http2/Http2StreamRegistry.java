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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Segmented registry addressed directly by the HTTP/2 stream identifier.
 *
 * <p>
 * Normal access performs one array lookup and one atomic slot operation without boxing. Synchronization is limited to
 * publishing a newly required segment.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class Http2StreamRegistry {

    /**
     * Bit shift dividing compact stream indexes into lazily allocated segments.
     */
    private static final int SEGMENT_SHIFT = 8;

    /**
     * Number of atomic stream slots in each segment.
     */
    private static final int SEGMENT_SIZE = 1 << SEGMENT_SHIFT;

    /**
     * Mask selecting the physical slot within a segment.
     */
    private static final int SEGMENT_MASK = SEGMENT_SIZE - 1;

    /**
     * Volatile directory of lazily allocated atomic stream segments.
     */
    private volatile AtomicReferenceArray<Http2StreamState>[] segments;

    /**
     * Atomic count incremented and decremented with successful slot publication and removal.
     */
    private final AtomicInteger active = new AtomicInteger();

    /**
     * Creates a registry with one empty directory position and no allocated segments.
     */
    Http2StreamRegistry() {
        segments = (AtomicReferenceArray<Http2StreamState>[]) new AtomicReferenceArray<?>[1];
    }

    /**
     * Registers a stream state exactly once.
     *
     * @param state state to register
     * @return {@code true} when the slot was empty
     */
    boolean open(final Http2StreamState state) {
        if (state == null) {
            throw new ValidateException("HTTP/2 stream state must not be null");
        }
        final int index = index(state.streamId());
        final AtomicReferenceArray<Http2StreamState> segment = segment(index, true);
        if (!segment.compareAndSet(index & SEGMENT_MASK, null, state)) {
            return false;
        }
        active.incrementAndGet();
        return true;
    }

    /**
     * Finds a stream without allocating or boxing its identifier.
     *
     * @param streamId positive HTTP/2 stream identifier
     * @return registered state, or {@code null}
     */
    Http2StreamState get(final int streamId) {
        final int index = index(streamId);
        final AtomicReferenceArray<Http2StreamState> segment = segment(index, false);
        return segment == null ? null : segment.get(index & SEGMENT_MASK);
    }

    /**
     * Removes a stream and immediately releases the registry reference.
     *
     * @param streamId positive HTTP/2 stream identifier
     * @return removed state, or {@code null}
     */
    Http2StreamState remove(final int streamId) {
        final int index = index(streamId);
        final AtomicReferenceArray<Http2StreamState> segment = segment(index, false);
        if (segment == null) {
            return null;
        }
        final int slot = index & SEGMENT_MASK;
        for (;;) {
            final Http2StreamState current = segment.get(slot);
            if (current == null) {
                return null;
            }
            if (segment.compareAndSet(slot, current, null)) {
                active.decrementAndGet();
                return current;
            }
        }
    }

    /**
     * Returns the current registered-stream count.
     *
     * @return number of non-null published stream slots
     */
    int activeCount() {
        return active.get();
    }

    /**
     * Fails and removes every registered stream.
     *
     * @param problem connection failure
     */
    void failAll(final RuntimeException problem) {
        if (problem == null) {
            throw new ValidateException("HTTP/2 registry failure must not be null");
        }
        final AtomicReferenceArray<Http2StreamState>[] snapshot = segments;
        for (final AtomicReferenceArray<Http2StreamState> segment : snapshot) {
            if (segment == null) {
                continue;
            }
            for (int slot = 0; slot < segment.length(); slot++) {
                final Http2StreamState state = segment.getAndSet(slot, null);
                if (state != null) {
                    active.decrementAndGet();
                    state.fail(problem);
                }
            }
        }
    }

    /**
     * Returns or creates the segment containing an unboxed stream index.
     *
     * @param index  compact stream index
     * @param create whether a missing segment should be allocated
     * @return segment, or {@code null}
     */
    private AtomicReferenceArray<Http2StreamState> segment(final int index, final boolean create) {
        final int segmentIndex = index >>> SEGMENT_SHIFT;
        AtomicReferenceArray<Http2StreamState>[] directory = segments;
        if (segmentIndex < directory.length && directory[segmentIndex] != null) {
            return directory[segmentIndex];
        }
        if (!create) {
            return null;
        }
        synchronized (this) {
            directory = segments;
            if (segmentIndex >= directory.length) {
                directory = Arrays.copyOf(directory, Math.max(segmentIndex + 1, directory.length << 1));
                segments = directory;
            }
            AtomicReferenceArray<Http2StreamState> segment = directory[segmentIndex];
            if (segment == null) {
                segment = new AtomicReferenceArray<>(SEGMENT_SIZE);
                directory[segmentIndex] = segment;
            }
            return segment;
        }
    }

    /**
     * Converts a valid stream identifier to its compact raw slot index.
     *
     * @param streamId stream identifier
     * @return compact index
     */
    private static int index(final int streamId) {
        if (streamId <= 0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
        return streamId >>> 1;
    }

}
