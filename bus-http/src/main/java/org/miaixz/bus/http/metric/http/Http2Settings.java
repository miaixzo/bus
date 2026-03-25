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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.net.HTTP;

import java.util.Arrays;

/**
 * Settings describe characteristics of the sending peer, which are used by the receiving peer. Settings are scoped to
 * an {@link Http2Connection connection}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Settings {

    /**
     * The total number of settings.
     */
    public static final int COUNT = 10;
    /**
     * The array of setting values.
     */
    private final int[] values = new int[COUNT];
    /**
     * A bitfield of which settings are set.
     */
    private int set;

    /**
     * Clears all settings.
     */
    public void clear() {
        set = 0;
        Arrays.fill(values, 0);
    }

    /**
     * Sets a setting with the given ID and value.
     *
     * @param id    The ID of the setting.
     * @param value The value of the setting.
     * @return This {@code Http2Settings} instance.
     */
    public Http2Settings set(int id, int value) {
        if (id < 0 || id >= values.length) {
            return this; // Ignore invalid IDs.
        }

        int bit = 1 << id;
        set |= bit;
        values[id] = value;
        return this;
    }

    /**
     * Returns true if a value has been assigned for the setting {@code id}.
     *
     * @param id The setting ID.
     * @return {@code true} if the setting is set.
     */
    public boolean isSet(int id) {
        int bit = 1 << id;
        return (set & bit) != 0;
    }

    /**
     * Returns the value for the setting {@code id}, or 0 if not set.
     *
     * @param id The setting ID.
     * @return The value of the setting.
     */
    public int get(int id) {
        return values[id];
    }

    /**
     * Returns the number of settings that have been set.
     *
     * @return The number of set settings.
     */
    public int size() {
        return Integer.bitCount(set);
    }

    /**
     * Returns the header table size, or -1 if not set.
     *
     * @return The header table size.
     */
    public int getHeaderTableSize() {
        int bit = 1 << HTTP.HEADER_TABLE_SIZE;
        return (bit & set) != 0 ? values[HTTP.HEADER_TABLE_SIZE] : -1;
    }

    /**
     * Returns whether server push is enabled.
     *
     * @param defaultValue The default value to return if not set.
     * @return {@code true} if server push is enabled.
     */
    public boolean getEnablePush(boolean defaultValue) {
        int bit = 1 << HTTP.ENABLE_PUSH;
        return ((bit & set) != 0 ? values[HTTP.ENABLE_PUSH] : (defaultValue ? 1 : 0)) == 1;
    }

    /**
     * Returns the maximum number of concurrent streams, or the default value if not set.
     *
     * @param defaultValue The default value.
     * @return The maximum number of concurrent streams.
     */
    public int getMaxConcurrentStreams(int defaultValue) {
        int bit = 1 << HTTP.MAX_CONCURRENT_STREAMS;
        return (bit & set) != 0 ? values[HTTP.MAX_CONCURRENT_STREAMS] : defaultValue;
    }

    /**
     * Returns the maximum frame size, or the default value if not set.
     *
     * @param defaultValue The default value.
     * @return The maximum frame size.
     */
    public int getMaxFrameSize(int defaultValue) {
        int bit = 1 << HTTP.MAX_FRAME_SIZE;
        return (bit & set) != 0 ? values[HTTP.MAX_FRAME_SIZE] : defaultValue;
    }

    /**
     * Returns the maximum header list size, or the default value if not set.
     *
     * @param defaultValue The default value.
     * @return The maximum header list size.
     */
    public int getMaxHeaderListSize(int defaultValue) {
        int bit = 1 << HTTP.MAX_HEADER_LIST_SIZE;
        return (bit & set) != 0 ? values[HTTP.MAX_HEADER_LIST_SIZE] : defaultValue;
    }

    /**
     * Returns the initial window size.
     *
     * @return The initial window size.
     */
    public int getInitialWindowSize() {
        int bit = 1 << HTTP.INITIAL_WINDOW_SIZE;
        return (bit & set) != 0 ? values[HTTP.INITIAL_WINDOW_SIZE] : HTTP.DEFAULT_INITIAL_WINDOW_SIZE;
    }

    /**
     * Merges the settings from {@code other} into this. If any setting is populated by both this and {@code other}, the
     * value and flags from {@code other} will be kept.
     *
     * @param other The other settings to merge.
     */
    public void merge(Http2Settings other) {
        for (int i = 0; i < COUNT; i++) {
            if (!other.isSet(i))
                continue;
            set(i, other.get(i));
        }
    }

}
