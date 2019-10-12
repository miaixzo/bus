/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.internal.http.second;

import java.util.Arrays;

/**
 * Settings describe characteristics of the sending peer, which are used by the receiving peer.
 * Settings are {@link Http2Connection connection} scoped.
 *
 * @author Kimi Liu
 * @version 5.0.0
 * @since JDK 1.8+
 */
public final class Settings {

    /**
     * From the HTTP/2 specs, the default initial window size for all streams is 64 KiB. (Chrome 25
     * uses 10 MiB).
     */
    static final int DEFAULT_INITIAL_WINDOW_SIZE = 65535;

    /**
     * HTTP/2: Size in bytes of the table used to decode the sender's header blocks.
     */
    static final int HEADER_TABLE_SIZE = 1;
    /**
     * HTTP/2: The peer must not send a PUSH_PROMISE frame when this is 0.
     */
    static final int ENABLE_PUSH = 2;
    /**
     * Sender's maximum number of concurrent streams.
     */
    static final int MAX_CONCURRENT_STREAMS = 4;
    /**
     * HTTP/2: Size in bytes of the largest frame payload the sender will accept.
     */
    static final int MAX_FRAME_SIZE = 5;
    /**
     * HTTP/2: Advisory only. Size in bytes of the largest header list the sender will accept.
     */
    static final int MAX_HEADER_LIST_SIZE = 6;
    /**
     * Window size in bytes.
     */
    static final int INITIAL_WINDOW_SIZE = 7;

    /**
     * Total number of settings.
     */
    static final int COUNT = 10;
    /**
     * Flag values.
     */
    private final int[] values = new int[COUNT];
    /**
     * Bitfield of which flags that values.
     */
    private int set;

    void clear() {
        set = 0;
        Arrays.fill(values, 0);
    }

    Settings set(int id, int value) {
        if (id < 0 || id >= values.length) {
            return this; // Discard unknown settings.
        }

        int bit = 1 << id;
        set |= bit;
        values[id] = value;
        return this;
    }

    boolean isSet(int id) {
        int bit = 1 << id;
        return (set & bit) != 0;
    }

    int get(int id) {
        return values[id];
    }

    int size() {
        return Integer.bitCount(set);
    }

    int getHeaderTableSize() {
        int bit = 1 << HEADER_TABLE_SIZE;
        return (bit & set) != 0 ? values[HEADER_TABLE_SIZE] : -1;
    }

    boolean getEnablePush(boolean defaultValue) {
        int bit = 1 << ENABLE_PUSH;
        return ((bit & set) != 0 ? values[ENABLE_PUSH] : defaultValue ? 1 : 0) == 1;
    }

    int getMaxConcurrentStreams(int defaultValue) {
        int bit = 1 << MAX_CONCURRENT_STREAMS;
        return (bit & set) != 0 ? values[MAX_CONCURRENT_STREAMS] : defaultValue;
    }

    int getMaxFrameSize(int defaultValue) {
        int bit = 1 << MAX_FRAME_SIZE;
        return (bit & set) != 0 ? values[MAX_FRAME_SIZE] : defaultValue;
    }

    int getMaxHeaderListSize(int defaultValue) {
        int bit = 1 << MAX_HEADER_LIST_SIZE;
        return (bit & set) != 0 ? values[MAX_HEADER_LIST_SIZE] : defaultValue;
    }

    int getInitialWindowSize() {
        int bit = 1 << INITIAL_WINDOW_SIZE;
        return (bit & set) != 0 ? values[INITIAL_WINDOW_SIZE] : DEFAULT_INITIAL_WINDOW_SIZE;
    }

    void merge(Settings other) {
        for (int i = 0; i < COUNT; i++) {
            if (!other.isSet(i)) continue;
            set(i, other.get(i));
        }
    }

}
