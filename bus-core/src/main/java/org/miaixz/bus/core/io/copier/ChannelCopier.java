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
package org.miaixz.bus.core.io.copier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * {@link ReadableByteChannel} 向 {@link WritableByteChannel} 拷贝
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChannelCopier extends IoCopier<ReadableByteChannel, WritableByteChannel> {

    /**
     * 构造
     */
    public ChannelCopier() {
        this(Normal._8192);
    }

    /**
     * 构造
     *
     * @param bufferSize 缓存大小
     */
    public ChannelCopier(final int bufferSize) {
        this(bufferSize, -1);
    }

    /**
     * 构造
     *
     * @param bufferSize 缓存大小
     * @param count      拷贝总数
     */
    public ChannelCopier(final int bufferSize, final long count) {
        this(bufferSize, count, null);
    }

    /**
     * 构造
     *
     * @param bufferSize 缓存大小
     * @param count      拷贝总数
     * @param progress   进度条
     */
    public ChannelCopier(final int bufferSize, final long count, final StreamProgress progress) {
        super(bufferSize, count, progress);
    }

    @Override
    public long copy(final ReadableByteChannel source, final WritableByteChannel target) {
        final StreamProgress progress = this.progress;
        if (null != progress) {
            progress.start();
        }
        final long size;
        try {
            size = doCopy(source, target, ByteBuffer.allocate(bufferSize(this.count)), progress);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null != progress) {
            progress.finish();
        }
        return size;
    }

    /**
     * 执行拷贝，如果限制最大长度，则按照最大长度读取，否则一直读取直到遇到-1
     *
     * @param source   {@link InputStream}
     * @param target   {@link OutputStream}
     * @param buffer   缓存
     * @param progress 进度条
     * @return 拷贝总长度
     * @throws IOException IO异常
     */
    private long doCopy(final ReadableByteChannel source, final WritableByteChannel target, final ByteBuffer buffer,
            final StreamProgress progress) throws IOException {
        long numToRead = this.count > 0 ? this.count : Long.MAX_VALUE;
        long total = 0;

        int read;
        while (numToRead > 0) {
            read = source.read(buffer);
            if (read < 0) {
                // 提前读取到末尾
                break;
            }
            buffer.flip();// 写转读
            target.write(buffer);
            buffer.clear();

            numToRead -= read;
            total += read;
            if (null != progress) {
                // 总长度未知的情况下，-1表示未知
                progress.progress(this.count < Long.MAX_VALUE ? this.count : -1, total);
            }
        }

        return total;
    }

}
