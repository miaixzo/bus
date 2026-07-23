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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.Builder;

/**
 * Compact HTTP/2 settings storage.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Settings {

    /**
     * Unsigned wire values indexed by standard setting identifier.
     */
    private final long[] values;

    /**
     * Bitset of explicitly set values.
     */
    private int set;

    /**
     * Creates default settings.
     */
    private Http2Settings() {
        this.values = new long[Normal._7];
    }

    /**
     * Creates default settings.
     *
     * @return mutable settings with no explicitly set fields
     */
    public static Http2Settings defaults() {
        return new Http2Settings();
    }

    /**
     * Sets a value.
     *
     * @param id    standard setting identifier in the inclusive range {@code 1..6}
     * @param value signed integer representation of the wire setting
     * @throws ValidateException if the identifier or setting-specific value is invalid
     */
    public void set(final int id, final int value) {
        set(id, (long) value);
    }

    /**
     * Sets an unsigned 32-bit wire value without signed truncation.
     *
     * @param id    setting id
     * @param value unsigned wire value
     * @throws ValidateException if the identifier or unsigned setting-specific value is invalid
     */
    public void set(final int id, final long value) {
        validateId(id);
        validateValue(id, value);
        values[id] = value;
        set |= Normal._1 << id;
    }

    /**
     * Gets a value.
     *
     * @param id standard setting identifier in the inclusive range {@code 1..6}
     * @return effective value saturated at {@link Integer#MAX_VALUE}
     * @throws ValidateException if the identifier is invalid
     */
    public int get(final int id) {
        return (int) Math.min(getLong(id), Integer.MAX_VALUE);
    }

    /**
     * Gets the exact unsigned 32-bit setting value.
     *
     * @param id setting id
     * @return explicitly stored or protocol-default unsigned value
     * @throws ValidateException if the identifier is invalid
     */
    public long getLong(final int id) {
        validateId(id);
        return isSet(id) ? values[id] : defaultValue(id);
    }

    /**
     * Returns whether a value is set.
     *
     * @param id standard setting identifier in the inclusive range {@code 1..6}
     * @return {@code true} when a peer or caller explicitly supplied the setting
     * @throws ValidateException if the identifier is invalid
     */
    public boolean isSet(final int id) {
        validateId(id);
        return (set & (Normal._1 << id)) != Normal._0;
    }

    /**
     * Merges settings from another instance.
     *
     * @param other settings whose explicitly present fields replace local fields
     * @throws ValidateException if {@code other} is {@code null}
     */
    public void merge(final Http2Settings other) {
        final Http2Settings checkedOther = Assert
                .notNull(other, () -> new ValidateException("HTTP/2 settings must not be null"));
        final int present = checkedOther.set;
        for (int id = Normal._1; id < Normal._7; id++) {
            if ((present & (Normal._1 << id)) != 0) {
                values[id] = checkedOther.values[id];
            }
        }
        set |= present;
    }

    /**
     * Returns the initial window size.
     *
     * @return effective per-stream initial flow-control window
     */
    public int initialWindowSize() {
        return get(Http.Setting.INITIAL_WINDOW_SIZE_ID);
    }

    /**
     * Returns the header compression table size.
     *
     * @return effective HPACK dynamic-table capacity
     */
    public int headerTableSize() {
        return get(Http.Setting.HEADER_TABLE_SIZE_ID);
    }

    /**
     * Returns the maximum concurrent stream count.
     *
     * @return effective concurrent-stream limit saturated at {@link Integer#MAX_VALUE}
     */
    public int maxConcurrentStreams() {
        return get(Http.Setting.MAX_CONCURRENT_STREAMS_ID);
    }

    /**
     * Returns the peer stream limit as an exact uint32 fact.
     *
     * @return unsigned peer stream limit as a long
     */
    public long maxConcurrentStreamsUnsigned() {
        return getLong(Http.Setting.MAX_CONCURRENT_STREAMS_ID);
    }

    /**
     * Returns the maximum frame payload size.
     *
     * @return effective maximum HTTP/2 frame payload size
     */
    public int maxFrameSize() {
        return get(Http.Setting.MAX_FRAME_SIZE_ID);
    }

    /**
     * Returns the maximum header list size.
     *
     * @return effective header-list limit saturated at {@link Integer#MAX_VALUE}
     */
    public int maxHeaderListSize() {
        return get(Http.Setting.MAX_HEADER_LIST_SIZE_ID);
    }

    /**
     * Returns the maximum header-list size as an exact uint32 fact.
     *
     * @return unsigned maximum header-list size as a long
     */
    public long maxHeaderListSizeUnsigned() {
        return getLong(Http.Setting.MAX_HEADER_LIST_SIZE_ID);
    }

    /**
     * Returns a copy.
     *
     * @return independent mutable copy preserving values and explicit-presence bits
     */
    Http2Settings copy() {
        final Http2Settings copy = new Http2Settings();
        System.arraycopy(values, Normal._0, copy.values, Normal._0, values.length);
        copy.set = set;
        return copy;
    }

    /**
     * Returns setting ids that are explicitly set.
     *
     * @return ascending array of standard identifiers explicitly present in this instance
     */
    int[] ids() {
        final int[] ids = new int[Integer.bitCount(set)];
        int index = Normal._0;
        for (int id = Normal._1; id < Normal._7; id++) {
            if ((set & (Normal._1 << id)) != Normal._0) {
                ids[index++] = id;
            }
        }
        return ids;
    }

    /**
     * Validates id.
     *
     * @param id candidate standard setting identifier
     * @throws ValidateException if the identifier is outside {@code 1..6}
     */
    private static void validateId(final int id) {
        if (id < Http.Setting.HEADER_TABLE_SIZE_ID || id > Http.Setting.MAX_HEADER_LIST_SIZE_ID) {
            throw new ValidateException("HTTP/2 setting id must be between 1 and 6");
        }
    }

    /**
     * Validates value.
     *
     * @param id    validated standard setting identifier
     * @param value candidate unsigned wire value
     * @throws ValidateException if the value exceeds uint32 or violates setting-specific constraints
     */
    private static void validateValue(final int id, final long value) {
        if (value < Normal._0 || value > Builder.UNSIGNED_INT_MASK) {
            throw new ValidateException("HTTP/2 setting value must be an unsigned 32-bit integer");
        }
        if (id == Http.Setting.ENABLE_PUSH_ID && value != Normal._0 && value != Normal._1) {
            throw new ValidateException("HTTP/2 enable push must be 0 or 1");
        }
        if (id == Http.Setting.INITIAL_WINDOW_SIZE_ID && value > Integer.MAX_VALUE) {
            throw new ValidateException("HTTP/2 initial window is too large");
        }
        if (id == Http.Setting.MAX_FRAME_SIZE_ID
                && (value < Builder.HTTP2_DEFAULT_MAX_FRAME_SIZE || value > (int) (Builder.BYTES_16_MIB - Normal._1))) {
            throw new ValidateException("HTTP/2 max frame size is out of range");
        }
    }

    /**
     * Returns a default value.
     *
     * @param id standard setting identifier
     * @return protocol default, using uint32 maximum for unspecified unbounded limits
     * @throws ValidateException if the identifier is outside {@code 1..6}
     */
    private static long defaultValue(final int id) {
        return switch (id) {
            case Http.Setting.HEADER_TABLE_SIZE_ID -> Normal._4096;
            case Http.Setting.ENABLE_PUSH_ID -> Normal._1;
            case Http.Setting.MAX_CONCURRENT_STREAMS_ID, Http.Setting.MAX_HEADER_LIST_SIZE_ID -> Builder.UNSIGNED_INT_MASK;
            case Http.Setting.INITIAL_WINDOW_SIZE_ID -> Http.Setting.DEFAULT_INITIAL_WINDOW_SIZE;
            case Http.Setting.MAX_FRAME_SIZE_ID -> Builder.HTTP2_DEFAULT_MAX_FRAME_SIZE;
            default -> throw new ValidateException("HTTP/2 setting id must be between 1 and 6");
        };
    }

}
