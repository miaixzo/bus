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

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Compact HTTP/2 settings storage.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Settings {

    /**
     * Header table size id.
     */
    public static final int HEADER_TABLE_SIZE = 1;

    /**
     * Enable push id.
     */
    public static final int ENABLE_PUSH = 2;

    /**
     * Max concurrent streams id.
     */
    public static final int MAX_CONCURRENT_STREAMS = 3;

    /**
     * Initial window size id.
     */
    public static final int INITIAL_WINDOW_SIZE = 4;

    /**
     * Max frame size id.
     */
    public static final int MAX_FRAME_SIZE = 5;

    /**
     * Max header list size id.
     */
    public static final int MAX_HEADER_LIST_SIZE = 6;

    /**
     * Setting slot count.
     */
    private static final int COUNT = 7;

    /**
     * Default header table size.
     */
    private static final int DEFAULT_HEADER_TABLE_SIZE = 4_096;

    /**
     * Default initial stream window.
     */
    private static final int DEFAULT_INITIAL_WINDOW = 65_535;

    /**
     * Default maximum frame size.
     */
    private static final int DEFAULT_MAX_FRAME_SIZE = 16_384;

    /**
     * Maximum unsigned 31-bit value.
     */
    private static final int MAX_UNSIGNED_31 = 0x7fffffff;

    /**
     * Largest legal frame size.
     */
    private static final int MAX_FRAME_SIZE_LIMIT = 16_777_215;

    /**
     * Setting values.
     */
    private final int[] values;

    /**
     * Bitset of explicitly set values.
     */
    private int set;

    /**
     * Creates default settings.
     */
    private Http2Settings() {
        this.values = new int[COUNT];
    }

    /**
     * Creates default settings.
     *
     * @return settings
     */
    public static Http2Settings defaults() {
        return new Http2Settings();
    }

    /**
     * Sets a value.
     *
     * @param id    id
     * @param value value
     */
    public void set(final int id, final int value) {
        validateId(id);
        validateValue(id, value);
        values[id] = value;
        set |= 1 << id;
    }

    /**
     * Gets a value.
     *
     * @param id id
     * @return value
     */
    public int get(final int id) {
        validateId(id);
        return isSet(id) ? values[id] : defaultValue(id);
    }

    /**
     * Returns whether a value is set.
     *
     * @param id id
     * @return true when set
     */
    public boolean isSet(final int id) {
        validateId(id);
        return (set & (1 << id)) != 0;
    }

    /**
     * Merges settings from another instance.
     *
     * @param other other settings
     */
    public void merge(final Http2Settings other) {
        if (other == null) {
            throw new ValidateException("HTTP/2 settings must not be null");
        }
        for (int id = 1; id < COUNT; id++) {
            if (other.isSet(id)) {
                set(id, other.get(id));
            }
        }
    }

    /**
     * Returns the initial window size.
     *
     * @return initial window size
     */
    public int initialWindowSize() {
        return get(INITIAL_WINDOW_SIZE);
    }

    /**
     * Returns the header compression table size.
     *
     * @return header table size
     */
    public int headerTableSize() {
        return get(HEADER_TABLE_SIZE);
    }

    /**
     * Returns the maximum concurrent stream count.
     *
     * @return max concurrent streams
     */
    public int maxConcurrentStreams() {
        return get(MAX_CONCURRENT_STREAMS);
    }

    /**
     * Returns the maximum frame payload size.
     *
     * @return max frame size
     */
    public int maxFrameSize() {
        return get(MAX_FRAME_SIZE);
    }

    /**
     * Returns the maximum header list size.
     *
     * @return max header list size
     */
    public int maxHeaderListSize() {
        return get(MAX_HEADER_LIST_SIZE);
    }

    /**
     * Returns a copy.
     *
     * @return copied settings
     */
    Http2Settings copy() {
        final Http2Settings copy = new Http2Settings();
        System.arraycopy(values, 0, copy.values, 0, values.length);
        copy.set = set;
        return copy;
    }

    /**
     * Returns setting ids that are explicitly set.
     *
     * @return ids
     */
    int[] ids() {
        final int[] ids = new int[Integer.bitCount(set)];
        int index = 0;
        for (int id = 1; id < COUNT; id++) {
            if (isSet(id)) {
                ids[index++] = id;
            }
        }
        return ids;
    }

    /**
     * Validates id.
     *
     * @param id id
     */
    private static void validateId(final int id) {
        if (id < HEADER_TABLE_SIZE || id > MAX_HEADER_LIST_SIZE) {
            throw new ValidateException("HTTP/2 setting id must be between 1 and 6");
        }
    }

    /**
     * Validates value.
     *
     * @param id    id
     * @param value value
     */
    private static void validateValue(final int id, final int value) {
        if (value < 0) {
            throw new ValidateException("HTTP/2 setting value must be non-negative");
        }
        if (id == ENABLE_PUSH && value != 0 && value != 1) {
            throw new ValidateException("HTTP/2 enable push must be 0 or 1");
        }
        if (id == HEADER_TABLE_SIZE && value > HpackCodec.MAX_DYNAMIC_TABLE_SIZE) {
            throw new ValidateException("HTTP/2 header table size is too large");
        }
        if (id == INITIAL_WINDOW_SIZE && value > MAX_UNSIGNED_31) {
            throw new ValidateException("HTTP/2 initial window is too large");
        }
        if (id == MAX_FRAME_SIZE && (value < DEFAULT_MAX_FRAME_SIZE || value > MAX_FRAME_SIZE_LIMIT)) {
            throw new ValidateException("HTTP/2 max frame size is out of range");
        }
    }

    /**
     * Returns a default value.
     *
     * @param id id
     * @return default value
     */
    private static int defaultValue(final int id) {
        return switch (id) {
            case HEADER_TABLE_SIZE -> DEFAULT_HEADER_TABLE_SIZE;
            case ENABLE_PUSH -> 1;
            case MAX_CONCURRENT_STREAMS, MAX_HEADER_LIST_SIZE -> MAX_UNSIGNED_31;
            case INITIAL_WINDOW_SIZE -> DEFAULT_INITIAL_WINDOW;
            case MAX_FRAME_SIZE -> DEFAULT_MAX_FRAME_SIZE;
            default -> throw new ValidateException("HTTP/2 setting id must be between 1 and 6");
        };
    }

}
