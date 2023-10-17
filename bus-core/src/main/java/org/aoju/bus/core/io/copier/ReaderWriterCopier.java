/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.io.copier;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.io.Progress;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.IoKit;

import java.io.*;

/**
 * {@link Reader} 向 {@link Writer} 拷贝
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReaderWriterCopier extends IoCopier<Reader, Writer> {

    /**
     * 构造
     */
    public ReaderWriterCopier() {
        this(IoKit.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 构造
     *
     * @param bufferSize 缓存大小
     */
    public ReaderWriterCopier(int bufferSize) {
        this(bufferSize, -1);
    }

    /**
     * 构造
     *
     * @param bufferSize 缓存大小
     * @param count      拷贝总数
     */
    public ReaderWriterCopier(int bufferSize, long count) {
        this(bufferSize, count, null);
    }

    /**
     * 构造
     *
     * @param bufferSize 缓存大小
     * @param count      拷贝总数
     * @param progress   进度条
     */
    public ReaderWriterCopier(int bufferSize, long count, Progress progress) {
        super(bufferSize, count, progress);
    }

    @Override
    public long copy(Reader source, Writer target) {
        Assert.notNull(source, "InputStream is null !");
        Assert.notNull(target, "OutputStream is null !");

        final Progress progress = this.progress;
        if (null != progress) {
            progress.start();
        }
        final long size;
        try {
            size = doCopy(source, target, new char[bufferSize(this.count)], progress);
            target.flush();
        } catch (IOException e) {
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
    private long doCopy(Reader source, Writer target, char[] buffer, Progress progress) throws IOException {
        long numToRead = this.count > 0 ? this.count : Long.MAX_VALUE;
        long total = 0;

        int read;
        while (numToRead > 0) {
            read = source.read(buffer, 0, bufferSize(numToRead));
            if (read < 0) {
                // 提前读取到末尾
                break;
            }
            target.write(buffer, 0, read);
            if (flushEveryBuffer) {
                target.flush();
            }

            numToRead -= read;
            total += read;
            if (null != progress) {
                progress.progress(this.count, total);
            }
        }

        return total;
    }

}
