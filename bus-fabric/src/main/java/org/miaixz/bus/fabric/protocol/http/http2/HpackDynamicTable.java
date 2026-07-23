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

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Allocation-stable ring implementation of the RFC 7541 dynamic table.
 *
 * <p>
 * Entries are stored newest first through a moving head. Capacity changes evict old entries in place and never shift
 * surviving entries. Exact and name lookup use precomputed hashes before comparing strings.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HpackDynamicTable {

    /**
     * Fixed ring slots containing live headers and cleared null entries.
     */
    private final Http2Header[] entries;

    /**
     * Precomputed combined name-and-value hashes aligned with {@link #entries}.
     */
    private final int[] exactHashes;

    /**
     * Precomputed field-name hashes aligned with {@link #entries}.
     */
    private final int[] nameHashes;

    /**
     * Precomputed HPACK entry sizes aligned with {@link #entries}.
     */
    private final int[] sizes;

    /**
     * Immutable allocation-time upper bound for effective table capacity.
     */
    private final int maximumBytes;

    /**
     * Physical ring index of the newest live entry, or zero when empty.
     */
    private int head;

    /**
     * Number of live entries retained in the ring.
     */
    private int count;

    /**
     * Sum of the RFC 7541 sizes of all live entries.
     */
    private int bytes;

    /**
     * Current effective byte capacity, bounded by {@link #maximumBytes}.
     */
    private int capacityBytes;

    /**
     * Creates a table with a fixed maximum allocation.
     *
     * @param maximumBytes non-negative hard maximum bytes
     */
    HpackDynamicTable(final int maximumBytes) {
        if (maximumBytes < 0) {
            throw new ValidateException("HPACK dynamic table maximum must not be negative");
        }
        this.maximumBytes = maximumBytes;
        this.capacityBytes = maximumBytes;
        final int slots = Math.max(1, maximumBytes / Builder.HTTP2_HPACK_ENTRY_OVERHEAD_BYTES);
        this.entries = new Http2Header[slots];
        this.exactHashes = new int[slots];
        this.nameHashes = new int[slots];
        this.sizes = new int[slots];
    }

    /**
     * Updates the effective capacity and evicts only as needed.
     *
     * @param value new effective byte capacity
     */
    void capacityBytes(final int value) {
        if (value < 0 || value > maximumBytes) {
            throw new ValidateException("HPACK dynamic table capacity exceeds configured maximum");
        }
        capacityBytes = value;
        evictToFit(0);
    }

    /**
     * Returns the effective byte capacity.
     *
     * @return current peer-selected capacity in bytes
     */
    int capacityBytes() {
        return capacityBytes;
    }

    /**
     * Returns the total RFC 7541 size of retained entries.
     *
     * @return retained byte count
     */
    int bytes() {
        return bytes;
    }

    /**
     * Returns the number of retained entries.
     *
     * @return live entry count
     */
    int count() {
        return count;
    }

    /**
     * Inserts an entry as the newest dynamic-table value.
     *
     * @param header validated immutable header
     */
    void insert(final Http2Header header) {
        if (header == null) {
            throw new ValidateException("HPACK dynamic table entry must not be null");
        }
        final int size = header.hpackSize();
        if (size > capacityBytes) {
            clear();
            return;
        }
        evictToFit(size);
        head = count == 0 ? 0 : decrement(head);
        entries[head] = header;
        nameHashes[head] = header.name().hashCode();
        exactHashes[head] = exactHash(header.name(), header.value());
        sizes[head] = size;
        count++;
        bytes += size;
    }

    /**
     * Returns a one-based dynamic entry, where one is newest.
     *
     * @param dynamicIndex one-based dynamic index
     * @return retained header
     */
    Http2Header get(final int dynamicIndex) {
        if (dynamicIndex <= 0 || dynamicIndex > count) {
            throw new ProtocolException("Invalid HPACK dynamic table index");
        }
        return entries[slot(dynamicIndex - 1)];
    }

    /**
     * Finds an exact header without constructing a temporary key.
     *
     * @param name  field name
     * @param value field value
     * @return one-based dynamic index, or zero
     */
    int findExact(final String name, final String value) {
        final int hash = exactHash(name, value);
        for (int offset = 0; offset < count; offset++) {
            final int slot = slot(offset);
            final Http2Header entry = entries[slot];
            if (exactHashes[slot] == hash && entry.name().equals(name) && entry.value().equals(value)) {
                return offset + 1;
            }
        }
        return 0;
    }

    /**
     * Finds a field name without constructing a temporary key.
     *
     * @param name field name
     * @return one-based dynamic index, or zero
     */
    int findName(final String name) {
        final int hash = name.hashCode();
        for (int offset = 0; offset < count; offset++) {
            final int slot = slot(offset);
            if (nameHashes[slot] == hash && entries[slot].name().equals(name)) {
                return offset + 1;
            }
        }
        return 0;
    }

    /**
     * Evicts every live entry, clears its aligned metadata, and resets the ring head.
     */
    void clear() {
        while (count != 0) {
            removeOldest();
        }
        head = 0;
    }

    /**
     * Evicts oldest entries until an insertion fits.
     *
     * @param incomingSize size of the pending entry
     */
    private void evictToFit(final int incomingSize) {
        while (count != 0 && (bytes + incomingSize > capacityBytes || count == entries.length)) {
            removeOldest();
        }
    }

    /**
     * Removes the oldest live entry and clears its header and aligned metadata references.
     */
    private void removeOldest() {
        final int oldest = slot(count - 1);
        bytes -= sizes[oldest];
        entries[oldest] = null;
        exactHashes[oldest] = 0;
        nameHashes[oldest] = 0;
        sizes[oldest] = 0;
        count--;
    }

    /**
     * Converts a newest-relative offset to a physical ring slot.
     *
     * @param offset zero-based newest-relative offset
     * @return physical slot
     */
    private int slot(final int offset) {
        final int value = head + offset;
        return value >= entries.length ? value - entries.length : value;
    }

    /**
     * Decrements a ring index.
     *
     * @param value current slot
     * @return previous slot with wraparound
     */
    private int decrement(final int value) {
        return value == 0 ? entries.length - 1 : value - 1;
    }

    /**
     * Computes a stable exact header hash.
     *
     * @param name  field name
     * @param value field value
     * @return combined hash
     */
    private static int exactHash(final String name, final String value) {
        return 31 * name.hashCode() + value.hashCode();
    }

}
