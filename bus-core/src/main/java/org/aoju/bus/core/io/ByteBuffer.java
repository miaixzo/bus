/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.io;

import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.ThreadKit;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 由字节数组段组成的不可变字节字符串 该类的存在是为了实现
 * 缓冲区的有效快照 它被实现为一个段数组,加上一个目录
 * 两个半部分,描述段如何组成这个字节字符串
 *
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
public class ByteBuffer extends ByteString {

    /**
     * 守护线程在空闲时期回收内存资源
     */
    private static final ScheduledThreadPoolExecutor BUFFER_POOL_CLEAN = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r, "BufferPoolClean");
        thread.setDaemon(true);
        return thread;
    });
    /**
     * 内存页游标
     */
    private final AtomicInteger cursor = new AtomicInteger(0);
    private transient byte[][] segments;
    private transient int[] directory;
    /**
     * 内存页组
     */
    private PageBuffer[] pageBuffers;
    private boolean enabled = true;

    public ByteBuffer(Buffer buffer, int byteCount) {
        super(null);
        IoKit.checkOffsetAndCount(buffer.size, 0, byteCount);

        int offset = 0;
        int segmentCount = 0;
        for (Segment s = buffer.head; offset < byteCount; s = s.next) {
            if (s.limit == s.pos) {
                throw new AssertionError("s.limit == s.pos");
            }
            offset += s.limit - s.pos;
            segmentCount++;
        }

        // Walk through the buffer again to assign segments and build the directory.
        this.segments = new byte[segmentCount][];
        this.directory = new int[segmentCount * 2];
        offset = 0;
        segmentCount = 0;
        for (Segment s = buffer.head; offset < byteCount; s = s.next) {
            segments[segmentCount] = s.data;
            offset += s.limit - s.pos;
            if (offset > byteCount) {
                offset = byteCount;
            }
            directory[segmentCount] = offset;
            directory[segmentCount + segments.length] = s.pos;
            s.shared = true;
            segmentCount++;
        }
    }

    /**
     * @param pageSize 内存页大小
     * @param pageNo   内存页个数
     * @param isDirect 是否使用直接缓冲区
     */
    public ByteBuffer(final int pageSize, final int pageNo, final boolean isDirect) {
        pageBuffers = new PageBuffer[pageNo];
        for (int i = 0; i < pageNo; i++) {
            pageBuffers[i] = new PageBuffer(pageBuffers, pageSize, isDirect);
        }
        if (pageNo == 0 || pageSize == 0) {
            future.cancel(false);
        }
    }

    @Override
    public String utf8() {
        return toByteString().utf8();
    }

    @Override
    public String string(Charset charset) {
        return toByteString().string(charset);
    }

    /**
     * 内存回收任务
     */
    private final ScheduledFuture<?> future = BUFFER_POOL_CLEAN.scheduleWithFixedDelay(new Runnable() {
        @Override
        public void run() {
            if (enabled) {
                for (PageBuffer pageBuffer : pageBuffers) {
                    pageBuffer.tryClean();
                }
            } else {
                if (null != pageBuffers) {
                    for (PageBuffer page : pageBuffers) {
                        page.release();
                    }
                    pageBuffers = null;
                }
                future.cancel(false);
            }
        }
    }, 500, 1000, TimeUnit.MILLISECONDS);

    @Override
    public String base64() {
        return toByteString().base64();
    }

    @Override
    public String hex() {
        return toByteString().hex();
    }

    @Override
    public ByteString toAsciiLowercase() {
        return toByteString().toAsciiLowercase();
    }

    @Override
    public ByteString toAsciiUppercase() {
        return toByteString().toAsciiUppercase();
    }

    @Override
    public ByteString md5() {
        return toByteString().md5();
    }

    @Override
    public ByteString sha1() {
        return toByteString().sha1();
    }

    @Override
    public ByteString sha256() {
        return toByteString().sha256();
    }

    @Override
    public ByteString hmacSha1(ByteString key) {
        return toByteString().hmacSha1(key);
    }

    @Override
    public ByteString hmacSha256(ByteString key) {
        return toByteString().hmacSha256(key);
    }

    @Override
    public String base64Url() {
        return toByteString().base64Url();
    }

    @Override
    public ByteString substring(int beginIndex) {
        return toByteString().substring(beginIndex);
    }

    @Override
    public ByteString substring(int beginIndex, int endIndex) {
        return toByteString().substring(beginIndex, endIndex);
    }

    @Override
    public byte getByte(int pos) {
        IoKit.checkOffsetAndCount(directory[segments.length - 1], pos, 1);
        int segment = segment(pos);
        int segmentOffset = segment == 0 ? 0 : directory[segment - 1];
        int segmentPos = directory[segment + segments.length];
        return segments[segment][pos - segmentOffset + segmentPos];
    }

    private int segment(int pos) {
        int i = Arrays.binarySearch(directory, 0, segments.length, pos + 1);
        return i >= 0 ? i : ~i;
    }

    @Override
    public int size() {
        return directory[segments.length - 1];
    }

    @Override
    public byte[] toByteArray() {
        byte[] result = new byte[directory[segments.length - 1]];
        int segmentOffset = 0;
        for (int s = 0, segmentCount = segments.length; s < segmentCount; s++) {
            int segmentPos = directory[segmentCount + s];
            int nextSegmentOffset = directory[s];
            System.arraycopy(segments[s], segmentPos, result, segmentOffset,
                    nextSegmentOffset - segmentOffset);
            segmentOffset = nextSegmentOffset;
        }
        return result;
    }

    @Override
    public java.nio.ByteBuffer asByteBuffer() {
        return java.nio.ByteBuffer.wrap(toByteArray()).asReadOnlyBuffer();
    }

    @Override
    public void write(OutputStream out) throws IOException {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        int segmentOffset = 0;
        for (int s = 0, segmentCount = segments.length; s < segmentCount; s++) {
            int segmentPos = directory[segmentCount + s];
            int nextSegmentOffset = directory[s];
            out.write(segments[s], segmentPos, nextSegmentOffset - segmentOffset);
            segmentOffset = nextSegmentOffset;
        }
    }

    @Override
    public void write(Buffer buffer) {
        int segmentOffset = 0;
        for (int s = 0, segmentCount = segments.length; s < segmentCount; s++) {
            int segmentPos = directory[segmentCount + s];
            int nextSegmentOffset = directory[s];
            Segment segment = new Segment(segments[s], segmentPos,
                    segmentPos + nextSegmentOffset - segmentOffset, true, false);
            if (null == buffer.head) {
                buffer.head = segment.next = segment.prev = segment;
            } else {
                buffer.head.prev.push(segment);
            }
            segmentOffset = nextSegmentOffset;
        }
        buffer.size += segmentOffset;
    }

    @Override
    public boolean rangeEquals(
            int offset, ByteString other, int otherOffset, int byteCount) {
        if (offset < 0 || offset > size() - byteCount) return false;
        for (int s = segment(offset); byteCount > 0; s++) {
            int segmentOffset = s == 0 ? 0 : directory[s - 1];
            int segmentSize = directory[s] - segmentOffset;
            int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
            int segmentPos = directory[segments.length + s];
            int arrayOffset = offset - segmentOffset + segmentPos;
            if (!other.rangeEquals(otherOffset, segments[s], arrayOffset, stepSize)) return false;
            offset += stepSize;
            otherOffset += stepSize;
            byteCount -= stepSize;
        }
        return true;
    }

    @Override
    public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
        if (offset < 0 || offset > size() - byteCount
                || otherOffset < 0 || otherOffset > other.length - byteCount) {
            return false;
        }
        for (int s = segment(offset); byteCount > 0; s++) {
            int segmentOffset = s == 0 ? 0 : directory[s - 1];
            int segmentSize = directory[s] - segmentOffset;
            int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
            int segmentPos = directory[segments.length + s];
            int arrayOffset = offset - segmentOffset + segmentPos;
            if (!IoKit.arrayRangeEquals(segments[s], arrayOffset, other, otherOffset, stepSize)) return false;
            offset += stepSize;
            otherOffset += stepSize;
            byteCount -= stepSize;
        }
        return true;
    }

    @Override
    public int indexOf(byte[] other, int fromIndex) {
        return toByteString().indexOf(other, fromIndex);
    }

    @Override
    public int lastIndexOf(byte[] other, int fromIndex) {
        return toByteString().lastIndexOf(other, fromIndex);
    }

    private ByteString toByteString() {
        return new ByteString(toByteArray());
    }

    @Override
    public byte[] internalArray() {
        return toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        return o instanceof ByteString
                && ((ByteString) o).size() == size()
                && rangeEquals(0, ((ByteString) o), 0, size());
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result != 0) return result;

        result = 1;
        int segmentOffset = 0;
        for (int s = 0, segmentCount = segments.length; s < segmentCount; s++) {
            byte[] segment = segments[s];
            int segmentPos = directory[segmentCount + s];
            int nextSegmentOffset = directory[s];
            int segmentSize = nextSegmentOffset - segmentOffset;
            for (int i = segmentPos, limit = segmentPos + segmentSize; i < limit; i++) {
                result = (31 * result) + segment[i];
            }
            segmentOffset = nextSegmentOffset;
        }
        return (hashCode = result);
    }

    @Override
    public String toString() {
        return toByteString().toString();
    }

    private Object writeReplace() {
        return toByteString();
    }

    /**
     * 申请FastBufferThread的线程对象,配合线程池申请会有更好的性能表现
     *
     * @param target Runnable
     * @param name   线程名
     * @return FastBufferThread线程对象
     */
    public Thread newThread(Runnable target, String name) {
        assertEnabled();
        return new ThreadKit.FastBufferThread(target, name);
    }

    /**
     * 申请内存页
     *
     * @return 缓存页对象
     */
    public PageBuffer allocatePageBuffer() {
        assertEnabled();
        return pageBuffers[(cursor.getAndIncrement() & Integer.MAX_VALUE) % pageBuffers.length];
    }

    private void assertEnabled() {
        if (!enabled) {
            throw new IllegalStateException("buffer pool is disable");
        }
    }

    /**
     * 释放回收内存
     */
    public void release() {
        enabled = false;
    }


}
