/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.io;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ByteBuffer内存池
 *
 * @author Kimi Liu
 * @version 5.9.6
 * @since JDK 1.8+
 */
public class BufferPool {

    private static Timer timer = new Timer("BufferPoolClean", true);
    private PageBuffer[] pageBufferList;
    /**
     * 内存页游标
     */
    private volatile AtomicInteger cursor = new AtomicInteger(0);

    /**
     * @param pageSize 内存页大小
     * @param poolSize 内存页个数
     * @param isDirect 是否使用直接缓冲区
     */
    public BufferPool(final int pageSize, final int poolSize, final boolean isDirect) {
        pageBufferList = new PageBuffer[poolSize];
        for (int i = 0; i < poolSize; i++) {
            pageBufferList[i] = new PageBuffer(pageSize, isDirect);
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (PageBuffer pageBuffer : pageBufferList) {
                    pageBuffer.tryClean();
                }
            }
        }, 500, 1000);
    }

    /**
     * 申请内存页
     *
     * @return 缓存页对象
     */
    public PageBuffer allocateBufferPage() {
        //轮训游标,均衡分配内存页
        return pageBufferList[cursor.getAndIncrement() % pageBufferList.length];
    }

}
