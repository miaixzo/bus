/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.metric.http;

import java.util.Arrays;

import org.miaixz.bus.core.net.HTTP;

/**
 * 设置描述发送对等点的特征，接收对等点使用这些特征 设置的作用域是{@link Http2Connection connection}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Http2Settings {

    /**
     * 设置总数
     */
    public static final int COUNT = 10;
    /**
     * 标志值
     */
    private final int[] values = new int[COUNT];
    /**
     * Bitfield值标志.
     */
    private int set;

    public void clear() {
        set = 0;
        Arrays.fill(values, 0);
    }

    public Http2Settings set(int id, int value) {
        if (id < 0 || id >= values.length) {
            return this;
        }

        int bit = 1 << id;
        set |= bit;
        values[id] = value;
        return this;
    }

    /**
     * 如果为设置{@code id}分配了一个值，则返回true
     *
     * @param id 标记
     * @return the true/false
     */
    public boolean isSet(int id) {
        int bit = 1 << id;
        return (set & bit) != 0;
    }

    /**
     * 设置{@code id}的值，如果未设置则返回0
     *
     * @param id 标记
     * @return the id
     */
    public int get(int id) {
        return values[id];
    }

    /**
     * 具有指定值的设置的大小
     *
     * @return 设置的大小
     */
    public int size() {
        return Integer.bitCount(set);
    }

    public int getHeaderTableSize() {
        int bit = 1 << HTTP.HEADER_TABLE_SIZE;
        return (bit & set) != 0 ? values[HTTP.HEADER_TABLE_SIZE] : -1;
    }

    public boolean getEnablePush(boolean defaultValue) {
        int bit = 1 << HTTP.ENABLE_PUSH;
        return ((bit & set) != 0 ? values[HTTP.ENABLE_PUSH] : defaultValue ? 1 : 0) == 1;
    }

    public int getMaxConcurrentStreams(int defaultValue) {
        int bit = 1 << HTTP.MAX_CONCURRENT_STREAMS;
        return (bit & set) != 0 ? values[HTTP.MAX_CONCURRENT_STREAMS] : defaultValue;
    }

    public int getMaxFrameSize(int defaultValue) {
        int bit = 1 << HTTP.MAX_FRAME_SIZE;
        return (bit & set) != 0 ? values[HTTP.MAX_FRAME_SIZE] : defaultValue;
    }

    public int getMaxHeaderListSize(int defaultValue) {
        int bit = 1 << HTTP.MAX_HEADER_LIST_SIZE;
        return (bit & set) != 0 ? values[HTTP.MAX_HEADER_LIST_SIZE] : defaultValue;
    }

    public int getInitialWindowSize() {
        int bit = 1 << HTTP.INITIAL_WINDOW_SIZE;
        return (bit & set) != 0 ? values[HTTP.INITIAL_WINDOW_SIZE] : HTTP.DEFAULT_INITIAL_WINDOW_SIZE;
    }

    /**
     * 将{@code other}写入其中。如果使用this和{@code other} 填充任何设置，则将保留{@code other}中的值和标志
     *
     * @param other 设置信息
     */
    public void merge(Http2Settings other) {
        for (int i = 0; i < COUNT; i++) {
            if (!other.isSet(i))
                continue;
            set(i, other.get(i));
        }
    }

}
