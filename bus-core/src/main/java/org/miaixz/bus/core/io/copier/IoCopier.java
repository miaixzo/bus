/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.io.copier;

import org.miaixz.bus.core.io.Progress;
import org.miaixz.bus.core.toolkit.IoKit;

/**
 * IO拷贝抽象，可自定义包括缓存、进度条等信息
 * 此对象非线程安全
 *
 * @param <S> 拷贝源类型，如InputStream、Reader等
 * @param <T> 拷贝目标类型，如OutputStream、Writer等
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class IoCopier<S, T> {

    protected final int bufferSize;
    /**
     * 拷贝总数
     */
    protected final long count;

    /**
     * 进度条
     */
    protected Progress progress;

    /**
     * 是否每次写出一个buffer内容就执行flush
     */
    protected boolean flushEveryBuffer;

    /**
     * 构造
     *
     * @param bufferSize 缓存大小，&lt; 0 表示默认{@link IoKit#DEFAULT_BUFFER_SIZE}
     * @param count      拷贝总数，-1表示无限制
     * @param progress   进度条
     */
    public IoCopier(int bufferSize, long count, Progress progress) {
        this.bufferSize = bufferSize > 0 ? bufferSize : IoKit.DEFAULT_BUFFER_SIZE;
        this.count = count <= 0 ? Long.MAX_VALUE : count;
        this.progress = progress;
    }

    /**
     * 执行拷贝
     *
     * @param source 拷贝源，如InputStream、Reader等
     * @param target 拷贝目标，如OutputStream、Writer等
     * @return 拷贝的实际长度
     */
    public abstract long copy(S source, T target);

    /**
     * 缓存大小，取默认缓存和目标长度最小值
     *
     * @param count 目标长度
     * @return 缓存大小
     */
    protected int bufferSize(long count) {
        return (int) Math.min(this.bufferSize, count);
    }

    /**
     * 设置是否每次写出一个buffer内容就执行flush
     *
     * @param flushEveryBuffer 是否每次写出一个buffer内容就执行flush
     * @return this
     */
    public IoCopier<S, T> setFlushEveryBuffer(boolean flushEveryBuffer) {
        this.flushEveryBuffer = flushEveryBuffer;
        return this;
    }

}
