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
package org.miaixz.bus.core.io.buffer;

import org.miaixz.bus.core.lang.Normal;

/**
 * 快速缓冲抽象类，用于快速读取、写入数据到缓冲区，减少内存复制 相对于普通Buffer，使用二维数组扩展长度，减少内存复制，提升性能
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FastBuffer {

    /**
     * 一个缓冲区的最小字节数
     */
    protected final int minChunkLen;

    /**
     * 缓冲数
     */
    protected int buffersCount;
    /**
     * 当前缓冲索引
     */
    protected int currentBufferIndex = -1;
    /**
     * 当前缓冲偏移量
     */
    protected int offset;
    /**
     * 缓冲字节数
     */
    protected int size;

    /**
     * 构造
     *
     * @param size 一个缓冲区的最小字节数
     */
    public FastBuffer(int size) {
        if (size <= 0) {
            size = Normal._8192;
        }
        this.minChunkLen = Math.abs(size);
    }

    /**
     * 当前缓冲位于缓冲区的索引位
     *
     * @return {@link #currentBufferIndex}
     */
    public int index() {
        return this.currentBufferIndex;
    }

    /**
     * 获取当前缓冲偏移量
     *
     * @return 当前缓冲偏移量
     */
    public int offset() {
        return this.offset;
    }

    /**
     * 复位缓冲
     */
    public void reset() {
        this.size = 0;
        this.offset = 0;
        this.currentBufferIndex = -1;
        this.buffersCount = 0;
    }

    /**
     * 获取缓冲总长度
     *
     * @return 缓冲总长度
     */
    public int size() {
        return this.size;
    }

    /**
     * 获取缓冲总长度
     *
     * @return 缓冲总长度
     */
    public int length() {
        return this.size;
    }

    /**
     * 是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * 检查现有缓冲区是否满足capacity，不满足则分配新的区域分配下一个缓冲区，不会小于1024
     *
     * @param capacity 理想缓冲区字节数
     */
    abstract protected void ensureCapacity(final int capacity);

}
