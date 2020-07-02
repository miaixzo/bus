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

import org.aoju.bus.core.lang.Normal;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ByteBuffer内存页
 *
 * @author Kimi Liu
 * @version 6.0.1
 * @since JDK 1.8+
 */
public class PageBuffer {

    /**
     * 当前空闲的虚拟Buffer
     */
    private List<VirtualBuffer> availableBuffers;
    /**
     * 待回收的虚拟Buffer
     */
    private ConcurrentLinkedQueue<VirtualBuffer> cleanBuffers = new ConcurrentLinkedQueue<>();

    /**
     * 当前缓存页的物理缓冲区
     */
    private ByteBuffer buffer;
    private ReentrantLock lock = new ReentrantLock();

    private long lastAllocateTime;

    /**
     * @param size   缓存页大小
     * @param direct 是否使用堆外内存
     */
    PageBuffer(int size, boolean direct) {
        availableBuffers = new LinkedList<>();
        this.buffer = allocate0(size, direct);
        availableBuffers.add(new VirtualBuffer(this, null, buffer.position(), buffer.limit()));
    }

    /**
     * 申请物理内存页空间
     *
     * @param size   物理空间大小
     * @param direct true:堆外缓冲区,false:堆内缓冲区
     * @return 缓冲区
     */
    private ByteBuffer allocate0(int size, boolean direct) {
        return direct ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocate(size);
    }

    public VirtualBuffer allocate(final int size) {
        lastAllocateTime = System.currentTimeMillis();
        VirtualBuffer cleanBuffer = cleanBuffers.poll();
        if (cleanBuffer != null && cleanBuffer.getParentLimit() - cleanBuffer.getParentPosition() >= size) {
            cleanBuffer.buffer().clear();
            cleanBuffer.buffer(cleanBuffer.buffer());
            return cleanBuffer;
        }
        lock.lock();
        try {
            if (cleanBuffer != null) {
                clean0(cleanBuffer);
            }
            while ((cleanBuffer = cleanBuffers.poll()) != null) {
                if (cleanBuffer.getParentLimit() - cleanBuffer.getParentPosition() >= size) {
                    cleanBuffer.buffer().clear();
                    cleanBuffer.buffer(cleanBuffer.buffer());
                    return cleanBuffer;
                } else {
                    clean0(cleanBuffer);
                }
            }
            Iterator<VirtualBuffer> iterator = availableBuffers.iterator();
            VirtualBuffer bufferChunk;
            while (iterator.hasNext()) {
                VirtualBuffer freeChunk = iterator.next();
                final int remaining = freeChunk.getParentLimit() - freeChunk.getParentPosition();
                if (remaining < size) {
                    continue;
                }
                if (remaining == size) {
                    iterator.remove();
                    buffer.limit(freeChunk.getParentLimit());
                    buffer.position(freeChunk.getParentPosition());
                    freeChunk.buffer(buffer.slice());
                    bufferChunk = freeChunk;
                } else {
                    buffer.limit(freeChunk.getParentPosition() + size);
                    buffer.position(freeChunk.getParentPosition());
                    bufferChunk = new VirtualBuffer(this, buffer.slice(), buffer.position(), buffer.limit());
                    freeChunk.setParentPosition(buffer.limit());
                }
                if (bufferChunk.buffer().remaining() != size) {
                    throw new RuntimeException("allocate " + size + ", buffer:" + bufferChunk);
                }
                return bufferChunk;
            }
        } finally {
            lock.unlock();
        }
        return new VirtualBuffer(null, allocate0(size, false), 0, 0);
    }

    void clean(VirtualBuffer cleanBuffer) {
        if (cleanBuffers.offer(cleanBuffer)) {
            return;
        }
        lock.lock();
        try {
            clean0(cleanBuffer);
        } finally {
            lock.unlock();
        }
    }

    void tryClean() {
        if (System.currentTimeMillis() - lastAllocateTime < 1000 || !lock.tryLock()) {
            return;
        }
        try {
            VirtualBuffer cleanBuffer;
            while ((cleanBuffer = cleanBuffers.poll()) != null) {
                clean0(cleanBuffer);
            }
        } finally {
            lock.unlock();
        }

    }

    private void clean0(VirtualBuffer cleanBuffer) {
        int index = 0;
        Iterator<VirtualBuffer> iterator = availableBuffers.iterator();
        while (iterator.hasNext()) {
            VirtualBuffer freeBuffer = iterator.next();
            //cleanBuffer在freeBuffer之前并且形成连续块
            if (freeBuffer.getParentPosition() == cleanBuffer.getParentLimit()) {
                freeBuffer.setParentPosition(cleanBuffer.getParentPosition());
                return;
            }
            //cleanBuffer与freeBuffer之后并形成连续块
            if (freeBuffer.getParentLimit() == cleanBuffer.getParentPosition()) {
                freeBuffer.setParentLimit(cleanBuffer.getParentLimit());
                //判断后一个是否连续
                if (iterator.hasNext()) {
                    VirtualBuffer next = iterator.next();
                    if (next.getParentPosition() == freeBuffer.getParentLimit()) {
                        freeBuffer.setParentLimit(next.getParentLimit());
                        iterator.remove();
                    } else if (next.getParentPosition() < freeBuffer.getParentLimit()) {
                        throw new IllegalStateException(Normal.EMPTY);
                    }
                }
                return;
            }
            if (freeBuffer.getParentPosition() > cleanBuffer.getParentLimit()) {
                availableBuffers.add(index, cleanBuffer);
                return;
            }
            index++;
        }
        availableBuffers.add(cleanBuffer);
    }

}
